package org.dromara.department.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.department.domain.vo.DailyCalendarMemberVo;
import org.dromara.department.domain.vo.DailyReportVo;

import java.time.LocalDate;
import java.util.List;

/** 日报日历聚合查询数据层。 */
@Mapper
public interface DailyCalendarMapper {

    @Select({
        "<script>",
        "select u.user_id, u.user_name, u.nick_name, p.job_title",
        "from sys_user u",
        "left join dm_person_profile p on p.user_id = u.user_id and p.del_flag = '0'",
        "where u.del_flag = '0' and u.status = '0' and u.dept_id = #{deptId}",
        "and coalesce(p.daily_report_enabled, '1') = '1'",
        "<if test='userId != null'> and u.user_id = #{userId} </if>",
        "order by u.user_name, u.user_id",
        "</script>"
    })
    List<DailyCalendarMemberVo> selectMembers(@Param("deptId") Long deptId, @Param("userId") Long userId);

    @Select({
        "select r.id, r.report_date, r.user_id, r.dept_id, r.today_work, r.tomorrow_plan,",
        "r.coordination_note, r.status, r.source_type, r.leave_id,",
        "u.user_name, u.nick_name, d.dept_name",
        "from dm_daily_report r",
        "left join sys_user u on u.user_id = r.user_id and u.del_flag = '0'",
        "left join sys_dept d on d.dept_id = r.dept_id and d.del_flag = '0'",
        "where r.del_flag = '0' and r.dept_id = #{deptId}",
        "and r.report_date between #{beginDate} and #{endDate}",
        "order by r.report_date, r.user_id"
    })
    List<DailyReportVo> selectReports(@Param("deptId") Long deptId,
                                      @Param("beginDate") LocalDate beginDate,
                                      @Param("endDate") LocalDate endDate);
}
