package org.dromara.department.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.ScoreProposal;
import org.dromara.department.domain.bo.ScoreProposalQueryBo;
import org.dromara.department.domain.vo.ScoreProposalVo;

/** SCORE提案数据层。 */
@Mapper
public interface ScoreProposalMapper extends BaseMapperPlus<ScoreProposal, ScoreProposalVo> {

    @Select({
        "<script>",
        "select s.id, s.dept_id, s.main_category_id, s.sub_category_id, s.proposer_user_id, s.company_name, s.team_members, s.employee_no, s.proposer_name,",
        "s.proposer_role, s.proposer_level, s.dept_name, s.main_category, s.sub_category,",
        "s.problem_description, s.improvement_measure, s.implementer_supervisor, s.before_oss_id,",
        "s.after_oss_id, s.start_date, s.planned_completion_date, s.actual_completion_date,",
        "s.completion_status, s.remark, s.review_status, s.review_comment, s.reviewer_user_id, s.create_time, s.update_time",
        "from dm_score_proposal s where s.del_flag = '0'",
        "<if test='bo.proposerName != null and bo.proposerName != \"\"'> and s.proposer_name like concat('%', #{bo.proposerName}, '%') </if>",
        "<if test='bo.mainCategory != null and bo.mainCategory != \"\"'> and s.main_category = #{bo.mainCategory} </if>",
        "<if test='bo.subCategory != null and bo.subCategory != \"\"'> and s.sub_category = #{bo.subCategory} </if>",
        "<if test='bo.completionStatus != null and bo.completionStatus != \"\"'> and s.completion_status = #{bo.completionStatus} </if>",
        "<if test='bo.reviewStatus != null and bo.reviewStatus != \"\"'> and s.review_status = #{bo.reviewStatus} </if>",
        "<if test='bo.beginDate != null'> and s.start_date &gt;= #{bo.beginDate} </if>",
        "<if test='bo.endDate != null'> and s.start_date &lt;= #{bo.endDate} </if>",
        "<choose>",
        "<when test='all == true'></when>",
        "<otherwise> and s.dept_id = #{deptId} </otherwise>",
        "</choose>",
        "order by s.start_date desc, s.id desc",
        "</script>"
    })
    Page<ScoreProposal> selectPageList(Page<ScoreProposal> page,
                                          @Param("bo") ScoreProposalQueryBo bo,
                                          @Param("deptId") Long deptId,
                                          @Param("all") boolean all);
}
