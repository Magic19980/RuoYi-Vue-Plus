package org.dromara.department.service;

import org.dromara.department.domain.bo.DailyCalendarOverrideBo;
import org.dromara.department.domain.bo.DailyLeaveBo;
import org.dromara.department.domain.vo.DailyCalendarOverrideVo;
import org.dromara.department.domain.vo.DailyCalendarVo;
import org.dromara.department.domain.vo.DailyLeaveVo;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/** 日报日历、工作日和休假业务接口。 */
public interface IDailyCalendarService {

    DailyCalendarVo queryCalendar(LocalDate month);

    List<DailyCalendarOverrideVo> queryOverrides(LocalDate beginDate, LocalDate endDate);

    Boolean saveOverride(DailyCalendarOverrideBo bo);

    Boolean deleteOverrides(Collection<Long> ids);

    List<DailyLeaveVo> queryLeaves(LocalDate beginDate, LocalDate endDate, Long userId);

    Boolean insertLeave(DailyLeaveBo bo);

    Boolean updateLeave(DailyLeaveBo bo);

    Boolean deleteLeaves(Collection<Long> ids);

    boolean isWorkday(Long deptId, LocalDate date);

    boolean isWorkday(Long deptId, Long userId, LocalDate date);

    /** 判断指定成员在指定科室、指定日期是否被日报任务要求填报。 */
    boolean isDailyReportRequired(Long deptId, Long userId, LocalDate date);
}
