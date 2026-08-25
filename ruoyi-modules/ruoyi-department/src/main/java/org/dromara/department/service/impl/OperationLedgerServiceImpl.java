package org.dromara.department.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.department.domain.DepartmentProject;
import org.dromara.department.domain.OperationRecord;
import org.dromara.department.domain.OperationSystem;
import org.dromara.department.domain.bo.OperationRecordBo;
import org.dromara.department.domain.bo.OperationRecordQueryBo;
import org.dromara.department.domain.bo.OperationSystemBo;
import org.dromara.department.domain.vo.OperationRecordImportVo;
import org.dromara.department.domain.vo.OperationRecordVo;
import org.dromara.department.domain.vo.OperationSummaryVo;
import org.dromara.department.domain.vo.OperationSystemImportVo;
import org.dromara.department.domain.vo.OperationSystemVo;
import org.dromara.department.mapper.OperationRecordMapper;
import org.dromara.department.mapper.OperationSystemMapper;
import org.dromara.department.mapper.DepartmentProjectMapper;
import org.dromara.department.service.IOperationLedgerService;
import org.dromara.department.service.DepartmentAccessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 运维台账业务实现。
 */
@RequiredArgsConstructor
@Service
public class OperationLedgerServiceImpl implements IOperationLedgerService {

    private static final String DEPT_VIEW_PERMISSION = "department:operationLedger:viewDept";
    private static final String SOURCE_MANUAL = "MANUAL";
    private static final String SOURCE_EXCEL = "EXCEL";
    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private final OperationRecordMapper operationRecordMapper;
    private final OperationSystemMapper operationSystemMapper;
    private final DepartmentProjectMapper departmentProjectMapper;
    private final DepartmentAccessService departmentAccessService;

    @Override
    public PageResult<OperationRecordVo> queryRecordPageList(OperationRecordQueryBo bo, PageQuery pageQuery) {
        OperationRecordQueryBo query = bo == null ? new OperationRecordQueryBo() : bo;
        Page<OperationRecordVo> page = pageQuery.build();
        Page<OperationRecordVo> result = operationRecordMapper.selectPageList(page, query, scopeDeptId(), canViewAll());
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public List<OperationRecordVo> queryRecordList(OperationRecordQueryBo bo) {
        Page<OperationRecordVo> page = new Page<>(1, Integer.MAX_VALUE);
        OperationRecordQueryBo query = bo == null ? new OperationRecordQueryBo() : bo;
        return operationRecordMapper.selectPageList(page, query, scopeDeptId(), canViewAll()).getRecords();
    }

    @Override
    public OperationRecordVo queryRecordById(Long id) {
        return toRecordVo(getAccessibleRecord(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertRecord(OperationRecordBo bo) {
        requireDept("维护运维记录");
        OperationRecord entity = new OperationRecord();
        copyRecord(bo, entity);
        entity.setDeptId(departmentAccessService.currentDeptId());
        applyProject(bo.getProjectId(), entity);
        entity.setSourceType(SOURCE_MANUAL);
        entity.setProcessStatus(normalizeStatus(bo.getProcessStatus()));
        calculateDurations(entity);
        return operationRecordMapper.insert(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateRecord(OperationRecordBo bo) {
        OperationRecord entity = getAccessibleRecord(bo.getId());
        copyRecord(bo, entity);
        applyProject(bo.getProjectId(), entity);
        entity.setProcessStatus(normalizeStatus(bo.getProcessStatus()));
        calculateDurations(entity);
        return operationRecordMapper.updateById(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteRecords(Collection<Long> ids) {
        for (Long id : ids) {
            getAccessibleRecord(id);
        }
        return operationRecordMapper.deleteByIds(ids) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importRecords(List<OperationRecordImportVo> rows) {
        if (rows == null || rows.isEmpty()) {
            return "未读取到工作记录数据";
        }
        requireDept("导入运维记录");
        int success = 0;
        int skipped = 0;
        for (OperationRecordImportVo row : rows) {
            if (row == null || (row.getRequestTime() == null && StringUtils.isBlank(row.getBusinessDescription())
                && StringUtils.isBlank(row.getCustomerUnit()))) {
                skipped++;
                continue;
            }
            OperationRecord entity = new OperationRecord();
            entity.setDeptId(departmentAccessService.currentDeptId());
            entity.setRequestPerson(row.getRequestPerson());
            entity.setCustomerUnit(row.getCustomerUnit());
            entity.setRequestRoleType(row.getRequestRoleType());
            entity.setRequestTime(row.getRequestTime());
            entity.setHandler(row.getHandler());
            entity.setProcessTime(row.getProcessTime());
            entity.setCompletionTime(row.getCompletionTime());
            entity.setResponseMinutes(parseDurationMinutes(row.getResponseDuration()));
            entity.setProcessingMinutes(parseDurationMinutes(row.getProcessingDuration()));
            entity.setLunchBreak(isYes(row.getLunchBreak()) ? "1" : "0");
            entity.setProcessStatus(normalizeStatus(row.getProcessStatus()));
            entity.setProcessMethod(row.getProcessMethod());
            entity.setSubmitter(row.getSubmitter());
            entity.setBusinessDescription(row.getBusinessDescription());
            entity.setSolution(row.getSolution());
            entity.setRemark(row.getRemark());
            entity.setSystemName(row.getSystemName());
            entity.setProjectId(findProjectId(entity.getDeptId(), row.getSystemName()));
            entity.setFaultType(row.getFaultType());
            entity.setSourceType(SOURCE_EXCEL);
            calculateDurations(entity);
            operationRecordMapper.insert(entity);
            success++;
        }
        return String.format("工作记录导入完成：成功 %d 条，跳过 %d 条", success, skipped);
    }

    @Override
    public PageResult<OperationSystemVo> querySystemPageList(LocalDate beginDate, LocalDate endDate, String systemName, PageQuery pageQuery) {
        Page<OperationSystemVo> page = pageQuery.build();
        Page<OperationSystemVo> result = operationSystemMapper.selectPageList(page, beginDate, endDate, systemName, scopeDeptId(), canViewAll());
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public List<OperationSystemVo> querySystemList(LocalDate beginDate, LocalDate endDate, String systemName) {
        Page<OperationSystemVo> page = new Page<>(1, Integer.MAX_VALUE);
        return operationSystemMapper.selectPageList(page, beginDate, endDate, systemName, scopeDeptId(), canViewAll()).getRecords();
    }

    @Override
    public OperationSystemVo querySystemById(Long id) {
        return toSystemVo(getAccessibleSystem(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertSystem(OperationSystemBo bo) {
        requireDept("维护系统在线率");
        OperationSystem entity = new OperationSystem();
        entity.setDeptId(departmentAccessService.currentDeptId());
        copySystem(bo, entity);
        entity.setSourceType(SOURCE_MANUAL);
        normalizeSystemMetrics(entity);
        return operationSystemMapper.insert(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateSystem(OperationSystemBo bo) {
        OperationSystem entity = getAccessibleSystem(bo.getId());
        copySystem(bo, entity);
        normalizeSystemMetrics(entity);
        return operationSystemMapper.updateById(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteSystems(Collection<Long> ids) {
        for (Long id : ids) {
            getAccessibleSystem(id);
        }
        return operationSystemMapper.deleteByIds(ids) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importSystems(List<OperationSystemImportVo> rows) {
        if (rows == null || rows.isEmpty()) {
            return "未读取到系统运维报告数据";
        }
        requireDept("导入系统在线率");
        int success = 0;
        int skipped = 0;
        LocalDate importDate = LocalDate.now().with(DayOfWeek.MONDAY);
        for (OperationSystemImportVo row : rows) {
            if (row == null || StringUtils.isBlank(row.getSystemName())) {
                skipped++;
                continue;
            }
            OperationSystem entity = new OperationSystem();
            entity.setDeptId(departmentAccessService.currentDeptId());
            entity.setStatDate(importDate);
            String projectName = StringUtils.isBlank(row.getProjectName()) ? row.getSystemName() : row.getProjectName();
            Long projectId = findProjectId(entity.getDeptId(), projectName);
            if (projectId == null) {
                skipped++;
                continue;
            }
            entity.setProjectId(projectId);
            entity.setSystemName(row.getSystemName());
            entity.setResponsiblePerson(row.getResponsiblePerson());
            entity.setOnlineDays(row.getOnlineDays());
            entity.setDowntimeMinutes(parseDurationMinutes(row.getDowntimeDuration()));
            entity.setOnlineRate(normalizeRate(row.getOnlineRate()));
            entity.setRemark(row.getRemark());
            entity.setSourceType(SOURCE_EXCEL);
            normalizeSystemMetrics(entity);
            operationSystemMapper.insert(entity);
            success++;
        }
        return String.format("系统在线率导入完成：成功 %d 条，跳过 %d 条（未匹配项目的记录会被跳过）；统计日期默认为本周周一，可继续编辑", success, skipped);
    }

    @Override
    public OperationSummaryVo buildSummary(LocalDate beginDate, LocalDate endDate) {
        if (beginDate == null || endDate == null || endDate.isBefore(beginDate)) {
            throw new ServiceException("运维汇总日期范围不正确");
        }
        List<OperationRecordVo> records = operationRecordMapper.selectForSummary(beginDate, endDate, scopeDeptId(), canViewAll());
        List<OperationSystemVo> systems = operationSystemMapper.selectForSummary(beginDate, endDate, scopeDeptId(), canViewAll());
        OperationSummaryVo summary = new OperationSummaryVo();
        summary.setTotalCount(records.size());
        int resolved = 0;
        int durationTotal = 0;
        int durationCount = 0;
        Map<String, OperationSummaryVo.DimensionCountVo> systemMap = new LinkedHashMap<>();
        Map<String, OperationSummaryVo.DimensionCountVo> faultMap = new LinkedHashMap<>();
        Map<String, OperationSummaryVo.DimensionCountVo> methodMap = new LinkedHashMap<>();
        for (OperationRecordVo record : records) {
            if (STATUS_COMPLETED.equals(record.getProcessStatus())) {
                resolved++;
            }
            if (record.getProcessingMinutes() != null && record.getProcessingMinutes() >= 0
                && (STATUS_COMPLETED.equals(record.getProcessStatus()) || record.getCompletionTime() != null)) {
                durationTotal += record.getProcessingMinutes();
                durationCount++;
            }
            String systemName = StringUtils.isBlank(record.getSystemName()) ? record.getCustomerUnit() : record.getSystemName();
            addDimension(systemMap, StringUtils.isBlank(systemName) ? "未分类" : systemName);
            String faultType = StringUtils.isBlank(record.getFaultType()) ? record.getProcessMethod() : record.getFaultType();
            addDimension(faultMap, StringUtils.isBlank(faultType) ? "未分类" : faultType);
            addDimension(methodMap, StringUtils.isBlank(record.getProcessMethod()) ? "未分类" : record.getProcessMethod());
        }
        summary.setResolvedCount(resolved);
        summary.setResolutionRate(records.isEmpty() ? BigDecimal.ZERO : percentage(resolved, records.size()));
        summary.setAverageProcessingMinutes(durationCount == 0 ? 0 : Math.round((float) durationTotal / durationCount));
        summary.setBySystem(toDimensionList(systemMap, records.size()));
        summary.setByFaultType(toDimensionList(faultMap, records.size()));
        summary.setByProcessMethod(toDimensionList(methodMap, records.size()));
        summary.setOnlineRate(calculateOnlineRate(systems));
        return summary;
    }

    private void copyRecord(OperationRecordBo bo, OperationRecord entity) {
        entity.setRequestPerson(bo.getRequestPerson());
        entity.setCustomerUnit(bo.getCustomerUnit());
        entity.setRequestRoleType(bo.getRequestRoleType());
        entity.setRequestTime(bo.getRequestTime());
        entity.setHandler(bo.getHandler());
        entity.setProcessTime(bo.getProcessTime());
        entity.setCompletionTime(bo.getCompletionTime());
        entity.setResponseMinutes(bo.getResponseMinutes());
        entity.setProcessingMinutes(bo.getProcessingMinutes());
        entity.setLunchBreak(isYes(bo.getLunchBreak()) ? "1" : "0");
        entity.setProcessStatus(bo.getProcessStatus());
        entity.setProcessMethod(bo.getProcessMethod());
        entity.setSubmitter(bo.getSubmitter());
        entity.setProjectId(bo.getProjectId());
        entity.setSystemName(bo.getSystemName());
        entity.setFaultType(bo.getFaultType());
        entity.setBusinessDescription(bo.getBusinessDescription());
        entity.setSolution(bo.getSolution());
        entity.setRemark(bo.getRemark());
    }

    private void copySystem(OperationSystemBo bo, OperationSystem entity) {
        if (bo.getStatDate() == null) {
            throw new ServiceException("统计日期不能为空");
        }
        if (StringUtils.isBlank(bo.getSystemName())) {
            throw new ServiceException("系统名称不能为空");
        }
        applySystemProject(bo.getProjectId(), entity);
        entity.setStatDate(bo.getStatDate());
        entity.setSystemName(bo.getSystemName());
        entity.setResponsiblePerson(bo.getResponsiblePerson());
        entity.setOnlineDays(bo.getOnlineDays());
        entity.setDowntimeMinutes(bo.getDowntimeMinutes());
        entity.setOnlineRate(bo.getOnlineRate());
        entity.setRemark(bo.getRemark());
    }

    private void calculateDurations(OperationRecord entity) {
        if (entity.getResponseMinutes() == null && entity.getRequestTime() != null && entity.getProcessTime() != null) {
            entity.setResponseMinutes(nonNegativeMinutes(Duration.between(entity.getRequestTime(), entity.getProcessTime())));
        }
        if (entity.getProcessingMinutes() == null && entity.getProcessTime() != null && entity.getCompletionTime() != null) {
            long minutes = Duration.between(entity.getProcessTime(), entity.getCompletionTime()).toMinutes();
            if ("1".equals(entity.getLunchBreak())) {
                minutes -= 120;
            }
            entity.setProcessingMinutes((int) Math.max(minutes, 0));
        }
    }

    private void normalizeSystemMetrics(OperationSystem entity) {
        entity.setOnlineRate(normalizeRate(entity.getOnlineRate()));
        if (entity.getOnlineRate() == null && entity.getOnlineDays() != null && entity.getOnlineDays().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal onlineMinutes = entity.getOnlineDays().multiply(BigDecimal.valueOf(1440));
            BigDecimal downtime = BigDecimal.valueOf(entity.getDowntimeMinutes() == null ? 0 : entity.getDowntimeMinutes());
            BigDecimal total = onlineMinutes.add(downtime);
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                entity.setOnlineRate(onlineMinutes.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP));
            }
        }
    }

    private BigDecimal calculateOnlineRate(List<OperationSystemVo> systems) {
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        for (OperationSystemVo system : systems) {
            BigDecimal rate = normalizeRate(system.getOnlineRate());
            if (rate != null) {
                total = total.add(rate);
                count++;
            }
        }
        return count == 0 ? BigDecimal.ZERO : total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private void addDimension(Map<String, OperationSummaryVo.DimensionCountVo> map, String name) {
        OperationSummaryVo.DimensionCountVo dimension = map.computeIfAbsent(name, key -> {
            OperationSummaryVo.DimensionCountVo value = new OperationSummaryVo.DimensionCountVo();
            value.setName(key);
            value.setCount(0);
            return value;
        });
        dimension.setCount(dimension.getCount() + 1);
    }

    private List<OperationSummaryVo.DimensionCountVo> toDimensionList(Map<String, OperationSummaryVo.DimensionCountVo> map, int total) {
        List<OperationSummaryVo.DimensionCountVo> result = new ArrayList<>(map.values());
        result.sort(Comparator.comparing(OperationSummaryVo.DimensionCountVo::getCount).reversed());
        for (OperationSummaryVo.DimensionCountVo dimension : result) {
            dimension.setPercentage(total == 0 ? BigDecimal.ZERO : percentage(dimension.getCount(), total));
        }
        return result;
    }

    private BigDecimal percentage(int numerator, int denominator) {
        return BigDecimal.valueOf(numerator * 100.0 / denominator).setScale(2, RoundingMode.HALF_UP);
    }

    private Integer parseDurationMinutes(String value) {
        if (StringUtils.isBlank(value) || "None".equalsIgnoreCase(value.trim())) {
            return null;
        }
        String normalized = value.trim().toLowerCase().replace("分钟", "min").replace("小时", "h").replace(" ", "");
        try {
            if (normalized.endsWith("h")) {
                return (int) Math.round(Double.parseDouble(normalized.substring(0, normalized.length() - 1)) * 60);
            }
            if (normalized.endsWith("min")) {
                return (int) Math.round(Double.parseDouble(normalized.substring(0, normalized.length() - 3)));
            }
            return (int) Math.round(Double.parseDouble(normalized));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer nonNegativeMinutes(Duration duration) {
        return (int) Math.max(duration.toMinutes(), 0);
    }

    private void applyProject(Long projectId, OperationRecord entity) {
        if (projectId == null) {
            throw new ServiceException("请选择项目");
        }
        DepartmentProject project = departmentProjectMapper.selectById(projectId);
        if (project == null || !Objects.equals(project.getDelFlag(), "0")
            || (!canViewAll() && !Objects.equals(project.getDeptId(), entity.getDeptId()))) {
            throw new ServiceException("项目不存在或无权访问");
        }
        entity.setProjectId(project.getId());
        entity.setSystemName(project.getProjectName());
    }

    private void applySystemProject(Long projectId, OperationSystem entity) {
        if (projectId == null) {
            throw new ServiceException("请选择项目");
        }
        DepartmentProject project = departmentProjectMapper.selectById(projectId);
        if (project == null || !Objects.equals(project.getDelFlag(), "0")
            || (!canViewAll() && !Objects.equals(project.getDeptId(), entity.getDeptId()))) {
            throw new ServiceException("项目不存在或无权访问");
        }
        entity.setProjectId(project.getId());
    }

    private Long findProjectId(Long deptId, String projectName) {
        if (deptId == null || StringUtils.isBlank(projectName)) {
            return null;
        }
        return departmentProjectMapper.selectIdByName(deptId, projectName.trim());
    }

    private String normalizeStatus(String status) {
        if (StringUtils.isBlank(status)) {
            return STATUS_PROCESSING;
        }
        String normalized = status.trim().toUpperCase();
        if (normalized.contains("取消") || normalized.contains("CANCEL")) {
            return STATUS_CANCELLED;
        }
        if (normalized.contains("完成") || normalized.contains("解决") || normalized.contains("COMPLETE") || normalized.contains("RESOLVED")) {
            return STATUS_COMPLETED;
        }
        return STATUS_PROCESSING;
    }

    private boolean isYes(String value) {
        return StringUtils.isNotBlank(value) && ("是".equals(value.trim()) || "1".equals(value.trim()) || "Y".equalsIgnoreCase(value.trim()) || "YES".equalsIgnoreCase(value.trim()));
    }

    private BigDecimal normalizeRate(BigDecimal value) {
        if (value == null) {
            return null;
        }
        BigDecimal result = value;
        if (result.compareTo(BigDecimal.ONE) <= 0) {
            result = result.multiply(BigDecimal.valueOf(100));
        }
        return result.setScale(2, RoundingMode.HALF_UP);
    }

    private OperationRecord getAccessibleRecord(Long id) {
        OperationRecord entity = operationRecordMapper.selectById(id);
        if (entity == null) {
            throw new ServiceException("运维记录不存在");
        }
        if (departmentAccessService.canViewEntityDept(entity.getDeptId(), DEPT_VIEW_PERMISSION)) {
            return entity;
        }
        throw new ServiceException("您没有访问该运维记录的权限");
    }

    private OperationSystem getAccessibleSystem(Long id) {
        OperationSystem entity = operationSystemMapper.selectById(id);
        if (entity == null) {
            throw new ServiceException("系统在线率记录不存在");
        }
        if (departmentAccessService.canViewEntityDept(entity.getDeptId(), DEPT_VIEW_PERMISSION)) {
            return entity;
        }
        throw new ServiceException("您没有访问该系统在线率记录的权限");
    }

    private OperationRecordVo toRecordVo(OperationRecord entity) {
        OperationRecordVo vo = new OperationRecordVo();
        vo.setId(entity.getId());
        vo.setDeptId(entity.getDeptId());
        vo.setProjectId(entity.getProjectId());
        vo.setProjectName(entity.getSystemName());
        vo.setRequestPerson(entity.getRequestPerson());
        vo.setCustomerUnit(entity.getCustomerUnit());
        vo.setRequestRoleType(entity.getRequestRoleType());
        vo.setRequestTime(entity.getRequestTime());
        vo.setHandler(entity.getHandler());
        vo.setProcessTime(entity.getProcessTime());
        vo.setCompletionTime(entity.getCompletionTime());
        vo.setResponseMinutes(entity.getResponseMinutes());
        vo.setProcessingMinutes(entity.getProcessingMinutes());
        vo.setLunchBreak(entity.getLunchBreak());
        vo.setProcessStatus(entity.getProcessStatus());
        vo.setProcessMethod(entity.getProcessMethod());
        vo.setSubmitter(entity.getSubmitter());
        vo.setSystemName(entity.getSystemName());
        vo.setFaultType(entity.getFaultType());
        vo.setBusinessDescription(entity.getBusinessDescription());
        vo.setSolution(entity.getSolution());
        vo.setRemark(entity.getRemark());
        vo.setSourceType(entity.getSourceType());
        vo.setSourceFileName(entity.getSourceFileName());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private OperationSystemVo toSystemVo(OperationSystem entity) {
        OperationSystemVo vo = new OperationSystemVo();
        vo.setId(entity.getId());
        vo.setDeptId(entity.getDeptId());
        vo.setProjectId(entity.getProjectId());
        if (entity.getProjectId() != null) {
            DepartmentProject project = departmentProjectMapper.selectById(entity.getProjectId());
            if (project != null) {
                vo.setProjectName(project.getProjectName());
            }
        }
        vo.setStatDate(entity.getStatDate());
        vo.setSystemName(entity.getSystemName());
        vo.setResponsiblePerson(entity.getResponsiblePerson());
        vo.setOnlineDays(entity.getOnlineDays());
        vo.setDowntimeMinutes(entity.getDowntimeMinutes());
        vo.setOnlineRate(entity.getOnlineRate());
        vo.setRemark(entity.getRemark());
        vo.setSourceType(entity.getSourceType());
        vo.setSourceFileName(entity.getSourceFileName());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private void requireDept(String action) {
        if (departmentAccessService.currentDeptId() == null) {
            throw new ServiceException("当前登录用户缺少部门信息，无法" + action);
        }
    }

    private boolean canViewAll() {
        return false;
    }

    private boolean canViewDepartment() {
        return departmentAccessService.canViewCurrentDepartment(DEPT_VIEW_PERMISSION);
    }

    private Long scopeDeptId() {
        return canViewAll() ? null : departmentAccessService.scopeDeptId(DEPT_VIEW_PERMISSION);
    }
}
