package org.dromara.department.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.RoomBookingBlock;
import org.dromara.department.domain.vo.RoomBookingBlockVo;

import java.time.LocalDateTime;
import java.util.List;

/** 房间封锁时段数据层。 */
@Mapper
public interface RoomBookingBlockMapper extends BaseMapperPlus<RoomBookingBlock, RoomBookingBlockVo> {

    @Select({
        "select b.id, b.room_id, r.room_name, b.start_at, b.end_at, b.reason, b.status, b.create_time",
        "from dm_room_booking_block b join dm_room_resource r on r.id = b.room_id and r.del_flag = '0'",
        "where b.room_id = #{roomId} and b.del_flag = '0' and b.status = 'BLOCKED'",
        "order by b.start_at asc, b.id asc"
    })
    List<RoomBookingBlockVo> selectVoByRoomId(@Param("roomId") Long roomId);

    @Select({
        "select * from dm_room_booking_block",
        "where room_id = #{roomId} and del_flag = '0' and status = 'BLOCKED'",
        "and start_at < #{endAt} and end_at > #{startAt}",
        "order by start_at asc"
    })
    List<RoomBookingBlock> selectConflicts(@Param("roomId") Long roomId,
                                           @Param("startAt") LocalDateTime startAt,
                                           @Param("endAt") LocalDateTime endAt);
}
