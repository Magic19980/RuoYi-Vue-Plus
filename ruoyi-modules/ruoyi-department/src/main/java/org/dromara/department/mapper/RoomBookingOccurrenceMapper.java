package org.dromara.department.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.RoomBookingOccurrence;
import org.dromara.department.domain.vo.RoomAnalyticsRoomVo;
import org.dromara.department.domain.vo.RoomAnalyticsSummaryVo;
import org.dromara.department.domain.vo.RoomAnalyticsDimensionVo;
import org.dromara.department.domain.vo.RoomAnalyticsHotSlotVo;
import org.dromara.department.domain.vo.RoomAnalyticsApprovalVo;

import java.time.LocalDateTime;
import java.util.List;

/** 预约实例数据层。 */
@Mapper
public interface RoomBookingOccurrenceMapper extends BaseMapperPlus<RoomBookingOccurrence, RoomBookingOccurrence> {

    @Select("select * from dm_room_booking_occurrence where booking_id = #{bookingId} and del_flag = '0' order by occurrence_no asc, room_id asc")
    List<RoomBookingOccurrence> selectByBookingId(@Param("bookingId") Long bookingId);

    @Select({
        "select o.* from dm_room_booking_occurrence o join dm_room_booking b on b.id = o.booking_id",
        "where o.room_id = #{roomId} and o.del_flag = '0' and b.del_flag = '0'",
        "and o.status in ('PENDING', 'CONFIRMED', 'IN_USE', 'COMPLETED')",
        "and o.start_at < #{endAt} and o.end_at > #{startAt}",
        "and (#{excludeBookingId} is null or o.booking_id <> #{excludeBookingId})",
        "order by o.start_at asc"
    })
    List<RoomBookingOccurrence> selectConflicts(@Param("roomId") Long roomId,
                                                 @Param("startAt") LocalDateTime startAt,
                                                 @Param("endAt") LocalDateTime endAt,
                                                 @Param("excludeBookingId") Long excludeBookingId);

    @Select({
        "select * from dm_room_booking_occurrence",
        "where del_flag = '0' and status = 'CONFIRMED' and start_at <= #{beforeAt}",
        "order by start_at asc limit 500"
    })
    List<RoomBookingOccurrence> selectNoShowCandidates(@Param("beforeAt") LocalDateTime beforeAt);

    @Select({
        "select * from dm_room_booking_occurrence",
        "where del_flag = '0' and status = 'IN_USE' and end_at <= #{now}",
        "order by end_at asc limit 500"
    })
    List<RoomBookingOccurrence> selectExpiredInUseCandidates(@Param("now") LocalDateTime now);

    @Select({
        "select * from dm_room_booking_occurrence",
        "where del_flag = '0' and status = 'PENDING' and approval_due_at is not null and approval_due_at <= #{now}",
        "order by approval_due_at asc limit 500"
    })
    List<RoomBookingOccurrence> selectApprovalTimeoutCandidates(@Param("now") LocalDateTime now);

    @Select({
        "<script>",
        "select count(1) as total_bookings,",
        "coalesce(sum(case when o.status in ('CONFIRMED', 'IN_USE', 'COMPLETED', 'RELEASED') then timestampdiff(minute, o.start_at, o.end_at) else 0 end), 0) as total_minutes,",
        "coalesce(sum(case when o.status = 'COMPLETED' then 1 else 0 end), 0) as completed_count,",
        "coalesce(sum(case when o.status = 'CANCELLED' then 1 else 0 end), 0) as cancelled_count,",
        "coalesce(sum(case when o.status = 'RELEASED' and o.check_in_at is null and o.cancellation_reason like '超过15分钟未签到%' then 1 else 0 end), 0) as no_show_count,",
        "coalesce(sum(case when o.status = 'RELEASED' then 1 else 0 end), 0) as released_count",
        "from dm_room_booking_occurrence o join dm_room_resource r on r.id = o.room_id and r.del_flag = '0'",
        "where o.del_flag = '0' and o.start_at &lt; #{endAt} and o.end_at &gt; #{beginAt}",
        "<choose>",
        "<when test='canManageAll'></when>",
        "<otherwise> and r.dept_id = #{deptId} </otherwise>",
        "</choose>",
        "</script>"
    })
    RoomAnalyticsSummaryVo selectAnalyticsSummary(@Param("beginAt") LocalDateTime beginAt,
                                                   @Param("endAt") LocalDateTime endAt,
                                                   @Param("deptId") Long deptId,
                                                   @Param("canManageAll") boolean canManageAll);

    @Select({
        "<script>",
        "select o.room_id, r.room_name, r.room_code, count(1) as booking_count,",
        "coalesce(sum(case when o.status in ('CONFIRMED', 'IN_USE', 'COMPLETED', 'RELEASED') then timestampdiff(minute, o.start_at, o.end_at) else 0 end), 0) as booked_minutes,",
        "coalesce(sum(case when o.status = 'COMPLETED' then 1 else 0 end), 0) as completed_count,",
        "coalesce(sum(case when o.status = 'CANCELLED' then 1 else 0 end), 0) as cancelled_count,",
        "coalesce(sum(case when o.status = 'RELEASED' and o.check_in_at is null and o.cancellation_reason like '超过15分钟未签到%' then 1 else 0 end), 0) as no_show_count",
        "from dm_room_booking_occurrence o join dm_room_resource r on r.id = o.room_id and r.del_flag = '0'",
        "where o.del_flag = '0' and o.start_at &lt; #{endAt} and o.end_at &gt; #{beginAt}",
        "<choose>",
        "<when test='canManageAll'></when>",
        "<otherwise> and r.dept_id = #{deptId} </otherwise>",
        "</choose>",
        "group by o.room_id, r.room_name, r.room_code",
        "order by booked_minutes desc, booking_count desc, o.room_id asc",
        "</script>"
    })
    List<RoomAnalyticsRoomVo> selectAnalyticsRooms(@Param("beginAt") LocalDateTime beginAt,
                                                    @Param("endAt") LocalDateTime endAt,
                                                    @Param("deptId") Long deptId,
                                                    @Param("canManageAll") boolean canManageAll);

    @Select({
        "<script>",
        "select b.dept_id as dimension_id, coalesce(d.dept_name, concat('科室#', b.dept_id)) as dimension_name,",
        "count(distinct b.id) as booking_count, coalesce(sum(case when o.status in ('CONFIRMED', 'IN_USE', 'COMPLETED', 'RELEASED') then timestampdiff(minute, o.start_at, o.end_at) else 0 end), 0) as booked_minutes,",
        "coalesce(sum(case when o.status = 'RELEASED' and o.check_in_at is null and o.cancellation_reason like '超过15分钟未签到%' then 1 else 0 end), 0) as no_show_count",
        "from dm_room_booking_occurrence o join dm_room_booking b on b.id = o.booking_id and b.del_flag = '0'",
        "left join sys_dept d on d.dept_id = b.dept_id",
        "join dm_room_resource r on r.id = o.room_id and r.del_flag = '0'",
        "where o.del_flag = '0' and o.start_at &lt; #{endAt} and o.end_at &gt; #{beginAt}",
        "<choose><when test='canManageAll'></when><otherwise>and r.dept_id = #{deptId}</otherwise></choose>",
        "group by b.dept_id, d.dept_name order by booked_minutes desc, booking_count desc",
        "</script>"
    })
    List<RoomAnalyticsDimensionVo> selectAnalyticsDepartments(@Param("beginAt") LocalDateTime beginAt,
                                                               @Param("endAt") LocalDateTime endAt,
                                                               @Param("deptId") Long deptId,
                                                               @Param("canManageAll") boolean canManageAll);

    @Select({
        "<script>",
        "select b.organizer_id as dimension_id, coalesce(u.nick_name, u.user_name, concat('用户#', b.organizer_id)) as dimension_name,",
        "count(distinct b.id) as booking_count, coalesce(sum(case when o.status in ('CONFIRMED', 'IN_USE', 'COMPLETED', 'RELEASED') then timestampdiff(minute, o.start_at, o.end_at) else 0 end), 0) as booked_minutes,",
        "coalesce(sum(case when o.status = 'RELEASED' and o.check_in_at is null and o.cancellation_reason like '超过15分钟未签到%' then 1 else 0 end), 0) as no_show_count",
        "from dm_room_booking_occurrence o join dm_room_booking b on b.id = o.booking_id and b.del_flag = '0'",
        "left join sys_user u on u.user_id = b.organizer_id",
        "join dm_room_resource r on r.id = o.room_id and r.del_flag = '0'",
        "where o.del_flag = '0' and o.start_at &lt; #{endAt} and o.end_at &gt; #{beginAt}",
        "<choose><when test='canManageAll'></when><otherwise>and r.dept_id = #{deptId}</otherwise></choose>",
        "group by b.organizer_id, u.nick_name, u.user_name order by booked_minutes desc, booking_count desc limit 100",
        "</script>"
    })
    List<RoomAnalyticsDimensionVo> selectAnalyticsUsers(@Param("beginAt") LocalDateTime beginAt,
                                                         @Param("endAt") LocalDateTime endAt,
                                                         @Param("deptId") Long deptId,
                                                         @Param("canManageAll") boolean canManageAll);

    @Select({
        "<script>",
        "select hour(o.start_at) as hour_of_day, count(1) as booking_count,",
        "coalesce(sum(case when o.status in ('CONFIRMED', 'IN_USE', 'COMPLETED', 'RELEASED') then timestampdiff(minute, o.start_at, o.end_at) else 0 end), 0) as booked_minutes",
        "from dm_room_booking_occurrence o join dm_room_resource r on r.id = o.room_id and r.del_flag = '0'",
        "where o.del_flag = '0' and o.start_at &lt; #{endAt} and o.end_at &gt; #{beginAt}",
        "<choose><when test='canManageAll'></when><otherwise>and r.dept_id = #{deptId}</otherwise></choose>",
        "group by hour(o.start_at) order by booking_count desc, hour_of_day asc",
        "</script>"
    })
    List<RoomAnalyticsHotSlotVo> selectAnalyticsHotSlots(@Param("beginAt") LocalDateTime beginAt,
                                                          @Param("endAt") LocalDateTime endAt,
                                                          @Param("deptId") Long deptId,
                                                          @Param("canManageAll") boolean canManageAll);

    @Select({
        "<script>",
        "select",
        "coalesce(sum(case when action = 'SUBMIT' then 1 else 0 end), 0) as submit_count,",
        "coalesce(sum(case when action = 'APPROVE' then 1 else 0 end), 0) as approve_count,",
        "coalesce(sum(case when action in ('REJECT', 'AUTO_REJECT') then 1 else 0 end), 0) as reject_count,",
        "coalesce(avg(case when a.action = 'APPROVE' then timestampdiff(minute, (select min(s.create_time) from dm_room_booking_approval s where s.booking_id = a.booking_id and s.action = 'SUBMIT' and s.create_time &lt;= a.create_time and s.del_flag = '0'), a.create_time) end), 0) as average_minutes,",
        "group_concat(distinct case when action in ('REJECT', 'AUTO_REJECT') and reason is not null then reason end separator '；') as reject_reasons",
        "from dm_room_booking_approval a join dm_room_booking b on b.id = a.booking_id and b.del_flag = '0'",
        "where a.del_flag = '0' and a.create_time >= #{beginAt} and a.create_time &lt; #{endAt}",
        "<if test='!canManageAll'>and b.dept_id = #{deptId}</if>",
        "</script>"
    })
    RoomAnalyticsApprovalVo selectAnalyticsApproval(@Param("beginAt") LocalDateTime beginAt,
                                                     @Param("endAt") LocalDateTime endAt,
                                                     @Param("deptId") Long deptId,
                                                     @Param("canManageAll") boolean canManageAll);
}
