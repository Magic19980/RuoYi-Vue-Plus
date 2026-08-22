package org.dromara.department.service;

import org.dromara.department.domain.bo.DailyCalendarConfigBo;
import org.dromara.department.domain.bo.DailyCalendarOverrideBo;
import org.dromara.department.domain.bo.DailyLeaveBo;
import org.dromara.department.domain.vo.DailyCalendarConfigVo;
import org.dromara.department.domain.vo.DailyCalendarOverrideVo;
import org.dromara.department.domain.vo.DailyCalendarVo;
import org.dromara.department.domain.vo.DailyLeaveVo;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/** 日报日历、工作日和休假业务接口。 */
public interface IDailyCalendarService {

    DailyCalendarVo queryCalendar(LocalDate month);

    DailyCalendarConfigVo queryConfig();

    DailyCalendarConfigVo saveConfig(DailyCalendarConfigBo bo);

    List<DailyCalendarOverrideVo> queryOverrides(LocalDate beginDate, LocalDate endDate);

    Boolean saveOverride(DailyCalendarOverrideBo bo);

    Boolean deleteOverrides(Collection<Long> ids);

    List<DailyLeaveVo> queryLeaves(LocalDate beginDate, LocalDate endDate, Long userId);

    Boolean insertLeave(DailyLeaveBo bo);

    Boolean updateLeave(DailyLeaveBo bo);

    Boolean deleteLeaves(Collection<Long> ids);

    boolean isWorkday(Long deptId, LocalDate date);

    boolean isWorkday(Long deptId, Long userId, LocalDate date);
}
