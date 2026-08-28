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

    /** 查询当前业务科室指定月份的完整工作日、休假和日报日历。 */
    DailyCalendarVo queryCalendar(LocalDate month);

    /** 查询当前日期的轻量日历数据，供首页看板使用。 */
    DailyCalendarVo queryTodayCalendar();

    /** 查询当前业务科室日期范围内的工作日覆盖配置。 */
    List<DailyCalendarOverrideVo> queryOverrides(LocalDate beginDate, LocalDate endDate);

    /** 新增或修改工作日覆盖配置。 */
    Boolean saveOverride(DailyCalendarOverrideBo bo);

    /** 删除工作日覆盖配置。 */
    Boolean deleteOverrides(Collection<Long> ids);

    /** 查询当前业务科室日期范围内的成员休假记录。 */
    List<DailyLeaveVo> queryLeaves(LocalDate beginDate, LocalDate endDate, Long userId);

    /** 新增成员休假记录。 */
    Boolean insertLeave(DailyLeaveBo bo);

    /** 修改成员休假记录。 */
    Boolean updateLeave(DailyLeaveBo bo);

    /** 删除成员休假记录。 */
    Boolean deleteLeaves(Collection<Long> ids);

    /** 判断指定科室在指定日期是否为工作日。 */
    boolean isWorkday(Long deptId, LocalDate date);

    /** 判断指定成员所在科室在指定日期是否为工作日。 */
    boolean isWorkday(Long deptId, Long userId, LocalDate date);

    /** 判断指定成员在指定科室、指定日期是否被日报任务要求填报。 */
    boolean isDailyReportRequired(Long deptId, Long userId, LocalDate date);
}
