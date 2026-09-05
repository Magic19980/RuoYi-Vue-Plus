package org.dromara.ecology.service.impl;

import cn.hutool.core.convert.Convert;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.helper.DataPermissionHelper;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.ecology.client.EcologyClient;
import org.dromara.ecology.client.EcologyClientResponse;
import org.dromara.ecology.config.EcologyProperties;
import org.dromara.ecology.domain.OaApplication;
import org.dromara.ecology.domain.OaApplicationDept;
import org.dromara.ecology.domain.OaApplicationAttachment;
import org.dromara.ecology.domain.OaApprovalParticipant;
import org.dromara.ecology.domain.OaCallbackCommand;
import org.dromara.ecology.domain.OaCallbackEvent;
import org.dromara.ecology.domain.OaProcessEventLog;
import org.dromara.ecology.domain.OaProcessInstance;
import org.dromara.ecology.domain.OaWorkflowConfig;
import org.dromara.ecology.domain.bo.OaApprovalParticipantBo;
import org.dromara.ecology.domain.bo.OaAttachmentBo;
import org.dromara.ecology.domain.bo.OaApplicationBo;
import org.dromara.ecology.domain.bo.OaApplicationQueryBo;
import org.dromara.ecology.domain.vo.OaApplicationVo;
import org.dromara.ecology.domain.vo.OaApprovalRulePreviewVo;
import org.dromara.ecology.domain.vo.OaApprovalParticipantVo;
import org.dromara.ecology.domain.vo.OaAttachmentPreviewVo;
import org.dromara.ecology.domain.vo.OaAttachmentVo;
import org.dromara.ecology.domain.vo.OaProcessEventLogVo;
import org.dromara.ecology.domain.vo.OaWorkflowConfigVo;
import org.dromara.ecology.mapper.OaApplicationMapper;
import org.dromara.ecology.mapper.OaApplicationDeptMapper;
import org.dromara.ecology.mapper.OaApplicationAttachmentMapper;
import org.dromara.ecology.mapper.OaApprovalParticipantMapper;
import org.dromara.ecology.mapper.OaCallbackEventMapper;
import org.dromara.ecology.mapper.OaProcessEventLogMapper;
import org.dromara.ecology.mapper.OaProcessInstanceMapper;
import org.dromara.ecology.service.IOaApplicationService;
import org.dromara.ecology.service.IOaBusinessTypeService;
import org.dromara.ecology.service.IOaDepartmentApprovalService;
import org.dromara.ecology.service.IOaWorkflowConfigService;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.SysDept;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.domain.vo.SysDeptVo;
import org.dromara.system.mapper.SysDeptMapper;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.domain.vo.SysOssVo;
import org.dromara.system.service.ISysOssService;
import org.dromara.system.service.ISysUserService;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** 泛微通用审批申请服务实现。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OaApplicationServiceImpl implements IOaApplicationService {

    private static final String DRAFT = "DRAFT";
    private static final String SUBMITTING = "SUBMITTING";
    private static final String SUBMITTED = "SUBMITTED";
    private static final String IN_PROGRESS = "IN_PROGRESS";
    private static final String APPROVED = "APPROVED";
    private static final String REJECTED = "REJECTED";
    private static final String FAILED = "FAILED";
    private static final String UNKNOWN = "UNKNOWN";
    private static final String CANCELLED = "CANCELLED";
    private static final String MIXED = "MIXED";
    private static final String AUTO_RULE = "AUTO_RULE";
    private static final String PLAN = "PLAN";
    private static final String MANUAL = "MANUAL";
    private static final String ENABLED = "ENABLED";
    private static final List<String> OA_ORGANIZATION_TYPES = List.of("SUBCOMPANY", "DEPARTMENT");
    private static final DateTimeFormatter REQUEST_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final OaApplicationMapper applicationMapper;
    private final OaApplicationDeptMapper applicationDeptMapper;
    private final OaApplicationAttachmentMapper attachmentMapper;
    private final OaApprovalParticipantMapper participantMapper;
    private final OaCallbackEventMapper callbackEventMapper;
    private final OaProcessInstanceMapper processMapper;
    private final OaProcessEventLogMapper eventMapper;
    private final IOaWorkflowConfigService workflowConfigService;
    private final IOaBusinessTypeService businessTypeService;
    private final IOaDepartmentApprovalService departmentApprovalService;
    private final SysDeptMapper sysDeptMapper;
    private final SysUserMapper sysUserMapper;
    private final ISysUserService userService;
    private final EcologyClient ecologyClient;
    private final EcologyProperties ecologyProperties;
    private final ISysOssService ossService;

    @Override
    public PageResult<OaApplicationVo> queryPage(OaApplicationQueryBo bo, PageQuery pageQuery) {
        OaApplicationQueryBo query = bo == null ? new OaApplicationQueryBo() : bo;
        boolean monitor = Boolean.TRUE.equals(query.getMonitor())
            && (LoginHelper.isSuperAdmin() || StpUtil.hasPermission("ecology:application:monitor"));
        Long currentUserId = LoginHelper.getUserId();
        Page<OaApplicationVo> page = applicationMapper.selectVoPage(pageQuery.build(), Wrappers.<OaApplication>lambdaQuery()
            .eq(StringUtils.isNotBlank(query.getBusinessType()), OaApplication::getBusinessType, query.getBusinessType())
            .like(StringUtils.isNotBlank(query.getTitle()), OaApplication::getTitle, query.getTitle())
            .eq(StringUtils.isNotBlank(query.getStatus()), OaApplication::getStatus, query.getStatus())
            .eq(!monitor, OaApplication::getApplicantUserId, currentUserId)
            .orderByDesc(BaseEntity::getCreateTime));
        page.getRecords().forEach(this::fillProcessInfo);
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override
    public OaApplicationVo queryById(Long id) {
        return toVo(getAccessible(id));
    }

    @Override
    public OaAttachmentPreviewVo previewAttachment(Long ossId) {
        OaApplicationAttachment attachment = findAccessibleAttachment(ossId);
        SysOssVo oss = ossService.getById(attachment.getOssId());
        if (oss == null) {
            throw new ServiceException("附件文件不存在");
        }
        OaAttachmentPreviewVo preview = new OaAttachmentPreviewVo();
        preview.setOssId(ossId);
        preview.setFileName(StringUtils.defaultIfBlank(attachment.getFileName(), oss.getOriginalName()));
        String fileName = StringUtils.defaultIfBlank(preview.getFileName(), "").toLowerCase(Locale.ROOT);
        if (!fileName.endsWith(".xls") && !fileName.endsWith(".xlsx")) {
            preview.setPreviewType("DOWNLOAD");
            preview.setMessage("当前附件类型不支持表格预览，请下载后查看");
            preview.setTruncated(false);
            return preview;
        }
        var response = ossService.download(attachment.getOssId());
        byte[] body = response == null ? null : response.getBody();
        if (body == null || body.length == 0) {
            throw new ServiceException("附件文件为空或无法读取");
        }
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(body))) {
            return readWorkbookPreview(preview, workbook);
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceException("附件预览失败：" + ex.getMessage());
        }
    }

    @Override
    public ResponseEntity<byte[]> downloadAttachment(Long ossId) {
        OaApplicationAttachment attachment = findAccessibleAttachment(ossId);
        try {
            return ossService.download(attachment.getOssId());
        } catch (Exception ex) {
            throw new ServiceException("附件下载失败：" + ex.getMessage());
        }
    }

    private OaApplicationAttachment findAccessibleAttachment(Long ossId) {
        if (ossId == null) {
            throw new ServiceException("附件不存在");
        }
        return attachmentMapper.selectList(
                Wrappers.<OaApplicationAttachment>lambdaQuery()
                    .eq(OaApplicationAttachment::getOssId, ossId)
                    .orderByDesc(OaApplicationAttachment::getId))
            .stream()
            .filter(item -> isAccessibleApplication(item.getApplicationId()))
            .findFirst()
            .orElseThrow(() -> new ServiceException("无权访问该附件或附件不存在"));
    }

    private boolean isAccessibleApplication(Long applicationId) {
        if (applicationId == null) {
            return false;
        }
        OaApplication application = applicationMapper.selectById(applicationId);
        try {
            assertAccessible(application);
            return true;
        } catch (ServiceException ex) {
            return false;
        }
    }

    private OaAttachmentPreviewVo readWorkbookPreview(OaAttachmentPreviewVo preview, Workbook workbook) {
        final int maxRows = 200;
        final int maxColumns = 40;
        List<String> sheetNames = new ArrayList<>();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            sheetNames.add(workbook.getSheetAt(i).getSheetName());
        }
        if (sheetNames.isEmpty()) {
            throw new ServiceException("附件没有可预览的工作表");
        }
        Sheet sheet = workbook.getSheetAt(0);
        DataFormatter formatter = new DataFormatter(Locale.ROOT);
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
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
        preview.setMessage(preview.getTruncated() ? "仅展示前 200 行、前 40 列，完整内容请下载附件查看" : "已按附件实际内容展示，公式按可计算结果展示，复杂公式请下载 Excel 核验");
        return preview;
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

    @Override
    public List<SysDeptVo> queryOaDepartments(String keyword, List<Long> deptIds, Long parentId) {
        String normalizedKeyword = StringUtils.trim(keyword);
        List<Long> normalizedDeptIds = deptIds == null ? List.of() : deptIds.stream()
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        boolean queryByIds = parentId == null && !normalizedDeptIds.isEmpty() && StringUtils.isBlank(normalizedKeyword);
        boolean queryChildren = parentId != null;
        var query = Wrappers.<SysDept>lambdaQuery()
            .select(SysDept::getDeptId, SysDept::getParentId, SysDept::getDeptName,
                SysDept::getStatus, SysDept::getOaSourceType, SysDept::getOaSourceId,
                SysDept::getOaSubcompanyId, SysDept::getAncestors, SysDept::getOrderNum)
            .in(SysDept::getOaSourceType, OA_ORGANIZATION_TYPES)
            .eq(SysDept::getStatus, SystemConstants.NORMAL)
            .eq(SysDept::getDelFlag, SystemConstants.NORMAL);
        if (queryChildren) {
            query.eq(SysDept::getParentId, parentId);
        } else if (queryByIds) {
            query.in(SysDept::getDeptId, normalizedDeptIds);
        } else if (StringUtils.isNotBlank(normalizedKeyword)) {
            // 搜索结果同时覆盖分部和部门，所有泛微组织节点都允许作为业务范围选择。
            query.like(SysDept::getDeptName, normalizedKeyword);
        } else {
            query.eq(SysDept::getParentId, 0L);
        }
        query.orderByAsc(SysDept::getOaSubcompanyId)
            .orderByAsc(SysDept::getAncestors)
            .orderByAsc(SysDept::getOrderNum)
            .orderByAsc(SysDept::getDeptId);
        if (!queryByIds && !queryChildren) {
            query.last("LIMIT 50");
        }
        List<SysDept> departments = DataPermissionHelper.ignore(() -> sysDeptMapper.selectList(query));
        if (StringUtils.isNotBlank(normalizedKeyword) && !departments.isEmpty()) {
            // 搜索结果也要保持组织树结构：补回命中节点的所有上级组织，前端才能正确展示层级路径。
            Set<Long> pathIds = new LinkedHashSet<>();
            departments.forEach(department -> {
                if (department.getDeptId() != null) {
                    pathIds.add(department.getDeptId());
                }
                if (StringUtils.isNotBlank(department.getAncestors())) {
                    pathIds.addAll(StringUtils.splitTo(department.getAncestors(), Convert::toLong));
                }
            });
            if (!pathIds.isEmpty()) {
                var pathQuery = Wrappers.<SysDept>lambdaQuery()
                    .select(SysDept::getDeptId, SysDept::getParentId, SysDept::getDeptName,
                        SysDept::getStatus, SysDept::getOaSourceType, SysDept::getOaSourceId,
                        SysDept::getOaSubcompanyId, SysDept::getAncestors, SysDept::getOrderNum)
                    .in(SysDept::getOaSourceType, OA_ORGANIZATION_TYPES)
                    .eq(SysDept::getStatus, SystemConstants.NORMAL)
                    .eq(SysDept::getDelFlag, SystemConstants.NORMAL)
                    .in(SysDept::getDeptId, pathIds)
                    .orderByAsc(SysDept::getOaSubcompanyId)
                    .orderByAsc(SysDept::getAncestors)
                    .orderByAsc(SysDept::getOrderNum)
                    .orderByAsc(SysDept::getDeptId);
                departments = DataPermissionHelper.ignore(() -> sysDeptMapper.selectList(pathQuery));
            }
        }
        Set<Long> parentDeptIds = findOaParentDeptIds(departments);
        return departments.stream().map(department -> toOaDepartmentVo(department,
            parentDeptIds.contains(department.getDeptId()))).toList();
    }

    /** 查询返回节点中哪些节点存在直属泛微组织节点，供前端树控件懒加载使用。 */
    private Set<Long> findOaParentDeptIds(List<SysDept> departments) {
        if (departments == null || departments.isEmpty()) {
            return Set.of();
        }
        List<Long> deptIds = departments.stream()
            .map(SysDept::getDeptId)
            .filter(Objects::nonNull)
            .toList();
        if (deptIds.isEmpty()) {
            return Set.of();
        }
        List<SysDept> childRows = DataPermissionHelper.ignore(() -> sysDeptMapper.lambda()
            .select(SysDept::getParentId)
            .in(SysDept::getParentId, deptIds)
            .in(SysDept::getOaSourceType, OA_ORGANIZATION_TYPES)
            .eq(SysDept::getStatus, SystemConstants.NORMAL)
            .eq(SysDept::getDelFlag, SystemConstants.NORMAL)
            .groupBy(SysDept::getParentId)
            .list());
        Set<Long> parentDeptIds = new HashSet<>();
        childRows.forEach(row -> {
            if (row.getParentId() != null) {
                parentDeptIds.add(row.getParentId());
            }
        });
        return parentDeptIds;
    }

    private SysDeptVo toOaDepartmentVo(SysDept source, boolean hasChildren) {
        SysDeptVo target = new SysDeptVo();
        target.setDeptId(source.getDeptId());
        target.setParentId(source.getParentId());
        target.setAncestors(source.getAncestors());
        target.setDeptName(source.getDeptName());
        target.setOaSourceType(source.getOaSourceType());
        target.setOaSourceId(source.getOaSourceId());
        target.setOrderNum(source.getOrderNum());
        target.setStatus(source.getStatus());
        target.setHasChildren(hasChildren);
        return target;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OaApplicationVo save(OaApplicationBo bo) {
        validateFormData(bo.getFormDataJson());
        validateAttachments(bo.getAttachments());
        validateParticipants(bo.getParticipants());
        String approvalMode = normalizeApprovalMode(bo.getApprovalMode());
        if (PLAN.equals(approvalMode) && bo.getApprovalPlanId() == null) {
            throw new ServiceException("选择审批方案时必须指定审批方案");
        }
        Long userId = LoginHelper.getUserId();
        SysUserVo user = userService.selectUserById(userId);
        if (user == null) {
            throw new ServiceException("当前登录用户不存在");
        }
        workflowConfigService.requireEnabled(bo.getWorkflowConfigId(), bo.getBusinessType());
        OaApplication entity;
        if (bo.getId() == null) {
            entity = new OaApplication();
            entity.setApplicationNo("OA" + System.currentTimeMillis());
            entity.setApplicantUserId(userId);
            entity.setApplicantName(user.getNickName());
            entity.setDeptId(user.getDeptId());
            entity.setStatus(DRAFT);
        } else {
            entity = getAccessible(bo.getId());
            if (!DRAFT.equals(entity.getStatus()) && !FAILED.equals(entity.getStatus())) {
                throw new ServiceException("只有草稿或提交失败的申请允许编辑");
            }
        }
        entity.setBusinessType(businessTypeService.requireEnabled(bo.getBusinessType()));
        entity.setSourceModule(StringUtils.trim(bo.getSourceModule()));
        entity.setBusinessId(StringUtils.trim(bo.getBusinessId()));
        entity.setBusinessNo(StringUtils.trim(bo.getBusinessNo()));
        List<Long> deptIds = normalizeDeptIds(bo.getDeptIds(), bo.getDeptId(), entity.getDeptId());
        if (deptIds.isEmpty() && user.getDeptId() != null) {
            deptIds = List.of(user.getDeptId());
        }
        entity.setDeptId(deptIds.isEmpty() ? bo.getDeptId() : deptIds.get(0));
        entity.setDeptIds(deptIds);
        entity.setTitle(StringUtils.trim(bo.getTitle()));
        entity.setContent(bo.getContent());
        entity.setUrgency(StringUtils.isBlank(bo.getUrgency()) ? "NORMAL" : StringUtils.trim(bo.getUrgency()));
        entity.setFormDataJson(bo.getFormDataJson());
        entity.setCompanyId(bo.getCompanyId());
        // 审批结构由所选审批方式的节点映射决定，不能由旧的 processType 覆盖。
        entity.setProcessType("CUSTOM");
        entity.setApprovalPlanId(PLAN.equals(approvalMode) ? bo.getApprovalPlanId() : null);
        entity.setApprovalMode(approvalMode);
        entity.setWorkflowConfigId(bo.getWorkflowConfigId());
        if (entity.getId() == null) {
            applicationMapper.insert(entity);
        } else {
            applicationMapper.updateById(entity);
        }
        replaceApplicationDepartments(entity.getId(), deptIds);
        replaceAttachments(entity.getId(), bo.getAttachments());
        replaceParticipants(entity.getId(), bo.getParticipants() == null && !MANUAL.equals(approvalMode)
            ? List.of() : bo.getParticipants());
        return toVo(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OaApplicationVo submit(Long id) {
        OaApplication application = applicationMapper.selectByIdForUpdate(id);
        assertAccessible(application);
        loadApplicationDepartments(application);
        if (!DRAFT.equals(application.getStatus()) && !FAILED.equals(application.getStatus())) {
            throw new ServiceException("当前申请状态不允许重复提交");
        }
        OaWorkflowConfig config = workflowConfigService.requireEnabled(application.getWorkflowConfigId(), application.getBusinessType());
        List<OaAttachmentBo> attachments = toAttachmentBos(application.getId());
        validateFormData(application.getFormDataJson());
        validateAttachments(attachments);
        validateDynamicFormData(config, application.getFormDataJson(), attachments);
        if (!"CUSTOM".equals(application.getProcessType())) {
            application.setProcessType("CUSTOM");
            applicationMapper.updateById(application);
        }
        SysUser binding = requireEnabledOaUser(application.getApplicantUserId());
        OaProcessInstance process = processMapper.selectByApplicationId(application.getId());
        if (process != null && StringUtils.isNotBlank(process.getOaRequestId())
            && !FAILED.equals(process.getLocalStatus())) {
            throw new ServiceException("该申请已经存在泛微流程：" + process.getOaRequestId());
        }
        if (process == null) {
            process = new OaProcessInstance();
            process.setApplicationId(application.getId());
            process.setRetryCount(0);
            process.setIdempotencyKey(UUID.randomUUID().toString());
        }
        String oldStatus = process.getLocalStatus();
        process.setBusinessType(application.getBusinessType());
        process.setBusinessId(application.getBusinessId());
        process.setBusinessNo(application.getBusinessNo());
        process.setSourceModule(application.getSourceModule());
        process.setBusinessTitle(application.getTitle());
        process.setWorkflowConfigId(config.getId());
        process.setWorkflowId(config.getWorkflowId());
        process.setApplicantUserId(application.getApplicantUserId());
        process.setApplicantOaUserId(binding.getOaSourceId());
        process.setRequestName(buildRequestName(config, application));
        process.setConfigSnapshotJson(JsonUtils.toJsonString(Map.of(
            "fieldMapping", StringUtils.defaultIfBlank(config.getFieldMappingJson(), "{}"),
            "specificFieldMapping", StringUtils.defaultIfBlank(config.getSpecificFieldMappingJson(), "{}"),
            "fieldSchema", StringUtils.defaultIfBlank(config.getFieldSchemaJson(), "{}"),
            "participantMapping", StringUtils.defaultIfBlank(config.getParticipantMappingJson(), "{}"),
            "workflowId", config.getWorkflowId(),
            "approvalCode", config.getApprovalCode()
        )));
        process.setLocalStatus(SUBMITTING);
        process.setFailReason(null);
        process.setRetryCount(process.getRetryCount() == null ? 1 : process.getRetryCount() + 1);
        if (process.getId() == null) {
            processMapper.insert(process);
        } else {
            processMapper.updateById(process);
        }
        application.setStatus(SUBMITTING);
        applicationMapper.updateById(application);
        addEvent(process, "SUBMIT_REQUEST", oldStatus, SUBMITTING, "POST doCreateRequest", null, null);

        if (MANUAL.equals(normalizeApprovalMode(application.getApprovalMode()))) {
            validateManualParticipants(application.getId(), config);
        } else if (departmentApprovalService.hasMatchingConfig(application)) {
            replaceParticipants(application.getId(), departmentApprovalService.resolve(application));
            applicationMapper.updateById(application);
        } else {
            throw new ServiceException("没有匹配到审批方案，请选择审批方案或临时指定审批人员");
        }
        resolveParticipants(application.getId(), process.getId());
        Map<String, Object> request = buildRequest(config, application, binding);
        try {
            EcologyClientResponse response = ecologyClient.createRequest(binding.getOaSourceId(), request);
            if (!response.isSuccess() || StringUtils.isBlank(response.getRequestId())) {
                if (StringUtils.isNotBlank(response.getRequestId())) {
                    markUnknown(application, process, response.getMessage(), response.getCode(), response.getRawBody());
                } else {
                    markFailed(application, process, response.getMessage(), response.getCode(), response.getRawBody());
                }
            } else {
                process.setOaRequestId(response.getRequestId());
                process.setOaStatus(response.getStatus());
                process.setOaStatusRaw(response.getRawBody());
                process.setLocalStatus(IN_PROGRESS);
                process.setSubmittedAt(LocalDateTime.now());
                process.setLastSyncAt(LocalDateTime.now());
                process.setOaLink(buildRequestLink(response.getRequestId()));
                process.setFailReason(null);
                processMapper.updateById(process);
                application.setStatus(IN_PROGRESS);
                application.setSubmittedAt(LocalDateTime.now());
                applicationMapper.updateById(application);
                markRelatedRecordsSubmitted(application.getId(), process.getId());
                addEvent(process, "SUBMIT_SUCCESS", SUBMITTING, IN_PROGRESS, "requestId=" + response.getRequestId(), response.getRawBody(), null);
            }
        } catch (Exception ex) {
            log.warn("提交泛微申请失败，applicationId={}", application.getId(), ex);
            markUnknown(application, process, ex.getMessage(), "CLIENT_ERROR", null);
        }
        return toVo(application);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OaApplicationVo sync(Long id) {
        OaApplication application = getAccessible(id);
        OaProcessInstance process = processMapper.selectByApplicationId(application.getId());
        if (process == null || StringUtils.isBlank(process.getOaRequestId())) {
            throw new ServiceException("该申请尚未生成泛微 requestId");
        }
        SysUser binding = requireEnabledOaUser(application.getApplicantUserId());
        String oldStatus = process.getLocalStatus();
        try {
            syncRemote(application, process, binding, "SYNC");
        } catch (Exception ex) {
            markSyncUnknown(application, process, oldStatus, ex.getMessage());
            log.warn("同步泛微申请失败，applicationId={}", application.getId(), ex);
        }
        return toVo(application);
    }

    @Override
    public List<OaApprovalRulePreviewVo> previewParticipants(Long id) {
        OaApplication application = getAccessible(id);
        List<OaApprovalParticipantBo> participants;
        if (!MANUAL.equals(normalizeApprovalMode(application.getApprovalMode()))
            && departmentApprovalService.hasMatchingConfig(application)) {
            participants = departmentApprovalService.resolve(application);
        } else {
            participants = participantMapper.selectByApplicationId(application.getId()).stream().map(item -> {
                OaApprovalParticipantBo participant = new OaApprovalParticipantBo();
                participant.setRuleId(item.getRuleId());
                participant.setRuleCode(item.getRuleCode());
                participant.setRuleName(item.getRuleName());
                participant.setStageCode(item.getStageCode());
                participant.setStageName(item.getStageName());
                participant.setStageOrder(item.getStageOrder());
                participant.setStageMode(item.getStageMode());
                participant.setParticipantRole(item.getParticipantRole());
                participant.setParticipantType(item.getParticipantType());
                participant.setLocalUserId(item.getLocalUserId());
                participant.setOaUserId(item.getOaUserId());
                participant.setSourceValue(item.getSourceValue());
                participant.setSortNo(item.getSortNo());
                participant.setRequired(item.getRequired());
                return participant;
            }).toList();
        }
        return participants.stream().map(this::toPreview).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleCallback(OaCallbackCommand command) {
        if (command == null || StringUtils.isBlank(command.eventKey()) || StringUtils.isBlank(command.requestId())) {
            throw new ServiceException("泛微回调参数不完整");
        }
        if (callbackEventMapper.selectByEventKey(command.eventKey()) != null) {
            return;
        }
        OaCallbackEvent event = new OaCallbackEvent();
        event.setEventKey(command.eventKey());
        event.setOaRequestId(command.requestId());
        event.setRawBody(limit(command.rawBody()));
        event.setEventStatus("RECEIVED");
        callbackEventMapper.insert(event);

        OaProcessInstance process = processMapper.selectByOaRequestId(command.requestId());
        if (process == null) {
            event.setEventStatus("IGNORED");
            event.setErrorMessage("未找到对应的本地审批实例");
            event.setProcessedAt(LocalDateTime.now());
            callbackEventMapper.updateById(event);
            return;
        }
        String oldStatus = process.getLocalStatus();
        String normalized = normalizeStatus(command.status(), true);
        process.setOaStatus(command.status());
        process.setOaStatusRaw(limit(command.rawBody()));
        process.setLocalStatus(normalized);
        process.setLastSyncAt(LocalDateTime.now());
        if (isTerminal(normalized)) {
            process.setCompletedAt(LocalDateTime.now());
        }
        processMapper.updateById(process);

        OaApplication application = applicationMapper.selectById(process.getApplicationId());
        if (application != null) {
            application.setStatus(normalized);
            applicationMapper.updateById(application);
        }
        addEvent(process, "CALLBACK", oldStatus, normalized, "POST /ecology/callback", command.rawBody(), null);
        event.setProcessId(process.getId());
        event.setEventStatus("PROCESSED");
        event.setProcessedAt(LocalDateTime.now());
        callbackEventMapper.updateById(event);
    }

    @Override
    public void reconcileDue() {
        if (!ecologyClient.isReady()) {
            log.debug("泛微客户端未就绪，跳过自动对账");
            return;
        }
        LocalDateTime before = LocalDateTime.now()
            .minusSeconds(Math.max(30, ecologyProperties.getReconcileStaleSeconds()));
        List<OaProcessInstance> processes = processMapper.selectDueForReconcile(before, ecologyProperties.getReconcileBatchSize());
        for (OaProcessInstance process : processes) {
            OaApplication application = applicationMapper.selectById(process.getApplicationId());
            if (application == null) {
                continue;
            }
            String oldStatus = process.getLocalStatus();
            try {
                if (StringUtils.isBlank(process.getOaRequestId())) {
                    markSyncUnknown(application, process, oldStatus, "本地提交状态超时且没有泛微 requestId，需人工核对");
                    continue;
                }
                SysUser binding = requireEnabledOaUser(application.getApplicantUserId());
                syncRemote(application, process, binding, "RECONCILE");
            } catch (Exception ex) {
                markSyncUnknown(application, process, oldStatus, ex.getMessage());
                log.warn("泛微审批自动对账失败，applicationId={}", application.getId(), ex);
            }
        }
    }

    @Override
    public List<OaProcessEventLogVo> queryEvents(Long id) {
        OaApplication application = getAccessible(id);
        OaProcessInstance process = processMapper.selectByApplicationId(application.getId());
        if (process == null) {
            return List.of();
        }
        List<OaProcessEventLogVo> result = new ArrayList<>();
        for (OaProcessEventLog item : eventMapper.selectByProcessId(process.getId())) {
            OaProcessEventLogVo vo = new OaProcessEventLogVo();
            vo.setId(item.getId());
            vo.setProcessId(item.getProcessId());
            vo.setEventType(item.getEventType());
            vo.setFromStatus(item.getFromStatus());
            vo.setToStatus(item.getToStatus());
            vo.setRequestSummary(item.getRequestSummary());
            vo.setResponseSummary(item.getResponseSummary());
            vo.setErrorCode(item.getErrorCode());
            vo.setCreateTime(item.getCreateTime());
            result.add(vo);
        }
        return result;
    }

    private OaApplication getAccessible(Long id) {
        OaApplication application = applicationMapper.selectById(id);
        assertAccessible(application);
        loadApplicationDepartments(application);
        return application;
    }

    private void assertAccessible(OaApplication application) {
        if (application == null) {
            throw new ServiceException("泛微审批申请不存在");
        }
        boolean monitor = LoginHelper.isSuperAdmin() || StpUtil.hasPermission("ecology:application:monitor");
        if (!monitor && !LoginHelper.getUserId().equals(application.getApplicantUserId())) {
            throw new ServiceException("无权访问该审批申请");
        }
    }

    private OaApplicationVo toVo(OaApplication application) {
        OaApplicationVo vo = new OaApplicationVo();
        vo.setId(application.getId());
        vo.setApplicationNo(application.getApplicationNo());
        vo.setBusinessType(application.getBusinessType());
        vo.setSourceModule(application.getSourceModule());
        vo.setBusinessId(application.getBusinessId());
        vo.setBusinessNo(application.getBusinessNo());
        vo.setTitle(application.getTitle());
        vo.setContent(application.getContent());
        vo.setUrgency(application.getUrgency());
        vo.setFormDataJson(application.getFormDataJson());
        vo.setApplicantUserId(application.getApplicantUserId());
        vo.setApplicantName(application.getApplicantName());
        vo.setDeptId(application.getDeptId());
        vo.setDeptIds(application.getDeptIds() == null || application.getDeptIds().isEmpty()
            ? application.getDeptId() == null ? List.of() : List.of(application.getDeptId())
            : application.getDeptIds());
        vo.setCompanyId(application.getCompanyId());
        vo.setProcessType(application.getProcessType());
        vo.setApprovalPlanId(application.getApprovalPlanId());
        vo.setApprovalMode(application.getApprovalMode());
        vo.setWorkflowConfigId(application.getWorkflowConfigId());
        vo.setStatus(application.getStatus());
        vo.setSubmittedAt(application.getSubmittedAt());
        vo.setCreateTime(application.getCreateTime());
        vo.setUpdateTime(application.getUpdateTime());
        vo.setAttachments(toAttachmentVo(application.getId()));
        vo.setParticipants(toParticipantVo(application.getId()));
        fillProcessInfo(vo, application);
        return vo;
    }

    private void fillProcessInfo(OaApplicationVo vo, OaApplication application) {
        OaProcessInstance process = processMapper.selectByApplicationId(application.getId());
        if (process == null) {
            return;
        }
        vo.setProcessId(process.getId());
        vo.setWorkflowId(process.getWorkflowId());
        vo.setOaRequestId(process.getOaRequestId());
        vo.setLocalStatus(process.getLocalStatus());
        vo.setOaStatus(process.getOaStatus());
        vo.setOaStatusRaw(process.getOaStatusRaw());
        vo.setRequestName(process.getRequestName());
        vo.setOaLink(process.getOaLink());
        vo.setFailReason(process.getFailReason());
        vo.setCompletedAt(process.getCompletedAt());
        vo.setLastSyncAt(process.getLastSyncAt());
        if (process.getWorkflowConfigId() != null) {
            try {
                OaWorkflowConfigVo config = workflowConfigService.queryById(process.getWorkflowConfigId(), application.getBusinessType());
                vo.setFormName(config.getFormName());
                vo.setWorkflowName(config.getWorkflowName());
                vo.setApprovalCode(config.getApprovalCode());
                vo.setApprovalName(config.getApprovalName());
            } catch (Exception ignored) {
                // 配置可能已被逻辑删除，历史实例仍允许查询。
            }
        }
    }

    private void fillProcessInfo(OaApplicationVo vo) {
        OaApplication application = applicationMapper.selectById(vo.getId());
        if (application != null) {
            fillProcessInfo(vo, application);
        }
    }

    private void loadApplicationDepartments(OaApplication application) {
        if (application == null || application.getId() == null) {
            return;
        }
        List<Long> deptIds = applicationDeptMapper.selectByApplicationId(application.getId()).stream()
            .map(OaApplicationDept::getDeptId).filter(java.util.Objects::nonNull).toList();
        if (deptIds.isEmpty() && application.getDeptId() != null) {
            deptIds = List.of(application.getDeptId());
        }
        application.setDeptIds(deptIds);
    }

    private Map<String, Object> buildRequest(OaWorkflowConfig config, OaApplication application, SysUser binding) {
        Map<String, Object> mapping = mergedFieldMapping(config);
        List<Map<String, Object>> mainData = new ArrayList<>();
        addField(mainData, StringUtils.defaultIfBlank(schemaFieldCode(config, "TITLE"), mappingField(mapping, "titleField", "wjmc")), application.getTitle());
        addField(mainData, StringUtils.defaultIfBlank(schemaFieldCode(config, "CONTENT"), mappingField(mapping, "contentField", "spxq")), application.getContent());
        addField(mainData, StringUtils.defaultIfBlank(schemaFieldCode(config, "APPLICANT"), mappingField(mapping, "applicantField", "tbr")), binding.getOaSourceId());
        addField(mainData, StringUtils.defaultIfBlank(schemaFieldCode(config, "APPLICANT_DATE"), mappingField(mapping, "applicantDateField", "tbrq")), REQUEST_TIME.format(LocalDateTime.now()));
        addField(mainData, StringUtils.defaultIfBlank(schemaFieldCode(config, "URGENCY"), mappingField(mapping, "urgencyField", null)), application.getUrgency());
        Map<String, Object> formData = StringUtils.isBlank(application.getFormDataJson())
            ? Map.of() : JsonUtils.parseMap(application.getFormDataJson());
        Object formFields = mapping.get("formFields");
        if (hasSchema(config)) {
            addSchemaFields(mainData, config, formData);
        } else if (formFields instanceof Map<?, ?> fieldMap) {
            fieldMap.forEach((source, target) -> {
                Object value = formData.get(String.valueOf(source));
                if (value != null && StringUtils.isNotBlank(String.valueOf(target))) {
                    addField(mainData, String.valueOf(target), value);
                }
            });
        }
        addParticipantFields(mainData, mapping, application.getId(), config);
        addAttachmentFields(mainData, mapping, config, application.getId());
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("workflowId", config.getWorkflowId());
        request.put("requestName", buildRequestName(config, application));
        request.put("mainData", JsonUtils.toJsonString(mainData));
        return request;
    }

    /** 合并公共字段与当前泛微表单专属字段；专属配置覆盖同名键。 */
    @SuppressWarnings("unchecked")
    private Map<String, Object> mergedFieldMapping(OaWorkflowConfig config) {
        Map<String, Object> common = StringUtils.isBlank(config.getFieldMappingJson())
            ? new LinkedHashMap<>() : JsonUtils.parseMap(config.getFieldMappingJson());
        Map<String, Object> specific = StringUtils.isBlank(config.getSpecificFieldMappingJson())
            ? Map.of() : JsonUtils.parseMap(config.getSpecificFieldMappingJson());
        Map<String, Object> merged = new LinkedHashMap<>(common);
        Object commonFormFields = common.get("formFields");
        Object specificFormFields = specific.get("formFields");
        if (commonFormFields instanceof Map<?, ?> commonValues && specificFormFields instanceof Map<?, ?> specificValues) {
            Map<String, Object> formFields = new LinkedHashMap<>((Map<String, Object>) commonValues);
            formFields.putAll((Map<String, Object>) specificValues);
            merged.put("formFields", formFields);
        }
        specific.forEach((key, value) -> {
            if (!"formFields".equals(key)) {
                merged.put(key, value);
            } else if (!(commonFormFields instanceof Map<?, ?>) || !(specificFormFields instanceof Map<?, ?>)) {
                merged.put(key, value);
            }
        });
        return merged;
    }

    private boolean hasSchema(OaWorkflowConfig config) {
        return config != null && StringUtils.isNotBlank(config.getFieldSchemaJson());
    }

    /** 将动态表单中的普通字段转换成泛微 mainData。 */
    private void addSchemaFields(List<Map<String, Object>> mainData, OaWorkflowConfig config,
                                 Map<String, Object> formData) {
        for (Map<String, Object> field : schemaFields(config)) {
            String semanticType = text(field.get("semanticType")).toUpperCase();
            if (isSystemField(semanticType)) {
                continue;
            }
            String key = text(field.get("key"));
            String oaFieldCode = text(field.get("oaFieldCode"));
            Object value = formData.get(key);
            if (StringUtils.isNotBlank(oaFieldCode) && !isBlankValue(value)) {
                addField(mainData, oaFieldCode, normalizeDynamicValue(value, field));
            }
        }
    }

    private boolean isSystemField(String semanticType) {
        return Set.of("TITLE", "CONTENT", "APPLICANT", "APPLICANT_DATE", "URGENCY",
            "APPROVAL_MODE", "COPY", "PARTICIPANT", "ATTACHMENT", "IMAGE", "SYSTEM")
            .contains(semanticType);
    }

    /** 返回具体表单的审批方式字段编码及该审批方式在泛微中的真实值。 */
    private Map<String, String> approvalModeMapping(OaWorkflowConfig config) {
        for (Map<String, Object> field : schemaFields(config)) {
            if (!"APPROVAL_MODE".equalsIgnoreCase(text(field.get("semanticType")))) {
                continue;
            }
            Map<String, String> result = new LinkedHashMap<>();
            result.put("fieldCode", text(field.get("oaFieldCode")));
            Object rawOptions = field.get("options");
            if (rawOptions instanceof Iterable<?> options) {
                for (Object rawOption : options) {
                    Map<String, Object> option = asMap(rawOption);
                    String optionCode = text(option.get("optionCode"));
                    String value = text(option.get("value"));
                    String oaValue = text(option.get("oaValue"));
                    if (StringUtils.equals(optionCode, config.getApprovalCode())
                        || StringUtils.equals(value, config.getApprovalCode())
                        || StringUtils.equals(text(option.get("label")), config.getApprovalName())) {
                        result.put("oaValue", oaValue);
                        return result;
                    }
                }
            }
            return result;
        }
        return Map.of();
    }

    private String schemaFieldCode(OaWorkflowConfig config, String semanticType) {
        for (Map<String, Object> field : schemaFields(config)) {
            String type = text(field.get("semanticType")).toUpperCase();
            String controlType = text(field.get("controlType")).toUpperCase();
            if (semanticType.equals(type) || ("IMAGE".equals(semanticType) && "IMAGE".equals(controlType))
                || ("ATTACHMENT".equals(semanticType) && "FILE".equals(controlType))) {
                return text(field.get("oaFieldCode"));
            }
        }
        return "";
    }

    private List<Map<String, Object>> schemaFields(OaWorkflowConfig config) {
        if (!hasSchema(config)) {
            return List.of();
        }
        Map<String, Object> schema = JsonUtils.parseMap(config.getFieldSchemaJson());
        Object rawFields = schema.get("fields");
        if (!(rawFields instanceof Iterable<?> values)) {
            return List.of();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object value : values) {
            Map<String, Object> field = asMap(value);
            if (!field.isEmpty()) {
                result.add(field);
            }
        }
        return result;
    }

    private String text(Object value) {
        return value == null ? "" : StringUtils.trim(String.valueOf(value));
    }

    private boolean isBlankValue(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof Iterable<?> values) {
            return !values.iterator().hasNext();
        }
        return value instanceof CharSequence && StringUtils.isBlank(String.valueOf(value));
    }

    private Object normalizeDynamicValue(Object value, Map<String, Object> field) {
        if (value instanceof Iterable<?> values) {
            List<String> items = new ArrayList<>();
            for (Object item : values) {
                String normalized = normalizeUserValue(item, field);
                if (StringUtils.isNotBlank(normalized)) {
                    items.add(normalized);
                }
            }
            return String.join(",", items);
        }
        String normalized = normalizeUserValue(value, field);
        return StringUtils.isBlank(normalized) ? value : normalized;
    }

    private String normalizeUserValue(Object value, Map<String, Object> field) {
        String controlType = text(field.get("controlType")).toUpperCase();
        if (!"USER_SINGLE".equals(controlType) && !"USER_MULTI".equals(controlType)) {
            return value == null ? "" : String.valueOf(value);
        }
        Long localUserId = Convert.toLong(value, null);
        if (localUserId == null) {
            return "";
        }
        SysUser user = sysUserMapper.selectById(localUserId);
        if (user == null || StringUtils.isBlank(user.getOaSourceId())) {
            throw new ServiceException("表单人员字段包含未同步泛微账号：" + localUserId);
        }
        return user.getOaSourceId();
    }

    private void addParticipantFields(List<Map<String, Object>> mainData, Map<String, Object> mapping,
                                      Long applicationId, OaWorkflowConfig config) {
        Map<String, Object> approvalMapping = StringUtils.isBlank(config.getParticipantMappingJson())
            ? Map.of() : JsonUtils.parseMap(config.getParticipantMappingJson());
        Map<String, String> approvalMode = approvalModeMapping(config);
        if (StringUtils.isNotBlank(schemaFieldCode(config, "APPROVAL_MODE"))
            && StringUtils.isBlank(approvalMode.get("oaValue"))) {
            throw new ServiceException("当前表单未配置所选审批方式对应的泛微实际值，请联系管理员完善表单字段选项");
        }
        String modeField = StringUtils.defaultIfBlank(approvalMode.get("fieldCode"),
            mappingField(approvalMapping, "modeField", mappingField(mapping, "approvalModeField", null)));
        Object mode = StringUtils.defaultIfBlank(approvalMode.get("oaValue"), config.getApprovalCode());
        String copyField = mappingField(approvalMapping, "copyField", mappingField(mapping, "copyField", null));
        Map<String, Object> participantFields = new LinkedHashMap<>();
        Object stages = approvalMapping.get("stages");
        if (stages instanceof Iterable<?> stageItems) {
            for (Object item : stageItems) {
                Map<String, Object> stage = asMap(item);
                String code = mappingField(stage, "code", null).toUpperCase();
                String field = mappingField(stage, "fieldCode", mappingField(stage, "field", null));
                if (StringUtils.isNotBlank(code) && StringUtils.isNotBlank(field)) {
                    participantFields.put(code, field);
                }
            }
        }
        if (participantFields.isEmpty() && StringUtils.isBlank(copyField) && StringUtils.isBlank(modeField)) {
            throw new ServiceException("审批方式未配置泛微节点字段映射，请先完善流程配置");
        }
        Map<String, List<String>> valuesByField = new LinkedHashMap<>();
        for (OaApprovalParticipant participant : participantMapper.selectByApplicationId(applicationId)) {
            if (StringUtils.isBlank(participant.getOaUserId())) {
                if (Boolean.TRUE.equals(participant.getRequired())) {
                    throw new ServiceException("审批节点缺少泛微审批人：" + participant.getStageCode());
                }
                continue;
            }
            boolean copy = "COPY".equalsIgnoreCase(participant.getParticipantRole());
            Object field = copy ? copyField : participantFields.get(StringUtils.defaultString(participant.getStageCode()).toUpperCase());
            if (field == null) {
                if (Boolean.TRUE.equals(participant.getRequired())) {
                    throw new ServiceException((copy ? "抄送节点" : "审批节点")
                        + "未配置泛微字段映射：" + participant.getStageCode());
                }
                continue;
            }
            String fieldName = String.valueOf(field);
            if (StringUtils.isBlank(fieldName)) {
                continue;
            }
            valuesByField.computeIfAbsent(fieldName, key -> new ArrayList<>()).add(participant.getOaUserId());
        }
        valuesByField.forEach((field, values) -> addField(mainData, field, String.join(",", values)));
        if (StringUtils.isNotBlank(modeField) && mode != null) {
            addField(mainData, modeField, mode);
        }
    }

    private void addAttachmentFields(List<Map<String, Object>> mainData, Map<String, Object> mapping,
                                     OaWorkflowConfig config, Long applicationId) {
        String imageField = StringUtils.defaultIfBlank(schemaFieldCode(config, "IMAGE"),
            mappingField(mapping, "imageAttachmentField", "tp"));
        String fileField = StringUtils.defaultIfBlank(schemaFieldCode(config, "ATTACHMENT"),
            mappingField(mapping, "fileAttachmentField", "fj"));
        List<Map<String, Object>> images = new ArrayList<>();
        List<Map<String, Object>> files = new ArrayList<>();
        for (OaApplicationAttachment attachment : attachmentMapper.selectByApplicationId(applicationId)) {
            String path = StringUtils.isNotBlank(attachment.getOaFilePath())
                ? attachment.getOaFilePath() : attachment.getFileUrl();
            if (StringUtils.isBlank(path)) {
                throw new ServiceException("附件没有可供泛微访问的 URL：" + attachment.getFileName());
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("filePath", path);
            item.put("fileName", attachment.getFileName());
            if ("IMAGE".equals(attachment.getAttachmentType())) {
                images.add(item);
            } else {
                files.add(item);
            }
        }
        if (!images.isEmpty() || ecologyProperties.isAttachmentWriteEmptyFields()) {
            addField(mainData, imageField, JsonUtils.toJsonString(images));
        }
        if (!files.isEmpty() || ecologyProperties.isAttachmentWriteEmptyFields()) {
            addField(mainData, fileField, JsonUtils.toJsonString(files));
        }
    }

    private void addField(List<Map<String, Object>> mainData, String fieldName, Object fieldValue) {
        if (StringUtils.isBlank(fieldName) || fieldValue == null) {
            return;
        }
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("fieldName", fieldName);
        field.put("fieldValue", fieldValue);
        mainData.add(field);
    }

    private String mappingField(Map<String, Object> mapping, String key, String defaultValue) {
        Object value = mapping.get(key);
        return value == null ? defaultValue : String.valueOf(value);
    }

    private String buildRequestName(OaWorkflowConfig config, OaApplication application) {
        String template = StringUtils.isBlank(config.getRequestNameTemplate()) ? "{businessType}-{title}" : config.getRequestNameTemplate();
        return template.replace("{businessType}", application.getBusinessType())
            .replace("{formName}", StringUtils.defaultIfBlank(config.getFormName(), config.getWorkflowName()))
            .replace("{workflowName}", config.getWorkflowName())
            .replace("{title}", application.getTitle())
            .replace("{applicationNo}", application.getApplicationNo());
    }

    private String buildRequestLink(String requestId) {
        if (StringUtils.isBlank(ecologyProperties.getRequestLinkTemplate())) {
            return null;
        }
        return ecologyProperties.getRequestLinkTemplate().replace("{requestId}", requestId);
    }

    private void markFailed(OaApplication application, OaProcessInstance process, String reason,
                            String errorCode, String response) {
        String message = StringUtils.isBlank(reason) ? "泛微接口返回失败" : reason;
        process.setLocalStatus(FAILED);
        process.setFailReason(limit(message));
        process.setOaStatusRaw(limit(response));
        processMapper.updateById(process);
        application.setStatus(FAILED);
        applicationMapper.updateById(application);
        addEvent(process, "SUBMIT_FAILED", SUBMITTING, FAILED, "POST doCreateRequest", response, errorCode);
    }

    private void markUnknown(OaApplication application, OaProcessInstance process, String reason,
                             String errorCode, String response) {
        String message = StringUtils.isBlank(reason)
            ? "泛微请求结果未知，请先查询泛微流程后再处理" : reason;
        process.setLocalStatus(UNKNOWN);
        process.setFailReason(limit(message));
        process.setOaStatusRaw(limit(response));
        process.setLastSyncAt(LocalDateTime.now());
        processMapper.updateById(process);
        application.setStatus(UNKNOWN);
        applicationMapper.updateById(application);
        addEvent(process, "SUBMIT_UNKNOWN", SUBMITTING, UNKNOWN, "POST doCreateRequest", response, errorCode);
    }

    private void markSyncUnknown(OaApplication application, OaProcessInstance process,
                                 String oldStatus, String reason) {
        process.setLocalStatus(UNKNOWN);
        process.setFailReason(limit(StringUtils.isBlank(reason) ? "泛微状态暂时无法确认" : reason));
        process.setLastSyncAt(LocalDateTime.now());
        processMapper.updateById(process);
        application.setStatus(UNKNOWN);
        applicationMapper.updateById(application);
        addEvent(process, "SYNC_FAILED", oldStatus, UNKNOWN, "GET getWorkflowRequest", null, "CLIENT_ERROR");
    }

    private void syncRemote(OaApplication application, OaProcessInstance process,
                            SysUser binding, String eventType) {
        EcologyClientResponse response = ecologyClient.queryRequest(process.getOaRequestId(), binding.getOaSourceId());
        if (!response.isSuccess()) {
            throw new ServiceException(StringUtils.isBlank(response.getMessage())
                ? "泛微状态查询失败" : response.getMessage());
        }
        String oldStatus = process.getLocalStatus();
        String normalized = normalizeStatus(response.getStatus(), true);
        process.setOaStatus(response.getStatus());
        process.setOaStatusRaw(response.getRawBody());
        process.setLocalStatus(normalized);
        process.setLastSyncAt(LocalDateTime.now());
        process.setFailReason(null);
        if (isTerminal(normalized)) {
            process.setCompletedAt(LocalDateTime.now());
        }
        processMapper.updateById(process);
        application.setStatus(normalized);
        applicationMapper.updateById(application);
        addEvent(process, eventType, oldStatus, normalized,
            "GET " + ecologyProperties.getRequestStatusPath(), response.getRawBody(), response.getCode());
    }

    private void markRelatedRecordsSubmitted(Long applicationId, Long processId) {
        for (OaApplicationAttachment attachment : attachmentMapper.selectByApplicationId(applicationId)) {
            attachment.setProcessId(processId);
            attachment.setUploadStatus("PROCESS_BOUND");
            attachmentMapper.updateById(attachment);
        }
        for (OaApprovalParticipant participant : participantMapper.selectByApplicationId(applicationId)) {
            participant.setProcessId(processId);
            participantMapper.updateById(participant);
        }
    }

    private void resolveParticipants(Long applicationId, Long processId) {
        for (OaApprovalParticipant participant : participantMapper.selectByApplicationId(applicationId)) {
            String type = StringUtils.isBlank(participant.getParticipantType())
                ? "USER" : participant.getParticipantType().toUpperCase();
            if ("USER".equals(type)) {
                if (participant.getLocalUserId() == null) {
                    throw new ServiceException("审批节点缺少本地审批人：" + participant.getStageCode());
                }
                SysUser binding = requireEnabledOaUser(participant.getLocalUserId());
                participant.setOaUserId(binding.getOaSourceId());
                participant.setOaUserName(binding.getNickName());
            } else if (!"OA_USER".equals(type) || StringUtils.isBlank(participant.getOaUserId())) {
                throw new ServiceException("暂不支持的审批人类型或审批人为空：" + type);
            }
            participant.setParticipantType(type);
            participant.setProcessId(processId);
            participantMapper.updateById(participant);
        }
    }

    private void replaceAttachments(Long applicationId, List<OaAttachmentBo> attachments) {
        if (attachments == null) {
            return;
        }
        attachmentMapper.deleteByApplicationId(applicationId);
        int index = 0;
        for (OaAttachmentBo item : attachments) {
            SysOssVo oss = ossService.getById(item.getOssId());
            if (oss == null) {
                throw new ServiceException("OSS 附件不存在：" + item.getOssId());
            }
            OaApplicationAttachment attachment = new OaApplicationAttachment();
            attachment.setApplicationId(applicationId);
            attachment.setOssId(oss.getOssId());
            attachment.setAttachmentType(normalizeAttachmentType(item.getAttachmentType()));
            attachment.setFileName(StringUtils.isBlank(oss.getOriginalName()) ? oss.getFileName() : oss.getOriginalName());
            attachment.setFileUrl(oss.getUrl());
            attachment.setSortNo(item.getSortNo() == null ? index : item.getSortNo());
            attachment.setUploadStatus(StringUtils.isBlank(oss.getUrl()) ? "PENDING" : "URL_READY");
            attachmentMapper.insert(attachment);
            index++;
        }
    }

    private void replaceParticipants(Long applicationId, List<OaApprovalParticipantBo> participants) {
        if (participants == null) {
            return;
        }
        participantMapper.deleteByApplicationId(applicationId);
        int index = 0;
        for (OaApprovalParticipantBo item : participants) {
            OaApprovalParticipant participant = new OaApprovalParticipant();
            participant.setApplicationId(applicationId);
            participant.setRuleId(item.getRuleId());
            participant.setRuleCode(StringUtils.trim(item.getRuleCode()));
            participant.setRuleName(StringUtils.trim(item.getRuleName()));
            participant.setStageCode(StringUtils.trim(item.getStageCode()));
            participant.setStageName(StringUtils.trim(item.getStageName()));
            participant.setStageOrder(item.getStageOrder());
            participant.setStageMode(StringUtils.isBlank(item.getStageMode()) ? "SEQUENTIAL" : item.getStageMode().toUpperCase());
            participant.setParticipantRole(StringUtils.isBlank(item.getParticipantRole()) ? "APPROVER" : item.getParticipantRole().toUpperCase());
            participant.setParticipantType(StringUtils.isBlank(item.getParticipantType())
                ? "USER" : item.getParticipantType().toUpperCase());
            participant.setLocalUserId(item.getLocalUserId());
            participant.setOaUserId(StringUtils.trim(item.getOaUserId()));
            participant.setSourceValue(StringUtils.trim(item.getSourceValue()));
            participant.setSortNo(item.getSortNo() == null ? index : item.getSortNo());
            participant.setRequired(item.getRequired() == null || item.getRequired());
            participantMapper.insert(participant);
            index++;
        }
    }

    private void replaceApplicationDepartments(Long applicationId, List<Long> deptIds) {
        applicationDeptMapper.deleteByApplicationId(applicationId);
        if (deptIds == null) {
            return;
        }
        int index = 0;
        for (Long deptId : deptIds) {
            if (deptId == null) {
                continue;
            }
            OaApplicationDept item = new OaApplicationDept();
            item.setApplicationId(applicationId);
            item.setDeptId(deptId);
            item.setSortNo(index++);
            applicationDeptMapper.insert(item);
        }
    }

    private List<Long> normalizeDeptIds(List<Long> deptIds, Long deptId, Long existingDeptId) {
        List<Long> values = new ArrayList<>();
        if (deptIds != null) {
            for (Long item : deptIds) {
                if (item != null && !values.contains(item)) {
                    values.add(item);
                }
            }
        }
        if (values.isEmpty() && deptId != null) {
            values.add(deptId);
        }
        if (values.isEmpty() && existingDeptId != null) {
            values.add(existingDeptId);
        }
        return values;
    }

    private List<OaAttachmentVo> toAttachmentVo(Long applicationId) {
        List<OaAttachmentVo> result = new ArrayList<>();
        for (OaApplicationAttachment item : attachmentMapper.selectByApplicationId(applicationId)) {
            OaAttachmentVo vo = new OaAttachmentVo();
            vo.setId(item.getId());
            vo.setApplicationId(item.getApplicationId());
            vo.setProcessId(item.getProcessId());
            vo.setOssId(item.getOssId());
            vo.setAttachmentType(item.getAttachmentType());
            vo.setFileName(item.getFileName());
            vo.setFileUrl(item.getFileUrl());
            vo.setSortNo(item.getSortNo());
            vo.setUploadStatus(item.getUploadStatus());
            vo.setOaFileId(item.getOaFileId());
            vo.setOaFilePath(item.getOaFilePath());
            vo.setFailReason(item.getFailReason());
            result.add(vo);
        }
        return result;
    }

    private List<OaApprovalParticipantVo> toParticipantVo(Long applicationId) {
        List<OaApprovalParticipantVo> result = new ArrayList<>();
        for (OaApprovalParticipant item : participantMapper.selectByApplicationId(applicationId)) {
            OaApprovalParticipantVo vo = new OaApprovalParticipantVo();
            vo.setId(item.getId());
            vo.setApplicationId(item.getApplicationId());
            vo.setProcessId(item.getProcessId());
            vo.setStageCode(item.getStageCode());
            vo.setStageName(item.getStageName());
            vo.setRuleId(item.getRuleId());
            vo.setRuleCode(item.getRuleCode());
            vo.setRuleName(item.getRuleName());
            vo.setStageOrder(item.getStageOrder());
            vo.setStageMode(item.getStageMode());
            vo.setParticipantRole(item.getParticipantRole());
            vo.setParticipantType(item.getParticipantType());
            vo.setLocalUserId(item.getLocalUserId());
            vo.setOaUserId(item.getOaUserId());
            vo.setOaUserName(item.getOaUserName());
            vo.setSourceValue(item.getSourceValue());
            vo.setSortNo(item.getSortNo());
            vo.setRequired(item.getRequired());
            result.add(vo);
        }
        return result;
    }

    private void validateAttachments(List<OaAttachmentBo> attachments) {
        if (attachments == null) {
            return;
        }
        if (attachments.size() > 50) {
            throw new ServiceException("单个申请最多上传50个附件");
        }
        Set<Long> ossIds = new HashSet<>();
        for (OaAttachmentBo item : attachments) {
            if (item == null || item.getOssId() == null || !ossIds.add(item.getOssId())) {
                throw new ServiceException("附件不能为空且不能重复");
            }
            normalizeAttachmentType(item.getAttachmentType());
        }
    }

    private void validateParticipants(List<OaApprovalParticipantBo> participants) {
        if (participants == null) {
            return;
        }
        if (participants.size() > 100) {
            throw new ServiceException("单个申请最多编排100名审批人");
        }
        for (OaApprovalParticipantBo item : participants) {
            if (item == null || StringUtils.isBlank(item.getStageCode())) {
                throw new ServiceException("审批节点编码不能为空");
            }
            String type = StringUtils.isBlank(item.getParticipantType())
                ? "USER" : item.getParticipantType().toUpperCase();
            if (!"USER".equals(type) && !"OA_USER".equals(type)) {
                throw new ServiceException("当前仅支持 USER、OA_USER 审批人类型");
            }
            if ("USER".equals(type) && item.getLocalUserId() == null) {
                throw new ServiceException("USER 类型审批人必须提供本地用户 ID");
            }
            if ("OA_USER".equals(type) && StringUtils.isBlank(item.getOaUserId())) {
                throw new ServiceException("OA_USER 类型审批人必须提供泛微用户 ID");
            }
        }
    }

    private void validateManualParticipants(Long applicationId, OaWorkflowConfig config) {
        List<OaApprovalParticipant> participants = participantMapper.selectByApplicationId(applicationId);
        List<OaApprovalParticipant> approvers = participants.stream()
            .filter(item -> "APPROVER".equalsIgnoreCase(item.getParticipantRole())).toList();
        if (approvers.isEmpty()) {
            throw new ServiceException("临时审批流程至少需要配置一名审批人");
        }
        Map<String, Map<String, Object>> stages = participantStages(config);
        if (stages.isEmpty()) {
            throw new ServiceException("审批方式尚未配置审批节点字段");
        }
        Set<String> actualStages = approvers.stream().map(item -> StringUtils.defaultString(item.getStageCode()).toUpperCase())
            .collect(java.util.stream.Collectors.toSet());
        for (Map.Entry<String, Map<String, Object>> entry : stages.entrySet()) {
            if (Boolean.FALSE.equals(entry.getValue().get("required"))) continue;
            if (!actualStages.contains(entry.getKey())) {
                throw new ServiceException("临时审批流程缺少节点人员：" + entry.getValue().getOrDefault("name", entry.getKey()));
            }
        }
        if (approvers.stream().anyMatch(item -> !stages.containsKey(StringUtils.defaultString(item.getStageCode()).toUpperCase()))) {
            throw new ServiceException("临时审批人节点与所选泛微审批方式不匹配");
        }
    }

    private String normalizeApprovalMode(String value) {
        String mode = StringUtils.isBlank(value) ? AUTO_RULE : value.toUpperCase();
        if (!AUTO_RULE.equals(mode) && !PLAN.equals(mode) && !MANUAL.equals(mode)) {
            throw new ServiceException("审批方式仅支持自动匹配、选择方案或临时指定");
        }
        return mode;
    }

    private OaApprovalRulePreviewVo toPreview(OaApprovalParticipantBo participant) {
        OaApprovalRulePreviewVo vo = new OaApprovalRulePreviewVo();
        vo.setRuleId(participant.getRuleId());
        vo.setRuleCode(participant.getRuleCode());
        vo.setRuleName(participant.getRuleName());
        vo.setStageCode(participant.getStageCode());
        vo.setStageName(participant.getStageName());
        vo.setStageOrder(participant.getStageOrder());
        vo.setStageMode(participant.getStageMode());
        vo.setParticipantRole(participant.getParticipantRole());
        vo.setParticipantType(participant.getParticipantType());
        vo.setLocalUserId(participant.getLocalUserId());
        vo.setOaUserId(participant.getOaUserId());
        vo.setOaUserName(participant.getLocalUserId() == null ? participant.getOaUserId()
            : java.util.Optional.ofNullable(sysUserMapper.selectById(participant.getLocalUserId()))
            .map(SysUser::getNickName).orElse(participant.getOaUserId()));
        vo.setSourceValue(participant.getSourceValue());
        vo.setSortNo(participant.getSortNo());
        vo.setRequired(participant.getRequired());
        return vo;
    }

    private String normalizeAttachmentType(String type) {
        String normalized = StringUtils.isBlank(type) ? "FILE" : type.toUpperCase();
        if (!"FILE".equals(normalized) && !"IMAGE".equals(normalized)) {
            throw new ServiceException("附件类型仅支持 FILE 或 IMAGE");
        }
        return normalized;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : Map.of();
    }

    private boolean isTerminal(String status) {
        return APPROVED.equals(status) || REJECTED.equals(status) || CANCELLED.equals(status);
    }

    private void addEvent(OaProcessInstance process, String eventType, String fromStatus, String toStatus,
                          String requestSummary, String responseSummary, String errorCode) {
        OaProcessEventLog event = new OaProcessEventLog();
        event.setProcessId(process.getId());
        event.setEventType(eventType);
        event.setFromStatus(fromStatus);
        event.setToStatus(toStatus);
        event.setRequestSummary(limit(requestSummary));
        event.setResponseSummary(limit(responseSummary));
        event.setErrorCode(errorCode);
        event.setIdempotencyKey(process.getIdempotencyKey());
        eventMapper.insert(event);
    }

    private String normalizeStatus(String rawStatus, boolean responseSuccess) {
        if (StringUtils.isBlank(rawStatus)) {
            return responseSuccess ? IN_PROGRESS : FAILED;
        }
        String status = rawStatus.toLowerCase();
        if (status.contains("reject") || status.contains("驳回") || status.contains("拒绝") || status.contains("不同意")) {
            return REJECTED;
        }
        if (status.contains("approve") || status.contains("通过") || status.contains("同意") || status.contains("已完成")) {
            return APPROVED;
        }
        if (status.contains("cancel") || status.contains("撤销") || status.contains("取消")) {
            return CANCELLED;
        }
        return IN_PROGRESS;
    }

    private void validateFormData(String formDataJson) {
        if (StringUtils.isNotBlank(formDataJson) && !JsonUtils.isJsonObject(formDataJson)) {
            throw new ServiceException("表单数据必须是 JSON 对象");
        }
    }

    /** 按当前业务选择的表单定义校验动态字段，保证正式提交的数据可以被泛微接受。 */
    private void validateDynamicFormData(OaWorkflowConfig config, String formDataJson,
                                         List<OaAttachmentBo> attachments) {
        if (!hasSchema(config)) {
            return;
        }
        Map<String, Object> formData = StringUtils.isBlank(formDataJson)
            ? Map.of() : JsonUtils.parseMap(formDataJson);
        for (Map<String, Object> field : schemaFields(config)) {
            String key = text(field.get("key"));
            String label = StringUtils.defaultIfBlank(text(field.get("label")), key);
            String semanticType = text(field.get("semanticType")).toUpperCase();
            String controlType = text(field.get("controlType")).toUpperCase();
            // 这些字段由系统在提交时自动生成或由审批配置维护，不应要求申请人在动态表单中填写。
            if (isAutoManagedField(semanticType)) {
                continue;
            }
            if ("FILE".equals(controlType) || "IMAGE".equals(controlType)
                || "ATTACHMENT".equals(semanticType) || "IMAGE".equals(semanticType)) {
                if (Boolean.TRUE.equals(field.get("required")) && !hasAttachmentType(attachments, "IMAGE".equals(controlType) ? "IMAGE" : "FILE")) {
                    throw new ServiceException("请上传必填附件：" + label);
                }
                continue;
            }
            if (Boolean.TRUE.equals(field.get("required")) && isBlankValue(formData.get(key))) {
                throw new ServiceException("请填写必填字段：" + label);
            }
            if (("SELECT".equals(controlType) || "RADIO".equals(controlType))
                && !isBlankValue(formData.get(key))) {
                validateDynamicOption(field, formData.get(key), label);
            }
        }
    }

    private boolean isAutoManagedField(String semanticType) {
        return Set.of("APPLICANT", "APPLICANT_DATE", "APPROVAL_MODE", "PARTICIPANT", "COPY", "SYSTEM")
            .contains(semanticType);
    }

    private void validateDynamicOption(Map<String, Object> field, Object value, String label) {
        Set<String> allowed = new HashSet<>();
        Object rawOptions = field.get("options");
        if (rawOptions instanceof Iterable<?> options) {
            for (Object rawOption : options) {
                String oaValue = text(asMap(rawOption).get("oaValue"));
                if (StringUtils.isNotBlank(oaValue)) {
                    allowed.add(oaValue);
                }
            }
        }
        for (Object item : value instanceof Iterable<?> values ? values : List.of(value)) {
            if (!allowed.contains(String.valueOf(item))) {
                throw new ServiceException("字段“" + label + "”的选项值无效，请重新选择");
            }
        }
    }

    private boolean hasAttachmentType(List<OaAttachmentBo> attachments, String type) {
        if (attachments == null) {
            return false;
        }
        return attachments.stream().anyMatch(item -> item != null
            && type.equals(normalizeAttachmentType(item.getAttachmentType())));
    }

    private List<OaAttachmentBo> toAttachmentBos(Long applicationId) {
        return attachmentMapper.selectByApplicationId(applicationId).stream().map(item -> {
            OaAttachmentBo attachment = new OaAttachmentBo();
            attachment.setOssId(item.getOssId());
            attachment.setAttachmentType(item.getAttachmentType());
            attachment.setSortNo(item.getSortNo());
            return attachment;
        }).toList();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Object>> participantStages(OaWorkflowConfig config) {
        Map<String, Object> mapping = config == null || StringUtils.isBlank(config.getParticipantMappingJson())
            ? Map.of() : JsonUtils.parseMap(config.getParticipantMappingJson());
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        Object stages = mapping.get("stages");
        if (stages instanceof Iterable<?> values) {
            for (Object value : values) {
                if (!(value instanceof Map<?, ?> raw)) continue;
                Map<String, Object> stage = (Map<String, Object>) raw;
                String code = String.valueOf(stage.getOrDefault("code", "")).trim().toUpperCase();
                String field = String.valueOf(stage.getOrDefault("fieldCode", stage.getOrDefault("field", ""))).trim();
                if (!code.isEmpty() && !field.isEmpty()) result.put(code, stage);
            }
        }
        return result;
    }

    /** 审批只允许使用已经由泛微 HRM 同步到本地的有效用户。 */
    private SysUser requireEnabledOaUser(Long userId) {
        SysUser user = userId == null ? null : sysUserMapper.selectById(userId);
        if (user == null || !SystemConstants.NORMAL.equals(user.getStatus())
            || !SystemConstants.NORMAL.equals(user.getDelFlag())
            || !"USER".equals(user.getOaSourceType())
            || StringUtils.isBlank(user.getOaSourceId())) {
            throw new ServiceException("当前用户尚未完成泛微人员同步或账号已停用");
        }
        return user;
    }

    private String limit(String value) {
        if (value == null) {
            return null;
        }
        return value.length() > 2000 ? value.substring(0, 2000) : value;
    }
}
