package org.dromara.department.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.RoomAmenityRelation;

import java.util.List;

/** 房间与设施关联数据层。 */
@Mapper
public interface RoomAmenityRelationMapper extends BaseMapperPlus<RoomAmenityRelation, RoomAmenityRelation> {

    @Select("select amenity_id from dm_room_amenity_relation where room_id = #{roomId} and del_flag = '0' order by id asc")
    List<Long> selectAmenityIds(@Param("roomId") Long roomId);

    @Select({
        "select a.amenity_name from dm_room_amenity_relation x",
        "join dm_room_amenity a on a.id = x.amenity_id and a.del_flag = '0'",
        "where x.room_id = #{roomId} and x.del_flag = '0' order by x.id asc"
    })
    List<String> selectAmenityNames(@Param("roomId") Long roomId);

    default void deleteByRoomId(Long roomId) {
        delete(Wrappers.<RoomAmenityRelation>lambdaQuery().eq(RoomAmenityRelation::getRoomId, roomId));
    }
}
