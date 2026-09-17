package org.dromara.department.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.RoomQuotaPolicy;
import org.dromara.department.domain.vo.RoomQuotaPolicyVo;
import org.dromara.department.domain.vo.RoomQuotaUsageVo;

import java.time.LocalDateTime;
import java.util.List;

/** 房间预约配额数据层。 */
@Mapper
public interface RoomQuotaPolicyMapper extends BaseMapperPlus<RoomQuotaPolicy, RoomQuotaPolicyVo> {

    @Select({
        "select q.id, q.room_id, r.room_name, q.subject_type, q.subject_id,",
        "case when q.subject_type = 'ALL' then '全公司'",
        "when q.subject_type = 'USER' then coalesce(u.nick_name, u.user_name)",
        "when q.subject_type = 'DEPT' then d.dept_name",
        "when q.subject_type = 'ROLE' then ro.role_name else '未知主体' end as subject_name,",
        "q.period_type, q.quota_minutes, q.quota_count, q.over_quota_action, q.unit_price, q.refund_rate, q.enabled, q.remark, q.create_time",
        "from dm_room_quota_policy q",
        "join dm_room_resource r on r.id = q.room_id and r.del_flag = '0'",
        "left join sys_user u on q.subject_type = 'USER' and u.user_id = q.subject_id",
        "left join sys_dept d on q.subject_type = 'DEPT' and d.dept_id = q.subject_id",
        "left join sys_role ro on q.subject_type = 'ROLE' and ro.role_id = q.subject_id",
        "where q.room_id = #{roomId} and q.del_flag = '0'",
        "order by q.subject_type asc, q.period_type asc, q.id asc"
    })
    List<RoomQuotaPolicyVo> selectVoByRoomId(@Param("roomId") Long roomId);

    @Select({
        "select * from dm_room_quota_policy",
        "where room_id = #{roomId} and del_flag = '0' and enabled = 1",
        "order by id asc"
    })
    List<RoomQuotaPolicy> selectEnabledByRoomId(@Param("roomId") Long roomId);

    @Select("select count(1) from sys_user_role where user_id = #{userId} and role_id = #{roleId}")
    long countRoleMember(@Param("roleId") Long roleId, @Param("userId") Long userId);

    @Select({
        "select coalesce(sum(timestampdiff(minute, o.start_at, o.end_at)), 0) as used_minutes, count(1) as used_count",
        "from dm_room_booking_occurrence o join dm_room_booking b on b.id = o.booking_id and b.del_flag = '0'",
        "where o.room_id = #{roomId} and b.organizer_id = #{userId} and o.del_flag = '0'",
        "and o.status in ('PENDING', 'CONFIRMED', 'IN_USE', 'COMPLETED')",
        "and o.start_at >= #{periodStart} and o.start_at < #{periodEnd}",
        "and (#{excludeBookingId} is null or o.booking_id &lt;&gt; #{excludeBookingId})"
    })
    RoomQuotaUsageVo selectUsage(@Param("roomId") Long roomId,
                                 @Param("userId") Long userId,
                                 @Param("periodStart") LocalDateTime periodStart,
                                 @Param("periodEnd") LocalDateTime periodEnd,
                                 @Param("excludeBookingId") Long excludeBookingId);
}
