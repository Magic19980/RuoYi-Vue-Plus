package org.dromara.department.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.RoomFloorPlan;
import org.dromara.department.domain.bo.RoomFloorPlanQueryBo;
import org.dromara.department.domain.vo.RoomFloorPlanVo;

import java.util.List;

/** 楼层平面图数据层。 */
@Mapper
public interface RoomFloorPlanMapper extends BaseMapperPlus<RoomFloorPlan, RoomFloorPlanVo> {

    @Select({
        "<script>",
        "select p.id, p.dept_id, d.dept_name, p.plan_name, p.building_name, p.floor_name, p.floor_no,",
        "p.map_image, p.map_data, p.status, p.remark, p.create_time",
        "from dm_room_floor_plan p left join sys_dept d on d.dept_id = p.dept_id",
        "where p.del_flag = '0'",
        "<if test=\"bo.planName != null and bo.planName != ''\"> and p.plan_name like concat('%', #{bo.planName}, '%') </if>",
        "<if test=\"bo.buildingName != null and bo.buildingName != ''\"> and p.building_name like concat('%', #{bo.buildingName}, '%') </if>",
        "<if test=\"bo.floorName != null and bo.floorName != ''\"> and p.floor_name like concat('%', #{bo.floorName}, '%') </if>",
        "<if test=\"bo.status != null and bo.status != ''\"> and p.status = #{bo.status} </if>",
        "<choose>",
        "<when test='canManageAll'></when>",
        "<when test='canManage'> and p.dept_id = #{deptId} </when>",
        "<otherwise>",
        "and p.status = 'ENABLED' and exists (select 1 from dm_room_resource r where r.floor_plan_id = p.id and r.del_flag = '0' and (",
        "  (not exists (select 1 from dm_room_resource_acl a0 where a0.room_id = r.id and a0.permission_type = 'VIEW' and a0.del_flag = '0') and (r.scope_type = 'PUBLIC' or r.dept_id = #{deptId}))",
        "  or exists (select 1 from dm_room_resource_acl a1 where a1.room_id = r.id and a1.permission_type = 'VIEW' and a1.del_flag = '0' and (a1.subject_type = 'ALL' or (a1.subject_type = 'USER' and a1.subject_id = #{userId}) or (a1.subject_type = 'DEPT' and a1.subject_id = #{deptId}) or (a1.subject_type = 'ROLE' and exists (select 1 from sys_user_role ur1 where ur1.user_id = #{userId} and ur1.role_id = a1.subject_id)))",
        ")))",
        "</otherwise>",
        "</choose>",
        "order by p.building_name asc, p.floor_no asc, p.floor_name asc, p.id asc",
        "</script>"
    })
    Page<RoomFloorPlanVo> selectPageList(Page<RoomFloorPlanVo> page,
                                          @Param("bo") RoomFloorPlanQueryBo bo,
                                          @Param("deptId") Long deptId,
                                          @Param("userId") Long userId,
                                          @Param("canManageAll") boolean canManageAll,
                                          @Param("canManage") boolean canManage);

    @Select({
        "<script>",
        "select p.id, p.dept_id, d.dept_name, p.plan_name, p.building_name, p.floor_name, p.floor_no,",
        "p.map_image, p.map_data, p.status, p.remark, p.create_time",
        "from dm_room_floor_plan p left join sys_dept d on d.dept_id = p.dept_id",
        "where p.del_flag = '0'",
        "and (#{canManageAll} or (#{canManage} and p.dept_id = #{deptId}) or (p.status = 'ENABLED' and exists (select 1 from dm_room_resource r where r.floor_plan_id = p.id and r.del_flag = '0' and (",
        "  (not exists (select 1 from dm_room_resource_acl a0 where a0.room_id = r.id and a0.permission_type = 'VIEW' and a0.del_flag = '0') and (r.scope_type = 'PUBLIC' or r.dept_id = #{deptId}))",
        "  or exists (select 1 from dm_room_resource_acl a1 where a1.room_id = r.id and a1.permission_type = 'VIEW' and a1.del_flag = '0' and (a1.subject_type = 'ALL' or (a1.subject_type = 'USER' and a1.subject_id = #{userId}) or (a1.subject_type = 'DEPT' and a1.subject_id = #{deptId}) or (a1.subject_type = 'ROLE' and exists (select 1 from sys_user_role ur1 where ur1.user_id = #{userId} and ur1.role_id = a1.subject_id)))",
        ")))))",
        "order by p.building_name asc, p.floor_no asc, p.floor_name asc, p.id asc",
        "</script>"
    })
    List<RoomFloorPlanVo> selectOptions(@Param("deptId") Long deptId,
                                        @Param("userId") Long userId,
                                        @Param("canManageAll") boolean canManageAll,
                                        @Param("canManage") boolean canManage);
}
