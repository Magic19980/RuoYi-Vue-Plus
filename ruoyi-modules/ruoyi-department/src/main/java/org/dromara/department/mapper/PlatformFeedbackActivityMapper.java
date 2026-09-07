package org.dromara.department.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.PlatformFeedbackActivity;
import org.dromara.department.domain.vo.PlatformFeedbackActivityVo;

import java.util.List;

/**
 * 平台问题与建议操作记录数据层。
 */
@Mapper
public interface PlatformFeedbackActivityMapper extends BaseMapperPlus<PlatformFeedbackActivity, PlatformFeedbackActivityVo> {

    @Select({
        "select a.id, a.action_type, a.action_note, a.from_status, a.to_status,",
        "coalesce(u.nick_name, u.user_name) as operator_name, a.create_time",
        "from dm_platform_feedback_activity a",
        "left join sys_user u on u.user_id = a.create_by and u.del_flag = '0'",
        "where a.feedback_id = #{feedbackId} and a.del_flag = '0'",
        "order by a.create_time asc, a.id asc"
    })
    List<PlatformFeedbackActivityVo> selectListByFeedbackId(@Param("feedbackId") Long feedbackId);
}
