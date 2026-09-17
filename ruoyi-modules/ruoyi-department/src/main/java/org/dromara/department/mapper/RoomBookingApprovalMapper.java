package org.dromara.department.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.RoomBookingApproval;
import org.dromara.department.domain.vo.RoomBookingApprovalVo;

import java.util.List;

/** 房间预约审批和状态变更历史数据层。 */
@Mapper
public interface RoomBookingApprovalMapper extends BaseMapperPlus<RoomBookingApproval, RoomBookingApprovalVo> {

    @Select("""
        select a.id, a.booking_id, a.occurrence_no, a.action, a.from_status, a.to_status,
               a.operator_id, coalesce(u.nick_name, u.user_name) as operator_name,
               a.reason, a.create_time
        from dm_room_booking_approval a
        left join sys_user u on u.user_id = a.operator_id
        where a.booking_id = #{bookingId}
        order by a.create_time desc, a.id desc
        """)
    List<RoomBookingApprovalVo> selectVoByBookingId(@Param("bookingId") Long bookingId);
}
