package org.dromara.department.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.department.domain.DailyCalendarOverride;
import org.dromara.department.domain.DailyLeave;
import org.dromara.department.domain.DailyReport;
import org.dromara.department.domain.DailyReportStatus;
import org.dromara.department.domain.DepartmentTaskAssignment;
import org.dromara.department.domain.bo.DailyCalendarOverrideBo;
import org.dromara.department.domain.bo.DailyLeaveBo;
import org.dromara.department.domain.vo.DailyCalendarCellVo;
import org.dromara.department.domain.vo.DailyCalendarDayVo;
import org.dromara.department.domain.vo.DailyCalendarMemberVo;
import org.dromara.department.domain.vo.DailyCalendarOverrideVo;
import org.dromara.department.domain.vo.DailyCalendarVo;
import org.dromara.department.domain.vo.DailyLeaveVo;
import org.dromara.department.domain.vo.DailyReportVo;
import org.dromara.department.mapper.DailyCalendarMapper;
import org.dromara.department.mapper.DailyCalendarOverrideMapper;
import org.dromara.department.mapper.DailyLeaveMapper;
import org.dromara.department.mapper.DailyReportMapper;
import org.dromara.department.mapper.DepartmentTaskAssignmentMapper;
import org.dromara.department.service.DepartmentAccessService;
import org.dromara.department.service.IDailyCalendarService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** 日报日历、工作日和休假业务实现。 */
@RequiredArgsConstructor
@Service
public class DailyCalendarServiceImpl implements IDailyCalendarService {

    private static final String DEPT_VIEW_PERMISSION = "department:dailyReport:viewDept";
    private static final String WORKDAY = "WORKDAY";
    private static final String REST = "REST";
    private static final String LEAVE_SOURCE = "LEAVE";
    private static final String DEFAULT_WORK_DAYS = "1,2,3,4,5";

    private final DailyCalendarOverrideMapper overrideMapper;
    private final DailyLeaveMapper leaveMapper;
    private final DailyCalendarMapper calendarMapper;
    private final DailyReportMapper dailyReportMapper;
    private final DepartmentTaskAssignmentMapper assignmentMapper;
    private final DepartmentAccessService departmentAccessService;

    @Override
    public DailyCalendarVo queryCalendar(LocalDate month) {
        Long deptId = requireDeptId();
        LocalDate firstDay = (month == null ? LocalDate.now() : month).withDayOfMonth(1);
        LocalDate monthEnd = firstDay.withDayOfMonth(firstDay.lengthOfMonth());
        LocalDate today = LocalDate.now();
        if (firstDay.isAfter(today)) {
            DailyCalendarVo futureResult = new DailyCalendarVo();
            futureResult.setMonth(firstDay);
            futureResult.setBeginDate(firstDay);
            futureResult.setEndDate(firstDay);
            futureResult.setWorkDays(DEFAULT_WORK_DAYS);
            futureResult.setDays(List.of());
            futureResult.setMembers(List.of());
            futureResult.setRequiredCount(0);
            futureResult.setFilledCount(0);
            futureResult.setMissingCount(0);
            futureResult.setLeaveCount(0);
            futureResult.setFutureMonth(true);
            return futureResult;
        }
        LocalDate lastDay = monthEnd.isAfter(today) ? today : monthEnd;
        Long userId = canViewDepartment() ? null : LoginHelper.getUserId();

        String workDays = DEFAULT_WORK_DAYS;
        Set<Integer> defaultWorkDays = parseWorkDays(workDays);
        Map<Long, Set<Integer>> personalWorkDays = new HashMap<>();
        List<DailyCalendarOverride> overrideRows = overrideMapper.selectList(Wrappers.<DailyCalendarOverride>lambdaQuery()
                .eq(DailyCalendarOverride::getDeptId, deptId)
                .between(DailyCalendarOverride::getCalendarDate, firstDay, lastDay)
                .orderByAsc(DailyCalendarOverride::getCalendarDate)
                .orderByAsc(DailyCalendarOverride::getUserId));
        Map<LocalDate, DailyCalendarOverride> globalRestOverrides = overrideRows.stream()
            .filter(item -> item.getUserId() == null && REST.equals(item.getDayType()))
            .collect(Collectors.toMap(DailyCalendarOverride::getCalendarDate, item -> item, (left, right) -> right));
        Set<String> userWorkdayOverrides = overrideRows.stream()
            .filter(item -> item.getUserId() != null && WORKDAY.equals(item.getDayType()))
            .map(item -> key(item.getUserId(), item.getCalendarDate()))
            .collect(Collectors.toSet());
        List<DailyCalendarDayVo> days = buildDays(firstDay, lastDay, workDays, globalRestOverrides);

        List<DailyCalendarMemberVo> rawMembers = calendarMapper.selectMembers(deptId, userId, firstDay, lastDay);
        Map<Long, List<ServicePeriod>> memberServicePeriods = new HashMap<>();
        List<DailyCalendarMemberVo> members = mergeCalendarMembers(rawMembers, memberServicePeriods);
        assignmentMapper.selectDailyWorkDays(deptId, firstDay, lastDay).stream()
            .filter(item -> item.getUserId() != null)
            .forEach(item -> personalWorkDays.computeIfAbsent(item.getUserId(), ignored -> new HashSet<>())
                .addAll(StringUtils.isBlank(item.getWorkDays())
                    ? defaultWorkDays
                    : parseWorkDays(normalizeWorkDays(item.getWorkDays()))));
        List<DailyReportVo> reports = calendarMapper.selectReports(deptId, firstDay, lastDay);
        Map<String, DailyReportVo> reportMap = reports.stream().collect(Collectors.toMap(
            report -> key(report.getUserId(), report.getReportDate()), report -> report, (left, right) -> right));
        List<DailyLeaveVo> leaves = leaveMapper.selectCalendarLeaves(deptId, firstDay, lastDay, userId);
        Map<String, DailyLeaveVo> leaveMap = new HashMap<>();
        for (DailyLeaveVo leave : leaves) {
            for (LocalDate date = leave.getStartDate(); !date.isAfter(leave.getEndDate()); date = date.plusDays(1)) {
                if (!date.isBefore(firstDay) && !date.isAfter(lastDay)) {
                    leaveMap.put(key(leave.getUserId(), date), leave);
                }
            }
        }

        int requiredCount = 0;
        int filledCount = 0;
        int missingCount = 0;
        int leaveCount = 0;
        for (DailyCalendarMemberVo member : members) {
            List<DailyCalendarCellVo> cells = new ArrayList<>();
            for (DailyCalendarDayVo day : days) {
                DailyReportVo report = reportMap.get(key(member.getUserId(), day.getDate()));
                DailyLeaveVo leave = leaveMap.get(key(member.getUserId(), day.getDate()));
                DailyCalendarCellVo cell = new DailyCalendarCellVo();
                cell.setDate(day.getDate());
                boolean inServicePeriod = isInServicePeriod(
                    memberServicePeriods.get(member.getUserId()), day.getDate());
                boolean hasTaskWorkDays = personalWorkDays.containsKey(member.getUserId());
                Set<Integer> memberWorkDays = hasTaskWorkDays
                    ? personalWorkDays.get(member.getUserId()) : Set.of();
                boolean personalWorkday = hasTaskWorkDays
                    && userWorkdayOverrides.contains(key(member.getUserId(), day.getDate()));
                boolean memberBaseWorkday = !Boolean.TRUE.equals(day.getDepartmentRest())
                    && memberWorkDays.contains(day.getDate().getDayOfWeek().getValue());
                boolean memberWorkday = inServicePeriod && hasTaskWorkDays && (personalWorkday || memberBaseWorkday);
                cell.setWorkday(memberWorkday);
                cell.setDayType(memberWorkday ? WORKDAY : REST);
                boolean changesDefault = hasTaskWorkDays
                    && memberWorkDays.contains(day.getDate().getDayOfWeek().getValue()) != defaultWorkDays.contains(day.getDate().getDayOfWeek().getValue());
                cell.setLabel(!hasTaskWorkDays ? "未分配日报任务" : personalWorkday ? "个人调休上班"
                    : changesDefault ? (memberWorkday ? "任务工作日" : "任务休息日") : day.getLabel());
                cell.setReportId(report == null ? null : report.getId());
                cell.setSourceType(report == null ? null : report.getSourceType());
                cell.setTodayWork(report == null ? null : report.getTodayWork());
                cell.setTomorrowPlan(report == null ? null : report.getTomorrowPlan());
                cell.setCoordinationNote(report == null ? null : report.getCoordinationNote());
                cell.setLeaveId(leave == null ? null : leave.getId());
                cell.setLeaveType(leave == null ? null : leave.getLeaveType());
                boolean leaveAuto = hasTaskWorkDays && leave != null
                    && (report == null || LEAVE_SOURCE.equals(report.getSourceType()));
                if (leaveAuto) {
                    cell.setState("LEAVE");
                    if (memberWorkday) {
                        leaveCount++;
                    }
                } else if (report != null) {
                    cell.setState("FILLED");
                } else if (memberWorkday) {
                    cell.setState("MISSING");
                } else {
                    cell.setState("REST");
                }
                if (memberWorkday) {
                    requiredCount++;
                    if ("MISSING".equals(cell.getState())) {
                        missingCount++;
                    } else {
                        filledCount++;
                    }
                }
                cells.add(cell);
            }
            member.setCells(cells);
        }

        DailyCalendarVo result = new DailyCalendarVo();
        result.setMonth(firstDay);
        result.setBeginDate(firstDay);
        result.setEndDate(lastDay);
        result.setFutureMonth(false);
        result.setWorkDays(workDays);
        result.setDays(days);
        result.setMembers(members);
        result.setRequiredCount(requiredCount);
        result.setFilledCount(filledCount);
        result.setMissingCount(missingCount);
        result.setLeaveCount(leaveCount);
        return result;
    }

    /**
     * 日历按人员展示，而不是按人员服务区间展示。
     *
     * <p>同一人员在同一科室可能存在“移除后重新加入”的多个服务区间，区间历史必须保留，
     * 但日历中只能显示一行人员。这里仅合并展示行，日报是否应填仍按所有服务区间的并集计算，
     * 因此不会把重新加入前的空档期误判为在岗。</p>
     */
    private List<DailyCalendarMemberVo> mergeCalendarMembers(
        List<DailyCalendarMemberVo> rawMembers,
        Map<Long, List<ServicePeriod>> memberServicePeriods
    ) {
        Map<Long, DailyCalendarMemberVo> merged = new LinkedHashMap<>();
        for (DailyCalendarMemberVo raw : rawMembers) {
            if (raw.getUserId() == null) {
                continue;
            }
            memberServicePeriods.computeIfAbsent(raw.getUserId(), ignored -> new ArrayList<>())
                .add(new ServicePeriod(raw.getJoinDate(), raw.getLeaveDate()));

            DailyCalendarMemberVo member = merged.get(raw.getUserId());
            if (member == null) {
                member = new DailyCalendarMemberVo();
                member.setUserId(raw.getUserId());
                member.setUserName(raw.getUserName());
                member.setNickName(raw.getNickName());
                member.setJobTitle(raw.getJobTitle());
                member.setSourceDeptName(raw.getSourceDeptName());
                member.setJoinDate(raw.getJoinDate());
                member.setLeaveDate(raw.getLeaveDate());
                merged.put(raw.getUserId(), member);
                continue;
            }
            // 仅用于人员行的摘要展示；实际日期判断使用 memberServicePeriods。
            if (raw.getJoinDate() != null
                && (member.getJoinDate() == null || raw.getJoinDate().isBefore(member.getJoinDate()))) {
                member.setJoinDate(raw.getJoinDate());
            }
            if (member.getLeaveDate() != null
                && (raw.getLeaveDate() == null || raw.getLeaveDate().isAfter(member.getLeaveDate()))) {
                member.setLeaveDate(raw.getLeaveDate());
            }
            if (org.dromara.common.core.utils.StringUtils.isBlank(member.getJobTitle())
                && org.dromara.common.core.utils.StringUtils.isNotBlank(raw.getJobTitle())) {
                member.setJobTitle(raw.getJobTitle());
            }
            if (org.dromara.common.core.utils.StringUtils.isBlank(member.getSourceDeptName())
                && org.dromara.common.core.utils.StringUtils.isNotBlank(raw.getSourceDeptName())) {
                member.setSourceDeptName(raw.getSourceDeptName());
            }
        }
        return new ArrayList<>(merged.values());
    }

    private boolean isInServicePeriod(List<ServicePeriod> periods, LocalDate date) {
        if (periods == null || periods.isEmpty() || date == null) {
            return false;
        }
        return periods.stream().anyMatch(period -> {
            LocalDate start = period.joinDate() == null ? LocalDate.MIN : period.joinDate();
            LocalDate endExclusive = period.leaveDate() == null ? LocalDate.MAX : period.leaveDate();
            return !date.isBefore(start) && date.isBefore(endExclusive);
        });
    }

    private record ServicePeriod(LocalDate joinDate, LocalDate leaveDate) {
    }

    @Override
    public List<DailyCalendarOverrideVo> queryOverrides(LocalDate beginDate, LocalDate endDate) {
        Long deptId = requireDeptId();
        LocalDate begin = beginDate == null ? LocalDate.now().withDayOfMonth(1) : beginDate;
        LocalDate end = endDate == null ? begin.withDayOfMonth(begin.lengthOfMonth()) : endDate;
        var query = Wrappers.<DailyCalendarOverride>lambdaQuery()
                .eq(DailyCalendarOverride::getDeptId, deptId)
                .between(DailyCalendarOverride::getCalendarDate, begin, end)
                .orderByAsc(DailyCalendarOverride::getCalendarDate);
        if (!canViewDepartment()) {
            query.and(wrapper -> wrapper.isNull(DailyCalendarOverride::getUserId)
                .or().eq(DailyCalendarOverride::getUserId, LoginHelper.getUserId()));
        }
        return overrideMapper.selectList(query)
            .stream().map(this::toOverrideVo).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveOverride(DailyCalendarOverrideBo bo) {
        Long deptId = requireDeptId();
        if (!WORKDAY.equals(bo.getDayType()) && !REST.equals(bo.getDayType())) {
            throw new ServiceException("日期类型只能选择调休上班或休息日");
        }
        Long targetUserId = WORKDAY.equals(bo.getDayType()) ? bo.getUserId() : null;
        if (WORKDAY.equals(bo.getDayType()) && targetUserId == null) {
            throw new ServiceException("调休上班必须选择具体人员");
        }
        if (!canViewDepartment()) {
            if (REST.equals(bo.getDayType())) {
                throw new ServiceException("只有科室管理员可以维护全科室休息日");
            }
            if (!Objects.equals(targetUserId, LoginHelper.getUserId())) {
                throw new ServiceException("只能维护本人的调休上班安排");
            }
        }
        if (targetUserId != null) {
            if (dailyReportMapper.countMemberInDeptAt(targetUserId, deptId, bo.getCalendarDate()) == 0) {
                throw new ServiceException("调休人员必须已纳入当前科室日报");
            }
        }
        DailyCalendarOverride entity = bo.getId() == null ? null : overrideMapper.selectById(bo.getId());
        if (entity != null && !Objects.equals(entity.getDeptId(), deptId)) {
            throw new ServiceException("不能维护其他科室的日期规则");
        }
        DailyCalendarOverride sameDate = overrideMapper.selectOne(Wrappers.<DailyCalendarOverride>lambdaQuery()
            .eq(DailyCalendarOverride::getDeptId, deptId)
            .eq(DailyCalendarOverride::getCalendarDate, bo.getCalendarDate())
            .apply(targetUserId == null ? "user_id is null" : "user_id = {0}", targetUserId));
        if (sameDate != null && (entity == null || !Objects.equals(sameDate.getId(), entity.getId()))) {
            entity = sameDate;
        }
        if (entity == null) {
            entity = new DailyCalendarOverride();
            entity.setDeptId(deptId);
        }
        entity.setCalendarDate(bo.getCalendarDate());
        entity.setDayType(bo.getDayType());
        entity.setUserId(targetUserId);
        entity.setRemark(StringUtils.trim(bo.getRemark()));
        if (entity.getId() == null) {
            overrideMapper.insert(entity);
        } else {
            overrideMapper.updateById(entity);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteOverrides(Collection<Long> ids) {
        for (Long id : ids) {
            DailyCalendarOverride entity = overrideMapper.selectById(id);
            if (entity != null && !Objects.equals(entity.getDeptId(), requireDeptId())) {
                throw new ServiceException("不能删除其他科室的日期规则");
            }
            if (entity != null && !canViewDepartment()
                && !Objects.equals(entity.getUserId(), LoginHelper.getUserId())) {
                throw new ServiceException("只能删除本人的调休上班安排");
            }
        }
        return overrideMapper.deleteByIds(ids) > 0;
    }

    @Override
    public List<DailyLeaveVo> queryLeaves(LocalDate beginDate, LocalDate endDate, Long userId) {
        Long deptId = requireDeptId();
        Long targetUserId = canViewDepartment() ? userId : LoginHelper.getUserId();
        return leaveMapper.selectCalendarLeaves(deptId, beginDate, endDate, targetUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertLeave(DailyLeaveBo bo) {
        assertLeaveUser(bo.getUserId(), bo.getStartDate());
        DailyLeave entity = new DailyLeave();
        entity.setDeptId(requireDeptId());
        entity.setUserId(resolveLeaveUser(bo.getUserId()));
        fillLeave(entity, bo);
        ensureNoOverlap(entity, null);
        leaveMapper.insert(entity);
        syncLeaveReports(entity);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateLeave(DailyLeaveBo bo) {
        DailyLeave entity = getLeaveAccessible(bo.getId());
        assertLeaveUser(bo.getUserId(), bo.getStartDate());
        Long oldUserId = entity.getUserId();
        Long targetUserId = resolveLeaveUser(bo.getUserId());
        entity.setUserId(targetUserId);
        fillLeave(entity, bo);
        ensureNoOverlap(entity, entity.getId());
        if (!Objects.equals(oldUserId, targetUserId)) {
            dailyReportMapper.delete(Wrappers.<DailyReport>lambdaQuery().eq(DailyReport::getLeaveId, entity.getId()));
        }
        leaveMapper.updateById(entity);
        dailyReportMapper.delete(Wrappers.<DailyReport>lambdaQuery().eq(DailyReport::getLeaveId, entity.getId())
            .and(wrapper -> wrapper.lt(DailyReport::getReportDate, entity.getStartDate())
                .or().gt(DailyReport::getReportDate, entity.getEndDate())));
        syncLeaveReports(entity);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteLeaves(Collection<Long> ids) {
        for (Long id : ids) {
            getLeaveAccessible(id);
        }
        dailyReportMapper.delete(Wrappers.<DailyReport>lambdaQuery().in(DailyReport::getLeaveId, ids));
        return leaveMapper.deleteByIds(ids) > 0;
    }

    @Override
    public boolean isWorkday(Long deptId, LocalDate date) {
        return isWorkday(deptId, null, date);
    }

    @Override
    public boolean isWorkday(Long deptId, Long userId, LocalDate date) {
        if (deptId == null || date == null) {
            return false;
        }
        if (userId != null && assignmentMapper.countDailyReportRequired(userId, deptId, date) == 0) {
            return false;
        }
        if (userId != null && overrideMapper.selectCount(Wrappers.<DailyCalendarOverride>lambdaQuery()
            .eq(DailyCalendarOverride::getDeptId, deptId)
            .eq(DailyCalendarOverride::getCalendarDate, date)
            .eq(DailyCalendarOverride::getUserId, userId)
            .eq(DailyCalendarOverride::getDayType, WORKDAY)) > 0) {
            return true;
        }
        DailyCalendarOverride globalRest = overrideMapper.selectOne(Wrappers.<DailyCalendarOverride>lambdaQuery()
            .eq(DailyCalendarOverride::getDeptId, deptId)
            .eq(DailyCalendarOverride::getCalendarDate, date)
            .eq(DailyCalendarOverride::getDayType, REST)
            .isNull(DailyCalendarOverride::getUserId));
        if (globalRest != null) {
            return false;
        }
        if (userId != null) {
            Set<Integer> taskWorkDays = assignmentMapper.selectUserDailyWorkDays(userId, deptId, date).stream()
                .filter(StringUtils::isNotBlank)
                .flatMap(workDays -> parseWorkDays(normalizeWorkDays(workDays)).stream())
                .collect(Collectors.toSet());
            if (!taskWorkDays.isEmpty()) {
                return taskWorkDays.contains(date.getDayOfWeek().getValue());
            }
        }
        return parseWorkDays(DEFAULT_WORK_DAYS).contains(date.getDayOfWeek().getValue());
    }

    @Override
    public boolean isDailyReportRequired(Long deptId, Long userId, LocalDate date) {
        return deptId != null && userId != null && date != null
            && assignmentMapper.countDailyReportRequired(userId, deptId, date) > 0;
    }

    private void syncLeaveReports(DailyLeave leave) {
        for (LocalDate date = leave.getStartDate(); !date.isAfter(leave.getEndDate()); date = date.plusDays(1)) {
            if (!isDailyReportRequired(leave.getDeptId(), leave.getUserId(), date)
                || !isWorkday(leave.getDeptId(), leave.getUserId(), date)) {
                continue;
            }
            DailyReport entity = dailyReportMapper.selectOne(Wrappers.<DailyReport>lambdaQuery()
                .eq(DailyReport::getReportDate, date).eq(DailyReport::getUserId, leave.getUserId())
                .eq(DailyReport::getDeptId, leave.getDeptId()));
            if (entity == null) {
                entity = new DailyReport();
                entity.setReportDate(date);
                entity.setUserId(leave.getUserId());
                entity.setDeptId(leave.getDeptId());
                entity.setTodayWork("休假");
                entity.setTomorrowPlan("");
                entity.setCoordinationNote(StringUtils.isBlank(leave.getReason()) ? null : leave.getReason());
                entity.setStatus(DailyReportStatus.SUBMITTED);
                entity.setSourceType(LEAVE_SOURCE);
                entity.setLeaveId(leave.getId());
                dailyReportMapper.insert(entity);
            } else if (LEAVE_SOURCE.equals(entity.getSourceType()) &&
                (entity.getLeaveId() == null || Objects.equals(entity.getLeaveId(), leave.getId()))) {
                entity.setTodayWork("休假");
                entity.setCoordinationNote(StringUtils.isBlank(leave.getReason()) ? null : leave.getReason());
                entity.setLeaveId(leave.getId());
                dailyReportMapper.updateById(entity);
            }
        }
    }

    private void fillLeave(DailyLeave entity, DailyLeaveBo bo) {
        if (bo.getStartDate().isAfter(bo.getEndDate())) {
            throw new ServiceException("休假开始日期不能晚于结束日期");
        }
        entity.setStartDate(bo.getStartDate());
        entity.setEndDate(bo.getEndDate());
        entity.setLeaveType(StringUtils.isBlank(bo.getLeaveType()) ? "休假" : bo.getLeaveType().trim());
        entity.setReason(StringUtils.trim(bo.getReason()));
        entity.setStatus("ENABLED");
    }

    private void ensureNoOverlap(DailyLeave leave, Long excludeId) {
        var wrapper = Wrappers.<DailyLeave>lambdaQuery()
            .eq(DailyLeave::getDeptId, leave.getDeptId())
            .eq(DailyLeave::getUserId, leave.getUserId())
            .eq(DailyLeave::getStatus, "ENABLED")
            .le(DailyLeave::getStartDate, leave.getEndDate())
            .ge(DailyLeave::getEndDate, leave.getStartDate());
        if (excludeId != null) {
            wrapper.ne(DailyLeave::getId, excludeId);
        }
        if (leaveMapper.selectCount(wrapper) > 0) {
            throw new ServiceException("同一人员的休假日期存在重叠，请合并后再保存");
        }
    }

    private DailyLeave getLeaveAccessible(Long id) {
        DailyLeave entity = leaveMapper.selectById(id);
        if (entity == null || !Objects.equals(entity.getDeptId(), requireDeptId())) {
            throw new ServiceException("休假记录不存在或无权访问");
        }
        if (!canViewDepartment() && !Objects.equals(entity.getUserId(), LoginHelper.getUserId())) {
            throw new ServiceException("只能维护本人的休假安排");
        }
        return entity;
    }

    private void assertLeaveUser(Long userId, LocalDate effectiveDate) {
        Long target = resolveLeaveUser(userId);
        if (!canViewDepartment() && !Objects.equals(target, LoginHelper.getUserId())) {
            throw new ServiceException("只能维护本人的休假安排");
        }
        Long currentDept = requireDeptId();
        if (dailyReportMapper.countMemberInDeptAt(target, currentDept,
            effectiveDate == null ? LocalDate.now() : effectiveDate) == 0) {
            throw new ServiceException("休假人员必须已纳入当前科室日报");
        }
    }

    private Long resolveLeaveUser(Long userId) {
        return canViewDepartment() ? userId : LoginHelper.getUserId();
    }

    private List<DailyCalendarDayVo> buildDays(LocalDate begin, LocalDate end, String workDays,
                                                Map<LocalDate, DailyCalendarOverride> overrides) {
        Set<Integer> defaultWorkDays = parseWorkDays(workDays);
        List<DailyCalendarDayVo> days = new ArrayList<>();
        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            DailyCalendarOverride override = overrides.get(date);
            boolean workday = override == null || !REST.equals(override.getDayType())
                ? defaultWorkDays.contains(date.getDayOfWeek().getValue()) : false;
            DailyCalendarDayVo day = new DailyCalendarDayVo();
            day.setDate(date);
            day.setDayOfWeek(date.getDayOfWeek().getValue());
            day.setWeekLabel(weekLabel(date.getDayOfWeek()));
            day.setDepartmentRest(override != null);
            day.setWorkday(workday);
            day.setDayType(workday ? WORKDAY : REST);
            day.setLabel(override == null ? (workday ? "工作日" : "休息日") : "休息日");
            day.setRemark(override == null ? null : override.getRemark());
            days.add(day);
        }
        return days;
    }

    private DailyCalendarOverrideVo toOverrideVo(DailyCalendarOverride entity) {
        DailyCalendarOverrideVo vo = new DailyCalendarOverrideVo();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setCalendarDate(entity.getCalendarDate());
        vo.setDayType(entity.getDayType());
        vo.setRemark(entity.getRemark());
        return vo;
    }

    private String normalizeWorkDays(String value) {
        Set<Integer> days = parseWorkDays(value);
        if (days.isEmpty()) {
            throw new ServiceException("至少选择一个工作日");
        }
        return days.stream().sorted().map(String::valueOf).collect(Collectors.joining(","));
    }

    private Set<Integer> parseWorkDays(String value) {
        Set<Integer> result = new HashSet<>();
        if (StringUtils.isBlank(value)) {
            return result;
        }
        for (String item : value.split(",")) {
            try {
                int day = Integer.parseInt(item.trim());
                if (day >= 1 && day <= 7) {
                    result.add(day);
                }
            } catch (NumberFormatException ignored) {
                // 忽略非法值，最终由空集合校验给出明确提示。
            }
        }
        return result;
    }

    private String weekLabel(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "一";
            case TUESDAY -> "二";
            case WEDNESDAY -> "三";
            case THURSDAY -> "四";
            case FRIDAY -> "五";
            case SATURDAY -> "六";
            case SUNDAY -> "日";
        };
    }

    private String key(Long userId, LocalDate date) {
        return userId + "_" + date;
    }

    private Long requireDeptId() {
        Long deptId = departmentAccessService.currentDeptId();
        if (deptId == null) {
            throw new ServiceException("当前登录用户缺少部门信息");
        }
        return deptId;
    }

    private boolean canViewDepartment() {
        return departmentAccessService.canViewCurrentDepartment(DEPT_VIEW_PERMISSION);
    }
}
