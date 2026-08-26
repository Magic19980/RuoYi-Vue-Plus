package org.dromara.department.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.department.domain.PersonProfile;
import org.dromara.department.domain.PersonProfileEvent;
import org.dromara.department.domain.bo.PersonProfileBo;
import org.dromara.department.domain.bo.PersonProfileBatchBo;
import org.dromara.department.domain.bo.PersonProfileEndBo;
import org.dromara.department.domain.bo.PersonProfileQueryBo;
import org.dromara.department.domain.bo.PersonUserOptionQueryBo;
import org.dromara.department.domain.vo.PersonProfileVo;
import org.dromara.department.domain.vo.PersonProfileImportVo;
import org.dromara.department.domain.vo.PersonDepartmentContextVo;
import org.dromara.department.domain.vo.PersonUserOptionVo;
import org.dromara.department.domain.vo.DepartmentConfigVo;
import org.dromara.department.mapper.DepartmentConfigMapper;
import org.dromara.department.mapper.PersonProfileMapper;
import org.dromara.department.mapper.PersonProfileEventMapper;
import org.dromara.department.service.IPersonProfileService;
import org.dromara.department.service.DepartmentAccessService;
import org.dromara.department.service.DepartmentMembershipSyncService;
import org.dromara.department.service.DepartmentScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 人员档案业务实现。
 */
@RequiredArgsConstructor
@Service
public class PersonProfileServiceImpl implements IPersonProfileService {

    private static final String DEPT_VIEW_PERMISSION = "department:person:viewDept";
    private static final long MAX_EXPORT_ROWS = 50_000L;

    private final PersonProfileMapper personProfileMapper;
    private final DepartmentConfigMapper departmentConfigMapper;
    private final PersonProfileEventMapper personProfileEventMapper;
    private final DepartmentAccessService departmentAccessService;
    private final DepartmentMembershipSyncService membershipSyncService;

    @Override
    public PageResult<PersonProfileVo> queryPageList(PersonProfileQueryBo bo, PageQuery pageQuery) {
        Page<PersonProfileVo> page = pageQuery.build();
        Page<PersonProfileVo> result = personProfileMapper.selectPageList(page, bo, LoginHelper.getUserId(), scope(), LocalDate.now());
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public PersonProfileVo queryById(Long id) {
        PersonProfile entity = getAccessible(id);
        PersonProfileQueryBo bo = new PersonProfileQueryBo();
        bo.setId(id);
        Page<PersonProfileVo> page = new Page<>(1, 1);
        bo.setIncludeHistory(true);
        Page<PersonProfileVo> result = personProfileMapper.selectPageList(page, bo, entity.getUserId(), DepartmentScope.all(), LocalDate.now());
        return result.getRecords().isEmpty() ? null : result.getRecords().get(0);
    }

    @Override
    public List<PersonProfileVo> queryList(PersonProfileQueryBo bo) {
        Page<PersonProfileVo> page = new Page<>(1, MAX_EXPORT_ROWS + 1);
        List<PersonProfileVo> records = personProfileMapper.selectPageList(
            page, bo, LoginHelper.getUserId(), scope(), LocalDate.now()).getRecords();
        ensureExportSize(records.size(), "人员档案");
        return records;
    }

    @Override
    public List<PersonUserOptionVo> queryUserOptions() {
        // 科室管理员可以把其他部门的协作人员纳入本部门日报，原所属部门仍保留在系统用户档案中。
        return personProfileMapper.selectUserOptions(userOptionScope());
    }

    @Override
    public PageResult<PersonUserOptionVo> queryUserOptionsPage(PersonUserOptionQueryBo bo, PageQuery pageQuery) {
        Page<PersonUserOptionVo> page = pageQuery.build();
        Page<PersonUserOptionVo> result = personProfileMapper.selectUserOptionsPage(
            page,
            bo,
            requireTargetDept(),
            userOptionScope(),
            LocalDate.now()
        );
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public List<PersonUserOptionVo> queryMemberUserOptions() {
        return personProfileMapper.selectMemberUserOptions(requireTargetDept(), LocalDate.now());
    }

    @Override
    public List<PersonDepartmentContextVo> queryMyDepartments() {
        if (LoginHelper.isSuperAdmin()) {
            return querySuperAdminDepartments();
        }
        membershipSyncService.syncMainDepartment(LoginHelper.getUserId(), LoginHelper.getMainDeptId());
        List<PersonDepartmentContextVo> contexts = personProfileMapper.selectCurrentDepartmentContexts(LoginHelper.getUserId(), LocalDate.now());
        Long currentDeptId = LoginHelper.getDeptId();
        // 只有一个有效关系且系统主部门已失效时，可以安全自动切换；多个关系必须由用户明确选择。
        if (contexts.size() == 1 && !Objects.equals(currentDeptId, contexts.get(0).getDeptId())) {
            PersonDepartmentContextVo context = contexts.get(0);
            LoginHelper.setActiveDept(context.getDeptId(), context.getDeptName(), context.getMemberType());
            currentDeptId = context.getDeptId();
        }
        for (PersonDepartmentContextVo context : contexts) {
            context.setCurrent(Objects.equals(currentDeptId, context.getDeptId()));
        }
        return contexts;
    }

    @Override
    public PersonDepartmentContextVo switchMyDepartment(Long deptId) {
        if (LoginHelper.isSuperAdmin()) {
            PersonDepartmentContextVo target = querySuperAdminDepartments().stream()
                .filter(item -> Objects.equals(item.getDeptId(), deptId))
                .findFirst()
                .orElseThrow(() -> new ServiceException("目标科室未启用或不存在，不能切换科室上下文"));
            LoginHelper.setActiveDept(target.getDeptId(), target.getDeptName(), target.getMemberType());
            target.setCurrent(true);
            return target;
        }
        PersonDepartmentContextVo target = personProfileMapper.selectCurrentDepartmentContexts(LoginHelper.getUserId(), LocalDate.now())
            .stream()
            .filter(item -> Objects.equals(item.getDeptId(), deptId))
            .findFirst()
            .orElseThrow(() -> new ServiceException("您当前未纳入该科室，不能切换科室上下文"));
        LoginHelper.setActiveDept(target.getDeptId(), target.getDeptName(), target.getMemberType());
        target.setCurrent(true);
        return target;
    }

    private List<PersonDepartmentContextVo> querySuperAdminDepartments() {
        List<DepartmentConfigVo> departments = departmentConfigMapper.selectEnabledDepartments();
        if (departments.isEmpty()) {
            throw new ServiceException("当前尚未配置启用的业务科室，请先完成科室配置");
        }
        Long currentDeptId = LoginHelper.getDeptId();
        boolean currentAvailable = false;
        for (DepartmentConfigVo department : departments) {
            if (Objects.equals(department.getDeptId(), currentDeptId)) {
                currentAvailable = true;
                break;
            }
        }
        if (!currentAvailable) {
            DepartmentConfigVo first = departments.get(0);
            LoginHelper.setActiveDept(first.getDeptId(), first.getDeptName(), null);
            currentDeptId = first.getDeptId();
        }
        Long selectedDeptId = currentDeptId;
        return departments.stream().map(item -> {
            PersonDepartmentContextVo context = new PersonDepartmentContextVo();
            context.setDeptId(item.getDeptId());
            context.setDeptName(item.getDeptName());
            context.setCurrent(Objects.equals(item.getDeptId(), selectedDeptId));
            return context;
        }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(PersonProfileBo bo) {
        PersonProfileBatchBo batchBo = new PersonProfileBatchBo();
        batchBo.setUserIds(List.of(bo.getUserId()));
        batchBo.setJoinDate(bo.getJoinDate());
        batchBo.setLeaveDate(bo.getLeaveDate());
        batchBo.setMemberType(bo.getMemberType());
        batchBo.setRemark(bo.getRemark());
        return insertBatch(batchBo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertBatch(PersonProfileBatchBo bo) {
        if (bo.getUserIds() == null || bo.getUserIds().isEmpty()) {
            throw new ServiceException("至少选择一名系统用户");
        }
        LocalDate joinDate = bo.getJoinDate() == null ? LocalDate.now() : bo.getJoinDate();
        validatePeriod(joinDate, bo.getLeaveDate());
        Long targetDeptId = requireTargetDept();
        Set<Long> userIds = new LinkedHashSet<>(bo.getUserIds());
        for (Long userId : userIds) {
            PersonUserOptionVo user = findUserOption(userId);
            if (hasOverlappingMembership(userId, targetDeptId, joinDate, bo.getLeaveDate(), null)) {
                throw new ServiceException("用户「" + displayUserName(user) + "」已经纳入当前科室");
            }
            PersonProfile entity = new PersonProfile();
            entity.setCreateDept(targetDeptId);
            entity.setMemberSource("MANUAL");
            PersonProfileBo item = new PersonProfileBo();
            item.setUserId(userId);
            item.setJoinDate(joinDate);
            item.setLeaveDate(bo.getLeaveDate());
            item.setMemberType(bo.getMemberType());
            item.setRemark(bo.getRemark());
            fillEntity(entity, item, joinDate);
            if (personProfileMapper.insert(entity) <= 0) {
                throw new ServiceException("新增用户「" + displayUserName(user) + "」的人员档案失败");
            }
            recordEvent(entity, "JOIN", joinDate, "人工批量纳入科室", LoginHelper.getUserId());
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(PersonProfileBo bo) {
        PersonProfile entity = getAccessible(bo.getId());
        String oldMemberType = entity.getMemberType();
        LocalDate oldJoinDate = entity.getJoinDate();
        LocalDate oldLeaveDate = entity.getLeaveDate();
        String oldRemark = entity.getRemark();
        LocalDate joinDate = bo.getJoinDate() == null ? entity.getJoinDate() : bo.getJoinDate();
        validatePeriod(joinDate, bo.getLeaveDate());
        findUserOption(bo.getUserId());
        Long targetDeptId = entity.getCreateDept() == null ? requireTargetDept() : entity.getCreateDept();
        if (hasOverlappingMembership(bo.getUserId(), targetDeptId, joinDate, bo.getLeaveDate(), bo.getId())) {
            throw new ServiceException("该用户在当前科室存在重叠的服务关系");
        }
        entity.setCreateDept(targetDeptId);
        fillEntity(entity, bo, joinDate);
        boolean success = personProfileMapper.updateById(entity) > 0;
        if (success && (!Objects.equals(oldMemberType, entity.getMemberType())
            || !Objects.equals(oldJoinDate, entity.getJoinDate())
            || !Objects.equals(oldLeaveDate, entity.getLeaveDate())
            || !Objects.equals(oldRemark, entity.getRemark()))) {
            recordEvent(entity, "CHANGE", entity.getJoinDate(), "人工修改服务关系", LoginHelper.getUserId());
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        for (Long id : ids) {
            PersonProfile entity = getAccessible(id);
            if (!"ENDED".equals(entity.getMemberStatus())) {
                endMembershipInternal(entity, LocalDate.now(), "手动移出科室");
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean endMembership(Long id, PersonProfileEndBo bo) {
        PersonProfile entity = getAccessible(id);
        validatePeriod(entity.getJoinDate(), bo.getLeaveDate());
        if ("ENDED".equals(entity.getMemberStatus()) && Objects.equals(entity.getLeaveDate(), bo.getLeaveDate())) {
            return true;
        }
        endMembershipInternal(entity, bo.getLeaveDate(), bo.getReason());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importData(List<PersonProfileImportVo> rows) {
        if (rows == null || rows.isEmpty()) {
            return "未读取到人员档案数据";
        }
        int success = 0;
        int skipped = 0;
        Long targetDeptId = requireTargetDept();
        Map<String, PersonUserOptionVo> usersByName = new HashMap<>();
        Set<String> userNames = rows.stream()
            .filter(Objects::nonNull)
            .map(PersonProfileImportVo::getUserName)
            .filter(org.dromara.common.core.utils.StringUtils::isNotBlank)
            .map(String::trim)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        if (!userNames.isEmpty()) {
            personProfileMapper.selectUserOptionsByNames(userNames)
                .forEach(item -> usersByName.put(item.getUserName(), item));
        }
        for (PersonProfileImportVo row : rows) {
            if (row == null || org.dromara.common.core.utils.StringUtils.isBlank(row.getUserName())) {
                skipped++;
                continue;
            }
            String userName = row.getUserName().trim();
            PersonUserOptionVo user = usersByName.get(userName);
            if (user == null) {
                throw new ServiceException("找不到有效系统用户：" + userName);
            }
            LocalDate joinDate = row.getJoinDate() == null ? LocalDate.now() : row.getJoinDate();
            validatePeriod(joinDate, null);
            String memberType = org.dromara.common.core.utils.StringUtils.isBlank(row.getMemberType()) ? "FULL" : row.getMemberType().trim().toUpperCase();
            if (!List.of("FULL", "TEMP").contains(memberType)) {
                    throw new ServiceException("成员类型只能是正式成员或临时协作：" + userName);
            }
            PersonProfile entity = personProfileMapper.selectActiveMembership(user.getUserId(), targetDeptId, joinDate);
            if (entity == null) {
                if (hasOverlappingMembership(user.getUserId(), targetDeptId, joinDate, null, null)) {
                    throw new ServiceException("该用户在当前科室存在重叠的服务关系：" + userName);
                }
                entity = new PersonProfile();
                entity.setUserId(user.getUserId());
                entity.setCreateDept(targetDeptId);
                entity.setJoinDate(joinDate);
                entity.setMemberType(memberType);
                entity.setMemberStatus("ACTIVE");
                entity.setMemberSource("MANUAL");
                personProfileMapper.insert(entity);
                recordEvent(entity, "JOIN", joinDate, "人工导入科室", LoginHelper.getUserId());
            }
            success++;
        }
        return String.format("导入完成：成功 %d 条，跳过 %d 条", success, skipped);
    }

    private void fillEntity(PersonProfile entity, PersonProfileBo bo, LocalDate joinDate) {
        entity.setUserId(bo.getUserId());
        entity.setJoinDate(joinDate);
        entity.setLeaveDate(bo.getLeaveDate());
        String memberType = org.dromara.common.core.utils.StringUtils.isBlank(bo.getMemberType()) ? "FULL" : bo.getMemberType().trim().toUpperCase();
        if (!List.of("FULL", "TEMP").contains(memberType)) {
            throw new ServiceException("成员类型只能是正式成员或临时协作");
        }
        entity.setMemberType(memberType);
        entity.setRemark(org.dromara.common.core.utils.StringUtils.trim(bo.getRemark()));
        entity.setMemberStatus(bo.getLeaveDate() != null && !bo.getLeaveDate().isAfter(LocalDate.now()) ? "ENDED" : "ACTIVE");
        if (!"ENDED".equals(entity.getMemberStatus())) {
            entity.setEndedAt(null);
            entity.setEndedBy(null);
            entity.setEndReason(null);
        }
    }

    private void endMembershipInternal(PersonProfile entity, LocalDate leaveDate, String reason) {
        validatePeriod(entity.getJoinDate(), leaveDate);
        entity.setLeaveDate(leaveDate);
        entity.setEndReason(org.dromara.common.core.utils.StringUtils.trim(reason));
        if (!leaveDate.isAfter(LocalDate.now())) {
            entity.setMemberStatus("ENDED");
            entity.setEndedAt(LocalDateTime.now());
            entity.setEndedBy(LoginHelper.getUserId());
        } else {
            entity.setMemberStatus("ACTIVE");
            entity.setEndedAt(null);
            entity.setEndedBy(null);
        }
        if (personProfileMapper.updateById(entity) <= 0) {
            throw new ServiceException("结束科室服务关系失败");
        }
        recordEvent(entity, "LEAVE", leaveDate, reason, LoginHelper.getUserId());
    }

    private void recordEvent(PersonProfile profile, String eventType, LocalDate effectiveDate,
                             String reason, Long operatorId) {
        PersonProfileEvent event = new PersonProfileEvent();
        event.setProfileId(profile.getId());
        event.setUserId(profile.getUserId());
        event.setDeptId(profile.getCreateDept());
        event.setEventType(eventType);
        event.setEffectiveDate(effectiveDate);
        event.setMemberType(profile.getMemberType());
        event.setReason(org.dromara.common.core.utils.StringUtils.trim(reason));
        event.setOperatorId(operatorId);
        event.setCreateDept(profile.getCreateDept());
        event.setCreateBy(operatorId);
        event.setCreateTime(LocalDateTime.now());
        personProfileEventMapper.insert(event);
    }

    private void validatePeriod(LocalDate joinDate, LocalDate leaveDate) {
        if (joinDate == null) {
            throw new ServiceException("加入日期不能为空");
        }
        if (leaveDate != null && leaveDate.isBefore(joinDate)) {
            throw new ServiceException("离开生效日不能早于加入日期");
        }
    }

    private boolean hasOverlappingMembership(Long userId, Long deptId, LocalDate joinDate, LocalDate leaveDate, Long excludeId) {
        return personProfileMapper.countOverlappingMembership(userId, deptId, joinDate, leaveDate, excludeId) > 0;
    }

    private PersonUserOptionVo findUserOption(Long userId) {
        PersonUserOptionVo option = personProfileMapper.selectUserOptionById(userId);
        if (option == null) {
            throw new ServiceException("系统用户不存在或已停用");
        }
        return option;
    }

    private String displayUserName(PersonUserOptionVo user) {
        if (org.dromara.common.core.utils.StringUtils.isNotBlank(user.getNickName())) {
            return user.getNickName() + "（" + user.getUserName() + "）";
        }
        return user.getUserName();
    }

    private Long requireTargetDept() {
        Long deptId = departmentAccessService.currentDeptId();
        if (deptId == null) {
            throw new ServiceException("当前登录用户缺少科室信息");
        }
        return deptId;
    }

    private PersonProfile getAccessible(Long id) {
        PersonProfile entity = personProfileMapper.selectById(id);
        if (entity == null) {
            throw new ServiceException("人员档案不存在");
        }
        if (scope().isAll()) {
            return entity;
        }
        if (departmentAccessService.canViewEntityDept(entity.getCreateDept(), DEPT_VIEW_PERMISSION)) {
            return entity;
        }
        if (Objects.equals(entity.getUserId(), LoginHelper.getUserId())) {
            return entity;
        }
        throw new ServiceException("您没有访问该人员档案的权限");
    }

    private DepartmentScope userOptionScope() {
        return canViewDepartment() ? DepartmentScope.all() : DepartmentScope.current(departmentAccessService.currentDeptId());
    }

    private DepartmentScope scope() {
        return departmentAccessService.scope(DEPT_VIEW_PERMISSION);
    }

    private boolean canViewDepartment() {
        return departmentAccessService.canViewCurrentDepartment(DEPT_VIEW_PERMISSION);
    }

    private void ensureExportSize(int size, String name) {
        if (size > MAX_EXPORT_ROWS) {
            throw new ServiceException(name + "导出数据超过" + MAX_EXPORT_ROWS + "条，请缩小查询范围后再导出");
        }
    }
}
