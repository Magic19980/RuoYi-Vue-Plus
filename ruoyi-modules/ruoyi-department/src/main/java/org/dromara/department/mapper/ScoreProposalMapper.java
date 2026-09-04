package org.dromara.department.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.ScoreProposal;
import org.dromara.department.domain.ScoreProposalStatus;
import org.dromara.department.domain.bo.ScoreProposalQueryBo;
import org.dromara.department.domain.vo.ScoreProposalMetricVo;
import org.dromara.department.domain.vo.ScoreProposalVo;
import org.dromara.department.service.DepartmentScope;

import java.time.LocalDateTime;

/** SCORE提案数据层。 */
@Mapper
public interface ScoreProposalMapper extends BaseMapperPlus<ScoreProposal, ScoreProposalVo> {

    /** 按权限范围分页查询SCORE提案。 */
    @Select({
        "<script>",
        "select s.id, s.dept_id, s.main_category_id, s.sub_category_id, s.proposer_user_id, s.company_name, s.team_member_user_ids, s.employee_no, s.proposer_name,",
        "post.job_title as proposer_role,",
        "s.proposer_level, s.dept_name, s.main_category, s.sub_category,",
        "s.problem_description, s.improvement_measure, s.implementer_supervisor, s.implementer_user_ids, s.before_oss_id,",
        "s.after_oss_id, s.start_date, s.planned_completion_date, s.actual_completion_date,",
        "s.completion_status, s.remark, s.review_status, s.review_comment, s.reviewer_user_id, s.reviewed_at,",
        "s.review_file_oss_id, s.review_file_name, s.revision_no, s.submitted_at, s.submitted_by,",
        "s.confirm_comment, s.confirmer_user_id, s.confirmed_at, s.create_time, s.update_time",
        "from dm_score_proposal s",
        "left join (select sup.user_id, group_concat(distinct sp.post_name order by sp.post_sort, sp.post_id separator '、') as job_title "
            + "from sys_user_post sup join sys_post sp on sp.post_id = sup.post_id and sp.status = '0' and sp.del_flag = '0' "
            + "group by sup.user_id) post on post.user_id = s.proposer_user_id",
        "where s.del_flag = '0'",
        "<if test='bo.proposerName != null and bo.proposerName != \"\"'> and s.proposer_name like concat('%', #{bo.proposerName}, '%') </if>",
        "<if test='bo.mainCategory != null and bo.mainCategory != \"\"'> and s.main_category = #{bo.mainCategory} </if>",
        "<if test='bo.subCategory != null and bo.subCategory != \"\"'> and s.sub_category = #{bo.subCategory} </if>",
        "<if test='bo.completionStatus != null and bo.completionStatus != \"\"'> and s.completion_status = #{bo.completionStatus} </if>",
        "<if test='bo.reviewStatus != null and bo.reviewStatus != \"\"'> and s.review_status = #{bo.reviewStatus} </if>",
        "<if test='bo.beginDate != null'> and s.start_date &gt;= #{bo.beginDate} </if>",
        "<if test='bo.endDate != null'> and s.start_date &lt;= #{bo.endDate} </if>",
        "<choose>",
        "<when test='scope.all'></when>",
        "<otherwise> and s.dept_id = #{scope.deptId} </otherwise>",
        "</choose>",
        "order by s.start_date desc, s.id desc",
        "</script>"
    })
    Page<ScoreProposal> selectPageList(Page<ScoreProposal> page,
                                          @Param("bo") ScoreProposalQueryBo bo,
                                          @Param("scope") DepartmentScope scope);

    /**
     * 一次查询月度精益指标所需的基础数据，减少首页看板的数据库往返次数。
     * 已通过数按现场确认时间统计，状态总数按提交审核时间统计，保持原业务口径。
     *
     * @param deptId    业务科室主键
     * @param monthStart 月份开始日期
     * @param monthEnd   月份结束日期
     * @param beginAt    统计开始时间
     * @param endAt      统计结束时间
     * @return 月度精益指标统计结果
     */
    @Select("select "
        + "(select count(distinct p.user_id) from dm_person_profile p "
        + "join sys_user u on u.user_id = p.user_id and u.del_flag = '0' and u.status = '0' "
        + "where p.create_dept = #{deptId} and p.del_flag = '0' "
        + "and p.join_date <= #{monthEnd} and (p.leave_date is null or p.leave_date > #{monthStart})) as member_count, "
        + "coalesce(sum(case when s.review_status = '" + ScoreProposalStatus.APPROVED + "' "
        + "and s.confirmed_at >= #{beginAt} and s.confirmed_at < #{endAt} then 1 else 0 end), 0) as approved_count, "
        + "coalesce(sum(case when s.review_status <> '" + ScoreProposalStatus.DRAFT + "' "
        + "and s.submitted_at >= #{beginAt} and s.submitted_at < #{endAt} then 1 else 0 end), 0) as total_count, "
        + "coalesce(sum(case when s.review_status = '" + ScoreProposalStatus.APPROVED + "' "
        + "and s.submitted_at >= #{beginAt} and s.submitted_at < #{endAt} then 1 else 0 end), 0) as status_approved_count, "
        + "coalesce(sum(case when s.review_status = '" + ScoreProposalStatus.PENDING + "' "
        + "and s.submitted_at >= #{beginAt} and s.submitted_at < #{endAt} then 1 else 0 end), 0) as pending_count, "
        + "coalesce(sum(case when s.review_status = '" + ScoreProposalStatus.PENDING_CONFIRM + "' "
        + "and s.submitted_at >= #{beginAt} and s.submitted_at < #{endAt} then 1 else 0 end), 0) as pending_confirm_count, "
        + "coalesce(sum(case when s.review_status = '" + ScoreProposalStatus.REJECTED + "' "
        + "and s.submitted_at >= #{beginAt} and s.submitted_at < #{endAt} then 1 else 0 end), 0) as rejected_count "
        + "from dm_score_proposal s where s.dept_id = #{deptId} and s.del_flag = '0'")
    ScoreProposalMetricVo selectMetricStats(@Param("deptId") Long deptId,
                                             @Param("monthStart") java.time.LocalDate monthStart,
                                             @Param("monthEnd") java.time.LocalDate monthEnd,
                                             @Param("beginAt") LocalDateTime beginAt,
                                             @Param("endAt") LocalDateTime endAt);
}
