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

    /**
     * 查询当前业务科室指定月份的完整工作日、休假和日报日历。
     *
     * @param month 月份中的任意日期
     * @return 月度日报日历
     */
    DailyCalendarVo queryCalendar(LocalDate month);

    /**
     * 查询当前日期的轻量日历数据，供首页看板使用。
     *
     * @return 当日日历数据
     */
    DailyCalendarVo queryTodayCalendar();

    /**
     * 查询当前业务科室日期范围内的工作日覆盖配置。
     *
     * @param beginDate 开始日期，包含当天
     * @param endDate   结束日期，包含当天
     * @return 日期覆盖配置列表
     */
    List<DailyCalendarOverrideVo> queryOverrides(LocalDate beginDate, LocalDate endDate);

    /**
     * 新增或修改工作日覆盖配置。
     *
     * @param bo 日期覆盖配置参数
     * @return 是否保存成功
     */
    Boolean saveOverride(DailyCalendarOverrideBo bo);

    /**
     * 删除工作日覆盖配置。
     *
     * @param ids 日期覆盖配置主键集合
     * @return 是否删除成功
     */
    Boolean deleteOverrides(Collection<Long> ids);

    /**
     * 查询当前业务科室日期范围内的成员休假记录。
     *
     * @param beginDate 开始日期，包含当天
     * @param endDate   结束日期，包含当天
     * @param userId    用户主键，为 {@code null} 时查询全科室
     * @return 休假记录列表
     */
    List<DailyLeaveVo> queryLeaves(LocalDate beginDate, LocalDate endDate, Long userId);

    /**
     * 新增成员休假记录。
     *
     * @param bo 休假新增参数
     * @return 是否新增成功
     */
    Boolean insertLeave(DailyLeaveBo bo);

    /**
     * 修改成员休假记录。
     *
     * @param bo 休假修改参数
     * @return 是否修改成功
     */
    Boolean updateLeave(DailyLeaveBo bo);

    /**
     * 删除成员休假记录。
     *
     * @param ids 休假记录主键集合
     * @return 是否删除成功
     */
    Boolean deleteLeaves(Collection<Long> ids);

    /**
     * 判断指定科室在指定日期是否为工作日。
     *
     * @param deptId 科室主键
     * @param date   待判断日期
     * @return 是否为工作日
     */
    boolean isWorkday(Long deptId, LocalDate date);

    /**
     * 判断指定成员所在科室在指定日期是否为工作日。
     *
     * @param deptId 科室主键
     * @param userId 用户主键
     * @param date   待判断日期
     * @return 是否为工作日
     */
    boolean isWorkday(Long deptId, Long userId, LocalDate date);

    /**
     * 判断指定成员在指定科室、指定日期是否被日报任务要求填报。
     *
     * @param deptId 科室主键
     * @param userId 用户主键
     * @param date   待判断日期
     * @return 是否需要填写日报
     */
    boolean isDailyReportRequired(Long deptId, Long userId, LocalDate date);
}
