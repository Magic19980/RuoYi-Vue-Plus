package org.dromara.department.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.ScoreProposal;
import org.dromara.department.domain.bo.ScoreProposalQueryBo;
import org.dromara.department.domain.vo.ScoreProposalMetricVo;
import org.dromara.department.domain.vo.ScoreProposalVo;
import org.dromara.department.service.DepartmentScope;

import java.time.LocalDateTime;

/** SCORE提案数据层。 */
@Mapper
public interface ScoreProposalMapper extends BaseMapperPlus<ScoreProposal, ScoreProposalVo> {

    @Select({
        "<script>",
        "select s.id, s.dept_id, s.main_category_id, s.sub_category_id, s.proposer_user_id, s.company_name, s.team_member_user_ids, s.employee_no, s.proposer_name,",
        "(select group_concat(distinct sp.post_name order by sp.post_sort, sp.post_id separator '、') "
            + "from sys_user_post sup join sys_post sp on sp.post_id = sup.post_id and sp.status = '0' and sp.del_flag = '0' "
            + "where sup.user_id = s.proposer_user_id) as proposer_role, "
            + "s.proposer_level, s.dept_name, s.main_category, s.sub_category,",
        "s.problem_description, s.improvement_measure, s.implementer_supervisor, s.implementer_user_ids, s.before_oss_id,",
        "s.after_oss_id, s.start_date, s.planned_completion_date, s.actual_completion_date,",
        "s.completion_status, s.remark, s.review_status, s.review_comment, s.reviewer_user_id, s.reviewed_at,",
        "s.review_file_oss_id, s.review_file_name, s.revision_no, s.submitted_at, s.submitted_by,",
        "s.confirm_comment, s.confirmer_user_id, s.confirmed_at, s.create_time, s.update_time",
        "from dm_score_proposal s where s.del_flag = '0'",
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

    /** 统计指定月份内现场确认通过的提案数。 */
    @Select("select count(1) from dm_score_proposal "
        + "where dept_id = #{deptId} and review_status = 'APPROVED' and del_flag = '0' "
        + "and confirmed_at >= #{beginAt} and confirmed_at < #{endAt}")
    int countApprovedInMonth(@Param("deptId") Long deptId,
                             @Param("beginAt") LocalDateTime beginAt,
                             @Param("endAt") LocalDateTime endAt);

    /** 统计指定月份内除暂存外各提案状态的数量。 */
    @Select("select count(1) as total_count, "
        + "coalesce(sum(case when review_status = 'APPROVED' then 1 else 0 end), 0) as status_approved_count, "
        + "coalesce(sum(case when review_status = 'PENDING' then 1 else 0 end), 0) as pending_count, "
        + "coalesce(sum(case when review_status = 'PENDING_CONFIRM' then 1 else 0 end), 0) as pending_confirm_count, "
        + "coalesce(sum(case when review_status = 'REJECTED' then 1 else 0 end), 0) as rejected_count "
        + "from dm_score_proposal where dept_id = #{deptId} and del_flag = '0' "
        + "and review_status <> 'DRAFT' and submitted_at >= #{beginAt} and submitted_at < #{endAt}")
    ScoreProposalMetricVo selectStatusStats(@Param("deptId") Long deptId,
                                             @Param("beginAt") LocalDateTime beginAt,
                                             @Param("endAt") LocalDateTime endAt);
}
