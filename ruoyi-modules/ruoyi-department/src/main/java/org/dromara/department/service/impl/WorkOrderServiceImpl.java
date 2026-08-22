package org.dromara.department.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.department.domain.WorkOrder;
import org.dromara.department.domain.WorkOrderDetail;
import org.dromara.department.domain.WorkOrderImportBatch;
import org.dromara.department.domain.bo.WorkOrderBo;
import org.dromara.department.domain.bo.WorkOrderDetailBo;
import org.dromara.department.domain.bo.WorkOrderQueryBo;
import org.dromara.department.domain.vo.WorkOrderDetailVo;
import org.dromara.department.domain.vo.WorkOrderExportVo;
import org.dromara.department.domain.vo.WorkOrderImportBatchVo;
import org.dromara.department.domain.vo.WorkOrderImportResultVo;
import org.dromara.department.domain.vo.WorkOrderSummaryVo;
import org.dromara.department.domain.vo.WorkOrderVo;
import org.dromara.department.mapper.WorkOrderImportBatchMapper;
import org.dromara.department.mapper.WorkOrderDetailMapper;
import org.dromara.department.mapper.WorkOrderMapper;
import org.dromara.department.service.IWorkOrderService;
import org.dromara.system.domain.SysOssExt;
import org.dromara.system.domain.vo.SysOssVo;
import org.dromara.system.service.ISysOssService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工单台账业务实现。
 */
@RequiredArgsConstructor
@Service
public class WorkOrderServiceImpl implements IWorkOrderService {

    private static final String SOURCE_PDF = "PDF";
    private static final String SOURCE_MANUAL = "MANUAL";
    private static final String REVIEW_PENDING = "PENDING";
    private static final String REVIEW_CONFIRMED = "CONFIRMED";
    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_RESOLVED = "RESOLVED";
    private static final String STATUS_CLOSED = "CLOSED";
    private static final Pattern DETAIL_NUMBER = Pattern.compile("[-+]?\\d+(?:\\.\\d+)?");

    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderDetailMapper workOrderDetailMapper;
    private final WorkOrderImportBatchMapper importBatchMapper;
    private final WorkOrderPdfParser pdfParser;
    private final ISysOssService ossService;

    @Override
    public PageResult<WorkOrderVo> queryPageList(WorkOrderQueryBo bo, PageQuery pageQuery) {
        WorkOrderQueryBo query = bo == null ? new WorkOrderQueryBo() : bo;
        Page<WorkOrderVo> page = pageQuery.build();
        Page<WorkOrderVo> result = workOrderMapper.selectPageList(page, query, scopeDeptId(), canViewAll());
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public List<WorkOrderVo> queryList(WorkOrderQueryBo bo) {
        Page<WorkOrderVo> page = new Page<>(1, Integer.MAX_VALUE);
        WorkOrderQueryBo query = bo == null ? new WorkOrderQueryBo() : bo;
        return workOrderMapper.selectPageList(page, query, scopeDeptId(), canViewAll()).getRecords();
    }

    @Override
    public List<WorkOrderExportVo> queryExportList(WorkOrderQueryBo bo) {
        List<WorkOrderExportVo> result = new ArrayList<>();
        for (WorkOrderVo parent : queryList(bo)) {
            List<WorkOrderDetail> details = workOrderDetailMapper.selectByWorkOrderId(parent.getId());
            if (details.isEmpty()) {
                result.add(toExportVo(parent, null));
            } else {
                details.forEach(detail -> result.add(toExportVo(parent, detail)));
            }
        }
        return result;
    }

    @Override
    public WorkOrderVo queryById(Long id) {
        return toVo(getAccessible(id));
    }

    @Override
    public List<WorkOrderDetailVo> queryDetails(Long workOrderId) {
        getAccessible(workOrderId);
        return workOrderDetailMapper.selectByWorkOrderId(workOrderId).stream()
            .map(this::toDetailVo)
            .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateDetailByBo(WorkOrderDetailBo bo) {
        WorkOrder parent = getAccessible(bo.getWorkOrderId());
        WorkOrderDetail detail = workOrderDetailMapper.selectById(bo.getId());
        if (detail == null || !Objects.equals(detail.getWorkOrderId(), parent.getId())) {
            throw new ServiceException("人工统计明细不存在或不属于当前人工单");
        }
        copyDetail(bo, detail);
        detail.setQuantity(resolveDetailQuantity(bo));
        boolean updated = workOrderDetailMapper.updateById(detail) > 0;
        if (updated) {
            syncParentFromDetails(parent);
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteDetails(Collection<Long> ids) {
        Map<Long, WorkOrder> parents = new LinkedHashMap<>();
        for (Long id : ids) {
            WorkOrderDetail detail = workOrderDetailMapper.selectById(id);
            if (detail == null) {
                throw new ServiceException("人工统计明细不存在");
            }
            WorkOrder parent = getAccessible(detail.getWorkOrderId());
            parents.put(parent.getId(), parent);
        }
        boolean deleted = workOrderDetailMapper.deleteByIds(ids) > 0;
        if (deleted) {
            parents.values().forEach(this::syncParentFromDetails);
        }
        return deleted;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(WorkOrderBo bo) {
        if (LoginHelper.getDeptId() == null) {
            throw new ServiceException("当前登录用户缺少部门信息，无法维护工单");
        }
        if (bo.getOccurDate() == null) {
            throw new ServiceException("手动新增工单必须填写发生年月");
        }
        WorkOrder entity = new WorkOrder();
        copyBo(bo, entity);
        entity.setDeptId(LoginHelper.getDeptId());
        entity.setSourceType(SOURCE_MANUAL);
        entity.setReviewStatus(REVIEW_CONFIRMED);
        entity.setStatus(StringUtils.isBlank(bo.getStatus()) ? STATUS_OPEN : bo.getStatus());
        entity.setQuantity(bo.getQuantity() == null ? BigDecimal.ONE : bo.getQuantity());
        return workOrderMapper.insert(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(WorkOrderBo bo) {
        WorkOrder entity = getAccessible(bo.getId());
        copyBo(bo, entity);
        if (StringUtils.isNotBlank(bo.getReviewStatus())) {
            entity.setReviewStatus(bo.getReviewStatus());
        }
        if (StringUtils.isBlank(entity.getReviewStatus())) {
            entity.setReviewStatus(REVIEW_PENDING);
        }
        if (REVIEW_CONFIRMED.equals(entity.getReviewStatus()) && entity.getOccurDate() == null) {
            throw new ServiceException("确认进入周报统计前必须填写发生年月");
        }
        entity.setQuantity(bo.getQuantity() == null ? BigDecimal.ONE : bo.getQuantity());
        if (StringUtils.isBlank(entity.getStatus())) {
            entity.setStatus(STATUS_OPEN);
        }
        return workOrderMapper.updateById(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        for (Long id : ids) {
            getAccessible(id);
        }
        for (Long id : ids) {
            workOrderDetailMapper.deleteByWorkOrderId(id);
        }
        return workOrderMapper.deleteByIds(ids) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkOrderImportResultVo importPdf(MultipartFile file) {
        if (LoginHelper.getDeptId() == null) {
            throw new ServiceException("当前登录用户缺少部门信息，无法导入工单");
        }
        if (file == null || file.isEmpty()) {
            throw new ServiceException("PDF文件不能为空");
        }
        String originalName = StringUtils.isBlank(file.getOriginalFilename()) ? "work-order.pdf" : file.getOriginalFilename();
        if (!originalName.toLowerCase().endsWith(".pdf")) {
            throw new ServiceException("只支持PDF文件");
        }
        SysOssExt ossExt = new SysOssExt();
        ossExt.setBizType("DEPARTMENT_WORK_ORDER_PDF");
        ossExt.setSource("systemImport");
        ossExt.setRefType("WORK_ORDER_IMPORT_BATCH");
        SysOssVo oss;
        WorkOrderPdfParser.ParseResult parsed;
        try {
            oss = ossService.upload(file, ossExt);
            parsed = pdfParser.parse(file.getBytes(), originalName);
        } catch (Exception ex) {
            throw ex instanceof ServiceException serviceException
                ? serviceException
                : new ServiceException("PDF导入失败：" + ex.getMessage());
        }

        WorkOrderImportBatch batch = new WorkOrderImportBatch();
        batch.setSourceFileName(originalName);
        batch.setOssId(oss.getOssId());
        batch.setSourcePeriodStart(parsed.getPeriodStart());
        batch.setSourcePeriodEnd(parsed.getPeriodEnd());
        batch.setPageCount(parsed.getPageCount());
        batch.setRecordCount(parsed.getRows().size());
        batch.setParsedRecordCount(0);
        batch.setPendingRecordCount(0);
        batch.setStatus("PARSING");
        importBatchMapper.insert(batch);

        List<WorkOrderPdfParser.ParsedRow> detailRows = parsed.getRows();
        WorkOrderPdfParser.ParsedRow firstRow = detailRows.get(0);
        WorkOrder entity = new WorkOrder();
        entity.setDeptId(LoginHelper.getDeptId());
        entity.setTicketNo("PDF-" + batch.getId());
        // 数据库仍使用 date 类型，统一按该月份第一天保存；页面只展示 yyyy-MM。
        entity.setOccurDate(parsed.getPeriodStart());
        entity.setSourcePeriodStart(parsed.getPeriodStart());
        entity.setSourcePeriodEnd(parsed.getPeriodEnd());
        entity.setRequestDept(commonValue(detailRows, WorkOrderPdfParser.ParsedRow::getRequestDept, "多部门（详见明细）"));
        entity.setSettlementUnit(commonValue(detailRows, WorkOrderPdfParser.ParsedRow::getSettlementUnit, "多项（详见明细）"));
        entity.setProjectOwner(commonValue(detailRows, WorkOrderPdfParser.ParsedRow::getProjectOwner, "多人（详见明细）"));
        entity.setSystemName(commonValue(detailRows, WorkOrderPdfParser.ParsedRow::getSystemName, "多项目（详见明细）"));
        entity.setWorkCategory("PDF人工单");
        entity.setTitle(commonValue(detailRows, WorkOrderPdfParser.ParsedRow::getTitle, "多项（详见明细）"));
        entity.setWorkContent(detailRows.size() == 1
            ? firstRow.getWorkContent()
            : "共" + detailRows.size() + "条人工统计明细，具体内容请点击“人工统计明细”查看");
        entity.setInstallDepartment(commonValue(detailRows, WorkOrderPdfParser.ParsedRow::getInstallDepartment, "多车间（详见明细）"));
        entity.setInstallTeam(commonValue(detailRows, WorkOrderPdfParser.ParsedRow::getInstallTeam, "多班组（详见明细）"));
        entity.setUnit(commonValue(detailRows, WorkOrderPdfParser.ParsedRow::getUnit, "多单位（详见明细）"));
        entity.setQuantity(detailRows.stream()
            .map(WorkOrderPdfParser.ParsedRow::getQuantity)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        entity.setResponsiblePerson(commonValue(detailRows, WorkOrderPdfParser.ParsedRow::getProjectOwner, "多人（详见明细）"));
        entity.setStatus(STATUS_OPEN);
        entity.setReviewStatus(REVIEW_PENDING);
        entity.setSourceType(SOURCE_PDF);
        entity.setSourceBatchId(batch.getId());
        entity.setSourceFileName(originalName);
        entity.setSourcePage(firstRow.getPage());
        entity.setParseConfidence(averageConfidence(detailRows));
        entity.setParseMessage("PDF已生成1条人工单主记录，保留" + detailRows.size() + "条人工统计明细；" +
            "请补充状态、故障类型和处理时长后再纳入周报");
        workOrderMapper.insert(entity);

        for (WorkOrderPdfParser.ParsedRow row : detailRows) {
            WorkOrderDetail detail = new WorkOrderDetail();
            detail.setWorkOrderId(entity.getId());
            detail.setSourcePage(row.getPage());
            detail.setSequenceNo(row.getSequence());
            detail.setRequestDept(row.getRequestDept());
            detail.setSettlementUnit(row.getSettlementUnit());
            detail.setProjectOwner(row.getProjectOwner());
            detail.setProjectName(row.getSystemName());
            detail.setProjectFeature(row.getTitle());
            detail.setUnit(row.getUnit());
            detail.setEngineeringQuantity(row.getEngineeringQuantity());
            detail.setChineseLabor(row.getChineseLabor());
            detail.setIndonesiaLabor(row.getIndonesiaLabor());
            detail.setInstallDepartment(row.getInstallDepartment());
            detail.setInstallTeam(row.getInstallTeam());
            detail.setWorkContent(row.getWorkContent());
            detail.setQuantity(row.getQuantity());
            detail.setParseMessage(row.getParseMessage());
            workOrderDetailMapper.insert(detail);
        }
        batch.setParsedRecordCount(1);
        batch.setPendingRecordCount(1);
        batch.setStatus("PARSED");
        importBatchMapper.updateById(batch);

        WorkOrderImportResultVo result = new WorkOrderImportResultVo();
        result.setBatch(toBatchVo(batch));
        result.setMessage("PDF解析完成：识别 " + detailRows.size() + " 条人工统计明细，生成 1 条人工单主记录；请点击列表中的“人工统计明细”查看原始列内容");
        return result;
    }

    @Override
    public WorkOrderSummaryVo buildSummary(LocalDate beginDate, LocalDate endDate) {
        return buildSummary(beginDate, endDate, false);
    }

    @Override
    public WorkOrderSummaryVo buildSummary(LocalDate beginDate, LocalDate endDate, boolean includePending) {
        if (beginDate == null || endDate == null || endDate.isBefore(beginDate)) {
            throw new ServiceException("工单汇总日期范围不正确");
        }
        List<WorkOrderVo> rows = workOrderMapper.selectForSummary(beginDate, endDate, scopeDeptId(), canViewAll(), includePending);
        WorkOrderSummaryVo summary = new WorkOrderSummaryVo();
        summary.setTotalCount(rows.size());
        summary.setPendingReviewCount(workOrderMapper.countPending(beginDate, endDate, scopeDeptId(), canViewAll()));
        summary.setUnattributedCount(countUnattributed());
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalEngineeringQuantity = BigDecimal.ZERO;
        BigDecimal totalChineseLabor = BigDecimal.ZERO;
        BigDecimal totalIndonesiaLabor = BigDecimal.ZERO;
        int detailCount = 0;
        int resolved = 0;
        int durationTotal = 0;
        int durationCount = 0;
        Map<String, WorkOrderSummaryVo.DimensionCountVo> systemMap = new LinkedHashMap<>();
        Map<String, WorkOrderSummaryVo.DimensionCountVo> faultMap = new LinkedHashMap<>();
        for (WorkOrderVo row : rows) {
            totalQuantity = totalQuantity.add(row.getQuantity() == null ? BigDecimal.ONE : row.getQuantity());
            List<WorkOrderDetail> details = workOrderDetailMapper.selectByWorkOrderId(row.getId());
            detailCount += details.size();
            for (WorkOrderDetail detail : details) {
                totalEngineeringQuantity = addFirstNumber(totalEngineeringQuantity, detail.getEngineeringQuantity());
                totalChineseLabor = addFirstNumber(totalChineseLabor, detail.getChineseLabor());
                totalIndonesiaLabor = addFirstNumber(totalIndonesiaLabor, detail.getIndonesiaLabor());
            }
            if (STATUS_RESOLVED.equals(row.getStatus()) || STATUS_CLOSED.equals(row.getStatus())) {
                resolved++;
                if (row.getResolutionMinutes() != null && row.getResolutionMinutes() >= 0) {
                    durationTotal += row.getResolutionMinutes();
                    durationCount++;
                }
            }
            addDimension(systemMap, StringUtils.isBlank(row.getSystemName()) ? "未分类" : row.getSystemName(), row);
            addDimension(faultMap, StringUtils.isBlank(row.getFaultType()) ? "未分类" : row.getFaultType(), row);
        }
        BigDecimal reportQuantity = totalChineseLabor.add(totalIndonesiaLabor).signum() > 0
            ? totalChineseLabor.add(totalIndonesiaLabor)
            : totalQuantity;
        summary.setTotalQuantity(reportQuantity);
        summary.setTotalEngineeringQuantity(totalEngineeringQuantity);
        summary.setTotalChineseLabor(totalChineseLabor);
        summary.setTotalIndonesiaLabor(totalIndonesiaLabor);
        summary.setTotalLaborQuantity(totalChineseLabor.add(totalIndonesiaLabor));
        summary.setDetailCount(detailCount);
        summary.setResolvedCount(resolved);
        summary.setResolutionRate(rows.isEmpty() ? BigDecimal.ZERO : BigDecimal.valueOf(resolved * 100.0 / rows.size()).setScale(2, RoundingMode.HALF_UP));
        summary.setAverageResolutionMinutes(durationCount == 0 ? 0 : Math.round((float) durationTotal / durationCount));
        summary.setBySystem(toDimensionList(systemMap, rows.size()));
        summary.setByFaultType(toDimensionList(faultMap, rows.size()));
        return summary;
    }

    private void copyBo(WorkOrderBo bo, WorkOrder entity) {
        entity.setTicketNo(bo.getTicketNo());
        entity.setOccurDate(bo.getOccurDate());
        entity.setSourcePeriodStart(bo.getSourcePeriodStart());
        entity.setSourcePeriodEnd(bo.getSourcePeriodEnd());
        entity.setRequestDept(bo.getRequestDept());
        entity.setSettlementUnit(bo.getSettlementUnit());
        entity.setProjectOwner(bo.getProjectOwner());
        entity.setSystemName(bo.getSystemName());
        entity.setInstallDepartment(bo.getInstallDepartment());
        entity.setInstallTeam(bo.getInstallTeam());
        entity.setWorkCategory(bo.getWorkCategory());
        entity.setFaultType(bo.getFaultType());
        entity.setTitle(bo.getTitle());
        entity.setWorkContent(bo.getWorkContent());
        entity.setUnit(bo.getUnit());
        entity.setQuantity(bo.getQuantity());
        entity.setResponsiblePerson(bo.getResponsiblePerson());
        entity.setHandler(bo.getHandler());
        entity.setStatus(bo.getStatus());
        entity.setResolutionMinutes(bo.getResolutionMinutes());
        entity.setFeedbackChannel(bo.getFeedbackChannel());
        entity.setRemark(bo.getRemark());
    }

    private void addDimension(Map<String, WorkOrderSummaryVo.DimensionCountVo> map, String name, WorkOrderVo row) {
        WorkOrderSummaryVo.DimensionCountVo dimension = map.computeIfAbsent(name, key -> {
            WorkOrderSummaryVo.DimensionCountVo value = new WorkOrderSummaryVo.DimensionCountVo();
            value.setName(key);
            value.setCount(0);
            value.setQuantity(BigDecimal.ZERO);
            return value;
        });
        dimension.setCount(dimension.getCount() + 1);
        dimension.setQuantity(dimension.getQuantity().add(row.getQuantity() == null ? BigDecimal.ONE : row.getQuantity()));
    }

    private List<WorkOrderSummaryVo.DimensionCountVo> toDimensionList(Map<String, WorkOrderSummaryVo.DimensionCountVo> map, int total) {
        List<WorkOrderSummaryVo.DimensionCountVo> result = new ArrayList<>(map.values());
        result.sort(Comparator.comparing(WorkOrderSummaryVo.DimensionCountVo::getCount).reversed());
        for (WorkOrderSummaryVo.DimensionCountVo dimension : result) {
            dimension.setPercentage(total == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(dimension.getCount() * 100.0 / total).setScale(2, RoundingMode.HALF_UP));
        }
        return result;
    }

    private int countUnattributed() {
        return workOrderMapper.selectList(Wrappers.<WorkOrder>lambdaQuery()
            .eq(WorkOrder::getReviewStatus, REVIEW_CONFIRMED)
            .isNull(WorkOrder::getOccurDate)
            .eq(!canViewAll(), WorkOrder::getDeptId, scopeDeptId())).size();
    }

    private WorkOrder getAccessible(Long id) {
        WorkOrder entity = workOrderMapper.selectById(id);
        if (entity == null) {
            throw new ServiceException("工单不存在");
        }
        if (canViewAll() || Objects.equals(entity.getDeptId(), LoginHelper.getDeptId())) {
            return entity;
        }
        throw new ServiceException("您没有访问该工单的权限");
    }

    private WorkOrderVo toVo(WorkOrder entity) {
        WorkOrderVo vo = new WorkOrderVo();
        vo.setId(entity.getId());
        vo.setDeptId(entity.getDeptId());
        vo.setTicketNo(entity.getTicketNo());
        vo.setOccurDate(entity.getOccurDate());
        vo.setSourcePeriodStart(entity.getSourcePeriodStart());
        vo.setSourcePeriodEnd(entity.getSourcePeriodEnd());
        vo.setRequestDept(entity.getRequestDept());
        vo.setSettlementUnit(entity.getSettlementUnit());
        vo.setProjectOwner(entity.getProjectOwner());
        vo.setSystemName(entity.getSystemName());
        vo.setInstallDepartment(entity.getInstallDepartment());
        vo.setInstallTeam(entity.getInstallTeam());
        vo.setWorkCategory(entity.getWorkCategory());
        vo.setFaultType(entity.getFaultType());
        vo.setTitle(entity.getTitle());
        vo.setWorkContent(entity.getWorkContent());
        vo.setUnit(entity.getUnit());
        vo.setQuantity(entity.getQuantity());
        vo.setResponsiblePerson(entity.getResponsiblePerson());
        vo.setHandler(entity.getHandler());
        vo.setStatus(entity.getStatus());
        vo.setResolutionMinutes(entity.getResolutionMinutes());
        vo.setFeedbackChannel(entity.getFeedbackChannel());
        vo.setReviewStatus(entity.getReviewStatus());
        vo.setSourceType(entity.getSourceType());
        vo.setSourceBatchId(entity.getSourceBatchId());
        vo.setSourceFileName(entity.getSourceFileName());
        vo.setSourcePage(entity.getSourcePage());
        vo.setDetailCount(workOrderDetailMapper.selectByWorkOrderId(entity.getId()).size());
        vo.setParseConfidence(entity.getParseConfidence());
        vo.setParseMessage(entity.getParseMessage());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private WorkOrderExportVo toExportVo(WorkOrderVo parent, WorkOrderDetail detail) {
        WorkOrderExportVo vo = new WorkOrderExportVo();
        vo.setId(parent.getId());
        vo.setTicketNo(parent.getTicketNo());
        vo.setOccurDate(parent.getOccurDate());
        vo.setDetailSequence(detail == null ? null : detail.getSequenceNo());
        vo.setRequestDept(detail == null ? parent.getRequestDept() : detail.getRequestDept());
        vo.setSettlementUnit(detail == null ? parent.getSettlementUnit() : detail.getSettlementUnit());
        vo.setProjectOwner(detail == null ? parent.getProjectOwner() : detail.getProjectOwner());
        vo.setProjectName(detail == null ? parent.getSystemName() : detail.getProjectName());
        vo.setProjectFeature(detail == null ? parent.getTitle() : detail.getProjectFeature());
        vo.setUnit(detail == null ? parent.getUnit() : detail.getUnit());
        vo.setEngineeringQuantity(detail == null ? null : detail.getEngineeringQuantity());
        vo.setChineseLabor(detail == null ? null : detail.getChineseLabor());
        vo.setIndonesiaLabor(detail == null ? null : detail.getIndonesiaLabor());
        vo.setInstallDepartment(detail == null ? parent.getInstallDepartment() : detail.getInstallDepartment());
        vo.setInstallTeam(detail == null ? parent.getInstallTeam() : detail.getInstallTeam());
        vo.setWorkContent(detail == null ? parent.getWorkContent() : detail.getWorkContent());
        vo.setQuantity(detail == null ? parent.getQuantity() : detail.getQuantity());
        vo.setWorkCategory(parent.getWorkCategory());
        vo.setFaultType(parent.getFaultType());
        vo.setStatus(parent.getStatus());
        vo.setResolutionMinutes(parent.getResolutionMinutes());
        vo.setFeedbackChannel(parent.getFeedbackChannel());
        vo.setReviewStatus(parent.getReviewStatus());
        vo.setSourceType(parent.getSourceType());
        vo.setSourceFileName(parent.getSourceFileName());
        vo.setSourcePage(detail == null ? parent.getSourcePage() : detail.getSourcePage());
        return vo;
    }

    private void copyDetail(WorkOrderDetailBo bo, WorkOrderDetail detail) {
        detail.setRequestDept(bo.getRequestDept());
        detail.setSettlementUnit(bo.getSettlementUnit());
        detail.setProjectOwner(bo.getProjectOwner());
        detail.setProjectName(bo.getProjectName());
        detail.setProjectFeature(bo.getProjectFeature());
        detail.setUnit(bo.getUnit());
        detail.setEngineeringQuantity(bo.getEngineeringQuantity());
        detail.setChineseLabor(bo.getChineseLabor());
        detail.setIndonesiaLabor(bo.getIndonesiaLabor());
        detail.setInstallDepartment(bo.getInstallDepartment());
        detail.setInstallTeam(bo.getInstallTeam());
        detail.setWorkContent(bo.getWorkContent());
    }

    private BigDecimal resolveDetailQuantity(WorkOrderDetailBo bo) {
        String value = firstNumber(bo.getEngineeringQuantity(), bo.getChineseLabor(), bo.getIndonesiaLabor());
        if (StringUtils.isBlank(value)) {
            return BigDecimal.ONE;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            return BigDecimal.ONE;
        }
    }

    private String firstNumber(String... values) {
        for (String value : values) {
            Matcher matcher = DETAIL_NUMBER.matcher(value == null ? "" : value);
            if (matcher.find()) {
                return matcher.group();
            }
        }
        return "";
    }

    private BigDecimal addFirstNumber(BigDecimal total, String value) {
        String number = firstNumber(value);
        if (StringUtils.isBlank(number)) {
            return total;
        }
        try {
            return total.add(new BigDecimal(number));
        } catch (NumberFormatException ex) {
            return total;
        }
    }

    private void syncParentFromDetails(WorkOrder parent) {
        List<WorkOrderDetail> details = workOrderDetailMapper.selectByWorkOrderId(parent.getId());
        if (details.isEmpty()) {
            parent.setQuantity(BigDecimal.ZERO);
            parent.setWorkContent("暂无人工统计明细");
            workOrderMapper.updateById(parent);
            return;
        }
        parent.setRequestDept(commonDetailValue(details, WorkOrderDetail::getRequestDept, "多部门（详见明细）"));
        parent.setSettlementUnit(commonDetailValue(details, WorkOrderDetail::getSettlementUnit, "多项（详见明细）"));
        parent.setProjectOwner(commonDetailValue(details, WorkOrderDetail::getProjectOwner, "多人（详见明细）"));
        parent.setSystemName(commonDetailValue(details, WorkOrderDetail::getProjectName, "多项目（详见明细）"));
        parent.setTitle(commonDetailValue(details, WorkOrderDetail::getProjectFeature, "多项（详见明细）"));
        parent.setInstallDepartment(commonDetailValue(details, WorkOrderDetail::getInstallDepartment, "多车间（详见明细）"));
        parent.setInstallTeam(commonDetailValue(details, WorkOrderDetail::getInstallTeam, "多班组（详见明细）"));
        parent.setUnit(commonDetailValue(details, WorkOrderDetail::getUnit, "多单位（详见明细）"));
        parent.setResponsiblePerson(parent.getProjectOwner());
        parent.setQuantity(details.stream()
            .map(WorkOrderDetail::getQuantity)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        parent.setWorkContent(details.size() == 1
            ? details.get(0).getWorkContent()
            : "共" + details.size() + "条人工统计明细，具体内容请点击“人工统计明细”查看");
        workOrderMapper.updateById(parent);
    }

    private String commonDetailValue(List<WorkOrderDetail> details,
                                     Function<WorkOrderDetail, String> getter,
                                     String multipleValue) {
        String first = null;
        for (WorkOrderDetail detail : details) {
            String value = getter.apply(detail);
            if (StringUtils.isBlank(value)) {
                continue;
            }
            value = value.trim();
            if (first == null) {
                first = value;
            } else if (!first.equals(value)) {
                return multipleValue;
            }
        }
        return first == null ? "" : first;
    }

    private WorkOrderDetailVo toDetailVo(WorkOrderDetail entity) {
        WorkOrderDetailVo vo = new WorkOrderDetailVo();
        vo.setId(entity.getId());
        vo.setWorkOrderId(entity.getWorkOrderId());
        vo.setSourcePage(entity.getSourcePage());
        vo.setSequenceNo(entity.getSequenceNo());
        vo.setRequestDept(entity.getRequestDept());
        vo.setSettlementUnit(entity.getSettlementUnit());
        vo.setProjectOwner(entity.getProjectOwner());
        vo.setProjectName(entity.getProjectName());
        vo.setProjectFeature(entity.getProjectFeature());
        vo.setUnit(entity.getUnit());
        vo.setEngineeringQuantity(entity.getEngineeringQuantity());
        vo.setChineseLabor(entity.getChineseLabor());
        vo.setIndonesiaLabor(entity.getIndonesiaLabor());
        vo.setInstallDepartment(entity.getInstallDepartment());
        vo.setInstallTeam(entity.getInstallTeam());
        vo.setWorkContent(entity.getWorkContent());
        vo.setQuantity(entity.getQuantity());
        vo.setParseMessage(entity.getParseMessage());
        return vo;
    }

    private String commonValue(List<WorkOrderPdfParser.ParsedRow> rows,
                               Function<WorkOrderPdfParser.ParsedRow, String> getter,
                               String multipleValue) {
        String first = null;
        for (WorkOrderPdfParser.ParsedRow row : rows) {
            String value = getter.apply(row);
            if (StringUtils.isBlank(value)) {
                continue;
            }
            value = value.trim();
            if (first == null) {
                first = value;
            } else if (!first.equals(value)) {
                return multipleValue;
            }
        }
        return first == null ? "" : first;
    }

    private BigDecimal averageConfidence(List<WorkOrderPdfParser.ParsedRow> rows) {
        BigDecimal total = rows.stream()
            .map(WorkOrderPdfParser.ParsedRow::getParseConfidence)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return rows.isEmpty() ? BigDecimal.ZERO : total.divide(BigDecimal.valueOf(rows.size()), 2, RoundingMode.HALF_UP);
    }

    private WorkOrderImportBatchVo toBatchVo(WorkOrderImportBatch entity) {
        WorkOrderImportBatchVo vo = new WorkOrderImportBatchVo();
        vo.setId(entity.getId());
        vo.setSourceFileName(entity.getSourceFileName());
        vo.setOssId(entity.getOssId());
        vo.setSourcePeriodStart(entity.getSourcePeriodStart());
        vo.setSourcePeriodEnd(entity.getSourcePeriodEnd());
        vo.setPageCount(entity.getPageCount());
        vo.setRecordCount(entity.getRecordCount());
        vo.setParsedRecordCount(entity.getParsedRecordCount());
        vo.setPendingRecordCount(entity.getPendingRecordCount());
        vo.setStatus(entity.getStatus());
        vo.setErrorMessage(entity.getErrorMessage());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private boolean canViewAll() {
        return LoginHelper.isSuperAdmin();
    }

    private Long scopeDeptId() {
        return canViewAll() ? null : LoginHelper.getDeptId();
    }
}
