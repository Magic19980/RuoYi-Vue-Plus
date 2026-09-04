package org.dromara.ecology.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.ecology.domain.OaApplication;
import org.dromara.ecology.domain.OaImportBatch;
import org.dromara.ecology.domain.OaImportBusinessConfig;
import org.dromara.ecology.domain.OaImportDeptAlias;
import org.dromara.ecology.domain.OaImportRecord;
import org.dromara.ecology.domain.OaWorkflowConfig;
import org.dromara.ecology.domain.bo.OaApplicationBo;
import org.dromara.ecology.domain.bo.OaApprovalParticipantBo;
import org.dromara.ecology.domain.bo.OaAttachmentBo;
import org.dromara.ecology.domain.bo.OaImportAttachmentPreviewBo;
import org.dromara.ecology.domain.bo.OaImportDeptMappingBo;
import org.dromara.ecology.domain.bo.OaImportQueryBo;
import org.dromara.ecology.domain.bo.OaImportSubmitBo;
import org.dromara.ecology.domain.vo.OaImportBatchVo;
import org.dromara.ecology.domain.vo.OaImportApprovalPreviewVo;
import org.dromara.ecology.domain.vo.OaImportAttachmentFixedCandidateVo;
import org.dromara.ecology.domain.vo.OaImportDeptMappingItemVo;
import org.dromara.ecology.domain.vo.OaImportGroupVo;
import org.dromara.ecology.domain.vo.OaImportRecordVo;
import org.dromara.ecology.domain.vo.OaDepartmentApprovalUserVo;
import org.dromara.ecology.domain.vo.OaDepartmentApprovalVo;
import org.dromara.ecology.domain.vo.OaAttachmentPreviewVo;
import org.dromara.ecology.mapper.OaImportBatchMapper;
import org.dromara.ecology.mapper.OaImportBusinessConfigMapper;
import org.dromara.ecology.mapper.OaImportDeptAliasMapper;
import org.dromara.ecology.mapper.OaImportRecordMapper;
import org.dromara.ecology.service.IOaApplicationService;
import org.dromara.ecology.service.IOaDepartmentApprovalService;
import org.dromara.ecology.service.IOaImportBusinessService;
import org.dromara.ecology.service.IOaWorkflowConfigService;
import org.dromara.system.domain.SysDept;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.SysOssExt;
import org.dromara.system.domain.vo.SysOssVo;
import org.dromara.system.mapper.SysDeptMapper;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.service.ISysOssService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** 通用 Excel 导入、分组、附件生成和泛微提交服务。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OaImportBusinessServiceImpl implements IOaImportBusinessService {

    private static final String ENABLED = "ENABLED";
    private static final String NEED_MAPPING = "NEED_MAPPING";
    private static final String READY = "READY";
    private static final String SUBMITTING = "SUBMITTING";
    private static final String SUBMITTED = "SUBMITTED";
    private static final String PARTIAL_FAILED = "PARTIAL_FAILED";
    private static final String FAILED = "FAILED";
    private static final String MATCHED = "MATCHED";
    private static final String UNMATCHED = "UNMATCHED";
    private static final String SKIPPED = "SKIPPED";
    private static final String SUBMITTED_RECORD = "SUBMITTED";
    private static final String FAILED_RECORD = "FAILED";
    private static final String OA_DEPARTMENT = "DEPARTMENT";
    private static final DateTimeFormatter BATCH_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final Pattern FORMULA_RANGE = Pattern.compile(
        "(\\$?[A-Z]{1,3})(\\$?)(\\d+):(\\$?[A-Z]{1,3})(\\$?)(\\d+)");
    private static final Pattern FORMULA_CELL = Pattern.compile(
        "(?<![A-Za-z0-9_])(\\$?[A-Z]{1,3})(\\$?)(\\d+)");
    private static final Pattern FIXED_LABEL_PATTERN = Pattern.compile("[^\\s:：]{1,32}[：:]");

    private final OaImportBusinessConfigMapper configMapper;
    private final OaImportBatchMapper batchMapper;
    private final OaImportRecordMapper recordMapper;
    private final OaImportDeptAliasMapper aliasMapper;
    private final SysDeptMapper sysDeptMapper;
    private final SysUserMapper sysUserMapper;
    private final ISysOssService ossService;
    private final IOaApplicationService applicationService;
    private final IOaDepartmentApprovalService departmentApprovalService;
    private final IOaWorkflowConfigService workflowConfigService;

    @Override
    public PageResult<OaImportBatchVo> queryPage(OaImportQueryBo bo, PageQuery pageQuery) {
        OaImportQueryBo query = bo == null ? new OaImportQueryBo() : bo;
        Page<OaImportBatch> page = batchMapper.selectPage(pageQuery.build(), Wrappers.<OaImportBatch>lambdaQuery()
            .eq(query.getConfigId() != null, OaImportBatch::getConfigId, query.getConfigId())
            .eq(StringUtils.isNotBlank(query.getBusinessType()), OaImportBatch::getBusinessType, StringUtils.trim(query.getBusinessType()))
            .like(StringUtils.isNotBlank(query.getBatchNo()), OaImportBatch::getBatchNo, query.getBatchNo())
            .eq(StringUtils.isNotBlank(query.getStatus()), OaImportBatch::getStatus, query.getStatus())
            .and(StringUtils.isNotBlank(query.getKeyword()), wrapper -> wrapper
                .like(OaImportBatch::getSourceFileName, query.getKeyword())
                .or().like(OaImportBatch::getBusinessType, query.getKeyword()))
            .orderByDesc(OaImportBatch::getCreateTime)
            .orderByDesc(OaImportBatch::getId));
        List<OaImportBatchVo> rows = page.getRecords().stream().map(this::toVo).toList();
        return PageResult.build(rows, page.getTotal());
    }

    @Override
    public OaImportBatchVo queryBatch(Long batchId) {
        return toVo(getBatch(batchId), true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long batchId) {
        OaImportBatch batch = getBatch(batchId);
        if (SUBMITTING.equals(batch.getStatus()) || SUBMITTED.equals(batch.getStatus())) {
            throw new ServiceException("正在提交或已提交泛微的批次不能删除");
        }
        List<OaImportRecord> records = recordMapper.selectByBatchId(batchId);
        if (records.stream().anyMatch(record -> record.getApplicationId() != null
            || SUBMITTED_RECORD.equals(record.getStatus()) || SUBMITTING.equals(record.getStatus()))) {
            throw new ServiceException("已创建泛微申请的批次不能删除");
        }
        List<Long> attachmentOssIds = records.stream().map(OaImportRecord::getAttachmentOssId)
            .filter(Objects::nonNull).distinct().toList();
        recordMapper.delete(Wrappers.<OaImportRecord>lambdaQuery()
            .eq(OaImportRecord::getBatchId, batchId));
        if (batchMapper.deleteById(batchId) != 1) {
            throw new ServiceException("导入批次删除失败，请刷新后重试");
        }
        if (!attachmentOssIds.isEmpty()) {
            ossService.deleteWithValidByIds(attachmentOssIds, false);
        }
        log.info("删除通用导入业务批次，batchId={}, batchNo={}", batchId, batch.getBatchNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OaImportBatchVo importData(Long configId, InputStream inputStream, String sourceFileName) {
        OaImportBusinessConfig config = requireEnabledConfig(configId);
        if (inputStream == null) {
            throw new ServiceException("导入文件不能为空");
        }
        List<Map<String, Object>> rows = readRows(config, inputStream);
        if (rows.isEmpty()) {
            throw new ServiceException("导入文件没有有效数据");
        }
        if (rows.size() > 20_000) {
            throw new ServiceException("单批次最多导入20000条数据");
        }

        List<SysDept> departments = selectActiveOaDepartments();
        Map<String, List<SysDept>> departmentsByName = departments.stream()
            .filter(item -> StringUtils.isNotBlank(item.getDeptName()))
            .collect(Collectors.groupingBy(item -> normalizeName(item.getDeptName()), LinkedHashMap::new, Collectors.toList()));
        Map<String, Long> aliases = aliasMapper.selectList(Wrappers.<OaImportDeptAlias>lambdaQuery()
                .eq(OaImportDeptAlias::getBusinessType, config.getBusinessType())
                .eq(OaImportDeptAlias::getStatus, SystemConstants.NORMAL))
            .stream()
            .filter(item -> item.getDeptId() != null && StringUtils.isNotBlank(item.getNormalizedName()))
            .collect(Collectors.toMap(OaImportDeptAlias::getNormalizedName, OaImportDeptAlias::getDeptId, (left, right) -> left));
        Map<String, List<SysDept>> companiesByName = selectActiveOaCompanies().stream()
            .filter(item -> StringUtils.isNotBlank(item.getDeptName()))
            .collect(Collectors.groupingBy(item -> normalizeName(item.getDeptName()), LinkedHashMap::new, Collectors.toList()));

        OaImportBatch batch = new OaImportBatch();
        batch.setConfigId(config.getId());
        batch.setBusinessType(config.getBusinessType());
        batch.setBatchNo("IMP" + BATCH_TIME.format(LocalDateTime.now()));
        batch.setSourceFileName(StringUtils.isBlank(sourceFileName) ? "导入数据.xlsx" : sourceFileName);
        batch.setStatus(READY);
        batch.setTotalCount(rows.size());
        batch.setMatchedCount(0);
        batch.setGroupCount(0);
        batch.setApplicationCount(0);
        batch.setFailedCount(0);
        batch.setSkippedCount(0);
        batch.setMessage("导入完成，等待提交");
        batchMapper.insert(batch);

        int rowNo = 1;
        for (Map<String, Object> data : rows) {
            OaImportRecord record = new OaImportRecord();
            record.setBatchId(batch.getId());
            record.setRowNo(rowNo++);
            record.setDataJson(JsonUtils.toJsonString(data));
            String rawDept = text(data.get(config.getDeptField()));
            SysDept dept = StringUtils.isBlank(rawDept) ? null
                : resolveDepartment(rawDept, departmentsByName, aliases);
            Long companyId = resolveCompany(text(data.get(config.getCompanyField())), companiesByName);
            if (companyId == null && dept != null) {
                companyId = inferCompanyId(dept);
            }
            if (dept != null) {
                record.setDeptId(dept.getDeptId());
                record.setCompanyId(companyId);
                record.setStatus(MATCHED);
            } else if (StringUtils.isBlank(config.getDeptField())) {
                record.setStatus(MATCHED);
            } else {
                record.setStatus(UNMATCHED);
                record.setErrorMessage("未匹配到本地泛微组织，请在导入详情中维护");
            }
            record.setGroupKey(buildGroupKey(config, data, record.getDeptId()));
            record.setGroupName(buildGroupName(config, data, dept));
            recordMapper.insert(record);
        }
        refreshBatch(batch, "导入完成，请检查业务归属组织匹配结果");
        return toVo(batch, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OaImportBatchVo mapDepartments(Long batchId, OaImportDeptMappingBo bo) {
        OaImportBatch batch = getBatch(batchId);
        OaImportBusinessConfig config = getConfig(batch.getConfigId());
        Map<String, Long> mappings = bo == null || bo.getMappings() == null ? Map.of() : bo.getMappings();
        Map<String, String> skippedDeptReasons = bo == null || bo.getSkippedDeptReasons() == null
            ? Map.of() : bo.getSkippedDeptReasons();
        if (mappings.isEmpty() && skippedDeptReasons.isEmpty()) {
            throw new ServiceException("请至少匹配或跳过一个来源组织");
        }
        if (StringUtils.isBlank(config.getDeptField())) {
            throw new ServiceException("当前业务模板没有配置业务归属组织字段");
        }

        Map<Long, SysDept> departments = selectActiveOaDepartments().stream()
            .filter(item -> item.getDeptId() != null)
            .collect(Collectors.toMap(SysDept::getDeptId, item -> item, (left, right) -> left));
        List<OaImportRecord> records = recordMapper.selectByBatchId(batchId);
        if (records.stream().anyMatch(item -> item.getApplicationId() != null
            || SUBMITTED_RECORD.equals(item.getStatus()) || SUBMITTING.equals(item.getStatus()))) {
            throw new ServiceException("当前批次已有泛微申请，不能再修改组织处理结果");
        }
        Set<String> skippedNames = skippedDeptReasons.keySet().stream()
            .filter(StringUtils::isNotBlank).map(StringUtils::trim).map(this::normalizeName).collect(Collectors.toSet());
        int mapped = 0;
        int skipped = 0;
        for (Map.Entry<String, Long> entry : mappings.entrySet()) {
            String sourceName = StringUtils.trim(entry.getKey());
            SysDept target = departments.get(entry.getValue());
            if (StringUtils.isBlank(sourceName) || target == null || skippedNames.contains(normalizeName(sourceName))) {
                continue;
            }
            String normalizedSource = normalizeName(sourceName);
            saveAlias(config.getBusinessType(), sourceName, normalizedSource, target);
            for (OaImportRecord record : records) {
                Map<String, Object> data = parseData(record.getDataJson());
                if (!normalizedSource.equals(normalizeName(text(data.get(config.getDeptField()))))) {
                    continue;
                }
                record.setDeptId(target.getDeptId());
                // 导入时如果已经通过 Excel 的公司字段匹配到公司，组织纠错不应覆盖这个结果；
                // 只有没有公司快照时，才根据组织树反推归属公司。
                record.setCompanyId(record.getCompanyId() == null ? inferCompanyId(target) : record.getCompanyId());
                record.setStatus(MATCHED);
                record.setErrorMessage(null);
                record.setSkipReason(null);
                record.setGroupKey(buildGroupKey(config, data, target.getDeptId()));
                record.setGroupName(buildGroupName(config, data, target));
                recordMapper.updateById(record);
                mapped++;
            }
        }
        for (Map.Entry<String, String> entry : skippedDeptReasons.entrySet()) {
            String sourceName = StringUtils.trim(entry.getKey());
            if (StringUtils.isBlank(sourceName)) {
                continue;
            }
            String normalizedSource = normalizeName(sourceName);
            String reason = StringUtils.isBlank(entry.getValue()) ? "本批次跳过" : StringUtils.trim(entry.getValue());
            reason = reason.length() > 500 ? reason.substring(0, 500) : reason;
            for (OaImportRecord record : records) {
                Map<String, Object> data = parseData(record.getDataJson());
                if (!normalizedSource.equals(normalizeName(text(data.get(config.getDeptField()))))) {
                    continue;
                }
                record.setDeptId(null);
                record.setStatus(SKIPPED);
                record.setSkipReason(reason);
                record.setErrorMessage(null);
                record.setGroupKey(buildGroupKey(config, data, null));
                record.setGroupName(buildGroupName(config, data, null));
                recordMapper.updateById(record);
                skipped++;
            }
        }
        int pending = (int) records.stream()
            .filter(item -> !SKIPPED.equals(item.getStatus()) && item.getDeptId() == null)
            .count();
        String mappingMessage = "组织处理完成，已匹配" + mapped + "条，已跳过" + skipped + "条"
            + (pending > 0 ? "；仍有" + pending + "条明细未完成组织匹配" : "；当前批次可以提交泛微");
        refreshBatch(batch, mappingMessage);
        return toVo(batch, true);
    }

    @Override
    public List<OaImportApprovalPreviewVo> previewApprovals(Long batchId) {
        OaImportBatch batch = getBatch(batchId);
        OaImportBusinessConfig config = getConfig(batch.getConfigId());
        List<OaImportRecord> activeRecords = recordMapper.selectByBatchId(batchId).stream()
            .filter(item -> !SKIPPED.equals(item.getStatus())).toList();
        if (activeRecords.isEmpty()) {
            return List.of();
        }
        return resolveGroupApprovals(config, groupRecords(activeRecords)).values().stream()
            .map(GroupApproval::getPreview)
            .toList();
    }

    @Override
    public OaImportBatchVo submit(Long batchId, OaImportSubmitBo bo) {
        OaImportBatch batch = getBatch(batchId);
        if (SUBMITTED.equals(batch.getStatus()) || SKIPPED.equals(batch.getStatus())) {
            return toVo(batch, true);
        }
        if (SUBMITTING.equals(batch.getStatus())) {
            throw new ServiceException("当前批次正在提交泛微，请勿重复操作");
        }
        OaImportBusinessConfig config = getConfig(batch.getConfigId());
        List<OaImportRecord> records = recordMapper.selectByBatchId(batchId);
        if (records.isEmpty()) {
            throw new ServiceException("当前批次没有导入明细");
        }
        List<OaImportRecord> activeRecords = records.stream()
            .filter(item -> !SKIPPED.equals(item.getStatus())).toList();
        if (activeRecords.isEmpty()) {
            refreshBatch(batch, "本批次明细已全部跳过，无需提交泛微");
            return toVo(batch, true);
        }
        if (!StringUtils.isBlank(config.getDeptField()) && activeRecords.stream().anyMatch(item -> item.getDeptId() == null)) {
            throw new ServiceException("仍有明细没有匹配业务归属组织，请先完成组织匹配");
        }
        String approvalMode = bo == null || StringUtils.isBlank(bo.getApprovalMode())
            ? "AUTO_RULE" : normalizeApprovalMode(bo.getApprovalMode());
        if (!"AUTO_RULE".equals(approvalMode)) {
            throw new ServiceException("通用导入必须按结算部门审批方案自动匹配，不能使用整批临时审批配置");
        }
        Map<String, Object> parameters = bo == null || bo.getParameters() == null
            ? new LinkedHashMap<>() : new LinkedHashMap<>(bo.getParameters());
        validateParameters(config, parameters);
        Map<String, List<OaImportRecord>> groups = groupRecords(activeRecords);
        Map<String, GroupApproval> approvalByGroup = resolveGroupApprovals(config, groups);
        List<String> invalidApprovals = approvalByGroup.values().stream()
            .filter(item -> !"MATCHED".equals(item.getPreview().getStatus()))
            .map(item -> {
                String deptName = firstNonBlank(item.getPreview().getBusinessDeptName(), item.getPreview().getGroupName());
                return deptName + "：" + firstNonBlank(item.getPreview().getMessage(), "未配置可用审批方案");
            })
            .toList();
        if (!invalidApprovals.isEmpty()) {
            throw new ServiceException("以下结算部门不能提交泛微审批：" + String.join("；", invalidApprovals));
        }

        int claimed = batchMapper.update(null, Wrappers.<OaImportBatch>lambdaUpdate()
            .set(OaImportBatch::getStatus, SUBMITTING)
            .set(OaImportBatch::getMessage, "正在按结算部门审批方案生成附件并提交泛微（已跳过明细不会提交）")
            .eq(OaImportBatch::getId, batchId)
            .in(OaImportBatch::getStatus, READY, PARTIAL_FAILED, FAILED));
        if (claimed != 1) {
            throw new ServiceException("当前批次状态已变化，请刷新后再操作");
        }
        batch = getBatch(batchId);
        batch.setMessage("正在按结算部门审批方案生成附件并提交泛微（已跳过明细不会提交）");
        int success = 0;
        int failed = 0;
        List<String> failures = new ArrayList<>();
        for (List<OaImportRecord> group : groups.values()) {
            if (group.stream().allMatch(item -> SUBMITTED_RECORD.equals(item.getStatus()) && item.getApplicationId() != null)) {
                success++;
                continue;
            }
            Long existingApplicationId = retryApplicationId(group);
            try {
                GroupApproval groupApproval = approvalByGroup.get(groupKey(group.get(0)));
                if (groupApproval == null || groupApproval.getPlan() == null || groupApproval.getWorkflow() == null) {
                    throw new ServiceException("未找到当前结算部门的有效审批方案");
                }
                Long attachmentOssId = group.stream().map(OaImportRecord::getAttachmentOssId)
                    .filter(Objects::nonNull).findFirst().orElse(null);
                Long applicationId = existingApplicationId;
                if (applicationId == null) {
                    // 用户上传过本地修改版时沿用该附件，避免提交时重新生成并覆盖用户修改。
                    if (attachmentOssId == null) {
                        attachmentOssId = generateAttachment(config, batch, group, parameters);
                    }
                    OaApplicationBo application = buildApplication(config, batch, group, parameters,
                        groupApproval.getWorkflow().getId(), approvalMode, groupApproval.getPlan().getId(),
                        null, attachmentOssId);
                    var saved = applicationService.save(application);
                    applicationId = saved.getId();
                    // 先记录申请 ID，再调用泛微。若远程提交失败，下一次可以重试原申请，避免生成重复草稿。
                    for (OaImportRecord record : group) {
                        record.setApplicationId(applicationId);
                        record.setAttachmentOssId(attachmentOssId);
                        record.setStatus(SUBMITTING);
                        recordMapper.updateById(record);
                    }
                }
                var submitted = applicationService.submit(applicationId);
                if (submitted == null || !"IN_PROGRESS".equalsIgnoreCase(submitted.getStatus())) {
                    String status = submitted == null ? "无返回状态" : submitted.getStatus();
                    throw new ServiceException("泛微申请未提交成功，当前状态：" + status + "，请到审批中心核查");
                }
                for (OaImportRecord record : group) {
                    record.setApplicationId(applicationId);
                    record.setAttachmentOssId(attachmentOssId);
                    record.setStatus(SUBMITTED_RECORD);
                    record.setErrorMessage(null);
                    recordMapper.updateById(record);
                }
                success++;
            } catch (Exception ex) {
                failed++;
                String reason = StringUtils.isBlank(ex.getMessage()) ? ex.getClass().getSimpleName() : ex.getMessage();
                reason = reason.length() > 500 ? reason.substring(0, 500) : reason;
                failures.add(StringUtils.isBlank(group.get(0).getGroupName()) ? group.get(0).getGroupKey() : group.get(0).getGroupName()
                    + "：" + reason);
                for (OaImportRecord record : group) {
                    record.setStatus(FAILED_RECORD);
                    record.setErrorMessage(reason);
                    recordMapper.updateById(record);
                }
                log.warn("通用导入批次提交泛微失败，batchId={}, groupKey={}", batchId, group.get(0).getGroupKey(), ex);
            }
        }
        String message = failures.isEmpty()
            ? "已成功提交" + success + "个泛微审批分组"
            : "成功" + success + "个分组，失败" + failed + "个：" + String.join("；", failures);
        refreshBatch(batch, message);
        return toVo(batch, true);
    }

    private Map<String, List<OaImportRecord>> groupRecords(List<OaImportRecord> records) {
        return records.stream().collect(Collectors.groupingBy(this::groupKey,
            LinkedHashMap::new, Collectors.toList()));
    }

    private String groupKey(OaImportRecord record) {
        return StringUtils.isBlank(record.getGroupKey()) ? "__ALL__" : record.getGroupKey();
    }

    private Map<String, GroupApproval> resolveGroupApprovals(OaImportBusinessConfig config,
                                                              Map<String, List<OaImportRecord>> groups) {
        Map<String, GroupApproval> result = new LinkedHashMap<>();
        groups.forEach((groupKey, group) -> result.put(groupKey, resolveGroupApproval(config, groupKey, group)));
        return result;
    }

    private GroupApproval resolveGroupApproval(OaImportBusinessConfig config, String groupKey,
                                               List<OaImportRecord> group) {
        OaImportApprovalPreviewVo preview = new OaImportApprovalPreviewVo();
        preview.setGroupKey(groupKey);
        preview.setGroupName(StringUtils.isBlank(group.get(0).getGroupName()) ? groupKey : group.get(0).getGroupName());
        preview.setRecordCount(group.size());
        List<Long> deptIds = group.stream().map(OaImportRecord::getDeptId).filter(Objects::nonNull).distinct().toList();
        if (deptIds.size() > 1) {
            preview.setStatus("AMBIGUOUS_DEPT");
            preview.setMessage("当前分组包含多个结算部门，请将结算部门字段加入业务模板的分组字段");
            return new GroupApproval(preview, null, null);
        }
        Long businessDeptId = deptIds.isEmpty() ? null : deptIds.get(0);
        preview.setBusinessDeptId(businessDeptId);
        preview.setBusinessDeptName(deptName(businessDeptId));
        OaDepartmentApprovalVo plan = null;
        try {
            plan = departmentApprovalService.resolveForImport(
                config.getBusinessType(), "OA_IMPORT", businessDeptId, group.get(0).getDataJson());
            if (plan == null) {
                preview.setStatus("MISSING_CONFIG");
                preview.setMessage("未配置该结算部门的审批方案，请先在审批方案管理中配置");
                return new GroupApproval(preview, null, null);
            }
            OaWorkflowConfig workflow = workflowConfigService.requireEnabled(plan.getWorkflowConfigId(), config.getBusinessType());
            departmentApprovalService.resolve(buildApprovalContext(config, group, businessDeptId, plan));
            List<OaDepartmentApprovalUserVo> users = plan.getUsers() == null ? List.of() : plan.getUsers();
            preview.setApprovalPlanId(plan.getId());
            preview.setPlanName(plan.getPlanName());
            preview.setWorkflowConfigId(workflow.getId());
            preview.setWorkflowId(workflow.getWorkflowId());
            preview.setWorkflowName(workflow.getWorkflowName());
            preview.setFormName(workflow.getFormName());
            preview.setApprovalCode(workflow.getApprovalCode());
            preview.setApprovalName(workflow.getApprovalName());
            preview.setProcessType(plan.getProcessType());
            preview.setApprovers(users.stream()
                .filter(item -> !"COPY".equalsIgnoreCase(item.getParticipantRole())).toList());
            preview.setCopyUsers(users.stream()
                .filter(item -> "COPY".equalsIgnoreCase(item.getParticipantRole())).toList());
            preview.setStatus("MATCHED");
            preview.setMessage("已匹配该结算部门审批方案");
            return new GroupApproval(preview, plan, workflow);
        } catch (Exception ex) {
            String reason = StringUtils.isBlank(ex.getMessage()) ? "审批方案不可用" : ex.getMessage();
            preview.setStatus("INVALID_CONFIG");
            preview.setMessage(reason.length() > 500 ? reason.substring(0, 500) : reason);
            return new GroupApproval(preview, plan, null);
        }
    }

    private OaApplication buildApprovalContext(OaImportBusinessConfig config, List<OaImportRecord> group,
                                               Long businessDeptId, OaDepartmentApprovalVo plan) {
        OaApplication application = new OaApplication();
        application.setBusinessType(config.getBusinessType());
        application.setSourceModule("OA_IMPORT");
        application.setWorkflowConfigId(plan.getWorkflowConfigId());
        application.setApprovalPlanId(plan.getId());
        application.setDeptId(businessDeptId);
        application.setDeptIds(businessDeptId == null ? List.of() : List.of(businessDeptId));
        application.setFormDataJson(group.get(0).getDataJson());
        return application;
    }

    @Override
    public OaAttachmentPreviewVo previewAttachment(Long batchId, OaImportAttachmentPreviewBo bo) {
        OaImportBatch batch = getBatch(batchId);
        OaImportBusinessConfig config = getConfig(batch.getConfigId());
        List<OaImportRecord> activeRecords = recordMapper.selectByBatchId(batchId).stream()
            .filter(item -> !SKIPPED.equals(item.getStatus())).toList();
        if (activeRecords.isEmpty()) {
            throw new ServiceException("当前批次没有可预览的有效明细");
        }
        if (!StringUtils.isBlank(config.getDeptField()) && activeRecords.stream().anyMatch(item -> item.getDeptId() == null)) {
            throw new ServiceException("仍有明细没有匹配业务归属组织，请先完成组织匹配");
        }
        String requestedGroupKey = bo == null ? null : StringUtils.trim(bo.getGroupKey());
        final String groupKey = StringUtils.isBlank(requestedGroupKey) ? "__ALL__" : requestedGroupKey;
        List<OaImportRecord> group = activeRecords.stream()
            .filter(item -> groupKey.equals(StringUtils.isBlank(item.getGroupKey()) ? "__ALL__" : item.getGroupKey()))
            .toList();
        if (group.isEmpty()) {
            throw new ServiceException("预览分组不存在或已被跳过，请刷新批次后重试");
        }
        Map<String, Object> parameters = bo == null || bo.getParameters() == null
            ? new LinkedHashMap<>() : new LinkedHashMap<>(bo.getParameters());
        validateParameters(config, parameters);
        GeneratedAttachment generated = buildAttachment(config, batch, group, parameters);
        OaAttachmentPreviewVo preview = readWorkbookPreview(generated.getBody(), generated.getFileName());
        preview.setMessage("提交前预览，不会上传或创建泛微申请；确认提交后系统才会正式生成附件。" + (StringUtils.isBlank(preview.getMessage()) ? "" : " " + preview.getMessage()));
        return preview;
    }

    @Override
    public ResponseEntity<byte[]> downloadAttachment(Long batchId, OaImportAttachmentPreviewBo bo) {
        OaImportBatch batch = getBatch(batchId);
        OaImportBusinessConfig config = getConfig(batch.getConfigId());
        List<OaImportRecord> group = attachmentGroup(batchId, bo);
        Long attachmentOssId = group.stream().map(OaImportRecord::getAttachmentOssId)
            .filter(Objects::nonNull).findFirst().orElse(null);
        if (attachmentOssId != null) {
            SysOssVo oss = ossService.getById(attachmentOssId);
            if (oss == null) {
                throw new ServiceException("附件文件不存在，请重新生成或上传");
            }
            ResponseEntity<byte[]> response = ossService.download(attachmentOssId);
            byte[] body = response == null ? null : response.getBody();
            if (body == null || body.length == 0) {
                throw new ServiceException("附件文件为空或无法读取");
            }
            String fileName = firstNonBlank(oss.getOriginalName(), "导入业务附件.xlsx");
            return attachmentDownloadResponse(body, fileName);
        }
        Map<String, Object> parameters = bo == null || bo.getParameters() == null
            ? new LinkedHashMap<>() : new LinkedHashMap<>(bo.getParameters());
        validateParameters(config, parameters);
        GeneratedAttachment generated = buildAttachment(config, batch, group, parameters);
        return attachmentDownloadResponse(generated.getBody(), generated.getFileName(), generated.getContentType());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OaImportBatchVo uploadAttachment(Long batchId, String groupKey, MultipartFile file) {
        OaImportBatch batch = getBatch(batchId);
        if (SUBMITTED.equals(batch.getStatus()) || SUBMITTING.equals(batch.getStatus())) {
            throw new ServiceException("当前批次已提交或正在提交，不能再修改附件");
        }
        OaImportBusinessConfig config = getConfig(batch.getConfigId());
        if (attachmentConfig(config) == null || !"GENERATED_TABLE".equalsIgnoreCase(attachmentConfig(config).getMode())) {
            throw new ServiceException("当前业务未配置可上传的 Excel 附件模板");
        }
        List<OaImportRecord> records = recordMapper.selectByBatchId(batchId).stream()
            .filter(item -> !SKIPPED.equals(item.getStatus())).toList();
        if (records.stream().anyMatch(item -> item.getApplicationId() != null
            || SUBMITTED_RECORD.equals(item.getStatus()) || SUBMITTING.equals(item.getStatus()))) {
            throw new ServiceException("当前批次已有泛微申请，不能再修改附件");
        }
        List<OaImportRecord> group = attachmentGroup(records, groupKey);
        validateUploadedAttachment(config, file);

        List<Long> oldAttachmentOssIds = group.stream().map(OaImportRecord::getAttachmentOssId)
            .filter(Objects::nonNull).distinct().toList();
        SysOssExt ossExt = new SysOssExt();
        ossExt.setBizType("OA_IMPORT_ATTACHMENT");
        ossExt.setSource("OA_IMPORT_MANUAL");
        ossExt.setRefId(batchId + ":" + groupKey(group.get(0)));
        ossExt.setRefType(config.getBusinessType());
        ossExt.setIsTemp(false);
        SysOssVo uploaded = null;
        try {
            uploaded = ossService.upload(file, ossExt);
            if (uploaded == null || uploaded.getOssId() == null) {
                throw new ServiceException("修改后的附件上传失败");
            }
            for (OaImportRecord record : group) {
                record.setAttachmentOssId(uploaded.getOssId());
                recordMapper.updateById(record);
            }
            if (!oldAttachmentOssIds.isEmpty()) {
                ossService.deleteWithValidByIds(oldAttachmentOssIds, false);
            }
            refreshBatch(batch, "分组“" + groupName(group) + "”的修改版附件已上传，可继续提交泛微");
            return toVo(getBatch(batchId), true);
        } catch (Exception ex) {
            if (uploaded != null && uploaded.getOssId() != null) {
                ossService.deleteWithValidByIds(List.of(uploaded.getOssId()), false);
            }
            if (ex instanceof ServiceException serviceException) {
                throw serviceException;
            }
            throw new ServiceException("上传修改后的附件失败：" + ex.getMessage());
        }
    }

    /** 获取当前批次中可用于附件操作的分组，跳过已明确排除的明细。 */
    private List<OaImportRecord> attachmentGroup(Long batchId, OaImportAttachmentPreviewBo bo) {
        List<OaImportRecord> records = recordMapper.selectByBatchId(batchId).stream()
            .filter(item -> !SKIPPED.equals(item.getStatus())).toList();
        return attachmentGroup(records, bo == null ? null : bo.getGroupKey());
    }

    private List<OaImportRecord> attachmentGroup(List<OaImportRecord> records, String requestedGroupKey) {
        String normalizedGroupKey = StringUtils.isBlank(requestedGroupKey) ? "__ALL__" : StringUtils.trim(requestedGroupKey);
        List<OaImportRecord> group = records.stream()
            .filter(item -> normalizedGroupKey.equals(groupKey(item)))
            .toList();
        if (group.isEmpty()) {
            throw new ServiceException("附件分组不存在或已被跳过，请刷新批次后重试");
        }
        return group;
    }

    private String groupName(List<OaImportRecord> group) {
        return firstNonBlank(group.get(0).getGroupName(), groupKey(group.get(0)));
    }

    private ResponseEntity<byte[]> attachmentDownloadResponse(byte[] body, String fileName) {
        return attachmentDownloadResponse(body, fileName,
            MediaTypeFactory.getMediaType(fileName).map(MediaType::toString).orElse(null));
    }

    private ResponseEntity<byte[]> attachmentDownloadResponse(byte[] body, String fileName, String contentType) {
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (StringUtils.isNotBlank(contentType)) {
            try {
                mediaType = MediaType.parseMediaType(contentType);
            } catch (Exception ignored) {
                // 使用二进制类型，避免异常文件类型阻断附件下载。
            }
        }
        String safeName = safeFileName(fileName);
        return ResponseEntity.ok()
            .contentType(mediaType)
            .contentLength(body.length)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(safeName, StandardCharsets.UTF_8).build().toString())
            .body(body);
    }

    /** 校验用户修改后的附件仍然对应当前业务模板，避免任意 Excel 被提交到泛微。 */
    private void validateUploadedAttachment(OaImportBusinessConfig config, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("请选择要上传的 Excel 附件");
        }
        String fileName = StringUtils.defaultIfBlank(file.getOriginalFilename(), "").toLowerCase(Locale.ROOT);
        if (!fileName.endsWith(".xls") && !fileName.endsWith(".xlsx")) {
            throw new ServiceException("修改后的附件仅支持 .xls 或 .xlsx 格式");
        }
        AttachmentConfig attachment = attachmentConfig(config);
        String targetSheetName = StringUtils.isBlank(attachment.getSheetName()) ? "数据明细" : attachment.getSheetName();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheet(targetSheetName);
            if (sheet == null) {
                throw new ServiceException("附件工作表不匹配，应包含工作表：" + targetSheetName);
            }
            int headerRowIndex = attachment.getHeaderRow() == null ? 0 : attachment.getHeaderRow();
            Row headerRow = sheet.getRow(headerRowIndex);
            if (headerRow == null) {
                throw new ServiceException("附件缺少模板表头，请不要删除模板结构");
            }
            List<OutputColumn> columns = attachment.getHeaders() == null ? List.of() : attachment.getHeaders();
            if (columns.isEmpty()) {
                columns = fieldDefinitions(config).stream()
                    .map(field -> new OutputColumn(field.getHeader(), field.getCode())).toList();
            }
            DataFormatter formatter = new DataFormatter(Locale.ROOT);
            for (int i = 0; i < columns.size(); i++) {
                OutputColumn column = columns.get(i);
                if (column == null || StringUtils.isBlank(column.getLabel())) {
                    continue;
                }
                int columnIndex = column.getColumnIndex() == null ? i : column.getColumnIndex();
                if (columnIndex < 0) {
                    throw new ServiceException("附件模板列位置不正确");
                }
                String actualLabel = formatter.formatCellValue(headerRow.getCell(columnIndex));
                if (!normalizeHeader(column.getLabel()).equals(normalizeHeader(actualLabel))) {
                    throw new ServiceException("附件表头与当前业务模板不一致，请使用本批次下载的附件进行修改");
                }
            }
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceException("无法读取修改后的 Excel 附件：" + ex.getMessage());
        }
    }

    private Long retryApplicationId(List<OaImportRecord> records) {
        Set<Long> applicationIds = records.stream().map(OaImportRecord::getApplicationId)
            .filter(Objects::nonNull).collect(Collectors.toSet());
        if (applicationIds.size() != 1 || records.stream().anyMatch(item -> SUBMITTED_RECORD.equals(item.getStatus()))) {
            return null;
        }
        return applicationIds.iterator().next();
    }

    private List<Map<String, Object>> readRows(OaImportBusinessConfig config, InputStream inputStream) {
        List<FieldDefinition> fields = fieldDefinitions(config);
        if (fields.isEmpty()) {
            throw new ServiceException("请先配置 Excel 字段定义");
        }
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = StringUtils.isBlank(config.getSheetName()) ? workbook.getSheetAt(0) : workbook.getSheet(config.getSheetName());
            if (sheet == null) {
                throw new ServiceException("找不到指定工作表：" + config.getSheetName());
            }
            int headerRowIndex = config.getHeaderRow() == null ? 0 : config.getHeaderRow();
            Row headerRow = sheet.getRow(headerRowIndex);
            if (headerRow == null) {
                throw new ServiceException("找不到 Excel 表头行");
            }
            DataFormatter formatter = new DataFormatter(Locale.ROOT);
            Map<String, Integer> columns = new LinkedHashMap<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                String header = normalizeHeader(formatter.formatCellValue(headerRow.getCell(i)));
                if (StringUtils.isNotBlank(header)) {
                    columns.putIfAbsent(header, i);
                }
            }
            for (FieldDefinition field : fields) {
                if (StringUtils.isBlank(field.getCode()) || StringUtils.isBlank(field.getHeader())) {
                    throw new ServiceException("Excel字段定义缺少 code 或 header");
                }
                if (!columns.containsKey(normalizeHeader(field.getHeader()))) {
                    throw new ServiceException("Excel缺少字段：" + field.getHeader());
                }
            }
            List<Map<String, Object>> rows = new ArrayList<>();
            Set<String> uniqueKeys = new HashSet<>();
            for (int rowIndex = headerRowIndex + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isBlankRow(row, formatter)) {
                    continue;
                }
                Map<String, Object> data = new LinkedHashMap<>();
                boolean hasValue = false;
                for (FieldDefinition field : fields) {
                    String value = formatter.formatCellValue(row.getCell(columns.get(normalizeHeader(field.getHeader())))).trim();
                    if (StringUtils.isNotBlank(value)) {
                        hasValue = true;
                    } else if (Boolean.TRUE.equals(field.getRequired())) {
                        throw new ServiceException("第" + (rowIndex + 1) + "行字段“" + field.getHeader() + "”不能为空");
                    }
                    data.put(field.getCode(), value);
                }
                if (!hasValue) {
                    continue;
                }
                String uniqueField = fields.stream().filter(field -> Boolean.TRUE.equals(field.getUniqueKey())).map(FieldDefinition::getCode).findFirst().orElse(null);
                if (StringUtils.isNotBlank(uniqueField)) {
                    String uniqueValue = normalizeName(text(data.get(uniqueField)));
                    if (StringUtils.isNotBlank(uniqueValue) && !uniqueKeys.add(uniqueValue)) {
                        throw new ServiceException("第" + (rowIndex + 1) + "行存在重复唯一值：" + uniqueValue);
                    }
                }
                rows.add(data);
            }
            return rows;
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceException("读取 Excel 失败：" + ex.getMessage());
        }
    }

    private boolean isBlankRow(Row row, DataFormatter formatter) {
        for (int i = row.getFirstCellNum(); i >= 0 && i < row.getLastCellNum(); i++) {
            if (StringUtils.isNotBlank(formatter.formatCellValue(row.getCell(i)))) {
                return false;
            }
        }
        return true;
    }

    private Long generateAttachment(OaImportBusinessConfig config, OaImportBatch batch,
                                    List<OaImportRecord> records, Map<String, Object> parameters) {
        AttachmentConfig attachment = attachmentConfig(config);
        if (attachment == null || !"GENERATED_TABLE".equalsIgnoreCase(attachment.getMode())) {
            return null;
        }
        GeneratedAttachment generated = buildAttachment(config, batch, records, parameters);
        SysOssExt ossExt = new SysOssExt();
        ossExt.setBizType("OA_IMPORT_ATTACHMENT");
        ossExt.setSource("OA_IMPORT_GENERATED");
        ossExt.setRefId(batch.getId().toString());
        ossExt.setRefType(config.getBusinessType());
        ossExt.setIsTemp(false);
        ossExt.setContentType(generated.getContentType());
        java.nio.file.Path tempFile = null;
        try {
            tempFile = Files.createTempFile("oa-import-", "-" + generated.getFileName());
            Files.write(tempFile, generated.getBody());
            SysOssVo oss = ossService.upload(tempFile.toFile(), ossExt);
            return oss.getOssId();
        } catch (Exception ex) {
            throw new ServiceException("生成通用业务附件失败：" + ex.getMessage());
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ex) {
                    log.warn("清理通用业务附件临时文件失败，path={}", tempFile, ex);
                }
            }
        }
    }

    private GeneratedAttachment buildAttachment(OaImportBusinessConfig config, OaImportBatch batch,
                                                List<OaImportRecord> records, Map<String, Object> parameters) {
        AttachmentConfig attachment = attachmentConfig(config);
        if (attachment == null || !"GENERATED_TABLE".equalsIgnoreCase(attachment.getMode())) {
            throw new ServiceException("当前业务未配置可生成的 Excel 附件模板");
        }
        List<OutputColumn> columns = attachment.getHeaders() == null ? List.of() : attachment.getHeaders();
        if (columns.isEmpty()) {
            columns = fieldDefinitions(config).stream().map(field -> new OutputColumn(field.getHeader(), field.getCode())).toList();
        }
        String groupName = StringUtils.isBlank(records.get(0).getGroupName()) ? records.get(0).getGroupKey() : records.get(0).getGroupName();
        Map<String, Object> context = buildContext(config, batch, records, parameters);
        String title = render(firstNonBlank(attachment.getTitleTemplate(), config.getBusinessName()), context, groupName);
        SysOssVo templateOss = attachment.getTemplateOssId() == null ? null : ossService.getById(attachment.getTemplateOssId());
        String templateFileName = firstNonBlank(attachment.getTemplateFileName(), templateOss == null ? null : templateOss.getOriginalName());
        boolean legacyExcel = StringUtils.isNotBlank(templateFileName)
            && templateFileName.toLowerCase(Locale.ROOT).endsWith(".xls")
            && !templateFileName.toLowerCase(Locale.ROOT).endsWith(".xlsx");
        String outputExtension = legacyExcel ? ".xls" : ".xlsx";
        String outputContentType = legacyExcel ? "application/vnd.ms-excel"
            : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        String fileName = safeFileName(render(firstNonBlank(attachment.getFileNameTemplate(), "{businessType}-{batchNo}-{groupName}"), context, groupName)) + outputExtension;
        try (Workbook workbook = openAttachmentWorkbook(attachment); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            String sheetName = StringUtils.isBlank(attachment.getSheetName()) ? "数据明细" : attachment.getSheetName();
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new ServiceException("附件模板中不存在工作表：" + sheetName + "，请重新上传模板并确认目标工作表位于第一张");
            }
            int titleRowIndex = attachment.getTitleRow() == null ? 0 : attachment.getTitleRow();
            int headerRowIndex = attachment.getHeaderRow() == null ? titleRowIndex + 1 : attachment.getHeaderRow();
            int dataStartRowIndex = attachment.getDataStartRow() == null ? headerRowIndex + 1 : attachment.getDataStartRow();
            Row titleRow = rowOf(sheet, titleRowIndex);
            cellOf(titleRow, 0).setCellValue(title);
            Row headerRow = rowOf(sheet, headerRowIndex);
            for (int i = 0; i < columns.size(); i++) {
                OutputColumn column = columns.get(i);
                int columnIndex = column.getColumnIndex() == null ? i : column.getColumnIndex();
                if (columnIndex < 0) {
                    throw new ServiceException("附件模板列位置不能小于 0");
                }
                // 空标题列可能是模板预留的公式列，不能因为映射为空而覆盖模板原有内容。
                if (StringUtils.isNotBlank(column.getLabel())) {
                    cellOf(headerRow, columnIndex).setCellValue(column.getLabel());
                }
            }

            int templateTotalRowIndex = attachment.getTotalRow() == null ? -1 : attachment.getTotalRow();
            int templateDetailCount = templateTotalRowIndex > dataStartRowIndex
                ? Math.max(1, templateTotalRowIndex - dataStartRowIndex) : 1;
            int tailStartRowIndex = templateTotalRowIndex > dataStartRowIndex
                ? templateTotalRowIndex : dataStartRowIndex + 1;
            int rowDelta = records.size() - templateDetailCount;
            if (rowDelta < 0 && templateTotalRowIndex > dataStartRowIndex) {
                // POI 移动合计行时不会自动删除被收缩出来的旧明细行，先清掉这段残留行，
                // 避免生成文件中在合计/固定尾部后面又出现模板样例数据。
                int staleStart = dataStartRowIndex + records.size();
                for (int rowIndex = staleStart; rowIndex < templateTotalRowIndex; rowIndex++) {
                    Row staleRow = sheet.getRow(rowIndex);
                    if (staleRow != null) {
                        sheet.removeRow(staleRow);
                    }
                }
            }
            int lastRowIndex = sheet.getLastRowNum();
            if (tailStartRowIndex <= lastRowIndex && rowDelta != 0) {
                // 只移动合计行及其后的固定内容，避免覆盖签字栏、说明和其他尾部区域。
                sheet.shiftRows(tailStartRowIndex, lastRowIndex, rowDelta);
            }
            Row detailPrototype = rowOf(sheet, dataStartRowIndex);
            for (int i = 0; i < records.size(); i++) {
                Map<String, Object> rowData = parseData(records.get(i).getDataJson());
                Row row = rowOf(sheet, i + dataStartRowIndex);
                if (row != detailPrototype) {
                    copyRow(detailPrototype, row, i);
                }
                for (int j = 0; j < columns.size(); j++) {
                    OutputColumn column = columns.get(j);
                    int columnIndex = column.getColumnIndex() == null ? j : column.getColumnIndex();
                    // field 为空表示该列由模板中的公式或固定内容负责，不覆盖模板单元格。
                    if (StringUtils.isBlank(column.getField())) {
                        continue;
                    }
                    Object value = rowData.get(column.getField());
                    if (value == null) {
                        value = context.get(column.getField());
                    }
                    Cell cell = cellOf(row, columnIndex);
                    if (value == null) {
                        // 已配置映射但本行没有值时清空，避免把模板样例值带到正式附件。
                        cell.setBlank();
                    } else {
                        setCellValue(cell, value);
                    }
                }
            }
            if (templateTotalRowIndex > dataStartRowIndex) {
                int totalRowIndex = dataStartRowIndex + records.size();
                Row totalRow = rowOf(sheet, totalRowIndex);
                adjustTotalFormulas(totalRow, dataStartRowIndex, dataStartRowIndex + templateDetailCount - 1,
                    dataStartRowIndex, totalRowIndex - 1);
                if (StringUtils.isNotBlank(attachment.getTotalLabel())) {
                    Cell labelCell = cellOf(totalRow, 0);
                    if (labelCell.getCellType() != CellType.FORMULA) {
                        labelCell.setCellValue(attachment.getTotalLabel());
                    }
                }
            }
            applyFixedCellMappings(sheet, attachment, context, tailStartRowIndex, rowDelta);
            // 公式由 Excel/在线预览端重新计算，避免将模板公式改写成固定数值。
            workbook.setForceFormulaRecalculation(true);
            workbook.write(output);
            GeneratedAttachment generated = new GeneratedAttachment();
            generated.setFileName(fileName);
            generated.setContentType(outputContentType);
            generated.setBody(output.toByteArray());
            return generated;
        } catch (Exception ex) {
            throw new ServiceException("生成通用业务附件失败：" + ex.getMessage());
        }
    }

    private Workbook openAttachmentWorkbook(AttachmentConfig attachment) throws IOException {
        if (attachment.getTemplateOssId() == null) {
            throw new ServiceException("未配置附件模板，请先在导入业务模板中上传附件模板");
        }
        ResponseEntity<byte[]> response = ossService.download(attachment.getTemplateOssId());
        byte[] body = response == null ? null : response.getBody();
        if (body == null || body.length == 0) {
            throw new ServiceException("附件模板不存在或无法读取：" + attachment.getTemplateOssId());
        }
        try {
            return WorkbookFactory.create(new ByteArrayInputStream(body));
        } catch (Exception ex) {
            throw new ServiceException("附件模板不是有效的 Excel 文件：" + ex.getMessage());
        }
    }

    /** 按配置覆盖模板固定区域；未确认的候选不会进入 fixedMappings，因此会继续保留原内容。 */
    private void applyFixedCellMappings(Sheet sheet, AttachmentConfig attachment, Map<String, Object> context,
                                        int tailStartRowIndex, int rowDelta) {
        if (attachment.getFixedMappings() == null || attachment.getFixedMappings().isEmpty()) {
            return;
        }
        for (FixedCellMapping mapping : attachment.getFixedMappings()) {
            if (mapping == null || mapping.getRow() == null || mapping.getColumn() == null) {
                continue;
            }
            boolean hasSegments = mapping.getSegments() != null && !mapping.getSegments().isEmpty();
            String mode = StringUtils.isBlank(mapping.getMode()) ? "KEEP"
                : mapping.getMode().trim().toUpperCase(Locale.ROOT);
            // 多标签配置的 mode 位于 segments 内，不能被外层默认 KEEP 提前跳过。
            if ("KEEP".equals(mode) && !hasSegments) {
                continue;
            }
            int targetRowIndex = mapping.getRow();
            if (targetRowIndex >= tailStartRowIndex) {
                targetRowIndex += rowDelta;
            }
            if (targetRowIndex < 0 || mapping.getColumn() < 0) {
                throw new ServiceException("固定区域单元格位置不正确：" + mapping.getCell());
            }
            Cell cell = fixedCellOf(sheet, targetRowIndex, mapping.getColumn(), mapping.getCell());
            if (hasSegments) {
                String originalContent = cell.getCellType() == CellType.STRING
                    ? cell.getStringCellValue() : new DataFormatter().formatCellValue(cell);
                cell.setCellValue(renderFixedCellSegments(originalContent, mapping.getSegments(), context,
                    text(context.get("groupName")), mapping.getCell()));
                continue;
            }
            String template = mapping.getTemplate();
            String label = StringUtils.isBlank(mapping.getLabel())
                ? fixedLabel(mapping.getOriginalContent()) : mapping.getLabel();
            if ("CLEAR".equals(mode)) {
                cell.setCellValue(StringUtils.isBlank(template) ? label : template);
                continue;
            }
            Map<String, Object> values = new LinkedHashMap<>(context);
            if ("FIELD".equals(mode) && StringUtils.isNotBlank(mapping.getField())) {
                values.put("value", context.get(mapping.getField()));
            }
            if (StringUtils.isBlank(template)) {
                template = label;
            }
            cell.setCellValue(render(template, values, text(context.get("groupName"))));
        }
    }

    /** 在同一个单元格内按用户配置的标签替换对应值，未配置的其他文本保持不变。 */
    private String renderFixedCellSegments(String originalContent, List<FixedCellSegment> segments,
                                           Map<String, Object> context, String groupName, String cellAddress) {
        if (StringUtils.isBlank(originalContent)) {
            return originalContent;
        }
        List<ResolvedFixedSegment> resolved = new ArrayList<>();
        Set<Integer> usedStarts = new HashSet<>();
        for (FixedCellSegment segment : segments) {
            if (segment == null || StringUtils.isBlank(segment.getLabel())) {
                throw new ServiceException("固定区域单元格 " + cellAddress + " 存在空标签，请重新配置");
            }
            String label = StringUtils.trim(segment.getLabel());
            int start = originalContent.indexOf(label);
            while (start >= 0 && usedStarts.contains(start)) {
                start = originalContent.indexOf(label, start + 1);
            }
            if (start < 0) {
                throw new ServiceException("固定区域单元格 " + cellAddress + " 中未找到标签：" + label);
            }
            usedStarts.add(start);
            resolved.add(new ResolvedFixedSegment(segment, label, start));
        }
        resolved.sort((left, right) -> Integer.compare(left.start, right.start));
        List<Integer> boundaries = fixedLabelBoundaries(originalContent, resolved);
        StringBuilder result = new StringBuilder(originalContent.length());
        int cursor = 0;
        for (ResolvedFixedSegment item : resolved) {
            if (item.start < cursor) {
                throw new ServiceException("固定区域单元格 " + cellAddress + " 的标签配置存在重叠：" + item.label);
            }
            result.append(originalContent, cursor, item.start);
            int labelEnd = item.start + item.label.length();
            int nextBoundary = originalContent.length();
            for (Integer boundary : boundaries) {
                if (boundary > labelEnd && boundary < nextBoundary) {
                    nextBoundary = boundary;
                }
            }
            String originalValueWithSpacing = originalContent.substring(labelEnd, nextBoundary);
            String spacing = trailingWhitespace(originalValueWithSpacing);
            String originalValue = spacing.isEmpty()
                ? originalValueWithSpacing : originalValueWithSpacing.substring(0,
                    originalValueWithSpacing.length() - spacing.length());
            result.append(item.label);
            result.append(renderFixedSegmentValue(item.segment, originalValue, context, groupName));
            result.append(spacing);
            cursor = nextBoundary;
        }
        return result.append(originalContent.substring(cursor)).toString();
    }

    private List<Integer> fixedLabelBoundaries(String content, List<ResolvedFixedSegment> resolved) {
        Set<Integer> positions = new HashSet<>();
        Matcher matcher = FIXED_LABEL_PATTERN.matcher(content);
        while (matcher.find()) {
            positions.add(matcher.start());
        }
        for (ResolvedFixedSegment item : resolved) {
            positions.add(item.start);
        }
        return positions.stream().sorted().collect(Collectors.toList());
    }

    private String trailingWhitespace(String value) {
        int start = value.length();
        while (start > 0 && Character.isWhitespace(value.charAt(start - 1))) {
            start--;
        }
        return value.substring(start);
    }

    private String renderFixedSegmentValue(FixedCellSegment segment, String originalValue,
                                           Map<String, Object> context, String groupName) {
        String mode = StringUtils.isBlank(segment.getMode()) ? "KEEP"
            : segment.getMode().trim().toUpperCase(Locale.ROOT);
        if ("KEEP".equals(mode)) {
            return originalValue;
        }
        if ("CLEAR".equals(mode)) {
            return "";
        }
        String template = segment.getTemplate();
        Map<String, Object> values = new LinkedHashMap<>(context);
        if ("FIELD".equals(mode)) {
            if (StringUtils.isBlank(segment.getField())) {
                throw new ServiceException("固定区域标签“" + segment.getLabel() + "”未配置导入字段");
            }
            values.put("value", context.get(segment.getField()));
            template = StringUtils.isBlank(template) ? "{value}" : template;
        } else if (StringUtils.isBlank(template)) {
            template = switch (mode) {
                case "CURRENT_USER" -> "{currentUserName}";
                case "CURRENT_DEPT" -> "{currentDeptName}";
                case "SETTLEMENT_DEPT" -> "{settlementDeptName}";
                case "SETTLEMENT_OWNER" -> "{settlementDeptOwner}";
                default -> "";
            };
        }
        return render(template, values, groupName);
    }

    private Cell fixedCellOf(Sheet sheet, int rowIndex, int columnIndex, String cellAddress) {
        for (CellRangeAddress region : sheet.getMergedRegions()) {
            if (region.isInRange(rowIndex, columnIndex)
                && (region.getFirstRow() != rowIndex || region.getFirstColumn() != columnIndex)) {
                throw new ServiceException("固定区域必须配置合并单元格左上角：" + cellAddress);
            }
        }
        return cellOf(rowOf(sheet, rowIndex), columnIndex);
    }

    private String fixedLabel(String content) {
        if (StringUtils.isBlank(content)) {
            return "";
        }
        int colonIndex = Math.max(content.indexOf('：'), content.indexOf(':'));
        return colonIndex < 0 ? content : StringUtils.trim(content.substring(0, colonIndex + 1));
    }

    private String userDisplayName(SysUser user) {
        if (user == null) {
            return "";
        }
        String name = firstNonBlank(user.getNickName(), user.getUserName());
        return name == null ? "" : name;
    }

    private OaAttachmentPreviewVo readWorkbookPreview(byte[] body, String fileName) {
        OaAttachmentPreviewVo preview = new OaAttachmentPreviewVo();
        preview.setFileName(fileName);
        preview.setContentType(StringUtils.endsWithIgnoreCase(fileName, ".xls")
            ? "application/vnd.ms-excel"
            : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(body))) {
            List<String> sheetNames = new ArrayList<>();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                sheetNames.add(workbook.getSheetAt(i).getSheetName());
            }
            if (sheetNames.isEmpty()) {
                throw new ServiceException("生成的附件没有可预览的工作表");
            }
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter(Locale.ROOT);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            final int maxRows = 200;
            final int maxColumns = 40;
            int lastRow = Math.max(sheet.getLastRowNum(), 0);
            int rowLimit = Math.min(lastRow, maxRows - 1);
            int columnCount = 0;
            List<Integer> rowNumbers = new ArrayList<>();
            List<List<String>> rows = new ArrayList<>();
            for (int rowIndex = 0; rowIndex <= rowLimit; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                int lastCell = row == null ? 0 : Math.max(row.getLastCellNum(), 0);
                int currentColumnCount = Math.min(lastCell, maxColumns);
                List<String> values = new ArrayList<>();
                boolean hasValue = false;
                for (int columnIndex = 0; columnIndex < currentColumnCount; columnIndex++) {
                    String value = row == null ? "" : formatter.formatCellValue(row.getCell(columnIndex), evaluator);
                    values.add(value);
                    hasValue |= StringUtils.isNotBlank(value);
                }
                if (hasValue) {
                    columnCount = Math.max(columnCount, values.size());
                    rowNumbers.add(rowIndex + 1);
                    rows.add(values);
                }
            }
            for (List<String> row : rows) {
                while (row.size() < columnCount) {
                    row.add("");
                }
            }
            List<String> columnLabels = new ArrayList<>();
            for (int index = 0; index < columnCount; index++) {
                columnLabels.add("第" + (index + 1) + "列");
            }
            preview.setPreviewType("TABLE");
            preview.setSheetNames(sheetNames);
            preview.setSheetName(sheet.getSheetName());
            preview.setColumnLabels(columnLabels);
            preview.setRowNumbers(rowNumbers);
            preview.setRows(rows);
            preview.setTruncated(lastRow >= maxRows || maxColumnCount(sheet) > maxColumns);
            preview.setMessage(preview.getTruncated()
                ? "仅展示前 200 行、前 40 列，完整内容请在提交后下载附件查看"
                : "已按生成后的附件内容展示，公式按可计算结果展示，复杂公式请在提交后下载 Excel 核验");
            return preview;
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceException("生成附件预览失败：" + ex.getMessage());
        }
    }

    private int maxColumnCount(Sheet sheet) {
        int max = 0;
        for (Row row : sheet) {
            if (row != null) {
                max = Math.max(max, Math.max(row.getLastCellNum(), 0));
            }
        }
        return max;
    }

    private Row rowOf(Sheet sheet, int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        return row == null ? sheet.createRow(rowIndex) : row;
    }

    private Cell cellOf(Row row, int columnIndex) {
        return row.getCell(columnIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
    }

    private void copyRow(Row source, Row target, int rowDelta) {
        if (source == target) {
            return;
        }
        for (Cell cell : target) {
            cell.setBlank();
        }
        target.setHeight(source.getHeight());
        target.setZeroHeight(source.getZeroHeight());
        for (Cell sourceCell : source) {
            Cell targetCell = target.getCell(sourceCell.getColumnIndex());
            if (targetCell == null) {
                targetCell = target.createCell(sourceCell.getColumnIndex());
            }
            targetCell.setCellStyle(sourceCell.getCellStyle());
            if (sourceCell.getHyperlink() != null) {
                targetCell.setHyperlink(sourceCell.getHyperlink());
            }
            switch (sourceCell.getCellType()) {
                case FORMULA -> targetCell.setCellFormula(shiftFormulaRows(sourceCell.getCellFormula(), rowDelta));
                case NUMERIC -> targetCell.setCellValue(sourceCell.getNumericCellValue());
                case BOOLEAN -> targetCell.setCellValue(sourceCell.getBooleanCellValue());
                case ERROR -> targetCell.setCellErrorValue(sourceCell.getErrorCellValue());
                case STRING -> targetCell.setCellValue(sourceCell.getRichStringCellValue());
                case BLANK -> targetCell.setBlank();
                default -> targetCell.setBlank();
            }
        }
    }

    private String shiftFormulaRows(String formula, int rowDelta) {
        if (StringUtils.isBlank(formula) || rowDelta == 0) {
            return formula;
        }
        Matcher matcher = FORMULA_CELL.matcher(formula);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String rowAbsolute = matcher.group(2);
            if (StringUtils.isNotBlank(rowAbsolute)) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group()));
                continue;
            }
            int row = Integer.parseInt(matcher.group(3)) + rowDelta;
            String replacement = matcher.group(1) + matcher.group(2) + row;
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private void adjustTotalFormulas(Row totalRow, int oldDataStart, int oldDataEnd,
                                     int newDataStart, int newDataEnd) {
        int oldStart = oldDataStart + 1;
        int oldEnd = oldDataEnd + 1;
        int newStart = newDataStart + 1;
        int newEnd = newDataEnd + 1;
        for (Cell cell : totalRow) {
            if (cell.getCellType() != CellType.FORMULA) {
                continue;
            }
            Matcher matcher = FORMULA_RANGE.matcher(cell.getCellFormula());
            StringBuffer result = new StringBuffer();
            while (matcher.find()) {
                int rangeStart = Integer.parseInt(matcher.group(3));
                int rangeEnd = Integer.parseInt(matcher.group(6));
                if (rangeStart == oldStart && rangeEnd == oldEnd) {
                    String replacement = matcher.group(1) + matcher.group(2) + newStart + ":"
                        + matcher.group(4) + matcher.group(5) + newEnd;
                    matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
                } else {
                    matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group()));
                }
            }
            matcher.appendTail(result);
            cell.setCellFormula(result.toString());
        }
    }

    private OaApplicationBo buildApplication(OaImportBusinessConfig config, OaImportBatch batch,
                                             List<OaImportRecord> records, Map<String, Object> parameters,
                                             Long workflowConfigId, String approvalMode, Long approvalPlanId,
                                             List<OaApprovalParticipantBo> participants, Long attachmentOssId) {
        Map<String, Object> context = buildContext(config, batch, records, parameters);
        String groupName = StringUtils.isBlank(records.get(0).getGroupName()) ? records.get(0).getGroupKey() : records.get(0).getGroupName();
        String title = render(firstNonBlank(config.getRequestNameTemplate(), "{businessName}-{batchNo}-{groupName}"), context, groupName);
        String content = render(firstNonBlank(config.getContentTemplate(), "导入批次：{batchNo}，分组：{groupName}，明细数：{rowCount}"), context, groupName);
        Map<String, Object> formData = new LinkedHashMap<>(context);
        formData.put("rowCount", records.size());
        formData.put("groupName", groupName);
        formData.put("groupKey", records.get(0).getGroupKey());
        Map<String, Object> mappedData = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : formMapping(config).entrySet()) {
            Object value = context.get(entry.getKey());
            if (value != null) {
                mappedData.put(entry.getValue().toString(), value);
                // 工作流配置的 formFields 以表单字段编码取值；同步放入顶层，
                // 让导入模板可以把业务字段映射到泛微字段而不要求业务方维护 JSON。
                formData.put(entry.getValue().toString(), value);
            }
        }
        formData.put("oaFields", mappedData);

        OaApplicationBo application = new OaApplicationBo();
        application.setBusinessType(config.getBusinessType());
        application.setSourceModule("OA_IMPORT");
        application.setBusinessId(batch.getId() + ":" + records.get(0).getGroupKey());
        application.setBusinessNo(batch.getBatchNo() + "-" + safeFileName(records.get(0).getGroupKey()));
        application.setDeptId(records.get(0).getDeptId());
        application.setDeptIds(records.stream().map(OaImportRecord::getDeptId).filter(Objects::nonNull).distinct().toList());
        application.setCompanyId(records.get(0).getCompanyId());
        application.setTitle(limit(title, 200));
        application.setContent(limit(content, 5000));
        application.setFormDataJson(limit(JsonUtils.toJsonString(formData), 20000));
        if (attachmentOssId != null) {
            OaAttachmentBo attachment = new OaAttachmentBo();
            attachment.setOssId(attachmentOssId);
            attachment.setAttachmentType("FILE");
            attachment.setSortNo(0);
            application.setAttachments(List.of(attachment));
        }
        application.setWorkflowConfigId(workflowConfigId);
        // 泛微审批方式由所选表单选项的 approvalCode 决定，不能再用旧 processType 猜测。
        application.setProcessType("CUSTOM");
        application.setApprovalMode(approvalMode);
        application.setApprovalPlanId(approvalPlanId);
        if ("MANUAL".equalsIgnoreCase(application.getApprovalMode())) {
            application.setParticipants(participants == null ? List.of() : participants);
        }
        return application;
    }

    private Map<String, Object> buildContext(OaImportBusinessConfig config, OaImportBatch batch,
                                             List<OaImportRecord> records, Map<String, Object> parameters) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("businessType", config.getBusinessType());
        context.put("businessName", config.getBusinessName());
        context.put("batchNo", batch.getBatchNo());
        context.put("sourceFileName", batch.getSourceFileName());
        context.putAll(parameters);
        Map<String, Object> first = parseData(records.get(0).getDataJson());
        context.putAll(first);
        context.put("rowCount", records.size());
        context.put("groupName", records.get(0).getGroupName());
        context.put("groupKey", records.get(0).getGroupKey());
        for (Map.Entry<String, AggregateRule> entry : aggregationRules(config).entrySet()) {
            context.put(entry.getKey(), aggregate(records, entry.getValue()));
        }
        Long currentUserId = LoginHelper.getUserId();
        SysUser currentUser = currentUserId == null ? null : sysUserMapper.selectById(currentUserId);
        Long currentDeptId = LoginHelper.getDeptId();
        SysDept settlementDept = records.get(0).getDeptId() == null ? null : sysDeptMapper.selectById(records.get(0).getDeptId());
        context.put("currentUserName", userDisplayName(currentUser));
        context.put("currentDeptName", currentDeptId == null ? "" : deptName(currentDeptId));
        context.put("settlementDeptName", settlementDept == null || settlementDept.getDeptName() == null
            ? "" : settlementDept.getDeptName());
        context.put("settlementDeptOwner", settlementDept == null ? "" : userDisplayName(
            settlementDept.getLeader() == null ? null : sysUserMapper.selectById(settlementDept.getLeader())));
        return context;
    }

    private Object aggregate(List<OaImportRecord> records, AggregateRule rule) {
        String operation = StringUtils.isBlank(rule.getOperation()) ? "SUM" : rule.getOperation().toUpperCase(Locale.ROOT);
        if ("COUNT".equals(operation)) {
            return records.size();
        }
        if ("FIRST".equals(operation)) {
            return parseData(records.get(0).getDataJson()).get(rule.getSource());
        }
        BigDecimal total = BigDecimal.ZERO;
        for (OaImportRecord record : records) {
            BigDecimal value = decimal(parseData(record.getDataJson()).get(rule.getSource()));
            if (value != null) {
                total = total.add(value);
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private void validateParameters(OaImportBusinessConfig config, Map<String, Object> parameters) {
        for (ParameterDefinition definition : parameterDefinitions(config)) {
            if (Boolean.TRUE.equals(definition.getRequired()) && StringUtils.isBlank(text(parameters.get(definition.getCode())))
                && StringUtils.isBlank(definition.getDefaultValue())) {
                throw new ServiceException("缺少提交参数：" + firstNonBlank(definition.getLabel(), definition.getCode()));
            }
            if (!parameters.containsKey(definition.getCode()) && StringUtils.isNotBlank(definition.getDefaultValue())) {
                parameters.put(definition.getCode(), definition.getDefaultValue());
            }
        }
    }

    private String buildGroupKey(OaImportBusinessConfig config, Map<String, Object> data, Long deptId) {
        List<String> fields = groupBy(config);
        if (fields.isEmpty()) {
            return "__ALL__";
        }
        List<String> values = new ArrayList<>();
        for (String field : fields) {
            if (field.equals(config.getDeptField()) && deptId != null) {
                values.add("DEPT:" + deptId);
            } else {
                values.add(normalizeName(text(data.get(field))));
            }
        }
        return String.join("|", values);
    }

    private String buildGroupName(OaImportBusinessConfig config, Map<String, Object> data, SysDept dept) {
        List<String> fields = groupBy(config);
        if (fields.isEmpty()) {
            return "全部数据";
        }
        List<String> values = new ArrayList<>();
        for (String field : fields) {
            if (field.equals(config.getDeptField()) && dept != null) {
                values.add(dept.getDeptName());
            } else {
                values.add(text(data.get(field)));
            }
        }
        return String.join(" / ", values);
    }

    private SysDept resolveDepartment(String sourceName, Map<String, List<SysDept>> departmentsByName, Map<String, Long> aliases) {
        String normalized = normalizeName(sourceName);
        Long aliasDeptId = aliases.get(normalized);
        if (aliasDeptId != null) {
            SysDept aliasDept = sysDeptMapper.selectById(aliasDeptId);
            if (isOaDepartment(aliasDept)) {
                return aliasDept;
            }
        }
        List<SysDept> exact = departmentsByName.getOrDefault(normalized, List.of());
        if (exact.size() == 1) {
            return exact.get(0);
        }
        if (exact.size() > 1) {
            return null;
        }
        List<SysDept> fuzzy = departmentsByName.entrySet().stream()
            .filter(entry -> !entry.getKey().isBlank()
                && (normalized.contains(entry.getKey()) || entry.getKey().contains(normalized)))
            .flatMap(entry -> entry.getValue().stream())
            .toList();
        return fuzzy.size() == 1 ? fuzzy.get(0) : null;
    }

    private void saveAlias(String businessType, String sourceName, String normalizedName, SysDept target) {
        OaImportDeptAlias entity = aliasMapper.selectByNormalizedName(businessType, normalizedName);
        if (entity == null) {
            entity = new OaImportDeptAlias();
            entity.setBusinessType(businessType);
            entity.setSourceDeptName(sourceName);
            entity.setNormalizedName(normalizedName);
        }
        entity.setBusinessType(businessType);
        entity.setDeptId(target.getDeptId());
        entity.setTargetDeptName(target.getDeptName());
        entity.setStatus(SystemConstants.NORMAL);
        if (entity.getId() == null) {
            aliasMapper.insert(entity);
        } else {
            aliasMapper.updateById(entity);
        }
    }

    private List<SysDept> selectActiveOaDepartments() {
        return sysDeptMapper.selectList(Wrappers.<SysDept>lambdaQuery()
            .eq(SysDept::getStatus, SystemConstants.NORMAL)
            .eq(SysDept::getOaSourceType, OA_DEPARTMENT)
            .orderByAsc(SysDept::getParentId)
            .orderByAsc(SysDept::getOrderNum)
            .orderByAsc(SysDept::getDeptId));
    }

    private Long inferCompanyId(SysDept dept) {
        SysDept current = dept;
        for (int i = 0; i < 30 && current != null; i++) {
            if ("SUBCOMPANY".equalsIgnoreCase(StringUtils.trim(current.getOaSourceType()))) {
                try {
                    return Long.valueOf(StringUtils.trim(current.getOaSourceId()));
                } catch (Exception ignored) {
                    return null;
                }
            }
            if (current.getParentId() == null || Objects.equals(current.getParentId(), current.getDeptId())) {
                break;
            }
            current = sysDeptMapper.selectById(current.getParentId());
        }
        return null;
    }

    private Long resolveCompany(String sourceName, Map<String, List<SysDept>> companiesByName) {
        if (StringUtils.isBlank(sourceName)) {
            return null;
        }
        String normalized = normalizeName(sourceName);
        List<SysDept> exact = companiesByName.getOrDefault(normalized, List.of());
        if (exact.size() != 1) {
            exact = companiesByName.values().stream().flatMap(List::stream)
                .filter(item -> normalized.equals(normalizeName(item.getOaSourceId())))
                .toList();
        }
        if (exact.size() == 1) {
            return oaSourceId(exact.get(0));
        }
        List<SysDept> fuzzy = companiesByName.entrySet().stream()
            .filter(entry -> !entry.getKey().isBlank()
                && (normalized.contains(entry.getKey()) || entry.getKey().contains(normalized)))
            .flatMap(entry -> entry.getValue().stream()).toList();
        return fuzzy.size() == 1 ? oaSourceId(fuzzy.get(0)) : null;
    }

    private Long oaSourceId(SysDept company) {
        try {
            return Long.valueOf(StringUtils.trim(company.getOaSourceId()));
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<SysDept> selectActiveOaCompanies() {
        return sysDeptMapper.selectList(Wrappers.<SysDept>lambdaQuery()
            .eq(SysDept::getStatus, SystemConstants.NORMAL)
            .eq(SysDept::getOaSourceType, "SUBCOMPANY")
            .orderByAsc(SysDept::getDeptId));
    }

    private void refreshBatch(OaImportBatch batch, String message) {
        List<OaImportRecord> records = recordMapper.selectByBatchId(batch.getId());
        List<OaImportRecord> activeRecords = records.stream().filter(item -> !SKIPPED.equals(item.getStatus())).toList();
        int skipped = records.size() - activeRecords.size();
        int matched = (int) activeRecords.stream().filter(item -> MATCHED.equals(item.getStatus()) || item.getDeptId() != null).count();
        Map<String, List<OaImportRecord>> groupedRecords = groupRecords(activeRecords);
        int applications = (int) records.stream()
            .filter(this::hasSubmitAttempt)
            .map(OaImportRecord::getApplicationId)
            .filter(Objects::nonNull)
            .distinct()
            .count();
        int failed = (int) groupedRecords.values().stream().filter(group -> FAILED_RECORD.equals(resolveGroupStatus(group))).count();
        int submitted = (int) groupedRecords.values().stream().filter(group -> SUBMITTED_RECORD.equals(resolveGroupStatus(group))).count();
        int groups = groupedRecords.size();
        batch.setTotalCount(records.size());
        batch.setMatchedCount(matched);
        batch.setGroupCount(groups);
        batch.setApplicationCount(applications);
        batch.setFailedCount(failed);
        batch.setSkippedCount(skipped);
        if (activeRecords.isEmpty()) {
            batch.setStatus(SKIPPED);
        } else if (matched < activeRecords.size()) {
            batch.setStatus(NEED_MAPPING);
        } else if (failed > 0 && submitted < groups) {
            batch.setStatus(submitted == 0 ? FAILED : PARTIAL_FAILED);
        } else if (submitted == groups) {
            batch.setStatus(SUBMITTED);
        } else {
            // 组织匹配完成后，批次可能原本处于 NEED_MAPPING、FAILED 等状态；
            // 只要还有可提交明细且当前没有失败/提交中的明细，就必须进入待提交阶段。
            batch.setStatus(READY);
        }
        if (message != null) {
            batch.setMessage(message);
        }
        batchMapper.updateById(batch);
    }

    private boolean hasSubmitAttempt(OaImportRecord record) {
        return record.getApplicationId() != null
            && (SUBMITTING.equals(record.getStatus())
            || SUBMITTED_RECORD.equals(record.getStatus())
            || FAILED_RECORD.equals(record.getStatus()));
    }

    private String resolveGroupStatus(List<OaImportRecord> records) {
        List<OaImportRecord> activeRecords = records.stream()
            .filter(item -> !SKIPPED.equals(item.getStatus())).toList();
        if (activeRecords.isEmpty()) {
            return SKIPPED;
        }
        if (activeRecords.stream().anyMatch(item -> FAILED_RECORD.equals(item.getStatus()))) {
            return FAILED_RECORD;
        }
        if (activeRecords.stream().anyMatch(item -> SUBMITTING.equals(item.getStatus()))) {
            return SUBMITTING;
        }
        if (activeRecords.stream().anyMatch(item -> SUBMITTED_RECORD.equals(item.getStatus()))) {
            return SUBMITTED_RECORD;
        }
        if (activeRecords.stream().anyMatch(item -> UNMATCHED.equals(item.getStatus()))) {
            return UNMATCHED;
        }
        return MATCHED;
    }

    private OaImportBatch getBatch(Long id) {
        if (id == null) {
            throw new ServiceException("导入批次不能为空");
        }
        OaImportBatch batch = batchMapper.selectById(id);
        if (batch == null) {
            throw new ServiceException("导入批次不存在");
        }
        return batch;
    }

    private OaImportBusinessConfig requireEnabledConfig(Long id) {
        OaImportBusinessConfig config = getConfig(id);
        if (!ENABLED.equalsIgnoreCase(config.getStatus())) {
            throw new ServiceException("通用导入业务模板不存在或已停用");
        }
        return config;
    }

    private OaImportBusinessConfig getConfig(Long id) {
        if (id == null) {
            throw new ServiceException("业务模板不能为空");
        }
        OaImportBusinessConfig config = configMapper.selectById(id);
        if (config == null) {
            throw new ServiceException("通用导入业务模板不存在");
        }
        return config;
    }

    private OaImportBatchVo toVo(OaImportBatch batch) {
        return toVo(batch, false);
    }

    private OaImportBatchVo toVo(OaImportBatch batch, boolean withRecords) {
        OaImportBatchVo vo = new OaImportBatchVo();
        vo.setId(batch.getId());
        vo.setConfigId(batch.getConfigId());
        vo.setBusinessType(batch.getBusinessType());
        OaImportBusinessConfig config = configMapper.selectById(batch.getConfigId());
        vo.setBusinessName(config == null ? null : config.getBusinessName());
        vo.setBatchNo(batch.getBatchNo());
        vo.setSourceFileName(batch.getSourceFileName());
        vo.setStatus(batch.getStatus());
        vo.setTotalCount(batch.getTotalCount());
        vo.setMatchedCount(batch.getMatchedCount());
        vo.setGroupCount(batch.getGroupCount());
        vo.setApplicationCount(batch.getApplicationCount());
        vo.setFailedCount(batch.getFailedCount());
        vo.setSkippedCount(batch.getSkippedCount());
        vo.setMessage(batch.getMessage());
        List<OaImportRecord> records = recordMapper.selectByBatchId(batch.getId());
        vo.setUnmatchedDeptNames(unmatchedDeptNames(config, records));
        vo.setSkippedDeptNames(skippedDeptNames(config, records));
        vo.setMappingItems(mappingItems(config, records));
        if (withRecords) {
            vo.setGroups(toGroupVos(records));
            vo.setRecords(records.stream().map(this::toRecordVo).toList());
        }
        return vo;
    }

    private List<OaImportGroupVo> toGroupVos(List<OaImportRecord> records) {
        return groupRecordsForDetail(records).values().stream().map(this::toGroupVo).toList();
    }

    private Map<String, List<OaImportRecord>> groupRecordsForDetail(List<OaImportRecord> records) {
        return records.stream().collect(Collectors.groupingBy(this::groupKey,
            LinkedHashMap::new, Collectors.toList()));
    }

    private OaImportGroupVo toGroupVo(List<OaImportRecord> records) {
        OaImportRecord first = records.get(0);
        Set<Long> applicationIds = records.stream().filter(this::hasSubmitAttempt)
            .map(OaImportRecord::getApplicationId)
            .filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
        OaImportGroupVo vo = new OaImportGroupVo();
        vo.setGroupKey(groupKey(first));
        vo.setGroupName(firstNonBlank(first.getGroupName(), groupKey(first).equals("__ALL__") ? "默认分组" : groupKey(first)));
        vo.setRecordCount(records.size());
        vo.setSkippedCount((int) records.stream().filter(item -> SKIPPED.equals(item.getStatus())).count());
        vo.setApplicationCount(applicationIds.size());
        vo.setApplicationId(applicationIds.size() == 1 ? applicationIds.iterator().next() : null);
        vo.setAttachmentOssId(records.stream().map(OaImportRecord::getAttachmentOssId)
            .filter(Objects::nonNull).findFirst().orElse(null));
        vo.setStatus(resolveGroupStatus(records));
        vo.setErrorMessage(records.stream().map(OaImportRecord::getErrorMessage)
            .filter(StringUtils::isNotBlank).findFirst().orElse(null));
        vo.setSkipReason(records.stream().map(OaImportRecord::getSkipReason)
            .filter(StringUtils::isNotBlank).findFirst().orElse(null));
        return vo;
    }

    private OaImportRecordVo toRecordVo(OaImportRecord record) {
        OaImportRecordVo vo = new OaImportRecordVo();
        vo.setId(record.getId());
        vo.setRowNo(record.getRowNo());
        vo.setData(parseData(record.getDataJson()));
        vo.setGroupKey(record.getGroupKey());
        vo.setGroupName(record.getGroupName());
        vo.setDeptId(record.getDeptId());
        if (record.getDeptId() != null) {
            SysDept dept = sysDeptMapper.selectById(record.getDeptId());
            vo.setDeptName(dept == null ? record.getGroupName() : dept.getDeptName());
        }
        vo.setCompanyId(record.getCompanyId());
        vo.setApplicationId(record.getApplicationId());
        vo.setAttachmentOssId(record.getAttachmentOssId());
        vo.setStatus(record.getStatus());
        vo.setErrorMessage(record.getErrorMessage());
        vo.setSkipReason(record.getSkipReason());
        return vo;
    }

    private List<String> unmatchedDeptNames(OaImportBusinessConfig config, List<OaImportRecord> records) {
        if (config == null || StringUtils.isBlank(config.getDeptField())) {
            return List.of();
        }
        return records.stream().filter(item -> UNMATCHED.equals(item.getStatus()) && item.getDeptId() == null)
            .map(item -> text(parseData(item.getDataJson()).get(config.getDeptField())))
            .filter(StringUtils::isNotBlank).map(StringUtils::trim).distinct().toList();
    }

    private List<String> skippedDeptNames(OaImportBusinessConfig config, List<OaImportRecord> records) {
        if (config == null || StringUtils.isBlank(config.getDeptField())) {
            return List.of();
        }
        return records.stream().filter(item -> SKIPPED.equals(item.getStatus()))
            .map(item -> text(parseData(item.getDataJson()).get(config.getDeptField())))
            .filter(StringUtils::isNotBlank).map(StringUtils::trim).distinct().toList();
    }

    /** 返回当前批次实际出现的全部来源组织，供组织处理页面回显和修改。 */
    private List<OaImportDeptMappingItemVo> mappingItems(OaImportBusinessConfig config,
                                                          List<OaImportRecord> records) {
        if (config == null || StringUtils.isBlank(config.getDeptField())) {
            return List.of();
        }
        Map<String, String> sourceNames = new LinkedHashMap<>();
        Map<String, List<OaImportRecord>> recordsBySource = new LinkedHashMap<>();
        for (OaImportRecord record : records) {
            String sourceName = StringUtils.trim(text(parseData(record.getDataJson()).get(config.getDeptField())));
            String normalizedName = normalizeName(sourceName);
            if (StringUtils.isBlank(normalizedName)) {
                continue;
            }
            sourceNames.putIfAbsent(normalizedName, sourceName);
            recordsBySource.computeIfAbsent(normalizedName, key -> new ArrayList<>()).add(record);
        }

        Set<Long> targetIds = recordsBySource.values().stream()
            .flatMap(List::stream)
            .map(OaImportRecord::getDeptId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, SysDept> departmentsById = targetIds.isEmpty() ? Map.of()
            : sysDeptMapper.selectBatchIds(targetIds).stream()
                .collect(Collectors.toMap(SysDept::getDeptId, item -> item, (left, right) -> left));

        List<OaImportDeptMappingItemVo> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : sourceNames.entrySet()) {
            List<OaImportRecord> sourceRecords = recordsBySource.get(entry.getKey());
            List<OaImportRecord> activeRecords = sourceRecords.stream()
                .filter(item -> !SKIPPED.equals(item.getStatus())).toList();
            Set<Long> sourceTargetIds = activeRecords.stream()
                .map(OaImportRecord::getDeptId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
            boolean pending = activeRecords.stream().anyMatch(item -> item.getDeptId() == null)
                || sourceTargetIds.size() > 1;

            OaImportDeptMappingItemVo item = new OaImportDeptMappingItemVo();
            item.setSourceDeptName(entry.getValue());
            item.setTargetDeptId(sourceTargetIds.size() == 1 ? sourceTargetIds.iterator().next() : null);
            SysDept target = item.getTargetDeptId() == null ? null : departmentsById.get(item.getTargetDeptId());
            item.setTargetDeptName(target == null ? null : target.getDeptName());
            item.setStatus(activeRecords.isEmpty() ? SKIPPED : pending ? UNMATCHED : MATCHED);
            item.setRecordCount(sourceRecords.size());
            item.setSkipReason(sourceRecords.stream()
                .map(OaImportRecord::getSkipReason)
                .filter(StringUtils::isNotBlank)
                .findFirst().orElse(null));
            result.add(item);
        }
        return result;
    }

    private List<FieldDefinition> fieldDefinitions(OaImportBusinessConfig config) {
        return StringUtils.isBlank(config.getFieldDefinitionsJson()) ? List.of()
            : JsonUtils.parseArray(config.getFieldDefinitionsJson(), FieldDefinition.class);
    }

    private List<ParameterDefinition> parameterDefinitions(OaImportBusinessConfig config) {
        return StringUtils.isBlank(config.getParameterDefinitionsJson()) ? List.of()
            : JsonUtils.parseArray(config.getParameterDefinitionsJson(), ParameterDefinition.class);
    }

    private List<String> groupBy(OaImportBusinessConfig config) {
        return StringUtils.isBlank(config.getGroupByJson()) ? List.of()
            : JsonUtils.parseArray(config.getGroupByJson(), String.class);
    }

    private Map<String, AggregateRule> aggregationRules(OaImportBusinessConfig config) {
        if (StringUtils.isBlank(config.getAggregationJson())) {
            return Map.of();
        }
        Map<String, AggregateRule> result = new LinkedHashMap<>();
        JsonUtils.parseMap(config.getAggregationJson()).forEach((key, value) -> {
            if (value instanceof Map<?, ?> map) {
                AggregateRule rule = new AggregateRule();
                rule.setSource(text(map.get("source")));
                rule.setOperation(text(map.get("operation")));
                result.put(key, rule);
            }
        });
        return result;
    }

    private Map<String, Object> formMapping(OaImportBusinessConfig config) {
        if (StringUtils.isBlank(config.getFormMappingJson())) {
            return Map.of();
        }
        return new LinkedHashMap<>(JsonUtils.parseMap(config.getFormMappingJson()));
    }

    private AttachmentConfig attachmentConfig(OaImportBusinessConfig config) {
        return StringUtils.isBlank(config.getAttachmentConfigJson()) ? null
            : JsonUtils.parseObject(config.getAttachmentConfigJson(), AttachmentConfig.class);
    }

    private Map<String, Object> parseData(String json) {
        if (StringUtils.isBlank(json)) {
            return new LinkedHashMap<>();
        }
        return new LinkedHashMap<>(JsonUtils.parseMap(json));
    }

    private String render(String template, Map<String, Object> context, String groupName) {
        String result = StringUtils.isBlank(template) ? "" : template;
        Map<String, Object> values = new HashMap<>(context);
        values.put("groupName", groupName);
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String value = entry.getValue() == null ? "" : String.valueOf(entry.getValue());
            result = result.replace("{" + entry.getKey() + "}", value)
                .replace("${" + entry.getKey() + "}", value);
        }
        return result;
    }

    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            return;
        }
        BigDecimal number = decimal(value);
        if (number != null && !(value instanceof String && !((String) value).matches("[-+]?\\d+(\\.\\d+)?"))) {
            cell.setCellValue(number.doubleValue());
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }

    private BigDecimal decimal(Object value) {
        if (value == null || StringUtils.isBlank(String.valueOf(value))) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(value).replace(",", "").trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private String normalizeHeader(String value) {
        return normalizeName(value).replace("_", "");
    }

    private String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
            .replaceAll("[\\s\\u00a0]+", "")
            .replace('－', '-')
            .replace('—', '-')
            .toUpperCase(Locale.ROOT);
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String deptName(Long deptId) {
        if (deptId == null) {
            return "全业务";
        }
        SysDept dept = sysDeptMapper.selectById(deptId);
        return dept == null ? String.valueOf(deptId) : dept.getDeptName();
    }

    private String firstNonBlank(String first, String fallback) {
        return StringUtils.isBlank(first) ? fallback : first;
    }

    private String normalizeApprovalMode(String value) {
        String mode = StringUtils.isBlank(value) ? "AUTO_RULE" : value.trim().toUpperCase(Locale.ROOT);
        if (!Set.of("AUTO_RULE", "PLAN", "MANUAL").contains(mode)) {
            throw new ServiceException("审批方式仅支持自动匹配、选择方案或临时指定");
        }
        return mode;
    }

    private String safeFileName(String value) {
        String result = StringUtils.isBlank(value) ? "导入业务附件" : value;
        return result.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private String limit(String value, int max) {
        if (value == null || value.length() <= max) {
            return value;
        }
        return value.substring(0, max);
    }

    private boolean isOaDepartment(SysDept dept) {
        return dept != null && SystemConstants.NORMAL.equals(dept.getStatus())
            && OA_DEPARTMENT.equalsIgnoreCase(StringUtils.trim(dept.getOaSourceType()));
    }

    @Data
    public static class FieldDefinition {
        private String code;
        private String header;
        private String type;
        private Boolean required;
        private Boolean uniqueKey;
    }

    @Data
    public static class ParameterDefinition {
        private String code;
        private String label;
        private String type;
        private Boolean required;
        private String defaultValue;
    }

    @Data
    public static class AggregateRule {
        private String source;
        private String operation;
    }

    @Data
    public static class OutputColumn {
        private Integer columnIndex;
        private String label;
        private String field;

        public OutputColumn() {
        }

        public OutputColumn(String label, String field) {
            this.label = label;
            this.field = field;
        }
    }

    @Data
    public static class FixedCellMapping {
        private String cell;
        private Integer row;
        private Integer column;
        private String originalContent;
        private String label;
        private String mode;
        private String template;
        private String field;
        private List<FixedCellSegment> segments;
    }

    @Data
    private static class FixedCellSegment {
        private String label;
        private String mode;
        private String template;
        private String field;
    }

    private static class ResolvedFixedSegment {
        private final FixedCellSegment segment;
        private final String label;
        private final int start;

        private ResolvedFixedSegment(FixedCellSegment segment, String label, int start) {
            this.segment = segment;
            this.label = label;
            this.start = start;
        }
    }

    @Data
    public static class AttachmentConfig {
        private String mode;
        private String sheetName;
        private String titleTemplate;
        private String fileNameTemplate;
        private String totalLabel;
        private List<OutputColumn> headers;
        private Long templateOssId;
        private String templateFileName;
        private Integer titleRow;
        private Integer headerRow;
        private Integer dataStartRow;
        private Integer totalRow;
        private List<OaImportAttachmentFixedCandidateVo> fixedCandidates;
        private List<FixedCellMapping> fixedMappings;
    }

    @Data
    private static class GeneratedAttachment {
        private String fileName;
        private String contentType;
        private byte[] body;
    }

    @Data
    private static class GroupApproval {
        private final OaImportApprovalPreviewVo preview;
        private final OaDepartmentApprovalVo plan;
        private final OaWorkflowConfig workflow;

        private GroupApproval(OaImportApprovalPreviewVo preview, OaDepartmentApprovalVo plan,
                              OaWorkflowConfig workflow) {
            this.preview = preview;
            this.plan = plan;
            this.workflow = workflow;
        }
    }

}
