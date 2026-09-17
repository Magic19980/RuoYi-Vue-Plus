package org.dromara.department.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.RoomApprovalRule;
import org.dromara.department.domain.vo.RoomApprovalRuleVo;

import java.util.List;

/** 房间预约审批规则数据层。 */
@Mapper
public interface RoomApprovalRuleMapper extends BaseMapperPlus<RoomApprovalRule, RoomApprovalRuleVo> {

    @Select({
        "select a.id, a.room_id, r.room_name, a.step_no, a.approver_type, a.approver_id,",
        "case when a.approver_type = 'ALL' then '全公司'",
        "when a.approver_type = 'USER' then coalesce(u.nick_name, u.user_name)",
        "when a.approver_type = 'DEPT' then d.dept_name",
        "when a.approver_type = 'ROLE' then ro.role_name else '未知主体' end as approver_name,",
        "a.timeout_minutes, a.enabled, a.remark, a.create_time",
        "from dm_room_approval_rule a",
        "join dm_room_resource r on r.id = a.room_id and r.del_flag = '0'",
        "left join sys_user u on a.approver_type = 'USER' and u.user_id = a.approver_id",
        "left join sys_dept d on a.approver_type = 'DEPT' and d.dept_id = a.approver_id",
        "left join sys_role ro on a.approver_type = 'ROLE' and ro.role_id = a.approver_id",
        "where a.room_id = #{roomId} and a.del_flag = '0'",
        "order by a.step_no asc, a.id asc"
    })
    List<RoomApprovalRuleVo> selectVoByRoomId(@Param("roomId") Long roomId);

    @Select({
        "select * from dm_room_approval_rule",
        "where room_id = #{roomId} and del_flag = '0' and enabled = 1",
        "order by step_no asc, id asc"
    })
    List<RoomApprovalRule> selectEnabledByRoomId(@Param("roomId") Long roomId);

    @Select("select count(1) from sys_user_role where user_id = #{userId} and role_id = #{roleId}")
    long countRoleMember(@Param("roleId") Long roleId, @Param("userId") Long userId);

    @Select({
        "select distinct u.user_id from sys_user u",
        "join dm_room_approval_rule a on a.room_id = #{roomId} and a.step_no = #{stepNo} and a.approver_type = 'USER' and a.approver_id = u.user_id and a.enabled = 1 and a.del_flag = '0'",
        "where u.del_flag = '0' and u.status = '0'",
        "union",
        "select distinct u.user_id from sys_user u",
        "join dm_room_approval_rule a on a.room_id = #{roomId} and a.step_no = #{stepNo} and a.approver_type = 'DEPT' and a.approver_id = u.dept_id and a.enabled = 1 and a.del_flag = '0'",
        "where u.del_flag = '0' and u.status = '0'",
        "union",
        "select distinct ur.user_id from sys_user_role ur",
        "join dm_room_approval_rule a on a.room_id = #{roomId} and a.step_no = #{stepNo} and a.approver_type = 'ROLE' and a.approver_id = ur.role_id and a.enabled = 1 and a.del_flag = '0'",
        "join sys_user u on u.user_id = ur.user_id and u.del_flag = '0' and u.status = '0'"
    })
    List<Long> selectApproverIds(@Param("roomId") Long roomId, @Param("stepNo") Integer stepNo);

    default RoomApprovalRule selectEnabledStep(Long roomId, Integer stepNo) {
        return selectOne(Wrappers.<RoomApprovalRule>lambdaQuery()
            .eq(RoomApprovalRule::getRoomId, roomId)
            .eq(RoomApprovalRule::getStepNo, stepNo)
            .eq(RoomApprovalRule::getEnabled, true));
    }
}
