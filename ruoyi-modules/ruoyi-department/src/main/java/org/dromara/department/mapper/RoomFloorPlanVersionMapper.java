package org.dromara.department.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.RoomFloorPlanVersion;
import org.dromara.department.domain.vo.RoomFloorPlanVersionVo;

import java.util.List;

/** 平面图版本快照数据层。 */
@Mapper
public interface RoomFloorPlanVersionMapper extends BaseMapperPlus<RoomFloorPlanVersion, RoomFloorPlanVersionVo> {

    @Select({
        "select v.id, v.floor_plan_id, v.version_no, v.plan_name, v.building_name, v.floor_name,",
        "v.floor_no, v.map_image, v.map_data, v.status, v.remark, v.version_note,",
        "v.create_by, u.nick_name as create_by_name, v.create_time",
        "from dm_room_floor_plan_version v left join sys_user u on u.user_id = v.create_by",
        "where v.floor_plan_id = #{floorPlanId}",
        "order by v.version_no desc, v.id desc"
    })
    List<RoomFloorPlanVersionVo> selectByFloorPlanId(@Param("floorPlanId") Long floorPlanId);

    @Select({
        "select id, floor_plan_id, version_no, plan_name, building_name, floor_name, floor_no,",
        "map_image, map_data, status, remark, version_note, create_by, create_time",
        "from dm_room_floor_plan_version where id = #{id} limit 1"
    })
    RoomFloorPlanVersion selectSnapshotById(@Param("id") Long id);

    @Select("select coalesce(max(version_no), 0) from dm_room_floor_plan_version where floor_plan_id = #{floorPlanId}")
    Integer selectMaxVersionNo(@Param("floorPlanId") Long floorPlanId);
}
