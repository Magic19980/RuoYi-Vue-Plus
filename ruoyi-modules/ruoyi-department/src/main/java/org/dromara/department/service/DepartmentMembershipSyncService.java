package org.dromara.department.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.event.UserDepartmentChangingEvent;
import org.dromara.common.core.event.UserDepartmentChangedEvent;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.department.domain.PersonProfile;
import org.dromara.department.domain.PersonProfileEvent;
import org.dromara.department.mapper.DepartmentConfigMapper;
import org.dromara.department.mapper.PersonProfileEventMapper;
import org.dromara.department.mapper.PersonProfileMapper;
import org.springframework.stereotype.Service;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统主部门与科室人员档案的同步服务。
 *
 * <p>只有已配置且启用的业务科室才会自动生成正式成员关系。临时协作关系不参与本服务的
 * 自动结束，人员移出后只关闭服务关系，不删除日报、任务和其他历史业务数据。</p>
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DepartmentMembershipSyncService {

    private static final long SYSTEM_OPERATOR = 0L;

    private final DepartmentConfigMapper departmentConfigMapper;
    private final PersonProfileMapper personProfileMapper;
    private final PersonProfileEventMapper personProfileEventMapper;

    @Transactional(rollbackFor = Exception.class)
    public void syncMainDepartment(Long userId, Long mainDeptId) {
        if (userId == null) {
            return;
        }
        LocalDate today = LocalDate.now();
        Long operatorId = LoginHelper.getUserId() == null ? SYSTEM_OPERATOR : LoginHelper.getUserId();
        if (mainDeptId != null && departmentConfigMapper.countEnabled(mainDeptId) > 0) {
            PersonProfile profile = personProfileMapper.selectLatestByUserDept(userId, mainDeptId);
            if (profile == null) {
                profile = new PersonProfile();
                profile.setUserId(userId);
                profile.setCreateDept(mainDeptId);
                profile.setJoinDate(today);
                profile.setMemberType("FULL");
                profile.setMemberStatus("ACTIVE");
                profile.setMemberSource("AUTO_MAIN");
                profile.setCreateBy(operatorId);
                profile.setCreateDept(mainDeptId);
                personProfileMapper.insert(profile);
                recordEvent(profile, "JOIN", today, "系统主部门自动纳入", operatorId);
            } else if (!"ACTIVE".equals(profile.getMemberStatus())
                || !"FULL".equals(profile.getMemberType())
                || profile.getLeaveDate() != null
                || !"AUTO_MAIN".equals(profile.getMemberSource())) {
                String eventType = "ACTIVE".equals(profile.getMemberStatus()) ? "CHANGE" : "REJOIN";
                personProfileMapper.activateMainMembership(profile.getId(), operatorId);
                profile.setMemberType("FULL");
                profile.setMemberStatus("ACTIVE");
                profile.setMemberSource("AUTO_MAIN");
                profile.setLeaveDate(null);
                recordEvent(profile, eventType, today, "系统主部门自动同步", operatorId);
            }
        }
        endAutoMemberships(userId, mainDeptId, today, operatorId, "系统主部门变更");
    }

    @Transactional(rollbackFor = Exception.class)
    public void syncConfiguredDepartment(Long deptId) {
        if (deptId == null || departmentConfigMapper.countEnabled(deptId) == 0) {
            return;
        }
        for (Long userId : personProfileMapper.selectMainUserIdsByDept(deptId)) {
            syncMainDepartment(userId, deptId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void disableDepartmentAutoMemberships(Long deptId) {
        if (deptId == null) {
            return;
        }
        LocalDate today = LocalDate.now();
        for (PersonProfile profile : personProfileMapper.selectActiveAutoMainMembershipsByDept(deptId)) {
            endProfile(profile, today, SYSTEM_OPERATOR, "业务科室停用");
        }
    }

    /**
     * 主部门变更前必须先结束旧主部门的有效服务关系，避免用户组织归属和人员档案脱节。
     */
    @EventListener
    public void onUserDepartmentChanging(UserDepartmentChangingEvent event) {
        if (event == null || event.getUserId() == null
            || java.util.Objects.equals(event.getOldDeptId(), event.getNewDeptId())
            || event.getOldDeptId() == null) {
            return;
        }
        if (personProfileMapper.countMemberInDept(event.getUserId(), event.getOldDeptId()) > 0) {
            throw new ServiceException("请先结束该用户在原科室的人员档案服务关系，再修改用户管理中的部门");
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onUserDepartmentChanged(UserDepartmentChangedEvent event) {
        try {
            syncMainDepartment(event.getUserId(), event.getNewDeptId());
        } catch (Exception ex) {
            log.error("同步用户主部门到科室人员档案失败，userId={}, oldDeptId={}, newDeptId={}",
                event.getUserId(), event.getOldDeptId(), event.getNewDeptId(), ex);
        }
    }

    private void endAutoMemberships(Long userId, Long targetDeptId, LocalDate leaveDate,
                                    Long operatorId, String reason) {
        List<PersonProfile> memberships = personProfileMapper.selectActiveAutoMainMemberships(userId);
        for (PersonProfile profile : memberships) {
            if (!java.util.Objects.equals(profile.getCreateDept(), targetDeptId)) {
                endProfile(profile, leaveDate, operatorId, reason);
            }
        }
    }

    private void endProfile(PersonProfile profile, LocalDate leaveDate, Long operatorId, String reason) {
        profile.setLeaveDate(leaveDate);
        profile.setMemberStatus("ENDED");
        profile.setEndedAt(LocalDateTime.now());
        profile.setEndedBy(operatorId);
        profile.setEndReason(reason);
        personProfileMapper.updateById(profile);
        recordEvent(profile, "LEAVE", leaveDate, reason, operatorId);
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
        event.setReason(reason);
        event.setOperatorId(operatorId);
        event.setCreateDept(profile.getCreateDept());
        event.setCreateBy(operatorId);
        event.setCreateTime(LocalDateTime.now());
        personProfileEventMapper.insert(event);
    }
}
