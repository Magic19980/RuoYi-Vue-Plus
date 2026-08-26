package org.dromara.department.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.DepartmentTaskAssignment;
import org.dromara.department.domain.vo.DepartmentTaskAssignmentVo;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/** 周期任务成员分配数据层。 */
@Mapper
public interface DepartmentTaskAssignmentMapper extends BaseMapperPlus<DepartmentTaskAssignment, DepartmentTaskAssignmentVo> {

    default List<DepartmentTaskAssignment> selectByRuleId(Long ruleId) {
        return selectList(Wrappers.<DepartmentTaskAssignment>lambdaQuery()
            .eq(DepartmentTaskAssignment::getRuleId, ruleId)
            .eq(DepartmentTaskAssignment::getDelFlag, "0")
            .orderByDesc(DepartmentTaskAssignment::getId));
    }

    default boolean existsActiveAssignment(Long ruleId, Long userId, Long excludeId) {
        return selectCount(Wrappers.<DepartmentTaskAssignment>lambdaQuery()
            .eq(DepartmentTaskAssignment::getRuleId, ruleId)
            .eq(DepartmentTaskAssignment::getUserId, userId)
            .eq(DepartmentTaskAssignment::getDelFlag, "0")
            .ne(excludeId != null, DepartmentTaskAssignment::getId, excludeId)) > 0;
    }

    default List<DepartmentTaskAssignment> selectActiveByUserId(Long userId, Long deptId, LocalDate date) {
        return selectList(Wrappers.<DepartmentTaskAssignment>lambdaQuery()
            .eq(DepartmentTaskAssignment::getUserId, userId)
            .eq(deptId != null, DepartmentTaskAssignment::getDeptId, deptId)
            .eq(DepartmentTaskAssignment::getStatus, "ENABLED")
            .eq(DepartmentTaskAssignment::getDelFlag, "0")
            .and(wrapper -> wrapper.isNull(DepartmentTaskAssignment::getEffectiveStart).or().le(DepartmentTaskAssignment::getEffectiveStart, date))
            .and(wrapper -> wrapper.isNull(DepartmentTaskAssignment::getEffectiveEnd).or().ge(DepartmentTaskAssignment::getEffectiveEnd, date)));
    }

    @Select({
        "<script>",
        "select id, rule_id, dept_id, user_id, effective_start, effective_end, work_days, reminder_time, status, remark",
        "from dm_department_task_assignment",
        "where del_flag = '0' and status = 'ENABLED'",
        "and (effective_start is null or effective_start &lt;= #{date})",
        "and (effective_end is null or effective_end &gt;= #{date})",
        "and rule_id in",
        "<foreach collection='ruleIds' item='ruleId' open='(' separator=',' close=')'>#{ruleId}</foreach>",
        "order by rule_id asc, id desc",
        "</script>"
    })
    List<DepartmentTaskAssignment> selectEnabledByRuleIds(@Param("ruleIds") Collection<Long> ruleIds,
                                                            @Param("date") LocalDate date);

    @Select("select count(1) from sys_user u join dm_person_profile p on p.user_id = u.user_id and p.del_flag = '0' and p.create_dept = #{deptId} "
        + "and p.member_status = 'ACTIVE' and p.join_date <= current_date and (p.leave_date is null or p.leave_date > current_date) "
        + "where u.user_id = #{userId} and u.del_flag = '0' and u.status = '0'")
    long countUserInDept(@Param("userId") Long userId, @Param("deptId") Long deptId);

    @Select("select distinct p.user_id from dm_person_profile p join sys_user u on u.user_id = p.user_id "
        + "and u.del_flag = '0' and u.status = '0' where p.create_dept = #{deptId} and p.del_flag = '0' "
        + "and p.member_status = 'ACTIVE' and p.join_date <= current_date "
        + "and (p.leave_date is null or p.leave_date > current_date)")
    List<Long> selectActiveUserIdsInDept(@Param("deptId") Long deptId);

    @Select("select count(1) from dm_person_profile p join sys_user u on u.user_id = p.user_id and u.del_flag = '0' and u.status = '0' "
        + "where p.user_id = #{userId} and p.create_dept = #{deptId} and p.del_flag = '0' "
        + "and p.join_date <= #{date} and (p.leave_date is null or p.leave_date > #{date}) "
        + "and exists (select 1 from dm_department_task_assignment a join dm_department_task_rule r on r.id = a.rule_id and r.del_flag = '0' "
        + "where a.user_id = p.user_id and a.dept_id = #{deptId} and r.dept_id = #{deptId} and r.task_type = 'DAILY_REPORT' "
        + "and a.status = 'ENABLED' and a.del_flag = '0' and r.status = 'ENABLED' "
        + "and (a.effective_start is null or a.effective_start <= #{date}) and (a.effective_end is null or a.effective_end >= #{date}) "
        + "and (r.effective_start is null or r.effective_start <= #{date}) and (r.effective_end is null or r.effective_end >= #{date}))")
    long countDailyReportRequired(@Param("userId") Long userId, @Param("deptId") Long deptId, @Param("date") LocalDate date);

    @Select("select a.work_days from dm_department_task_assignment a join dm_person_profile p on p.user_id = a.user_id and p.create_dept = a.dept_id and p.del_flag = '0' "
        + "join dm_department_task_rule r on r.id = a.rule_id and r.del_flag = '0' "
        + "where a.user_id = #{userId} and a.dept_id = #{deptId} and r.dept_id = #{deptId} and r.task_type = 'DAILY_REPORT' "
        + "and p.join_date <= #{date} and (p.leave_date is null or p.leave_date > #{date}) "
        + "and a.status = 'ENABLED' and a.del_flag = '0' and r.status = 'ENABLED' "
        + "and (a.effective_start is null or a.effective_start <= #{date}) and (a.effective_end is null or a.effective_end >= #{date}) "
        + "and (r.effective_start is null or r.effective_start <= #{date}) and (r.effective_end is null or r.effective_end >= #{date})")
    List<String> selectUserDailyWorkDays(@Param("userId") Long userId, @Param("deptId") Long deptId, @Param("date") LocalDate date);

    @Select("select distinct a.user_id, a.work_days from dm_department_task_assignment a join dm_person_profile p on p.user_id = a.user_id and p.create_dept = a.dept_id and p.del_flag = '0' "
        + "join dm_department_task_rule r on r.id = a.rule_id and r.del_flag = '0' "
        + "where a.dept_id = #{deptId} and r.dept_id = #{deptId} and r.task_type = 'DAILY_REPORT' "
        + "and p.join_date <= #{endDate} and (p.leave_date is null or p.leave_date > #{beginDate}) "
        + "and a.status = 'ENABLED' and a.del_flag = '0' and r.status = 'ENABLED' "
        + "and (a.effective_start is null or a.effective_start <= #{endDate}) and (a.effective_end is null or a.effective_end >= #{beginDate}) "
        + "and (r.effective_start is null or r.effective_start <= #{endDate}) and (r.effective_end is null or r.effective_end >= #{beginDate})")
    List<DepartmentTaskAssignment> selectDailyWorkDays(@Param("deptId") Long deptId,
                                                         @Param("beginDate") LocalDate beginDate,
                                                         @Param("endDate") LocalDate endDate);

    @Select("select count(1) from sys_user where user_id = #{userId} and del_flag = '0' and status = '0'")
    long countActiveUser(@Param("userId") Long userId);

    @Select("select id, rule_id, dept_id, user_id, effective_start, effective_end, work_days, reminder_time, status, remark from dm_department_task_assignment where id = #{id} and del_flag = '0'")
    DepartmentTaskAssignment selectValidById(@Param("id") Long id);
}
