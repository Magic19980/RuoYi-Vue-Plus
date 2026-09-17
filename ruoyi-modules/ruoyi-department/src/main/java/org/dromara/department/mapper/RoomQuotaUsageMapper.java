package org.dromara.department.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.RoomQuotaUsage;
import org.dromara.department.domain.vo.RoomQuotaUsageRecordVo;

import java.util.List;

/** 配额流水数据层。 */
@Mapper
public interface RoomQuotaUsageMapper extends BaseMapperPlus<RoomQuotaUsage, RoomQuotaUsageRecordVo> {

    @Select({
        "select u.id, u.room_id, u.booking_id, u.occurrence_no, u.user_id,",
        "coalesce(su.nick_name, su.user_name) as user_name, u.action, u.minutes, u.booking_count, u.amount, u.reason, u.create_time",
        "from dm_room_quota_usage u left join sys_user su on su.user_id = u.user_id",
        "where u.room_id = #{roomId} and u.del_flag = '0' order by u.create_time desc, u.id desc"
    })
    List<RoomQuotaUsageRecordVo> selectVoByRoomId(@Param("roomId") Long roomId);
}
