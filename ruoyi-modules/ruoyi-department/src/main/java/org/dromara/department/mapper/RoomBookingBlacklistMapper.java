package org.dromara.department.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.RoomBookingBlacklist;
import org.dromara.department.domain.vo.RoomBookingBlacklistVo;

import java.util.List;

/** 房间预约黑名单数据层。 */
@Mapper
public interface RoomBookingBlacklistMapper extends BaseMapperPlus<RoomBookingBlacklist, RoomBookingBlacklistVo> {

    @Select({
        "select b.id, b.room_id, r.room_name, b.subject_type, b.subject_id,",
        "case when b.subject_type = 'ALL' then '全公司'",
        "when b.subject_type = 'USER' then coalesce(u.nick_name, u.user_name)",
        "when b.subject_type = 'DEPT' then d.dept_name",
        "when b.subject_type = 'ROLE' then ro.role_name else '未知主体' end as subject_name,",
        "b.reason, b.enabled, b.create_time",
        "from dm_room_booking_blacklist b",
        "join dm_room_resource r on r.id = b.room_id and r.del_flag = '0'",
        "left join sys_user u on b.subject_type = 'USER' and u.user_id = b.subject_id",
        "left join sys_dept d on b.subject_type = 'DEPT' and d.dept_id = b.subject_id",
        "left join sys_role ro on b.subject_type = 'ROLE' and ro.role_id = b.subject_id",
        "where b.room_id = #{roomId} and b.del_flag = '0'",
        "order by b.enabled desc, b.subject_type asc, b.id asc"
    })
    List<RoomBookingBlacklistVo> selectVoByRoomId(@Param("roomId") Long roomId);

    @Select({
        "select count(1) from dm_room_booking_blacklist b",
        "where b.room_id = #{roomId} and b.del_flag = '0' and b.enabled = 1",
        "and (b.subject_type = 'ALL'",
        "or (b.subject_type = 'USER' and b.subject_id = #{userId})",
        "or (b.subject_type = 'DEPT' and b.subject_id = #{deptId})",
        "or (b.subject_type = 'ROLE' and exists (select 1 from sys_user_role ur where ur.user_id = #{userId} and ur.role_id = b.subject_id)))"
    })
    long countMatched(@Param("roomId") Long roomId,
                      @Param("userId") Long userId,
                      @Param("deptId") Long deptId);
}
