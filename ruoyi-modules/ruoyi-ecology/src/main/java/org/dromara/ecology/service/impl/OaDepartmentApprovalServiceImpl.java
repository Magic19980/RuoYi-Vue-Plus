package org.dromara.ecology.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.ecology.domain.OaApplication;
import org.dromara.ecology.domain.OaDepartmentApproval;
import org.dromara.ecology.domain.OaDepartmentApprovalUser;
import org.dromara.ecology.domain.OaWorkflowConfig;
import org.dromara.ecology.domain.bo.OaApprovalParticipantBo;
import org.dromara.ecology.domain.bo.OaDepartmentApprovalBo;
import org.dromara.ecology.domain.bo.OaDepartmentApprovalUserBo;
import org.dromara.ecology.domain.vo.OaDepartmentApprovalUserVo;
import org.dromara.ecology.domain.vo.OaDepartmentApprovalVo;
import org.dromara.ecology.mapper.OaDepartmentApprovalMapper;
import org.dromara.ecology.mapper.OaDepartmentApprovalUserMapper;
import org.dromara.ecology.service.IOaBusinessTypeService;
import org.dromara.ecology.service.IOaDepartmentApprovalService;
import org.dromara.ecology.service.IOaWorkflowConfigService;
import org.dromara.system.domain.SysDept;
import org.dromara.system.domain.SysUser;
import org.dromara.system.mapper.SysDeptMapper;
import org.dromara.system.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** 泛微审批方案服务实现。 */
@Service
@RequiredArgsConstructor
public class OaDepartmentApprovalServiceImpl implements IOaDepartmentApprovalService {

    private static final String ENABLED = "ENABLED";
    private static final String APPROVER = "APPROVER";
    private static final String COPY = "COPY";
    private static final String SEQUENTIAL = "SEQUENTIAL";
    private static final String OA_USER = "USER";
    private static final List<String> OA_ORGANIZATION_TYPES = List.of("SUBCOMPANY", "DEPARTMENT");

    private final OaDepartmentApprovalMapper mapper;
    private final OaDepartmentApprovalUserMapper userMapper;
    private final IOaWorkflowConfigService workflowConfigService;
    private final IOaBusinessTypeService businessTypeService;
    private final SysDeptMapper sysDeptMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public List<OaDepartmentApprovalVo> queryList(Long workflowConfigId, String businessType, String sourceModule,
                                                   Long businessDeptId, boolean enabledOnly) {
        List<OaDepartmentApproval> rows = mapper.selectList(Wrappers.<OaDepartmentApproval>lambdaQuery()
            .eq(workflowConfigId != null, OaDepartmentApproval::getWorkflowConfigId, workflowConfigId)
            .eq(StringUtils.isNotBlank(businessType), OaDepartmentApproval::getBusinessType, StringUtils.trim(businessType))
            .and(StringUtils.isNotBlank(sourceModule), query -> query
                .eq(OaDepartmentApproval::getSourceModule, StringUtils.trim(sourceModule))
                .or().isNull(OaDepartmentApproval::getSourceModule))
            .and(businessDeptId != null, query -> query
                .eq(OaDepartmentApproval::getBusinessDeptId, businessDeptId)
                .or().isNull(OaDepartmentApproval::getBusinessDeptId))
            .eq(enabledOnly, OaDepartmentApproval::getStatus, ENABLED)
            .orderByAsc(OaDepartmentApproval::getBusinessType)
            .orderByDesc(OaDepartmentApproval::getBusinessDeptId)
            .orderByDesc(OaDepartmentApproval::getPriority)
            .orderByAsc(OaDepartmentApproval::getPlanName)
            .orderByAsc(OaDepartmentApproval::getId));
        return rows.stream().map(this::toVo).toList();
    }

    @Override
    public OaDepartmentApprovalVo queryById(Long id) {
        OaDepartmentApproval entity = mapper.selectById(id);
        if (entity == null) {
            throw new ServiceException("泛微审批方案不存在");
        }
        return toVo(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(OaDepartmentApprovalBo bo) {
        validate(bo, null);
        OaDepartmentApproval entity = new OaDepartmentApproval();
        copy(bo, entity);
        mapper.insert(entity);
        replaceUsers(entity.getId(), bo.getUsers());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(OaDepartmentApprovalBo bo) {
        validate(bo, bo.getId());
        OaDepartmentApproval entity = mapper.selectById(bo.getId());
        if (entity == null) {
            throw new ServiceException("泛微审批方案不存在");
        }
        copy(bo, entity);
        mapper.updateById(entity);
        userMapper.deleteByApprovalId(entity.getId());
        replaceUsers(entity.getId(), bo.getUsers());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        userMapper.deleteByApprovalId(id);
        return mapper.deleteById(id) > 0;
    }

    @Override
    public boolean hasMatchingConfig(OaApplication application) {
        return !findMatches(application).isEmpty();
    }

    @Override
    public List<OaApprovalParticipantBo> resolve(OaApplication application) {
        List<OaDepartmentApproval> matches = findMatches(application);
        if (matches.isEmpty()) {
            return List.of();
        }
        OaDepartmentApproval plan = matches.get(0);
        OaWorkflowConfig config = workflowConfigService.requireEnabled(plan.getWorkflowConfigId(), plan.getBusinessType());
        application.setApprovalPlanId(plan.getId());
        application.setProcessType("CUSTOM");
        Map<String, Map<String, Object>> stageDefinitions = participantStages(config);
        if (stageDefinitions.isEmpty()) {
            throw new ServiceException("审批方式尚未配置审批节点字段");
        }
        List<OaDepartmentApprovalUser> configuredUsers = userMapper.selectByApprovalId(plan.getId());
        Map<Long, SysUser> users = loadUsers(configuredUsers);
        List<OaApprovalParticipantBo> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        Map<String, Integer> stageOrders = new LinkedHashMap<>();
        for (OaDepartmentApprovalUser item : configuredUsers) {
            SysUser user = users.get(item.getLocalUserId());
            if (!isEnabledOaUser(user)) {
                throw new ServiceException("审批方案中的用户未完成泛微同步或已停用：" + item.getLocalUserId());
            }
            boolean copy = COPY.equalsIgnoreCase(item.getParticipantRole());
            String role = copy ? COPY : APPROVER;
            String stageCode = normalizeStageCode(item.getStageCode(), stageDefinitions, copy);
            if (!seen.add(stageCode + ":" + role + ":" + user.getUserId())) {
                continue;
            }
            OaApprovalParticipantBo participant = new OaApprovalParticipantBo();
            participant.setStageCode(stageCode);
            Map<String, Object> definition = stageDefinitions.get(stageCode);
            participant.setStageName(StringUtils.isBlank(item.getStageName())
                ? String.valueOf(definition.getOrDefault("name", stageCode)) : item.getStageName());
            participant.setStageOrder(numberValue(definition.get("sortNo"), 1));
            participant.setStageMode(copy ? SEQUENTIAL
                : StringUtils.isBlank(item.getStageMode())
                    ? String.valueOf(definition.getOrDefault("mode", SEQUENTIAL)) : item.getStageMode());
            participant.setParticipantRole(role);
            participant.setParticipantType(OA_USER);
            participant.setLocalUserId(user.getUserId());
            participant.setSourceValue("APPROVAL_PLAN:" + plan.getId());
            int order = item.getSortNo() == null ? stageOrders.getOrDefault(stageCode, 0) : item.getSortNo();
            participant.setSortNo(order);
            stageOrders.put(stageCode, Math.max(stageOrders.getOrDefault(stageCode, 0), order + 1));
            participant.setRequired(!copy);
            result.add(participant);
        }
        if (result.stream().noneMatch(item -> APPROVER.equals(item.getParticipantRole()))) {
            throw new ServiceException("审批方案至少需要一名审批人");
        }
        Set<String> actualStages = result.stream()
            .filter(item -> APPROVER.equals(item.getParticipantRole()))
            .map(OaApprovalParticipantBo::getStageCode)
            .collect(Collectors.toSet());
        for (Map.Entry<String, Map<String, Object>> entry : stageDefinitions.entrySet()) {
            if (!Boolean.FALSE.equals(entry.getValue().get("required")) && !actualStages.contains(entry.getKey())) {
                throw new ServiceException("审批方案缺少节点人员：" + entry.getValue().getOrDefault("name", entry.getKey()));
            }
        }
        return result;
    }

    @Override
    public OaDepartmentApprovalVo resolveForImport(String businessType, String sourceModule,
                                                   Long businessDeptId, String formDataJson) {
        if (StringUtils.isBlank(businessType)) {
            return null;
        }
        OaApplication context = new OaApplication();
        context.setBusinessType(StringUtils.trim(businessType));
        context.setSourceModule(StringUtils.trim(sourceModule));
        context.setDeptId(businessDeptId);
        context.setFormDataJson(formDataJson);
        List<OaDepartmentApproval> candidates = mapper.selectList(Wrappers.<OaDepartmentApproval>lambdaQuery()
            .eq(OaDepartmentApproval::getBusinessType, StringUtils.trim(businessType))
            .and(StringUtils.isNotBlank(sourceModule), query -> query
                .eq(OaDepartmentApproval::getSourceModule, StringUtils.trim(sourceModule))
                .or().isNull(OaDepartmentApproval::getSourceModule))
            .eq(OaDepartmentApproval::getStatus, ENABLED));
        return candidates.stream()
            // 导入业务必须配置具体结算部门方案；全组织通用方案不能掩盖部门漏配。
            .filter(item -> businessDeptId == null
                ? item.getBusinessDeptId() == null
                : item.getBusinessDeptId() != null && matchesBusinessDept(item.getBusinessDeptId(), context))
            .filter(item -> matchesCondition(item.getMatchConditionJson(), context))
            .sorted(Comparator
                .comparing((OaDepartmentApproval item) -> StringUtils.equals(
                    StringUtils.trim(item.getSourceModule()), StringUtils.trim(sourceModule)) ? 1 : 0)
                .reversed()
                .thenComparing(item -> deptSpecificity(item.getBusinessDeptId(), businessDeptId), Comparator.reverseOrder())
                .thenComparing(item -> conditionSize(item.getMatchConditionJson()), Comparator.reverseOrder())
                .thenComparing(item -> item.getPriority() == null ? 0 : item.getPriority(), Comparator.reverseOrder())
                .thenComparing(OaDepartmentApproval::getId, Comparator.reverseOrder()))
            .map(this::toVo)
            .findFirst()
            .orElse(null);
    }

    private List<OaDepartmentApproval> findMatches(OaApplication application) {
        if (application == null || application.getWorkflowConfigId() == null
            || StringUtils.isBlank(application.getBusinessType())) {
            return List.of();
        }
        if (application.getApprovalPlanId() != null) {
            OaDepartmentApproval plan = mapper.selectOne(Wrappers.<OaDepartmentApproval>lambdaQuery()
                .eq(OaDepartmentApproval::getId, application.getApprovalPlanId())
                .eq(OaDepartmentApproval::getStatus, ENABLED));
            if (plan == null || !application.getWorkflowConfigId().equals(plan.getWorkflowConfigId())
                || !StringUtils.equals(StringUtils.trim(application.getBusinessType()), StringUtils.trim(plan.getBusinessType()))
                || !matchesBusinessDept(plan.getBusinessDeptId(), application)) {
                throw new ServiceException("所选审批方案与当前业务、泛微流程或业务归属组织不匹配");
            }
            return List.of(plan);
        }
        List<OaDepartmentApproval> candidates = mapper.selectEnabledByApplication(
                application.getWorkflowConfigId(), StringUtils.trim(application.getBusinessType())).stream()
            .filter(item -> StringUtils.isBlank(item.getSourceModule())
                || StringUtils.equals(StringUtils.trim(item.getSourceModule()), StringUtils.trim(application.getSourceModule())))
            .filter(item -> matchesBusinessDept(item.getBusinessDeptId(), application))
            .filter(item -> matchesCondition(item.getMatchConditionJson(), application))
            .sorted(Comparator
                .comparing((OaDepartmentApproval item) -> StringUtils.isBlank(item.getSourceModule()) ? 0 : 1)
                .thenComparing(item -> item.getBusinessDeptId() == null ? 1 : 0)
                .thenComparing(item -> -conditionSize(item.getMatchConditionJson()))
                .thenComparing(item -> -(item.getPriority() == null ? 0 : item.getPriority()))
                .thenComparing(OaDepartmentApproval::getId, Comparator.reverseOrder()))
            .toList();
        return candidates.isEmpty() ? List.of() : List.of(candidates.get(0));
    }

    private void validate(OaDepartmentApprovalBo bo, Long currentId) {
        if (bo == null) {
            throw new ServiceException("审批方案不能为空");
        }
        String businessType = StringUtils.trim(bo.getBusinessType());
        String sourceModule = StringUtils.trim(bo.getSourceModule());
        String planName = StringUtils.trim(bo.getPlanName());
        if (StringUtils.isBlank(businessType)) {
            throw new ServiceException("业务类型不能为空");
        }
        businessType = businessTypeService.requireEnabled(businessType);
        bo.setBusinessType(businessType);
        if (StringUtils.isBlank(planName)) {
            throw new ServiceException("审批方案名称不能为空");
        }
        validateBusinessDept(bo.getBusinessDeptId());
        if (StringUtils.isNotBlank(bo.getMatchConditionJson())
            && !JsonUtils.isJsonObject(bo.getMatchConditionJson())) {
            throw new ServiceException("匹配条件必须是 JSON 对象");
        }
        OaWorkflowConfig workflowConfig = workflowConfigService.requireEnabled(bo.getWorkflowConfigId(), bo.getBusinessType());
        Map<String, Map<String, Object>> stageDefinitions = participantStages(workflowConfig);
        if (stageDefinitions.isEmpty()) {
            throw new ServiceException("审批方式尚未配置审批节点字段");
        }
        if (bo.getUsers() == null || bo.getUsers().isEmpty()) {
            throw new ServiceException("至少配置一名审批人");
        }
        if (bo.getUsers().size() > 100) {
            throw new ServiceException("单个审批方案最多维护100名人员");
        }
        Set<String> uniqueRoleUsers = new HashSet<>();
        boolean hasApprover = false;
        for (OaDepartmentApprovalUserBo user : bo.getUsers()) {
            if (user == null || user.getLocalUserId() == null) {
                throw new ServiceException("审批人员不能为空");
            }
            String role = normalizeRole(user.getParticipantRole());
            user.setParticipantRole(role);
            boolean copy = COPY.equals(role);
            String stageCode = normalizeStageCode(user.getStageCode(), stageDefinitions, copy);
            user.setStageCode(stageCode);
            user.setStageName(StringUtils.isBlank(user.getStageName())
                ? copy ? "抄送人员" : String.valueOf(stageDefinitions.get(stageCode).getOrDefault("name", stageCode))
                : StringUtils.trim(user.getStageName()));
            user.setStageMode(copy ? SEQUENTIAL : String.valueOf(
                stageDefinitions.get(stageCode).getOrDefault("mode", SEQUENTIAL)));
            if (!uniqueRoleUsers.add(stageCode + ":" + role + ":" + user.getLocalUserId())) {
                throw new ServiceException("同一方案中不能重复选择同一人员");
            }
            hasApprover |= APPROVER.equals(role);
        }
        if (!hasApprover) {
            throw new ServiceException("至少配置一名审批人，抄送人不能替代审批人");
        }
        Set<String> stages = bo.getUsers().stream()
            .filter(item -> APPROVER.equals(item.getParticipantRole()))
            .map(OaDepartmentApprovalUserBo::getStageCode)
            .collect(Collectors.toSet());
        for (Map.Entry<String, Map<String, Object>> entry : stageDefinitions.entrySet()) {
            if (!Boolean.FALSE.equals(entry.getValue().get("required")) && !stages.contains(entry.getKey())) {
                throw new ServiceException("审批方案缺少节点人员：" + entry.getValue().getOrDefault("name", entry.getKey()));
            }
        }
        List<Long> userIds = bo.getUsers().stream().map(OaDepartmentApprovalUserBo::getLocalUserId).distinct().toList();
        Map<Long, SysUser> users = sysUserMapper.selectByIds(userIds).stream()
            .collect(Collectors.toMap(SysUser::getUserId, item -> item, (left, right) -> left));
        for (Long userId : userIds) {
            if (!isEnabledOaUser(users.get(userId))) {
                throw new ServiceException("只能选择已同步到本地的有效泛微用户：" + userId);
            }
        }
        List<OaDepartmentApproval> sameName = mapper.selectList(Wrappers.<OaDepartmentApproval>lambdaQuery()
            .eq(OaDepartmentApproval::getWorkflowConfigId, bo.getWorkflowConfigId())
            .eq(OaDepartmentApproval::getBusinessType, businessType)
            .eq(OaDepartmentApproval::getPlanName, planName)
            .eq(StringUtils.isNotBlank(sourceModule), OaDepartmentApproval::getSourceModule, sourceModule)
            .isNull(StringUtils.isBlank(sourceModule), OaDepartmentApproval::getSourceModule)
            .eq(bo.getBusinessDeptId() != null, OaDepartmentApproval::getBusinessDeptId, bo.getBusinessDeptId())
            .isNull(bo.getBusinessDeptId() == null, OaDepartmentApproval::getBusinessDeptId));
        if (sameName.stream().anyMatch(item -> currentId == null || !currentId.equals(item.getId()))) {
            throw new ServiceException("相同业务、流程和方案名称已存在，请直接编辑原方案");
        }
        bo.setBusinessType(businessType);
        bo.setSourceModule(sourceModule);
        bo.setPlanName(planName);
        bo.setMatchConditionJson(StringUtils.isBlank(bo.getMatchConditionJson()) ? null : StringUtils.trim(bo.getMatchConditionJson()));
        bo.setPriority(bo.getPriority() == null ? 0 : bo.getPriority());
        bo.setStatus(ENABLED.equalsIgnoreCase(bo.getStatus()) ? ENABLED : "DISABLED");
    }

    private void copy(OaDepartmentApprovalBo bo, OaDepartmentApproval entity) {
        entity.setWorkflowConfigId(bo.getWorkflowConfigId());
        entity.setBusinessType(StringUtils.trim(bo.getBusinessType()));
        entity.setSourceModule(StringUtils.trim(bo.getSourceModule()));
        entity.setBusinessDeptId(bo.getBusinessDeptId());
        entity.setPlanName(StringUtils.trim(bo.getPlanName()));
        entity.setMatchConditionJson(StringUtils.isBlank(bo.getMatchConditionJson()) ? null : StringUtils.trim(bo.getMatchConditionJson()));
        entity.setPriority(bo.getPriority() == null ? 0 : bo.getPriority());
        entity.setStatus(ENABLED.equalsIgnoreCase(bo.getStatus()) ? ENABLED : "DISABLED");
        entity.setRemark(StringUtils.trim(bo.getRemark()));
    }

    private void replaceUsers(Long approvalId, List<OaDepartmentApprovalUserBo> users) {
        int index = 0;
        for (OaDepartmentApprovalUserBo item : users) {
            OaDepartmentApprovalUser entity = new OaDepartmentApprovalUser();
            entity.setApprovalId(approvalId);
            entity.setLocalUserId(item.getLocalUserId());
            entity.setStageCode(StringUtils.isBlank(item.getStageCode()) ? "APPROVAL" : item.getStageCode());
            entity.setStageName(StringUtils.trim(item.getStageName()));
            entity.setStageMode(StringUtils.isBlank(item.getStageMode()) ? SEQUENTIAL : item.getStageMode());
            entity.setParticipantRole(normalizeRole(item.getParticipantRole()));
            entity.setSortNo(item.getSortNo() == null ? index : item.getSortNo());
            userMapper.insert(entity);
            index++;
        }
    }

    private OaDepartmentApprovalVo toVo(OaDepartmentApproval entity) {
        OaDepartmentApprovalVo vo = new OaDepartmentApprovalVo();
        vo.setId(entity.getId());
        vo.setWorkflowConfigId(entity.getWorkflowConfigId());
        vo.setBusinessType(entity.getBusinessType());
        vo.setSourceModule(entity.getSourceModule());
        vo.setBusinessDeptId(entity.getBusinessDeptId());
        vo.setBusinessDeptName(deptName(entity.getBusinessDeptId()));
        vo.setPlanName(entity.getPlanName());
        vo.setMatchConditionJson(entity.getMatchConditionJson());
        vo.setPriority(entity.getPriority());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setUsers(toUserVos(entity.getId()));
        try {
            OaWorkflowConfig config = workflowConfigService.requireEnabled(entity.getWorkflowConfigId(), entity.getBusinessType());
            vo.setWorkflowName(config.getWorkflowName());
            vo.setFormName(config.getFormName());
            vo.setProcessType(config.getApprovalCode());
            vo.setApprovalCode(config.getApprovalCode());
            vo.setApprovalName(config.getApprovalName());
            vo.setParticipantMappingJson(config.getParticipantMappingJson());
        } catch (Exception ignored) {
            // 流程停用时仍返回方案，方便管理员处理。
        }
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    private List<OaDepartmentApprovalUserVo> toUserVos(Long approvalId) {
        List<OaDepartmentApprovalUser> rows = userMapper.selectByApprovalId(approvalId);
        Map<Long, SysUser> users = loadUsers(rows);
        return rows.stream().map(item -> {
            SysUser user = users.get(item.getLocalUserId());
            OaDepartmentApprovalUserVo vo = new OaDepartmentApprovalUserVo();
            vo.setId(item.getId());
            vo.setApprovalId(item.getApprovalId());
            vo.setLocalUserId(item.getLocalUserId());
            vo.setStageCode(item.getStageCode());
            vo.setStageName(item.getStageName());
            vo.setStageMode(item.getStageMode());
            vo.setUserName(user == null ? null : user.getUserName());
            vo.setNickName(user == null ? null : user.getNickName());
            vo.setEmployeeNo(user == null ? null : user.getEmployeeNo());
            vo.setOaUserId(user == null ? null : user.getOaSourceId());
            vo.setDeptName(user == null || user.getDeptId() == null ? null : deptName(user.getDeptId()));
            vo.setParticipantRole(item.getParticipantRole());
            vo.setSortNo(item.getSortNo());
            return vo;
        }).toList();
    }

    private String deptName(Long deptId) {
        if (deptId == null) {
            return null;
        }
        SysDept dept = sysDeptMapper.selectById(deptId);
        return dept == null ? null : dept.getDeptName();
    }

    private void validateBusinessDept(Long businessDeptId) {
        if (businessDeptId == null) {
            return;
        }
        SysDept dept = sysDeptMapper.selectById(businessDeptId);
        if (dept == null || !SystemConstants.NORMAL.equals(dept.getStatus())
            || !SystemConstants.NORMAL.equals(dept.getDelFlag())
            || !OA_ORGANIZATION_TYPES.contains(dept.getOaSourceType())) {
            throw new ServiceException("业务归属组织必须选择有效的泛微同步组织");
        }
    }

    /** 组织方案既匹配组织本身，也匹配其下级组织，便于按上级业务组织统一维护方案。 */
    private boolean matchesBusinessDept(Long businessDeptId, OaApplication application) {
        if (businessDeptId == null) {
            return true;
        }
        List<Long> deptIds = new ArrayList<>();
        if (application.getDeptIds() != null) {
            deptIds.addAll(application.getDeptIds());
        }
        if (deptIds.isEmpty() && application.getDeptId() != null) {
            deptIds.add(application.getDeptId());
        }
        for (Long deptId : deptIds) {
            if (businessDeptId.equals(deptId)) {
                return true;
            }
            SysDept dept = sysDeptMapper.selectById(deptId);
            String ancestors = dept == null ? null : dept.getAncestors();
            if (StringUtils.isNotBlank(ancestors)
                && ("," + ancestors + ",").contains("," + businessDeptId + ",")) {
                return true;
            }
        }
        return false;
    }

    private Map<Long, SysUser> loadUsers(Collection<OaDepartmentApprovalUser> rows) {
        List<Long> ids = rows.stream().map(OaDepartmentApprovalUser::getLocalUserId)
            .filter(java.util.Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return sysUserMapper.selectByIds(ids).stream()
            .collect(Collectors.toMap(SysUser::getUserId, item -> item, (left, right) -> left, LinkedHashMap::new));
    }

    private boolean matchesCondition(String json, OaApplication application) {
        if (StringUtils.isBlank(json)) {
            return true;
        }
        Map<String, Object> conditions = JsonUtils.parseMap(json);
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("businessType", application.getBusinessType());
        context.put("sourceModule", application.getSourceModule());
        context.put("businessId", application.getBusinessId());
        context.put("businessNo", application.getBusinessNo());
        context.put("title", application.getTitle());
        context.put("deptId", application.getDeptId());
        context.put("companyId", application.getCompanyId());
        if (StringUtils.isNotBlank(application.getFormDataJson()) && JsonUtils.isJsonObject(application.getFormDataJson())) {
            context.putAll(JsonUtils.parseMap(application.getFormDataJson()));
        }
        return conditions.entrySet().stream().allMatch(entry -> matchValue(context.get(entry.getKey()), entry.getValue()));
    }

    private boolean matchValue(Object actual, Object expected) {
        if (expected instanceof Map<?, ?> operators) {
            if (operators.containsKey("in")) {
                return matchValue(actual, operators.get("in"));
            }
            if (operators.containsKey("equals")) {
                return matchValue(actual, operators.get("equals"));
            }
            if (operators.containsKey("contains")) {
                return actual != null && String.valueOf(actual).contains(String.valueOf(operators.get("contains")));
            }
            if (operators.containsKey("notEquals")) {
                return !matchValue(actual, operators.get("notEquals"));
            }
        }
        if (expected instanceof Collection<?> values) {
            return values.stream().anyMatch(item -> matchScalar(actual, item));
        }
        return matchScalar(actual, expected);
    }

    private boolean matchScalar(Object actual, Object expected) {
        return actual != null && expected != null
            && StringUtils.equalsIgnoreCase(String.valueOf(actual).trim(), String.valueOf(expected).trim());
    }

    private int conditionSize(String json) {
        return StringUtils.isBlank(json) ? 0 : JsonUtils.parseMap(json).size();
    }

    private int deptSpecificity(Long configuredDeptId, Long businessDeptId) {
        if (configuredDeptId == null || businessDeptId == null) {
            return 0;
        }
        if (configuredDeptId.equals(businessDeptId)) {
            return 2;
        }
        return matchesBusinessDept(configuredDeptId, applicationWithDept(businessDeptId)) ? 1 : 0;
    }

    private OaApplication applicationWithDept(Long businessDeptId) {
        OaApplication application = new OaApplication();
        application.setDeptId(businessDeptId);
        return application;
    }

    private boolean isEnabledOaUser(SysUser user) {
        return user != null && SystemConstants.NORMAL.equals(user.getStatus())
            && SystemConstants.NORMAL.equals(user.getDelFlag())
            && OA_USER.equals(user.getOaSourceType()) && StringUtils.isNotBlank(user.getOaSourceId());
    }

    private String normalizeRole(String value) {
        String role = StringUtils.isBlank(value) ? APPROVER : value.toUpperCase();
        if (!APPROVER.equals(role) && !COPY.equals(role)) {
            throw new ServiceException("人员类型仅支持审批人或抄送人");
        }
        return role;
    }

    private String normalizeStageCode(String value, Map<String, Map<String, Object>> definitions, boolean copy) {
        if (copy) {
            return "COPY";
        }
        String stage = StringUtils.isBlank(value) ? "" : value.trim().toUpperCase();
        if ("APPROVAL".equals(stage) && definitions.size() == 1) {
            return definitions.keySet().iterator().next();
        }
        if (!definitions.containsKey(stage)) {
            throw new ServiceException("审批人节点与所选泛微审批方式不匹配：" + stage);
        }
        return stage;
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
                if (StringUtils.isNotBlank(code) && StringUtils.isNotBlank(field)) result.put(code, stage);
            }
        }
        return result;
    }

    private int numberValue(Object value, int fallback) {
        if (value instanceof Number number) return number.intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
