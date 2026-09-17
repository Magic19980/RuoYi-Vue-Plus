package org.dromara.department.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.RoomResourceAcl;
import org.dromara.department.domain.vo.RoomResourceAclVo;

import java.util.List;

/** 房间级授权数据层。 */
@Mapper
public interface RoomResourceAclMapper extends BaseMapperPlus<RoomResourceAcl, RoomResourceAclVo> {

    @Select({
        "select a.id, a.room_id, a.subject_type, a.subject_id, a.permission_type, a.create_time,",
        "case when a.subject_type = 'ALL' then '全公司'",
        "when a.subject_type = 'USER' then coalesce(u.nick_name, u.user_name)",
        "when a.subject_type = 'DEPT' then d.dept_name",
        "when a.subject_type = 'ROLE' then r.role_name else '未知主体' end as subject_name",
        "from dm_room_resource_acl a",
        "left join sys_user u on a.subject_type = 'USER' and u.user_id = a.subject_id",
        "left join sys_dept d on a.subject_type = 'DEPT' and d.dept_id = a.subject_id",
        "left join sys_role r on a.subject_type = 'ROLE' and r.role_id = a.subject_id",
        "where a.room_id = #{roomId} and a.del_flag = '0'",
        "order by a.permission_type asc, a.subject_type asc, a.id asc"
    })
    List<RoomResourceAclVo> selectVoByRoomId(@Param("roomId") Long roomId);

    @Select({
        "select count(1) from dm_room_resource_acl",
        "where room_id = #{roomId} and permission_type = #{permissionType} and del_flag = '0'"
    })
    long countByRoomAndPermission(@Param("roomId") Long roomId,
                                  @Param("permissionType") String permissionType);

    @Select({
        "select count(1) from dm_room_resource_acl a",
        "where a.room_id = #{roomId} and a.permission_type = #{permissionType} and a.del_flag = '0'",
        "and (",
        "  (a.subject_type = 'ALL')",
        "  or (a.subject_type = 'USER' and a.subject_id = #{userId})",
        "  or (a.subject_type = 'DEPT' and a.subject_id = #{deptId})",
        "  or (a.subject_type = 'ROLE' and exists (select 1 from sys_user_role ur where ur.user_id = #{userId} and ur.role_id = a.subject_id))",
        ")"
    })
    long countGranted(@Param("roomId") Long roomId,
                      @Param("permissionType") String permissionType,
                      @Param("userId") Long userId,
                      @Param("deptId") Long deptId);

    /** 查询房间级审批 ACL 命中的有效用户，供待审批通知使用。 */
    @Select({
        "select distinct u.user_id from sys_user u",
        "where u.del_flag = '0' and u.status = '0'",
        "and exists (select 1 from dm_room_resource_acl a",
        "  where a.room_id = #{roomId} and a.permission_type = 'APPROVE' and a.del_flag = '0'",
        "    and (a.subject_type = 'ALL'",
        "      or (a.subject_type = 'USER' and a.subject_id = u.user_id)",
        "      or (a.subject_type = 'DEPT' and a.subject_id = u.dept_id)",
        "      or (a.subject_type = 'ROLE' and exists (select 1 from sys_user_role ur where ur.user_id = u.user_id and ur.role_id = a.subject_id))",
        "    )",
        ")"
    })
    List<Long> selectApproverIds(@Param("roomId") Long roomId);
}
