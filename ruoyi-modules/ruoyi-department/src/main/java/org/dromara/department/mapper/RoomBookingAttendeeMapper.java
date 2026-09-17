package org.dromara.department.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.RoomBookingAttendee;

import java.util.Collection;
import java.util.List;

/** 预约参与者数据层。 */
@Mapper
public interface RoomBookingAttendeeMapper extends BaseMapperPlus<RoomBookingAttendee, RoomBookingAttendee> {

    @Select("select * from dm_room_booking_attendee where booking_id = #{bookingId} and del_flag = '0' order by id asc")
    List<RoomBookingAttendee> selectByBookingId(@Param("bookingId") Long bookingId);

    @Select({
        "<script>",
        "select distinct booking_id from dm_room_booking_attendee where user_id = #{userId} and del_flag = '0' and booking_id in",
        "<foreach collection='bookingIds' item='bookingId' open='(' separator=',' close=')'>#{bookingId}</foreach>",
        "</script>"
    })
    List<Long> selectBookingIdsByUserId(@Param("bookingIds") Collection<Long> bookingIds,
                                        @Param("userId") Long userId);
}
