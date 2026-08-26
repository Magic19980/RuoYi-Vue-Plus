package org.dromara.department.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.api.domain.DepartmentMembershipDTO;
import org.dromara.department.domain.PersonProfile;
import org.dromara.department.domain.bo.PersonProfileQueryBo;
import org.dromara.department.domain.bo.PersonUserOptionQueryBo;
import org.dromara.department.domain.vo.PersonProfileVo;
import org.dromara.department.domain.vo.PersonDepartmentContextVo;
import org.dromara.department.domain.vo.PersonUserOptionVo;
import org.dromara.department.service.DepartmentScope;

import java.util.List;
import java.util.Collection;
import java.time.LocalDate;

/**
 * 人员档案数据层。
 */
@Mapper
public interface PersonProfileMapper extends BaseMapperPlus<PersonProfile, PersonProfileVo> {

    @Select({
        "<script>",
        "select p.id, p.user_id, u.user_name, u.nick_name, d.dept_name,",
        "u.employee_no, (select group_concat(distinct sp.post_name order by sp.post_sort, sp.post_id separator '、') "
            + "from sys_user_post sup join sys_post sp on sp.post_id = sup.post_id and sp.status = '0' and sp.del_flag = '0' "
            + "where sup.user_id = p.user_id) as job_title, p.remark, p.join_date, p.leave_date,",
        "p.member_type, p.member_status, p.end_reason, p.ended_at",
        "from dm_person_profile p",
        "left join sys_user u on u.user_id = p.user_id and u.del_flag = '0'",
        "left join sys_dept d on d.dept_id = p.create_dept and d.del_flag = '0'",
        "where p.del_flag = '0'",
        "<if test='bo.id != null'> and p.id = #{bo.id} </if>",
        "<if test='bo.userName != null and bo.userName != \"\"'> and (u.user_name like concat('%', #{bo.userName}, '%') or u.nick_name like concat('%', #{bo.userName}, '%')) </if>",
        "<if test='bo.jobTitle != null and bo.jobTitle != \"\"'> and exists (select 1 from sys_user_post sup join sys_post sp on sp.post_id = sup.post_id "
            + "and sp.status = '0' and sp.del_flag = '0' where sup.user_id = p.user_id "
            + "and sp.post_name like concat('%', #{bo.jobTitle}, '%')) </if>",
        "<if test='bo.includeHistory != true'> and p.member_status = 'ACTIVE' and p.join_date &lt;= #{today} and (p.leave_date is null or p.leave_date &gt; #{today}) </if>",
        "<choose>",
        "<when test='scope.all'>",
        "</when>",
        "<when test='scope.deptId != null'> and p.create_dept = #{scope.deptId} </when>",
        "<otherwise> and p.user_id = #{userId} </otherwise>",
        "</choose>",
        "order by p.id desc",
        "</script>"
    })
    Page<PersonProfileVo> selectPageList(Page<PersonProfileVo> page,
                                          @Param("bo") PersonProfileQueryBo bo,
                                          @Param("userId") Long userId,
                                          @Param("scope") DepartmentScope scope,
                                          @Param("today") LocalDate today);

    @Select({
        "<script>",
        "select u.user_id, u.dept_id, u.user_name, u.nick_name, u.employee_no, "
            + "(select group_concat(distinct sp.post_name order by sp.post_sort, sp.post_id separator '、') "
            + "from sys_user_post sup join sys_post sp on sp.post_id = sup.post_id and sp.status = '0' and sp.del_flag = '0' "
            + "where sup.user_id = u.user_id) as job_title, d.dept_name",
        "from sys_user u left join sys_dept d on d.dept_id = u.dept_id and d.del_flag = '0'",
        "where u.del_flag = '0' and u.status = '0'",
        "<if test='!scope.all and scope.deptId != null'> and u.dept_id = #{scope.deptId} </if>",
        "order by u.user_name",
        "</script>"
    })
    List<PersonUserOptionVo> selectUserOptions(@Param("scope") DepartmentScope scope);

    @Select({
        "<script>",
        "select u.user_id, u.dept_id, u.user_name, u.nick_name, u.employee_no, "
            + "(select group_concat(distinct sp.post_name order by sp.post_sort, sp.post_id separator '、') "
            + "from sys_user_post sup join sys_post sp on sp.post_id = sup.post_id and sp.status = '0' and sp.del_flag = '0' "
            + "where sup.user_id = u.user_id) as job_title, d.dept_name",
        "from sys_user u left join sys_dept d on d.dept_id = u.dept_id and d.del_flag = '0'",
        "where u.del_flag = '0' and u.status = '0' and u.user_name in",
        "<foreach collection='userNames' item='userName' open='(' separator=',' close=')'>#{userName}</foreach>",
        "order by u.user_name",
        "</script>"
    })
    List<PersonUserOptionVo> selectUserOptionsByNames(@Param("userNames") Collection<String> userNames);

    @Select({
        "<script>",
        "select u.user_id, u.dept_id, u.user_name, u.nick_name, u.employee_no, "
            + "(select group_concat(distinct sp.post_name order by sp.post_sort, sp.post_id separator '、') "
            + "from sys_user_post sup join sys_post sp on sp.post_id = sup.post_id and sp.status = '0' and sp.del_flag = '0' "
            + "where sup.user_id = u.user_id) as job_title, d.dept_name",
        "from sys_user u left join sys_dept d on d.dept_id = u.dept_id and d.del_flag = '0'",
        "where u.del_flag = '0' and u.status = '0' and u.user_id in",
        "<foreach collection='userIds' item='userId' open='(' separator=',' close=')'>#{userId}</foreach>",
        "order by u.user_name",
        "</script>"
    })
    List<PersonUserOptionVo> selectUserOptionsByIds(@Param("userIds") Collection<Long> userIds);

    @Select("select u.user_id, u.dept_id, u.user_name, u.nick_name, u.employee_no, "
        + "(select group_concat(distinct sp.post_name order by sp.post_sort, sp.post_id separator '、') "
        + "from sys_user_post sup join sys_post sp on sp.post_id = sup.post_id and sp.status = '0' and sp.del_flag = '0' "
        + "where sup.user_id = u.user_id) as job_title, d.dept_name "
        + "from sys_user u left join sys_dept d on d.dept_id = u.dept_id and d.del_flag = '0' "
        + "where u.user_id = #{userId} and u.del_flag = '0' and u.status = '0'")
    PersonUserOptionVo selectUserOptionById(@Param("userId") Long userId);

    @Select({
        "<script>",
        "select u.user_id, u.dept_id, u.user_name, u.nick_name, u.employee_no, "
            + "(select group_concat(distinct sp.post_name order by sp.post_sort, sp.post_id separator '、') "
            + "from sys_user_post sup join sys_post sp on sp.post_id = sup.post_id and sp.status = '0' and sp.del_flag = '0' "
            + "where sup.user_id = u.user_id) as job_title, d.dept_name",
        "from sys_user u left join sys_dept d on d.dept_id = u.dept_id and d.del_flag = '0'",
        "where u.del_flag = '0' and u.status = '0'",
        "and not exists (select 1 from dm_person_profile p where p.user_id = u.user_id and p.create_dept = #{targetDeptId} and p.del_flag = '0'",
        "and p.member_status = 'ACTIVE' and p.join_date &lt;= #{today} and (p.leave_date is null or p.leave_date &gt; #{today}))",
        "<if test='!scope.all and scope.deptId != null'> and u.dept_id = #{scope.deptId} </if>",
        "<if test='bo.deptId != null'> and u.dept_id = #{bo.deptId} </if>",
        "<if test='bo.keyword != null and bo.keyword != \"\"'>",
        "and (u.user_name like concat('%', #{bo.keyword}, '%')",
        "or u.nick_name like concat('%', #{bo.keyword}, '%')",
        "or u.employee_no like concat('%', #{bo.keyword}, '%')",
        "or d.dept_name like concat('%', #{bo.keyword}, '%'))",
        "</if>",
        "order by u.user_name",
        "</script>"
    })
    Page<PersonUserOptionVo> selectUserOptionsPage(Page<PersonUserOptionVo> page,
                                                     @Param("bo") PersonUserOptionQueryBo bo,
                                                     @Param("targetDeptId") Long targetDeptId,
                                                     @Param("scope") DepartmentScope scope,
                                                     @Param("today") LocalDate today);

    @Select("select distinct u.user_id, u.dept_id, u.user_name, u.nick_name, u.employee_no, "
        + "(select group_concat(distinct sp.post_name order by sp.post_sort, sp.post_id separator '、') "
        + "from sys_user_post sup join sys_post sp on sp.post_id = sup.post_id and sp.status = '0' and sp.del_flag = '0' "
        + "where sup.user_id = p.user_id) as job_title, d.dept_name "
        + "from dm_person_profile p join sys_user u on u.user_id = p.user_id and u.del_flag = '0' "
        + "left join sys_dept d on d.dept_id = u.dept_id and d.del_flag = '0' "
        + "where p.create_dept = #{deptId} and p.del_flag = '0' and p.member_status = 'ACTIVE' "
        + "and p.join_date <= #{today} and (p.leave_date is null or p.leave_date > #{today}) "
        + "and u.status = '0' order by u.user_name")
    List<PersonUserOptionVo> selectMemberUserOptions(@Param("deptId") Long deptId, @Param("today") LocalDate today);

    @Select({
        "<script>",
        "select count(1) from dm_person_profile p",
        "where p.user_id = #{userId} and p.create_dept = #{deptId} and p.del_flag = '0'",
        "<if test='excludeId != null'> and p.id &lt;&gt; #{excludeId} </if>",
        "<if test='leaveDate != null'> and (p.join_date is null or p.join_date &lt; #{leaveDate}) </if>",
        "and (p.leave_date is null or p.leave_date &gt; #{joinDate})",
        "</script>"
    })
    long countOverlappingMembership(@Param("userId") Long userId,
                                    @Param("deptId") Long deptId,
                                    @Param("joinDate") LocalDate joinDate,
                                    @Param("leaveDate") LocalDate leaveDate,
                                    @Param("excludeId") Long excludeId);

    @Select("select count(1) from dm_person_profile p join sys_user u on u.user_id = p.user_id and u.del_flag = '0' and u.status = '0' "
        + "where p.user_id = #{userId} and p.create_dept = #{deptId} and p.del_flag = '0' and p.member_status = 'ACTIVE' "
        + "and p.join_date <= current_date and (p.leave_date is null or p.leave_date > current_date)")
    long countMemberInDept(@Param("userId") Long userId, @Param("deptId") Long deptId);

    @Select("select count(1) from dm_person_profile p join sys_user u on u.user_id = p.user_id and u.del_flag = '0' and u.status = '0' "
        + "where p.user_id = #{userId} and p.create_dept = #{deptId} and p.del_flag = '0' "
        + "and p.join_date <= #{date} and (p.leave_date is null or p.leave_date > #{date})")
    long countMemberInDeptAt(@Param("userId") Long userId, @Param("deptId") Long deptId, @Param("date") LocalDate date);

    @Select("select count(1) from dm_person_profile p join sys_user u on u.user_id = p.user_id "
        + "and u.del_flag = '0' and u.status = '0' "
        + "where p.user_id = #{userId} and p.create_dept = #{deptId} and p.member_type = 'FULL' "
        + "and p.del_flag = '0' and p.member_status = 'ACTIVE' "
        + "and p.join_date <= #{date} and (p.leave_date is null or p.leave_date > #{date})")
    long countFullMemberInDeptAt(@Param("userId") Long userId, @Param("deptId") Long deptId, @Param("date") LocalDate date);

    @Select("select p.* from dm_person_profile p join sys_user u on u.user_id = p.user_id and u.del_flag = '0' and u.status = '0' "
        + "where p.user_id = #{userId} and p.create_dept = #{deptId} and p.del_flag = '0' and p.member_status = 'ACTIVE' "
        + "and p.join_date <= #{date} and (p.leave_date is null or p.leave_date > #{date}) "
        + "order by p.join_date desc, p.id desc limit 1")
    PersonProfile selectActiveMembership(@Param("userId") Long userId, @Param("deptId") Long deptId, @Param("date") LocalDate date);

    @Select("select p.* from dm_person_profile p where p.user_id = #{userId} and p.create_dept = #{deptId} and p.del_flag = '0' "
        + "order by case when p.member_status = 'ACTIVE' then 0 else 1 end, p.join_date desc, p.id desc limit 1")
    PersonProfile selectLatestByUserDept(@Param("userId") Long userId, @Param("deptId") Long deptId);

    @Select("select p.* from dm_person_profile p where p.user_id = #{userId} and p.member_source = 'AUTO_MAIN' "
        + "and p.del_flag = '0' and p.member_status = 'ACTIVE'")
    List<PersonProfile> selectActiveAutoMainMemberships(@Param("userId") Long userId);

    @Select("select p.* from dm_person_profile p where p.create_dept = #{deptId} and p.member_source = 'AUTO_MAIN' "
        + "and p.del_flag = '0' and p.member_status = 'ACTIVE'")
    List<PersonProfile> selectActiveAutoMainMembershipsByDept(@Param("deptId") Long deptId);

    /**
     * 查询用户当前仍在服务的科室关系，用于保护用户删除操作。
     */
    @Select({
        "<script>",
        "select membership.user_id, membership.user_name, membership.dept_id, membership.dept_name",
        "from (",
        "select distinct p.user_id, u.user_name, p.create_dept as dept_id,",
        "coalesce(d.dept_name, concat('科室#', p.create_dept)) as dept_name",
        "from dm_person_profile p",
        "left join sys_user u on u.user_id = p.user_id",
        "left join sys_dept d on d.dept_id = p.create_dept",
        "where p.del_flag = '0' and p.member_status = 'ACTIVE'",
        "and p.join_date &lt;= current_date and (p.leave_date is null or p.leave_date &gt; current_date)",
        "and p.user_id in",
        "<foreach collection='userIds' item='userId' open='(' separator=',' close=')'>#{userId}</foreach>",
        ") membership",
        "order by membership.user_id, membership.dept_name, membership.dept_id",
        "</script>"
    })
    List<DepartmentMembershipDTO> selectActiveMembershipsByUserIds(@Param("userIds") Collection<Long> userIds);

    @Select("select u.user_id from sys_user u where u.dept_id = #{deptId} and u.del_flag = '0' and u.status = '0'")
    List<Long> selectMainUserIdsByDept(@Param("deptId") Long deptId);

    @Update("update dm_person_profile set member_type = 'FULL', member_status = 'ACTIVE', member_source = 'AUTO_MAIN', "
        + "leave_date = null, ended_at = null, ended_by = null, end_reason = null, update_by = #{operatorId}, update_time = now() "
        + "where id = #{profileId} and del_flag = '0'")
    int activateMainMembership(@Param("profileId") Long profileId, @Param("operatorId") Long operatorId);

    @Select("select p.create_dept as dept_id, d.dept_name, p.member_type, p.join_date, p.leave_date "
        + "from dm_person_profile p "
        + "join dm_department c on c.dept_id = p.create_dept and c.del_flag = '0' and c.status = 'ENABLED' "
        + "join sys_dept d on d.dept_id = p.create_dept and d.del_flag = '0' and d.status = '0' "
        + "where p.id = (select p2.id from dm_person_profile p2 "
        + "where p2.user_id = #{userId} and p2.create_dept = p.create_dept and p2.del_flag = '0' "
        + "and p2.member_status = 'ACTIVE' and p2.join_date <= #{date} "
        + "and (p2.leave_date is null or p2.leave_date > #{date}) "
        + "order by p2.join_date desc, p2.id desc limit 1) "
        + "order by d.dept_name, p.join_date desc, p.id desc")
    List<PersonDepartmentContextVo> selectCurrentDepartmentContexts(@Param("userId") Long userId,
                                                                       @Param("date") LocalDate date);
}
