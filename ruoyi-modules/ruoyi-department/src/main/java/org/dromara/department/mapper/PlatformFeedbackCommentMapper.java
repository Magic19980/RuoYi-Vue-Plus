package org.dromara.department.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.PlatformFeedbackComment;
import org.dromara.department.domain.vo.PlatformFeedbackCommentVo;

/**
 * 平台问题与建议评论数据层。
 */
@Mapper
public interface PlatformFeedbackCommentMapper extends BaseMapperPlus<PlatformFeedbackComment, PlatformFeedbackCommentVo> {

    @Select({
        "select c.id, c.feedback_id, c.content, coalesce(u.nick_name, u.user_name) as author_name, d.dept_name,",
        "case when c.create_by = #{userId} then 1 else 0 end as mine, c.create_time",
        "from dm_platform_feedback_comment c",
        "left join sys_user u on u.user_id = c.create_by and u.del_flag = '0'",
        "left join sys_dept d on d.dept_id = c.create_dept and d.del_flag = '0'",
        "where c.feedback_id = #{feedbackId} and c.del_flag = '0'",
        "order by c.create_time asc, c.id asc"
    })
    Page<PlatformFeedbackCommentVo> selectPageByFeedbackId(Page<PlatformFeedbackCommentVo> page,
                                                            @Param("feedbackId") Long feedbackId,
                                                            @Param("userId") Long userId);
}
