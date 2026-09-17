package org.dromara.department.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.RoomResource;
import org.dromara.department.domain.bo.RoomResourceQueryBo;
import org.dromara.department.domain.vo.RoomResourceVo;

import java.util.List;

/** 房间资源数据层。 */
@Mapper
public interface RoomResourceMapper extends BaseMapperPlus<RoomResource, RoomResourceVo> {

    @Select({
        "<script>",
        "select r.id, r.dept_id, r.room_code, r.room_name, r.room_type, r.location, r.floor_plan_id,",
        "r.building_name, r.floor_name, r.area_name, r.merge_group,",
        "r.display_image, r.photo_urls, r.usage_guide, r.manager_user_id, r.display_screen, u.nick_name as manager_name,",
        "r.capacity, r.amenities,",
        "r.status, r.scope_type, r.open_time, r.close_time, r.allow_weekend, r.allow_cross_day, r.approval_mode,",
        "r.max_advance_days, r.max_duration_minutes, r.allow_recurring, r.remark, r.create_time",
        "from dm_room_resource r left join sys_user u on u.user_id = r.manager_user_id where r.del_flag = '0'",
        "<if test=\"bo.roomCode != null and bo.roomCode != ''\"> and r.room_code like concat('%', #{bo.roomCode}, '%') </if>",
        "<if test=\"bo.roomName != null and bo.roomName != ''\"> and r.room_name like concat('%', #{bo.roomName}, '%') </if>",
        "<if test=\"bo.roomType != null and bo.roomType != ''\"> and r.room_type = #{bo.roomType} </if>",
        "<if test=\"bo.location != null and bo.location != ''\"> and r.location like concat('%', #{bo.location}, '%') </if>",
        "<if test='bo.minCapacity != null'> and r.capacity &gt;= #{bo.minCapacity} </if>",
        "<if test=\"bo.amenities != null and bo.amenities != ''\"> and find_in_set(#{bo.amenities}, r.amenities) &gt; 0 </if>",
        "<if test=\"bo.status != null and bo.status != ''\"> and r.status = #{bo.status} </if>",
        "<if test=\"bo.scopeType != null and bo.scopeType != ''\"> and r.scope_type = #{bo.scopeType} </if>",
         "<choose>",
         "<when test='canManageAll'></when>",
         "<when test='canManage'> and r.dept_id = #{deptId} </when>",
         "<otherwise>",
         "and (",
         "  (not exists (select 1 from dm_room_resource_acl a0 where a0.room_id = r.id and a0.permission_type = 'VIEW' and a0.del_flag = '0') and (r.scope_type = 'PUBLIC' or r.dept_id = #{deptId}))",
         "  or exists (select 1 from dm_room_resource_acl a1 where a1.room_id = r.id and a1.permission_type = 'VIEW' and a1.del_flag = '0' and (a1.subject_type = 'ALL' or (a1.subject_type = 'USER' and a1.subject_id = #{userId}) or (a1.subject_type = 'DEPT' and a1.subject_id = #{deptId}) or (a1.subject_type = 'ROLE' and exists (select 1 from sys_user_role ur1 where ur1.user_id = #{userId} and ur1.role_id = a1.subject_id))))",
         ")",
         "</otherwise>",
         "</choose>",
        "order by r.status asc, r.room_name asc, r.id asc",
        "</script>"
    })
    Page<RoomResourceVo> selectPageList(Page<RoomResourceVo> page,
                                         @Param("bo") RoomResourceQueryBo bo,
                                         @Param("deptId") Long deptId,
                                         @Param("userId") Long userId,
                                         @Param("canManageAll") boolean canManageAll,
                                         @Param("canManage") boolean canManage);

    @Select({
        "<script>",
        "select r.id, r.dept_id, r.room_code, r.room_name, r.room_type, r.location, r.floor_plan_id,",
        "r.building_name, r.floor_name, r.area_name, r.merge_group,",
        "r.display_image, r.photo_urls, r.usage_guide, r.manager_user_id, r.display_screen, u.nick_name as manager_name,",
        "r.capacity, r.amenities,",
        "r.status, r.scope_type, r.open_time, r.close_time, r.allow_weekend, r.allow_cross_day, r.approval_mode,",
        "r.max_advance_days, r.max_duration_minutes, r.allow_recurring, r.remark",
        "from dm_room_resource r left join sys_user u on u.user_id = r.manager_user_id where r.del_flag = '0' and r.status = 'ENABLED'",
        "and not exists (select 1 from dm_room_booking_blacklist bl where bl.room_id = r.id and bl.del_flag = '0' and bl.enabled = 1 and (bl.subject_type = 'ALL' or (bl.subject_type = 'USER' and bl.subject_id = #{userId}) or (bl.subject_type = 'DEPT' and bl.subject_id = #{deptId}) or (bl.subject_type = 'ROLE' and exists (select 1 from sys_user_role ubr where ubr.user_id = #{userId} and ubr.role_id = bl.subject_id))))",
         "and (#{canManageAll} or (",
         "  (not exists (select 1 from dm_room_resource_acl a0 where a0.room_id = r.id and a0.permission_type = 'BOOK' and a0.del_flag = '0') and (r.scope_type = 'PUBLIC' or r.dept_id = #{deptId}))",
         "  or exists (select 1 from dm_room_resource_acl a1 where a1.room_id = r.id and a1.permission_type = 'BOOK' and a1.del_flag = '0' and (a1.subject_type = 'ALL' or (a1.subject_type = 'USER' and a1.subject_id = #{userId}) or (a1.subject_type = 'DEPT' and a1.subject_id = #{deptId}) or (a1.subject_type = 'ROLE' and exists (select 1 from sys_user_role ur1 where ur1.user_id = #{userId} and ur1.role_id = a1.subject_id))))",
         "))",
        "order by r.room_name asc, r.id asc",
        "</script>"
    })
    List<RoomResourceVo> selectOptions(@Param("deptId") Long deptId,
                                        @Param("userId") Long userId,
                                        @Param("canManageAll") boolean canManageAll);

    @Select({
        "<script>",
        "select r.id, r.dept_id, r.room_code, r.room_name, r.room_type, r.location, r.floor_plan_id,",
        "r.building_name, r.floor_name, r.area_name, r.merge_group,",
        "r.display_image, r.photo_urls, r.usage_guide, r.manager_user_id, r.display_screen, u.nick_name as manager_name,",
        "r.capacity, r.amenities, r.status, r.scope_type, r.open_time, r.close_time, r.allow_weekend, r.allow_cross_day, r.approval_mode,",
        "r.max_advance_days, r.max_duration_minutes, r.allow_recurring, r.remark",
        "from dm_room_resource r left join sys_user u on u.user_id = r.manager_user_id where r.del_flag = '0'",
        "and (#{canManageAll} or (#{canManage} and r.dept_id = #{deptId}) or (",
        "  (not exists (select 1 from dm_room_resource_acl a0 where a0.room_id = r.id and a0.permission_type = 'VIEW' and a0.del_flag = '0') and (r.scope_type = 'PUBLIC' or r.dept_id = #{deptId}))",
        "  or exists (select 1 from dm_room_resource_acl a1 where a1.room_id = r.id and a1.permission_type = 'VIEW' and a1.del_flag = '0' and (a1.subject_type = 'ALL' or (a1.subject_type = 'USER' and a1.subject_id = #{userId}) or (a1.subject_type = 'DEPT' and a1.subject_id = #{deptId}) or (a1.subject_type = 'ROLE' and exists (select 1 from sys_user_role ur1 where ur1.user_id = #{userId} and ur1.role_id = a1.subject_id))))",
        "))",
        "order by r.room_name asc, r.id asc",
        "</script>"
    })
    List<RoomResourceVo> selectViewOptions(@Param("deptId") Long deptId,
                                           @Param("userId") Long userId,
                                           @Param("canManageAll") boolean canManageAll,
                                           @Param("canManage") boolean canManage);

    @Select("select count(1) from dm_room_booking_occurrence where room_id = #{roomId} and del_flag = '0' and status in ('PENDING', 'CONFIRMED', 'IN_USE', 'COMPLETED')")
    long countBookings(@Param("roomId") Long roomId);
}
