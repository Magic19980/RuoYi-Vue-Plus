package org.dromara.department.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.PlatformFeedback;
import org.dromara.department.domain.bo.PlatformFeedbackQueryBo;
import org.dromara.department.domain.vo.PlatformFeedbackSummaryVo;
import org.dromara.department.domain.vo.PlatformFeedbackUserOptionVo;
import org.dromara.department.domain.vo.PlatformFeedbackVo;

import java.util.List;

/**
 * 平台问题与建议数据层。
 */
@Mapper
public interface PlatformFeedbackMapper extends BaseMapperPlus<PlatformFeedback, PlatformFeedbackVo> {

    @Select({
        "<script>",
        "select f.id, f.feedback_no, f.feedback_type, f.title, f.description, f.module_name, f.page_title, f.page_path, f.business_ref,",
        "f.reproduce_steps, f.expected_result, f.actual_result, f.impact_scope, f.priority, f.status, f.assignee_id,",
        "f.resolution_note, f.attachment_oss_ids, f.closed_at, f.create_by, f.create_time, f.update_time,",
        "coalesce(reporter.nick_name, reporter.user_name) as reporter_name, rd.dept_name as reporter_dept_name,",
        "coalesce(assignee.nick_name, assignee.user_name) as assignee_name, ad.dept_name as assignee_dept_name,",
        "case when f.create_by = #{userId} then 1 else 0 end as mine, case when f.assignee_id = #{userId} then 1 else 0 end as assigned_to_me",
        "from dm_platform_feedback f",
        "left join sys_user reporter on reporter.user_id = f.create_by and reporter.del_flag = '0'",
        "left join sys_dept rd on rd.dept_id = f.create_dept and rd.del_flag = '0'",
        "left join sys_user assignee on assignee.user_id = f.assignee_id and assignee.del_flag = '0'",
        "left join sys_dept ad on ad.dept_id = assignee.dept_id and ad.del_flag = '0'",
        "where f.del_flag = '0'",
        "and (#{all} = true or #{handler} = true or f.create_by = #{userId} or f.assignee_id = #{userId})",
        "<if test='bo.feed == &quot;MINE&quot;'> and f.create_by = #{userId} </if>",
        "<if test='bo.feed == &quot;TODO&quot;'> and f.assignee_id = #{userId} and f.status not in ('CLOSED', 'REJECTED') </if>",
        "<if test='bo.feed == &quot;FOLLOWING&quot;'> and (f.create_by = #{userId} or f.assignee_id = #{userId}) </if>",
        "<if test='bo.keyword != null and bo.keyword != &quot;&quot;'> and (f.feedback_no like concat('%', #{bo.keyword}, '%') or f.title like concat('%', #{bo.keyword}, '%') or f.description like concat('%', #{bo.keyword}, '%')) </if>",
        "<if test='bo.feedbackType != null and bo.feedbackType != &quot;&quot;'> and f.feedback_type = #{bo.feedbackType} </if>",
        "<if test='bo.priority != null and bo.priority != &quot;&quot;'> and f.priority = #{bo.priority} </if>",
        "<if test='bo.status != null and bo.status != &quot;&quot;'> and f.status = #{bo.status} </if>",
        "<if test='bo.moduleName != null and bo.moduleName != &quot;&quot;'> and f.module_name = #{bo.moduleName} </if>",
        "<if test='bo.assigneeId != null'> and f.assignee_id = #{bo.assigneeId} </if>",
        "order by case when f.status in ('PENDING', 'PROCESSING') then 0 else 1 end, f.priority = 'URGENT' desc, f.update_time desc, f.id desc",
        "</script>"
    })
    Page<PlatformFeedbackVo> selectPageList(Page<PlatformFeedbackVo> page,
                                            @Param("bo") PlatformFeedbackQueryBo bo,
                                            @Param("userId") Long userId,
                                            @Param("all") boolean all,
                                            @Param("handler") boolean handler);

    @Select({
        "select f.id, f.feedback_no, f.feedback_type, f.title, f.description, f.module_name, f.page_title, f.page_path, f.business_ref,",
        "f.reproduce_steps, f.expected_result, f.actual_result, f.impact_scope, f.priority, f.status, f.assignee_id,",
        "f.resolution_note, f.attachment_oss_ids, f.closed_at, f.create_by, f.create_time, f.update_time,",
        "coalesce(reporter.nick_name, reporter.user_name) as reporter_name, rd.dept_name as reporter_dept_name,",
        "coalesce(assignee.nick_name, assignee.user_name) as assignee_name, ad.dept_name as assignee_dept_name,",
        "case when f.create_by = #{userId} then 1 else 0 end as mine, case when f.assignee_id = #{userId} then 1 else 0 end as assigned_to_me",
        "from dm_platform_feedback f",
        "left join sys_user reporter on reporter.user_id = f.create_by and reporter.del_flag = '0'",
        "left join sys_dept rd on rd.dept_id = f.create_dept and rd.del_flag = '0'",
        "left join sys_user assignee on assignee.user_id = f.assignee_id and assignee.del_flag = '0'",
        "left join sys_dept ad on ad.dept_id = assignee.dept_id and ad.del_flag = '0'",
        "where f.id = #{id} and f.del_flag = '0'",
        "and (#{all} = true or #{handler} = true or f.create_by = #{userId} or f.assignee_id = #{userId})"
    })
    PlatformFeedbackVo selectDetailById(@Param("id") Long id, @Param("userId") Long userId,
                                        @Param("all") boolean all, @Param("handler") boolean handler);

    @Select({
        "<script>",
        "select count(1) as total_count,",
        "sum(case when f.status = 'PENDING' then 1 else 0 end) as pending_count,",
        "sum(case when f.status = 'PROCESSING' then 1 else 0 end) as processing_count,",
        "sum(case when f.status = 'WAITING_CONFIRM' then 1 else 0 end) as waiting_count,",
        "sum(case when f.status = 'CLOSED' then 1 else 0 end) as closed_count,",
        "sum(case when f.feedback_type = 'SUGGESTION' then 1 else 0 end) as suggestion_count",
        "from dm_platform_feedback f where f.del_flag = '0'",
        "and (#{all} = true or #{handler} = true or f.create_by = #{userId} or f.assignee_id = #{userId})",
        "</script>"
    })
    PlatformFeedbackSummaryVo selectSummary(@Param("userId") Long userId, @Param("all") boolean all,
                                             @Param("handler") boolean handler);

    @Select({
        "<script>",
        "select u.user_id, coalesce(u.nick_name, u.user_name) as user_name, u.dept_id, d.dept_name",
        "from sys_user u",
        "left join sys_dept d on d.dept_id = u.dept_id and d.del_flag = '0'",
        "where u.del_flag = '0' and u.status = '0'",
        "<if test='keyword != null and keyword != \"\"'>",
        "and (u.nick_name like concat('%', #{keyword}, '%')",
        "or u.user_name like concat('%', #{keyword}, '%')",
        "or d.dept_name like concat('%', #{keyword}, '%'))",
        "</if>",
        "order by d.dept_name, u.nick_name, u.user_name",
        "limit 50",
        "</script>"
    })
    List<PlatformFeedbackUserOptionVo> selectUserOptions(@Param("keyword") String keyword);

    @Select("select u.user_id, coalesce(u.nick_name, u.user_name) as user_name, u.dept_id, d.dept_name from sys_user u left join sys_dept d on d.dept_id = u.dept_id and d.del_flag = '0' where u.user_id = #{userId} and u.del_flag = '0' and u.status = '0'")
    PlatformFeedbackUserOptionVo selectActiveUserOption(@Param("userId") Long userId);
}
