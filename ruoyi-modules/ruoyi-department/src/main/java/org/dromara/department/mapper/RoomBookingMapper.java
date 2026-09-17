package org.dromara.department.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.RoomBooking;
import org.dromara.department.domain.bo.RoomBookingQueryBo;
import org.dromara.department.domain.vo.RoomBookingVo;
import org.dromara.department.domain.vo.RoomUserOptionVo;

import java.util.List;

/** 房间预约系列数据层。 */
@Mapper
public interface RoomBookingMapper extends BaseMapperPlus<RoomBooking, RoomBookingVo> {

    @Select({
        "<script>",
        "select b.id, o.id as occurrence_id, o.room_id, r.room_code, r.room_name, r.room_type, r.location,",
        "b.title, b.organizer_id, coalesce(u.nick_name, u.user_name) as organizer_name, b.dept_id, d.dept_name,",
        "o.start_at, o.end_at, o.status, o.status as occurrence_status, o.approval_step, o.approval_due_at, o.check_in_at, o.check_out_at, b.visibility, b.recurrence_type, o.occurrence_no, b.description",
        "from dm_room_booking_occurrence o",
        "join dm_room_booking b on b.id = o.booking_id and b.del_flag = '0'",
        "join dm_room_resource r on r.id = o.room_id and r.del_flag = '0'",
        "left join sys_user u on u.user_id = b.organizer_id",
        "left join sys_dept d on d.dept_id = b.dept_id",
        "where o.del_flag = '0'",
        "<choose>",
        "<when test=\"bo.status != null and bo.status != ''\"> and o.status = #{bo.status} </when>",
        "<otherwise> and o.status not in ('CANCELLED', 'REJECTED') </otherwise>",
        "</choose>",
        "<if test='bo.beginAt != null'> and o.end_at &gt; #{bo.beginAt} </if>",
        "<if test='bo.endAt != null'> and o.start_at &lt; #{bo.endAt} </if>",
        "<if test='bo.roomId != null'> and o.room_id = #{bo.roomId} </if>",
        "<if test=\"bo.roomType != null and bo.roomType != ''\"> and r.room_type = #{bo.roomType} </if>",
        "<if test=\"bo.keyword != null and bo.keyword != ''\"> and (b.title like concat('%', #{bo.keyword}, '%') or r.room_name like concat('%', #{bo.keyword}, '%')) </if>",
        "<if test='bo.mine != null and bo.mine'> and b.organizer_id = #{userId} </if>",
                "<if test='bo.approvalOnly != null and bo.approvalOnly'> and (#{canManageAll} or #{canApprove} or exists (select 1 from dm_room_resource_acl ap where ap.room_id = r.id and ap.permission_type = 'APPROVE' and ap.del_flag = '0' and (ap.subject_type = 'ALL' or (ap.subject_type = 'USER' and ap.subject_id = #{userId}) or (ap.subject_type = 'DEPT' and ap.subject_id = #{deptId}) or (ap.subject_type = 'ROLE' and exists (select 1 from sys_user_role upr where upr.user_id = #{userId} and upr.role_id = ap.subject_id)))) or exists (select 1 from dm_room_approval_rule ar0 where ar0.room_id = r.id and ar0.step_no = coalesce(o.approval_step, 1) and ar0.enabled = 1 and ar0.del_flag = '0' and (ar0.approver_type = 'ALL' or (ar0.approver_type = 'USER' and ar0.approver_id = #{userId}) or (ar0.approver_type = 'DEPT' and ar0.approver_id = #{deptId}) or (ar0.approver_type = 'ROLE' and exists (select 1 from sys_user_role ur4 where ur4.user_id = #{userId} and ur4.role_id = ar0.approver_id))))) </if>",
        "and (#{canManageAll} or (#{canApprove} and o.status = 'PENDING') or b.organizer_id = #{userId} or exists (select 1 from dm_room_booking_attendee ba0 where ba0.booking_id = b.id and ba0.user_id = #{userId} and ba0.del_flag = '0') or (",
        "  (not exists (select 1 from dm_room_resource_acl a0 where a0.room_id = r.id and a0.permission_type = 'VIEW' and a0.del_flag = '0') and (r.scope_type = 'PUBLIC' or r.dept_id = #{deptId}))",
        "  or exists (select 1 from dm_room_resource_acl a1 where a1.room_id = r.id and a1.permission_type = 'VIEW' and a1.del_flag = '0' and (a1.subject_type = 'ALL' or (a1.subject_type = 'USER' and a1.subject_id = #{userId}) or (a1.subject_type = 'DEPT' and a1.subject_id = #{deptId}) or (a1.subject_type = 'ROLE' and exists (select 1 from sys_user_role ur1 where ur1.user_id = #{userId} and ur1.role_id = a1.subject_id))))",
        "  or (o.status = 'PENDING' and (",
        "    exists (select 1 from dm_room_resource_acl a2 where a2.room_id = r.id and a2.permission_type = 'APPROVE' and a2.del_flag = '0' and (a2.subject_type = 'ALL' or (a2.subject_type = 'USER' and a2.subject_id = #{userId}) or (a2.subject_type = 'DEPT' and a2.subject_id = #{deptId}) or (a2.subject_type = 'ROLE' and exists (select 1 from sys_user_role ur2 where ur2.user_id = #{userId} and ur2.role_id = a2.subject_id))))",
        "    or exists (select 1 from dm_room_approval_rule ar where ar.room_id = r.id and ar.step_no = coalesce(o.approval_step, 1) and ar.enabled = 1 and ar.del_flag = '0' and (ar.approver_type = 'ALL' or (ar.approver_type = 'USER' and ar.approver_id = #{userId}) or (ar.approver_type = 'DEPT' and ar.approver_id = #{deptId}) or (ar.approver_type = 'ROLE' and exists (select 1 from sys_user_role ur3 where ur3.user_id = #{userId} and ur3.role_id = ar.approver_id))))",
        "  ))",
        "))",
        "order by o.start_at asc, r.room_name asc, b.id asc",
        "</script>"
    })
    List<RoomBookingVo> selectCalendar(@Param("bo") RoomBookingQueryBo bo,
                                        @Param("deptId") Long deptId,
                                        @Param("userId") Long userId,
                                        @Param("canManageAll") boolean canManageAll,
                                        @Param("canApprove") boolean canApprove);

    @Select({
        "<script>",
        "select b.id, o.id as occurrence_id, o.room_id, r.room_code, r.room_name, r.room_type, r.location,",
        "b.title, b.organizer_id, coalesce(u.nick_name, u.user_name) as organizer_name, b.dept_id, d.dept_name,",
        "o.start_at, o.end_at, o.status, o.status as occurrence_status, o.approval_step, o.approval_due_at, o.check_in_at, o.check_out_at, b.visibility, b.recurrence_type, o.occurrence_no, b.description",
        "from dm_room_booking_occurrence o",
        "join dm_room_booking b on b.id = o.booking_id and b.del_flag = '0'",
        "join dm_room_resource r on r.id = o.room_id and r.del_flag = '0'",
        "left join sys_user u on u.user_id = b.organizer_id",
        "left join sys_dept d on d.dept_id = b.dept_id",
        "where o.del_flag = '0'",
        "<choose>",
        "<when test=\"bo.status != null and bo.status != ''\"> and o.status = #{bo.status} </when>",
        "<otherwise> and o.status not in ('CANCELLED', 'REJECTED') </otherwise>",
        "</choose>",
        "<if test='bo.beginAt != null'> and o.end_at &gt; #{bo.beginAt} </if>",
        "<if test='bo.endAt != null'> and o.start_at &lt; #{bo.endAt} </if>",
        "<if test='bo.roomId != null'> and o.room_id = #{bo.roomId} </if>",
        "<if test=\"bo.roomType != null and bo.roomType != ''\"> and r.room_type = #{bo.roomType} </if>",
        "<if test=\"bo.keyword != null and bo.keyword != ''\"> and (b.title like concat('%', #{bo.keyword}, '%') or r.room_name like concat('%', #{bo.keyword}, '%')) </if>",
        "<if test='bo.mine != null and bo.mine'> and b.organizer_id = #{userId} </if>",
        "<if test='bo.approvalOnly != null and bo.approvalOnly'> and (#{canManageAll} or #{canApprove} or exists (select 1 from dm_room_resource_acl ap where ap.room_id = r.id and ap.permission_type = 'APPROVE' and ap.del_flag = '0' and (ap.subject_type = 'ALL' or (ap.subject_type = 'USER' and ap.subject_id = #{userId}) or (ap.subject_type = 'DEPT' and ap.subject_id = #{deptId}) or (ap.subject_type = 'ROLE' and exists (select 1 from sys_user_role upr where upr.user_id = #{userId} and upr.role_id = ap.subject_id)))) or exists (select 1 from dm_room_approval_rule ar0 where ar0.room_id = r.id and ar0.step_no = coalesce(o.approval_step, 1) and ar0.enabled = 1 and ar0.del_flag = '0' and (ar0.approver_type = 'ALL' or (ar0.approver_type = 'USER' and ar0.approver_id = #{userId}) or (ar0.approver_type = 'DEPT' and ar0.approver_id = #{deptId}) or (ar0.approver_type = 'ROLE' and exists (select 1 from sys_user_role ur4 where ur4.user_id = #{userId} and ur4.role_id = ar0.approver_id))))) </if>",
        "and (#{canManageAll} or (#{canApprove} and o.status = 'PENDING') or b.organizer_id = #{userId} or exists (select 1 from dm_room_booking_attendee ba0 where ba0.booking_id = b.id and ba0.user_id = #{userId} and ba0.del_flag = '0') or (",
        "  (not exists (select 1 from dm_room_resource_acl a0 where a0.room_id = r.id and a0.permission_type = 'VIEW' and a0.del_flag = '0') and (r.scope_type = 'PUBLIC' or r.dept_id = #{deptId}))",
        "  or exists (select 1 from dm_room_resource_acl a1 where a1.room_id = r.id and a1.permission_type = 'VIEW' and a1.del_flag = '0' and (a1.subject_type = 'ALL' or (a1.subject_type = 'USER' and a1.subject_id = #{userId}) or (a1.subject_type = 'DEPT' and a1.subject_id = #{deptId}) or (a1.subject_type = 'ROLE' and exists (select 1 from sys_user_role ur1 where ur1.user_id = #{userId} and ur1.role_id = a1.subject_id))))",
        "  or (o.status = 'PENDING' and (",
        "    exists (select 1 from dm_room_resource_acl a2 where a2.room_id = r.id and a2.permission_type = 'APPROVE' and a2.del_flag = '0' and (a2.subject_type = 'ALL' or (a2.subject_type = 'USER' and a2.subject_id = #{userId}) or (a2.subject_type = 'DEPT' and a2.subject_id = #{deptId}) or (a2.subject_type = 'ROLE' and exists (select 1 from sys_user_role ur2 where ur2.user_id = #{userId} and ur2.role_id = a2.subject_id))))",
        "    or exists (select 1 from dm_room_approval_rule ar where ar.room_id = r.id and ar.step_no = coalesce(o.approval_step, 1) and ar.enabled = 1 and ar.del_flag = '0' and (ar.approver_type = 'ALL' or (ar.approver_type = 'USER' and ar.approver_id = #{userId}) or (ar.approver_type = 'DEPT' and ar.approver_id = #{deptId}) or (ar.approver_type = 'ROLE' and exists (select 1 from sys_user_role ur3 where ur3.user_id = #{userId} and ur3.role_id = ar.approver_id))))",
        "  ))",
        "))",
        "order by o.start_at asc, r.room_name asc, b.id asc",
        "</script>"
    })
    Page<RoomBookingVo> selectPageList(Page<RoomBookingVo> page,
                                        @Param("bo") RoomBookingQueryBo bo,
                                        @Param("deptId") Long deptId,
                                        @Param("userId") Long userId,
                                        @Param("canManageAll") boolean canManageAll,
                                        @Param("canApprove") boolean canApprove);

    /** 按预约实例统计当前用户真正可处理的待审批数量，组合预约的多间房只计一次。 */
    @Select({
        "<script>",
        "select count(*) from (",
        "  select distinct o.booking_id, o.occurrence_no",
        "  from dm_room_booking_occurrence o",
        "  join dm_room_booking b on b.id = o.booking_id and b.del_flag = '0'",
        "  join dm_room_resource r on r.id = o.room_id and r.del_flag = '0'",
        "  where o.del_flag = '0' and o.status = 'PENDING'",
        "    and (",
        "      #{canManageAll} or #{canApprove}",
        "      or (",
        "        (",
        "          exists (select 1 from dm_room_approval_rule ar0 where ar0.room_id = r.id and ar0.step_no = coalesce(o.approval_step, 1) and ar0.enabled = 1 and ar0.del_flag = '0' and (ar0.approver_type = 'ALL' or (ar0.approver_type = 'USER' and ar0.approver_id = #{userId}) or (ar0.approver_type = 'DEPT' and ar0.approver_id = #{deptId}) or (ar0.approver_type = 'ROLE' and exists (select 1 from sys_user_role ur0 where ur0.user_id = #{userId} and ur0.role_id = ar0.approver_id))))",
        "          or (",
        "            not exists (select 1 from dm_room_approval_rule ar1 where ar1.room_id = r.id and ar1.enabled = 1 and ar1.del_flag = '0')",
        "            and (#{canApprove} or exists (select 1 from dm_room_resource_acl ap where ap.room_id = r.id and ap.permission_type = 'APPROVE' and ap.del_flag = '0' and (ap.subject_type = 'ALL' or (ap.subject_type = 'USER' and ap.subject_id = #{userId}) or (ap.subject_type = 'DEPT' and ap.subject_id = #{deptId}) or (ap.subject_type = 'ROLE' and exists (select 1 from sys_user_role upr where upr.user_id = #{userId} and upr.role_id = ap.subject_id)))))",
        "          )",
        "        )",
        "        and (",
        "          (not exists (select 1 from dm_room_resource_acl a0 where a0.room_id = r.id and a0.permission_type = 'VIEW' and a0.del_flag = '0') and (r.scope_type = 'PUBLIC' or r.dept_id = #{deptId}))",
        "          or exists (select 1 from dm_room_resource_acl a1 where a1.room_id = r.id and a1.permission_type = 'VIEW' and a1.del_flag = '0' and (a1.subject_type = 'ALL' or (a1.subject_type = 'USER' and a1.subject_id = #{userId}) or (a1.subject_type = 'DEPT' and a1.subject_id = #{deptId}) or (a1.subject_type = 'ROLE' and exists (select 1 from sys_user_role ur1 where ur1.user_id = #{userId} and ur1.role_id = a1.subject_id))))",
        "          or exists (select 1 from dm_room_resource_acl a2 where a2.room_id = r.id and a2.permission_type = 'APPROVE' and a2.del_flag = '0' and (a2.subject_type = 'ALL' or (a2.subject_type = 'USER' and a2.subject_id = #{userId}) or (a2.subject_type = 'DEPT' and a2.subject_id = #{deptId}) or (a2.subject_type = 'ROLE' and exists (select 1 from sys_user_role ur2 where ur2.user_id = #{userId} and ur2.role_id = a2.subject_id))))",
        "          or exists (select 1 from dm_room_approval_rule ar2 where ar2.room_id = r.id and ar2.step_no = coalesce(o.approval_step, 1) and ar2.enabled = 1 and ar2.del_flag = '0' and (ar2.approver_type = 'ALL' or (ar2.approver_type = 'USER' and ar2.approver_id = #{userId}) or (ar2.approver_type = 'DEPT' and ar2.approver_id = #{deptId}) or (ar2.approver_type = 'ROLE' and exists (select 1 from sys_user_role ur3 where ur3.user_id = #{userId} and ur3.role_id = ar2.approver_id))))",
        "        )",
        "      )",
        "    )",
        ") pending_tasks",
        "</script>"
    })
    long selectPendingCount(@Param("deptId") Long deptId,
                            @Param("userId") Long userId,
                            @Param("canManageAll") boolean canManageAll,
                            @Param("canApprove") boolean canApprove);

    @Select({
        "select u.user_id, u.user_name, u.nick_name, d.dept_name from sys_user u left join sys_dept d on d.dept_id = u.dept_id",
        "where u.del_flag = '0' and u.status = '0' and (u.dept_id = #{deptId} or u.user_id = #{userId} or #{canManageAll})",
        "order by u.nick_name asc, u.user_name asc"
    })
    List<RoomUserOptionVo> selectUserOptions(@Param("deptId") Long deptId,
                                             @Param("userId") Long userId,
                                             @Param("canManageAll") boolean canManageAll);

    @Select("""
        select distinct u.user_id
        from sys_user u
        join sys_user_role ur on ur.user_id = u.user_id
        join sys_role_menu rm on rm.role_id = ur.role_id
        join sys_menu m on m.menu_id = rm.menu_id
        where u.del_flag = '0' and u.status = '0'
          and m.perms = 'department:room:approve'
        """)
    List<Long> selectApproverIds();
}
