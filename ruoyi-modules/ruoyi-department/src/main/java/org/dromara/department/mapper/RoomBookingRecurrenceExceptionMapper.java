package org.dromara.department.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.RoomBookingRecurrenceException;
import org.dromara.department.domain.vo.RoomBookingRecurrenceExceptionVo;

import java.util.List;

/** 循环预约例外数据层。 */
@Mapper
public interface RoomBookingRecurrenceExceptionMapper extends BaseMapperPlus<RoomBookingRecurrenceException, RoomBookingRecurrenceExceptionVo> {

    @Select({
        "select id, booking_id, occurrence_no, occurrence_date, reason, status, create_time",
        "from dm_room_booking_recurrence_exception",
        "where booking_id = #{bookingId} and del_flag = '0' and status = 'SKIPPED'",
        "order by occurrence_no asc"
    })
    List<RoomBookingRecurrenceExceptionVo> selectVoByBookingId(@Param("bookingId") Long bookingId);

    @Select({
        "select * from dm_room_booking_recurrence_exception",
        "where booking_id = #{bookingId} and occurrence_no = #{occurrenceNo} and del_flag = '0'"
    })
    RoomBookingRecurrenceException selectByBookingAndOccurrence(@Param("bookingId") Long bookingId,
                                                                @Param("occurrenceNo") Integer occurrenceNo);
}
