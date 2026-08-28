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
        "select distinct u.user_id, u.user_name, u.nick_name, post.job_title, source_dept.dept_name as source_dept_name,",
        "p.join_date, p.leave_date",
        "from sys_user u",
        "join dm_person_profile p on p.user_id = u.user_id and p.create_dept = #{deptId} and p.del_flag = '0'",
        "left join (select sup.user_id, group_concat(distinct sp.post_name order by sp.post_sort, sp.post_id separator '、') as job_title "
            + "from sys_user_post sup join sys_post sp on sp.post_id = sup.post_id and sp.status = '0' and sp.del_flag = '0' "
            + "group by sup.user_id) post on post.user_id = u.user_id",
        "left join sys_dept source_dept on source_dept.dept_id = u.dept_id and source_dept.del_flag = '0'",
        "where u.del_flag = '0' and u.status = '0'",
        "and p.join_date &lt;= #{endDate} and (p.leave_date is null or p.leave_date &gt; #{beginDate})",
        "<if test='userId != null'> and u.user_id = #{userId} </if>",
        "order by u.user_name, u.user_id",
        "</script>"
    })
    List<DailyCalendarMemberVo> selectMembers(@Param("deptId") Long deptId, @Param("userId") Long userId,
                                               @Param("beginDate") LocalDate beginDate, @Param("endDate") LocalDate endDate);

    @Select({
        "<script>",
        "select r.id, r.report_date, r.user_id, r.dept_id, r.today_work, r.tomorrow_plan,",
        "r.coordination_note, r.status, r.source_type, r.leave_id,",
        "u.user_name, u.nick_name, d.dept_name",
        "from dm_daily_report r",
        "left join sys_user u on u.user_id = r.user_id and u.del_flag = '0'",
        "left join sys_dept d on d.dept_id = r.dept_id and d.del_flag = '0'",
        "where r.del_flag = '0' and r.dept_id = #{deptId}",
        "and r.report_date between #{beginDate} and #{endDate}",
        "<if test='userId != null'> and r.user_id = #{userId} </if>",
        "order by r.report_date, r.user_id",
        "</script>"
    })
    List<DailyReportVo> selectReports(@Param("deptId") Long deptId,
                                      @Param("userId") Long userId,
                                      @Param("beginDate") LocalDate beginDate,
                                      @Param("endDate") LocalDate endDate);
}
