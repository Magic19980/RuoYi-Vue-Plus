package org.dromara.department.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.utils.IdGeneratorUtil;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.department.domain.DepartmentReviewRule;
import org.dromara.department.domain.PersonProfile;
import org.dromara.department.domain.DepartmentTaskAssignment;
import org.dromara.department.domain.DepartmentTaskInstance;
import org.dromara.department.domain.DepartmentTaskRule;
import org.dromara.department.domain.bo.DepartmentReviewRuleBo;
import org.dromara.department.domain.bo.DepartmentTaskAssignmentBo;
import org.dromara.department.domain.bo.DepartmentTaskRuleBo;
import org.dromara.department.domain.vo.DepartmentReviewRuleVo;
import org.dromara.department.domain.vo.DepartmentTaskAssignmentVo;
import org.dromara.department.domain.vo.DepartmentTaskProgressVo;
import org.dromara.department.domain.vo.DepartmentTaskRuleVo;
import org.dromara.department.domain.vo.PersonUserOptionVo;
import org.dromara.department.mapper.DepartmentReviewRuleMapper;
import org.dromara.department.mapper.DepartmentTaskAssignmentMapper;
import org.dromara.department.mapper.DepartmentTaskCompletionMapper;
import org.dromara.department.mapper.DepartmentTaskInstanceMapper;
import org.dromara.department.mapper.DepartmentTaskMapper;
import org.dromara.department.mapper.DepartmentTaskRuleMapper;
import org.dromara.department.mapper.PersonProfileMapper;
import org.dromara.department.service.DepartmentAccessService;
import org.dromara.department.service.DepartmentContextResolver;
import org.dromara.department.service.IDepartmentTaskService;
import org.dromara.department.service.IDailyCalendarService;
import org.dromara.system.api.MessageService;
import org.dromara.system.api.domain.PushPayloadDTO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** 审核配置、周期任务分配、进度统计及提醒。 */
@RequiredArgsConstructor
@Service
public class DepartmentTaskServiceImpl implements IDepartmentTaskService {

    private static final String ENABLED = "ENABLED";
    private static final String DISABLED = "DISABLED";
    private static final String SCORE = "SCORE_PROPOSAL";
    private static final String FIVE_WHY = "FIVE_WHY";
    private static final String DAILY = "DAILY_REPORT";

    private final DepartmentReviewRuleMapper reviewRuleMapper;
    private final DepartmentTaskRuleMapper taskRuleMapper;
    private final DepartmentTaskAssignmentMapper assignmentMapper;
    private final DepartmentTaskInstanceMapper instanceMapper;
    private final DepartmentTaskCompletionMapper completionMapper;
    private final DepartmentTaskMapper taskMapper;
    private final PersonProfileMapper personProfileMapper;
    private final IDailyCalendarService dailyCalendarService;
    private final MessageService messageService;
    private final DepartmentContextResolver departmentContextResolver;
    private final DepartmentAccessService departmentAccessService;

    @Override
    public List<DepartmentTaskRuleVo> queryRuleList() {
        Long deptId = currentDeptId();
        List<DepartmentTaskRule> rules = taskRuleMapper.selectList(Wrappers.<DepartmentTaskRule>lambdaQuery()
            .eq(DepartmentTaskRule::getDeptId, deptId)
            .eq(DepartmentTaskRule::getDelFlag, "0")
            .orderByAsc(DepartmentTaskRule::getTaskType)
            .orderByAsc(DepartmentTaskRule::getId));
        return rules.stream().map(this::toRuleVo).toList();
    }

    @Override
    public DepartmentTaskRuleVo queryRuleById(Long id) {
        return toRuleVo(getRule(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveRule(DepartmentTaskRuleBo bo) {
        requireDept();
        validateRule(bo);
        DepartmentTaskRule entity = bo.getId() == null ? new DepartmentTaskRule() : getRule(bo.getId());
        if (entity.getId() == null) {
            entity.setDeptId(currentDeptId());
        }
        boolean daily = DAILY.equals(bo.getTaskType());
        entity.setTaskName(StringUtils.trim(bo.getTaskName()));
        entity.setTaskType(bo.getTaskType());
        entity.setCycleType(daily ? "DAY" : bo.getCycleType());
        entity.setRequiredCount(daily ? 1 : bo.getRequiredCount());
        entity.setDeadlineDay(daily ? 0 : (bo.getDeadlineDay() == null ? 0 : bo.getDeadlineDay()));
        entity.setDeadlineTime(daily ? LocalTime.of(18, 0) : (bo.getDeadlineTime() == null ? LocalTime.of(18, 0) : bo.getDeadlineTime()));
        entity.setCountMode(daily ? "SUBMITTED" : (StringUtils.isBlank(bo.getCountMode()) ? "SUBMITTED" : bo.getCountMode()));
        entity.setRemindHours(daily ? 0 : (bo.getRemindHours() == null ? 24 : Math.max(0, bo.getRemindHours())));
        entity.setEffectiveStart(bo.getEffectiveStart());
        entity.setEffectiveEnd(bo.getEffectiveEnd());
        entity.setStatus(StringUtils.isBlank(bo.getStatus()) ? ENABLED : bo.getStatus());
        entity.setRemark(bo.getRemark());
        return entity.getId() == null ? taskRuleMapper.insert(entity) > 0 : taskRuleMapper.updateById(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteRule(Long id) {
        DepartmentTaskRule rule = getRule(id);
        assignmentMapper.delete(Wrappers.<DepartmentTaskAssignment>lambdaUpdate()
            .eq(DepartmentTaskAssignment::getRuleId, rule.getId()));
        return taskRuleMapper.deleteById(rule.getId()) > 0;
    }

    @Override
    public List<DepartmentTaskAssignmentVo> queryAssignments(Long ruleId) {
        DepartmentTaskRule rule = getRule(ruleId);
        Map<Long, PersonUserOptionVo> users = userMap();
        return assignmentMapper.selectByRuleId(rule.getId()).stream().map(item -> {
            DepartmentTaskAssignmentVo vo = new DepartmentTaskAssignmentVo();
            vo.setId(item.getId());
            vo.setRuleId(item.getRuleId());
            vo.setDeptId(item.getDeptId());
            vo.setUserId(item.getUserId());
            vo.setEffectiveStart(item.getEffectiveStart());
            vo.setEffectiveEnd(item.getEffectiveEnd());
            vo.setWorkDays(item.getWorkDays());
            vo.setReminderTime(item.getReminderTime());
            vo.setStatus(item.getStatus());
            vo.setRemark(item.getRemark());
            PersonUserOptionVo user = users.get(item.getUserId());
            if (user != null) {
                vo.setUserName(user.getUserName());
                vo.setNickName(user.getNickName());
            }
            return vo;
        }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveAssignment(DepartmentTaskAssignmentBo bo) {
        requireDept();
        DepartmentTaskRule rule = getRule(bo.getRuleId());
        if (assignmentMapper.countUserInDept(bo.getUserId(), rule.getDeptId()) == 0) {
            throw new ServiceException("只能给已纳入该科室的有效成员分配任务");
        }
        DepartmentTaskAssignment entity = bo.getId() == null ? new DepartmentTaskAssignment() : getAssignment(bo.getId());
        if (entity.getId() == null) {
            entity.setRuleId(rule.getId());
            entity.setDeptId(rule.getDeptId());
        } else if (!Objects.equals(entity.getRuleId(), rule.getId())) {
            throw new ServiceException("任务分配规则不匹配");
        }
        if (assignmentMapper.existsActiveAssignment(rule.getId(), bo.getUserId(), entity.getId())) {
            throw new ServiceException("该成员已经分配过此任务");
        }
        PersonProfile membership = personProfileMapper.selectActiveMembership(
            bo.getUserId(), rule.getDeptId(), bo.getEffectiveStart() == null ? LocalDate.now() : bo.getEffectiveStart());
        if (membership == null) {
            throw new ServiceException("成员在任务生效日期不属于该科室");
        }
        LocalDate effectiveStart = bo.getEffectiveStart() == null || bo.getEffectiveStart().isBefore(membership.getJoinDate())
            ? membership.getJoinDate() : bo.getEffectiveStart();
        LocalDate effectiveEnd = bo.getEffectiveEnd();
        if (membership.getLeaveDate() != null && (effectiveEnd == null || !effectiveEnd.isBefore(membership.getLeaveDate().minusDays(1)))) {
            effectiveEnd = membership.getLeaveDate().minusDays(1);
        }
        if (effectiveEnd != null && effectiveEnd.isBefore(effectiveStart)) {
            throw new ServiceException("任务结束日期不能早于成员加入日期");
        }
        entity.setUserId(bo.getUserId());
        entity.setEffectiveStart(effectiveStart);
        entity.setEffectiveEnd(effectiveEnd);
        if (DAILY.equals(rule.getTaskType())) {
            entity.setWorkDays(normalizeWorkDays(bo.getWorkDays()));
            entity.setReminderTime(bo.getReminderTime() == null ? LocalTime.of(18, 0) : bo.getReminderTime());
        } else {
            entity.setWorkDays(null);
            entity.setReminderTime(null);
        }
        entity.setStatus(StringUtils.isBlank(bo.getStatus()) ? ENABLED : bo.getStatus());
        entity.setRemark(bo.getRemark());
        return entity.getId() == null ? assignmentMapper.insert(entity) > 0 : assignmentMapper.updateById(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteAssignment(Long id) {
        return assignmentMapper.deleteById(getAssignment(id).getId()) > 0;
    }

    @Override
    public List<DepartmentTaskProgressVo> queryMyTasks() {
        Long userId = LoginHelper.getUserId();
        LocalDate today = LocalDate.now();
        Map<Long, PersonUserOptionVo> users = userMap();
        List<DepartmentTaskProgressVo> result = new ArrayList<>();
        Long deptId = currentDeptId();
        Set<Long> activeMemberIds = new HashSet<>(assignmentMapper.selectActiveUserIdsInDept(deptId));
        List<DepartmentTaskAssignment> assignments = assignmentMapper.selectActiveByUserId(userId, deptId, today);
        Set<Long> ruleIds = assignments.stream().map(DepartmentTaskAssignment::getRuleId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, DepartmentTaskRule> rules = ruleIds.isEmpty() ? Map.of() : taskRuleMapper.selectBatchIds(ruleIds).stream()
            .collect(Collectors.toMap(DepartmentTaskRule::getId, item -> item, (left, right) -> left));
        for (DepartmentTaskAssignment assignment : assignments) {
            if (assignment.getDeptId() == null || !activeMemberIds.contains(userId)) {
                continue;
            }
            DepartmentTaskRule rule = rules.get(assignment.getRuleId());
            if (rule == null || !ENABLED.equals(rule.getStatus())) {
                continue;
            }
            if (!isRuleEffective(rule, today)) {
                continue;
            }
            if (DAILY.equals(rule.getTaskType()) && !dailyCalendarService.isWorkday(rule.getDeptId(), userId, today)) {
                continue;
            }
            result.add(buildProgress(rule, assignment, userId, users.get(userId), today));
        }
        return result;
    }

    @Override
    public List<Long> queryDailyReportUserIds(LocalDate beginDate, LocalDate endDate) {
        if (beginDate == null || endDate == null || beginDate.isAfter(endDate)) {
            return Collections.emptyList();
        }
        return assignmentMapper.selectDailyWorkDays(currentDeptId(), beginDate, endDate).stream()
            .map(DepartmentTaskAssignment::getUserId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }

    @Override
    public List<DepartmentReviewRuleVo> queryReviewRuleList() {
        List<DepartmentReviewRule> rules = reviewRuleMapper.selectList(Wrappers.<DepartmentReviewRule>lambdaQuery()
            .eq(DepartmentReviewRule::getDeptId, currentDeptId())
            .eq(DepartmentReviewRule::getDelFlag, "0")
            .orderByAsc(DepartmentReviewRule::getTaskType));
        Map<Long, PersonUserOptionVo> users = userMap();
        return rules.stream().map(item -> toReviewVo(item, users)).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveReviewRule(DepartmentReviewRuleBo bo) {
        requireDept();
        if (!SCORE.equals(bo.getTaskType()) && !FIVE_WHY.equals(bo.getTaskType())) {
            throw new ServiceException("不支持的审核业务类型");
        }
        if (bo.getReviewerUserId() == null || assignmentMapper.countUserInDept(bo.getReviewerUserId(), currentDeptId()) == 0) {
            throw new ServiceException("主审核人不能为空且必须是当前科室有效成员");
        }
        if (bo.getBackupReviewerUserId() != null && assignmentMapper.countUserInDept(bo.getBackupReviewerUserId(), currentDeptId()) == 0) {
            throw new ServiceException("备用审核人必须是当前科室有效成员");
        }
        DepartmentReviewRule entity = bo.getId() == null ? new DepartmentReviewRule() : getReviewRule(bo.getId());
        if (entity.getId() == null) entity.setDeptId(currentDeptId());
        entity.setTaskType(bo.getTaskType());
        entity.setReviewerUserId(bo.getReviewerUserId());
        entity.setBackupReviewerUserId(bo.getBackupReviewerUserId());
        entity.setEffectiveStart(bo.getEffectiveStart());
        entity.setEffectiveEnd(bo.getEffectiveEnd());
        entity.setStatus(StringUtils.isBlank(bo.getStatus()) ? ENABLED : bo.getStatus());
        entity.setRemark(bo.getRemark());
        try {
            return entity.getId() == null ? reviewRuleMapper.insert(entity) > 0 : reviewRuleMapper.updateById(entity) > 0;
        } catch (Exception ex) {
            throw new ServiceException("该业务类型已经存在审核配置，请直接编辑原配置");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteReviewRule(Long id) {
        return reviewRuleMapper.deleteById(getReviewRule(id).getId()) > 0;
    }

    @Override
    public void checkReviewer(String taskType, Long deptId) {
        if (LoginHelper.isSuperAdmin()) return;
        DepartmentReviewRule rule = reviewRuleMapper.selectEnabledRule(deptId, taskType);
        if (rule == null) {
            throw new ServiceException("当前科室尚未配置" + taskTypeLabel(taskType) + "审核人");
        }
        Long userId = LoginHelper.getUserId();
        if (!Objects.equals(userId, rule.getReviewerUserId()) && !Objects.equals(userId, rule.getBackupReviewerUserId())) {
            throw new ServiceException("您不是当前" + taskTypeLabel(taskType) + "配置的审核人");
        }
    }

    /** 每30分钟检查一次到期前/逾期任务，消息会存入成员消息盒子并推送给在线用户。 */
    @Scheduled(cron = "0 0/30 * * * ?")
    public void remindTasks() {
        LocalDate today = LocalDate.now();
        Map<Long, PersonUserOptionVo> users = userMap();
        Map<Long, Set<Long>> activeMemberIdsByDept = new HashMap<>();
        for (DepartmentTaskRule rule : taskRuleMapper.selectEnabledRules()) {
            if (!isRuleEffective(rule, today)) continue;
            Set<Long> activeMemberIds = activeMemberIdsByDept.computeIfAbsent(
                rule.getDeptId(), deptId -> new HashSet<>(assignmentMapper.selectActiveUserIdsInDept(deptId))
            );
            for (DepartmentTaskAssignment assignment : assignmentMapper.selectList(Wrappers.<DepartmentTaskAssignment>lambdaQuery()
                .eq(DepartmentTaskAssignment::getRuleId, rule.getId())
                .eq(DepartmentTaskAssignment::getStatus, ENABLED)
                .eq(DepartmentTaskAssignment::getDelFlag, "0"))) {
                if (!isAssignmentEffective(assignment, today)) continue;
                if (!Objects.equals(assignment.getDeptId(), rule.getDeptId())
                    || !activeMemberIds.contains(assignment.getUserId())) continue;
                if (DAILY.equals(rule.getTaskType()) && !dailyCalendarService.isWorkday(rule.getDeptId(), assignment.getUserId(), today)) continue;
                DepartmentTaskProgressVo progress = buildProgress(rule, assignment, assignment.getUserId(), users.get(assignment.getUserId()), today);
                if ("COMPLETED".equals(progress.getStatus())) continue;
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime remindAt = DAILY.equals(rule.getTaskType())
                    ? progress.getDeadline()
                    : progress.getDeadline().minusHours(rule.getRemindHours() == null ? 24 : rule.getRemindHours());
                String type = now.isAfter(progress.getDeadline()) ? "OVERDUE" : "BEFORE";
                if (("BEFORE".equals(type) && now.isBefore(remindAt)) || taskMapper.insertReminderLog(IdGeneratorUtil.nextLongId(), rule.getId(), assignment.getUserId(), progress.getPeriodStart(), type) == 0) {
                    continue;
                }
                String name = users.get(assignment.getUserId()) == null ? "您" : users.get(assignment.getUserId()).getNickName();
                String message = DAILY.equals(rule.getTaskType())
                    ? name + "，请填写今日日报。" + ("OVERDUE".equals(type) ? "已超过今日提醒时间，请尽快补填。" : "请按时完成。")
                    : name + "，" + rule.getTaskName() + "本周期要求完成" + rule.getRequiredCount() + "次，当前完成" + progress.getCompletedCount() + "次。" + ("OVERDUE".equals(type) ? "已超过截止时间，请尽快补齐。" : "请按时完成。 ");
                PushPayloadDTO payload = PushPayloadDTO.of("MESSAGE", "BACKEND", message, null);
                payload.setPath("/department/task");
                messageService.publishMessage(List.of(assignment.getUserId()), payload);
            }
        }
    }

    private DepartmentTaskProgressVo buildProgress(DepartmentTaskRule rule, DepartmentTaskAssignment assignment, Long userId, PersonUserOptionVo user, LocalDate today) {
        boolean daily = DAILY.equals(rule.getTaskType());
        Period period = daily ? new Period(today, today) : periodOf(rule.getCycleType(), today);
        int required = daily ? 1 : (rule.getRequiredCount() == null ? 1 : rule.getRequiredCount());
        LocalDate deadlineDate = daily ? today : (rule.getDeadlineDay() == null || rule.getDeadlineDay() <= 0
            ? period.end() : period.start().plusDays(rule.getDeadlineDay() - 1L));
        if (deadlineDate.isAfter(period.end())) deadlineDate = period.end();
        LocalTime deadlineTime = daily
            ? (assignment.getReminderTime() == null ? LocalTime.of(18, 0) : assignment.getReminderTime())
            : (rule.getDeadlineTime() == null ? LocalTime.of(18, 0) : rule.getDeadlineTime());
        LocalDateTime deadline = LocalDateTime.of(deadlineDate, deadlineTime);
        DepartmentTaskInstance instance = ensureInstance(rule, assignment, userId, period, deadline, required);
        List<Long> sourceIds = sourceIds(rule, userId, period, today);
        syncCompletions(instance, rule.getTaskType(), sourceIds);
        int completed = sourceIds.size();
        instance.setCompletedCount(completed);
        String status = completed >= required ? "COMPLETED" : LocalDateTime.now().isAfter(deadline) ? "OVERDUE" : completed > 0 ? "IN_PROGRESS" : "NOT_STARTED";
        instance.setStatus(status);
        instance.setCompletedAt("COMPLETED".equals(status) ? LocalDateTime.now() : null);
        instanceMapper.updateById(instance);
        DepartmentTaskProgressVo vo = new DepartmentTaskProgressVo();
        vo.setRuleId(rule.getId());
        vo.setInstanceId(instance.getId());
        vo.setAssignmentId(assignment.getId());
        vo.setUserId(userId);
        vo.setUserName(user == null ? String.valueOf(userId) : (StringUtils.isBlank(user.getNickName()) ? user.getUserName() : user.getNickName()));
        vo.setTaskName(rule.getTaskName());
        vo.setTaskType(rule.getTaskType());
        vo.setCycleType(daily ? "DAY" : rule.getCycleType());
        vo.setPeriodStart(period.start());
        vo.setPeriodEnd(period.end());
        vo.setDeadline(deadline);
        vo.setRequiredCount(required);
        vo.setCompletedCount(completed);
        vo.setStatus(status);
        vo.setStatusLabel(switch (status) { case "COMPLETED" -> "已完成"; case "OVERDUE" -> "已逾期"; case "IN_PROGRESS" -> "进行中"; default -> "未开始"; });
        vo.setReminderText(daily ? "今日提醒 " + deadline.toLocalTime() : "截止 " + deadline.toLocalDate() + " " + deadline.toLocalTime());
        return vo;
    }

    private DepartmentTaskInstance ensureInstance(DepartmentTaskRule rule, DepartmentTaskAssignment assignment,
                                                  Long userId, Period period, LocalDateTime deadline, int required) {
        DepartmentTaskInstance instance = instanceMapper.selectActive(rule.getId(), userId, period.start());
        if (instance != null) {
            instance.setPeriodEnd(period.end());
            instance.setDeadline(deadline);
            instance.setRequiredCount(required);
            return instance;
        }
        instance = new DepartmentTaskInstance();
        instance.setRuleId(rule.getId());
        instance.setDeptId(rule.getDeptId());
        instance.setUserId(userId);
        instance.setPeriodStart(period.start());
        instance.setPeriodEnd(period.end());
        instance.setDeadline(deadline);
        instance.setRequiredCount(required);
        instance.setCompletedCount(0);
        instance.setStatus("NOT_STARTED");
        instance.setGeneratedAt(LocalDateTime.now());
        try {
            instanceMapper.insert(instance);
        } catch (Exception ex) {
            instance = instanceMapper.selectActive(rule.getId(), userId, period.start());
            if (instance == null) throw ex;
        }
        return instance;
    }

    private List<Long> sourceIds(DepartmentTaskRule rule, Long userId, Period period, LocalDate today) {
        LocalDate end = period.end().isAfter(today) ? today : period.end();
        boolean approved = "APPROVED".equals(rule.getCountMode());
        return switch (rule.getTaskType()) {
            case SCORE -> taskMapper.selectScoreIds(rule.getDeptId(), userId, period.start(), end, approved);
            case FIVE_WHY -> taskMapper.selectFiveWhyIds(rule.getDeptId(), userId, period.start(), end, approved);
            case DAILY -> taskMapper.selectDailyReportIds(rule.getDeptId(), userId, period.start(), end);
            default -> List.of();
        };
    }

    private void syncCompletions(DepartmentTaskInstance instance, String taskType, List<Long> sourceIds) {
        HashSet<Long> actual = new HashSet<>(sourceIds);
        HashSet<Long> existing = new HashSet<>(completionMapper.selectSourceIds(instance.getId()));
        if (actual.equals(existing)) return;
        completionMapper.deleteByInstanceId(instance.getId());
        for (Long sourceId : actual) {
            completionMapper.insertIgnore(IdGeneratorUtil.nextLongId(), instance.getId(), taskType, sourceId);
        }
    }

    private Period periodOf(String cycleType, LocalDate date) {
        if ("WEEK".equals(cycleType)) {
            LocalDate start = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            return new Period(start, start.plusDays(6));
        }
        if ("QUARTER".equals(cycleType)) {
            int month = ((date.getMonthValue() - 1) / 3) * 3 + 1;
            LocalDate start = LocalDate.of(date.getYear(), month, 1);
            return new Period(start, start.plusMonths(3).minusDays(1));
        }
        LocalDate start = date.with(TemporalAdjusters.firstDayOfMonth());
        return new Period(start, date.with(TemporalAdjusters.lastDayOfMonth()));
    }

    private boolean isRuleEffective(DepartmentTaskRule rule, LocalDate date) {
        return (rule.getEffectiveStart() == null || !date.isBefore(rule.getEffectiveStart()))
            && (rule.getEffectiveEnd() == null || !date.isAfter(rule.getEffectiveEnd()));
    }

    private boolean isAssignmentEffective(DepartmentTaskAssignment assignment, LocalDate date) {
        return (assignment.getEffectiveStart() == null || !date.isBefore(assignment.getEffectiveStart()))
            && (assignment.getEffectiveEnd() == null || !date.isAfter(assignment.getEffectiveEnd()));
    }

    private DepartmentTaskRuleVo toRuleVo(DepartmentTaskRule entity) {
        DepartmentTaskRuleVo vo = new DepartmentTaskRuleVo();
        vo.setId(entity.getId()); vo.setDeptId(entity.getDeptId()); vo.setTaskName(entity.getTaskName()); vo.setTaskType(entity.getTaskType());
        vo.setCycleType(entity.getCycleType()); vo.setRequiredCount(entity.getRequiredCount()); vo.setDeadlineDay(entity.getDeadlineDay());
        vo.setDeadlineTime(entity.getDeadlineTime()); vo.setCountMode(entity.getCountMode()); vo.setRemindHours(entity.getRemindHours());
        vo.setEffectiveStart(entity.getEffectiveStart()); vo.setEffectiveEnd(entity.getEffectiveEnd()); vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark()); vo.setAssignmentCount(taskRuleMapper.countAssignments(entity.getId()));
        return vo;
    }

    private DepartmentReviewRuleVo toReviewVo(DepartmentReviewRule entity, Map<Long, PersonUserOptionVo> users) {
        DepartmentReviewRuleVo vo = new DepartmentReviewRuleVo();
        vo.setId(entity.getId()); vo.setDeptId(entity.getDeptId()); vo.setTaskType(entity.getTaskType()); vo.setReviewerUserId(entity.getReviewerUserId());
        vo.setBackupReviewerUserId(entity.getBackupReviewerUserId()); vo.setEffectiveStart(entity.getEffectiveStart()); vo.setEffectiveEnd(entity.getEffectiveEnd());
        vo.setStatus(entity.getStatus()); vo.setRemark(entity.getRemark());
        PersonUserOptionVo reviewer = users.get(entity.getReviewerUserId());
        PersonUserOptionVo backup = users.get(entity.getBackupReviewerUserId());
        vo.setReviewerName(userLabel(reviewer, entity.getReviewerUserId())); vo.setBackupReviewerName(userLabel(backup, entity.getBackupReviewerUserId()));
        return vo;
    }

    private String userLabel(PersonUserOptionVo user, Long userId) {
        if (user == null) return userId == null ? "" : String.valueOf(userId);
        return StringUtils.isBlank(user.getNickName()) ? user.getUserName() : user.getNickName() + "（" + user.getUserName() + "）";
    }

    private Map<Long, PersonUserOptionVo> userMap() {
        List<PersonUserOptionVo> users = personProfileMapper.selectUserOptions(null, true);
        if (CollUtil.isEmpty(users)) return Collections.emptyMap();
        Map<Long, PersonUserOptionVo> map = new HashMap<>();
        users.forEach(item -> map.put(item.getUserId(), item));
        return map;
    }

    private String normalizeWorkDays(String value) {
        if (StringUtils.isBlank(value)) {
            return "1,2,3,4,5";
        }
        Set<Integer> days = Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(StringUtils::isNotBlank)
            .map(item -> {
                try {
                    return Integer.valueOf(item);
                } catch (NumberFormatException ex) {
                    return 0;
                }
            })
            .filter(item -> item >= 1 && item <= 7)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        if (days.isEmpty()) {
            throw new ServiceException("日报任务至少需要配置一个工作日");
        }
        return days.stream().sorted().map(String::valueOf).collect(Collectors.joining(","));
    }

    private DepartmentTaskRule getRule(Long id) {
        DepartmentTaskRule entity = taskRuleMapper.selectById(id);
        if (entity == null || !departmentAccessService.canViewEntityDept(entity.getDeptId(), "department:task:viewDept")) {
            throw new ServiceException("任务规则不存在或无权访问");
        }
        return entity;
    }

    private DepartmentTaskAssignment getAssignment(Long id) {
        DepartmentTaskAssignment entity = assignmentMapper.selectById(id);
        if (entity == null || !departmentAccessService.canViewEntityDept(entity.getDeptId(), "department:task:viewDept")) {
            throw new ServiceException("任务分配不存在或无权访问");
        }
        return entity;
    }

    private DepartmentReviewRule getReviewRule(Long id) {
        DepartmentReviewRule entity = reviewRuleMapper.selectById(id);
        if (entity == null || !departmentAccessService.canViewEntityDept(entity.getDeptId(), "department:task:viewDept")) {
            throw new ServiceException("审核配置不存在或无权访问");
        }
        return entity;
    }

    private void validateRule(DepartmentTaskRuleBo bo) {
        if (!List.of(SCORE, FIVE_WHY, DAILY).contains(bo.getTaskType())) throw new ServiceException("不支持的任务类型");
        if (DAILY.equals(bo.getTaskType())) {
            if (bo.getCycleType() != null && !List.of("DAY", "MONTH").contains(bo.getCycleType())) {
                throw new ServiceException("日报任务只能按工作日逐日执行");
            }
            return;
        }
        if (!List.of("WEEK", "MONTH", "QUARTER").contains(bo.getCycleType())) throw new ServiceException("不支持的周期类型");
        if (bo.getRequiredCount() == null || bo.getRequiredCount() < 1) throw new ServiceException("要求次数必须大于0");
        if (bo.getDeadlineDay() != null && bo.getDeadlineDay() < 0) throw new ServiceException("截止日序号不能小于0");
    }

    private void requireDept() {
        currentDeptId();
    }

    private Long currentDeptId() {
        return departmentAccessService.currentDeptId();
    }

    private String taskTypeLabel(String type) {
        return SCORE.equals(type) ? "SCORE提案" : FIVE_WHY.equals(type) ? "5WHY" : type;
    }

    private record Period(LocalDate start, LocalDate end) { }
}
