package org.dromara.department.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/** 周期任务统计数据层。 */
@Mapper
public interface DepartmentTaskMapper {

    /** 统计指定时间范围内用户提交的SCORE提案数量。 */
    @Select({
        "<script>",
        "select count(1) from dm_score_proposal s where s.del_flag = '0' and s.dept_id = #{deptId}",
        "and s.proposer_user_id = #{userId} and s.submitted_at &gt;= #{startDate} and s.submitted_at &lt; date_add(#{endDate}, interval 1 day)",
        "<if test='approved'> and s.review_status = 'APPROVED' </if><if test='!approved'> and s.review_status &lt;&gt; 'DRAFT' </if>",
        "</script>"
    })
    int countScore(@Param("deptId") Long deptId, @Param("userId") Long userId,
                   @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,
                   @Param("approved") boolean approved);

    /** 查询指定时间范围内用户提交的SCORE提案主键。 */
    @Select({
        "<script>",
        "select s.id from dm_score_proposal s where s.del_flag = '0' and s.dept_id = #{deptId}",
        "and s.proposer_user_id = #{userId} and s.submitted_at &gt;= #{startDate} and s.submitted_at &lt; date_add(#{endDate}, interval 1 day)",
        "<if test='approved'> and s.review_status = 'APPROVED' </if><if test='!approved'> and s.review_status &lt;&gt; 'DRAFT' </if>",
        "</script>"
    })
    List<Long> selectScoreIds(@Param("deptId") Long deptId, @Param("userId") Long userId,
                              @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,
                              @Param("approved") boolean approved);

    /** 统计指定时间范围内用户提交的5WHY分析数量。 */
    @Select({
        "<script>",
        "select count(1) from dm_five_why f where f.del_flag = '0' and f.dept_id = #{deptId}",
        "and f.analyst_user_id = #{userId} and f.analysis_date &gt;= #{startDate} and f.analysis_date &lt;= #{endDate}",
        "<if test='approved'> and f.review_status = 'APPROVED' </if>",
        "</script>"
    })
    int countFiveWhy(@Param("deptId") Long deptId, @Param("userId") Long userId,
                     @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,
                     @Param("approved") boolean approved);

    /** 查询指定时间范围内用户提交的5WHY分析主键。 */
    @Select({
        "<script>",
        "select f.id from dm_five_why f where f.del_flag = '0' and f.dept_id = #{deptId}",
        "and f.analyst_user_id = #{userId} and f.analysis_date &gt;= #{startDate} and f.analysis_date &lt;= #{endDate}",
        "<if test='approved'> and f.review_status = 'APPROVED' </if>",
        "</script>"
    })
    List<Long> selectFiveWhyIds(@Param("deptId") Long deptId, @Param("userId") Long userId,
                                @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,
                                @Param("approved") boolean approved);

    /** 统计指定时间范围内用户提交的日报数量。 */
    @Select("select count(1) from dm_daily_report r where r.del_flag = '0' and r.dept_id = #{deptId} and r.user_id = #{userId} and r.report_date >= #{startDate} and r.report_date <= #{endDate}")
    int countDailyReport(@Param("deptId") Long deptId, @Param("userId") Long userId,
                         @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /** 查询指定时间范围内用户提交的日报主键。 */
    @Select("select r.id from dm_daily_report r where r.del_flag = '0' and r.dept_id = #{deptId} and r.user_id = #{userId} and r.report_date >= #{startDate} and r.report_date <= #{endDate}")
    List<Long> selectDailyReportIds(@Param("deptId") Long deptId, @Param("userId") Long userId,
                                    @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /** 幂等写入周期任务提醒日志。 */
    @Insert("insert ignore into dm_department_task_reminder_log (id, rule_id, user_id, period_start, reminder_type, create_time) values (#{id}, #{ruleId}, #{userId}, #{periodStart}, #{reminderType}, now())")
    int insertReminderLog(@Param("id") Long id, @Param("ruleId") Long ruleId, @Param("userId") Long userId,
                          @Param("periodStart") LocalDate periodStart, @Param("reminderType") String reminderType);
}
