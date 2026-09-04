package org.dromara.department.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.department.domain.ScoreProposalReviewTask;
import org.dromara.department.domain.vo.ScoreProposalReviewTaskVo;

import java.util.List;

/** SCORE提案事件型审核任务数据层。 */
@Mapper
public interface ScoreProposalReviewTaskMapper extends BaseMapperPlus<ScoreProposalReviewTask, ScoreProposalReviewTaskVo> {

    /** 查询指定用户待处理的SCORE提案审核任务。 */
    @Select("select t.id, t.proposal_id, t.dept_id, t.revision_no, t.stage, t.assignee_user_id, t.status, "
        + "t.deadline, t.create_time, s.proposer_name, s.main_category, s.sub_category "
        + "from dm_score_proposal_review_task t "
        + "join dm_score_proposal s on s.id = t.proposal_id and s.del_flag = '0' "
        + "where t.assignee_user_id = #{userId} and t.status = 'PENDING' and t.del_flag = '0' "
        + "order by case when t.deadline is null then 1 else 0 end, t.deadline asc, t.create_time desc")
    List<ScoreProposalReviewTaskVo> selectPendingByUserId(@Param("userId") Long userId);

    /** 查询指定提案阶段中指定审核人的待处理任务。 */
    default ScoreProposalReviewTask selectPending(Long proposalId, Integer revisionNo, String stage, Long assigneeUserId) {
        return selectOne(Wrappers.<ScoreProposalReviewTask>lambdaQuery()
            .eq(ScoreProposalReviewTask::getProposalId, proposalId)
            .eq(ScoreProposalReviewTask::getRevisionNo, revisionNo)
            .eq(ScoreProposalReviewTask::getStage, stage)
            .eq(ScoreProposalReviewTask::getAssigneeUserId, assigneeUserId)
            .eq(ScoreProposalReviewTask::getStatus, "PENDING")
            .eq(ScoreProposalReviewTask::getDelFlag, "0")
            .last("limit 1"));
    }

    /** 查询指定提案阶段的全部待处理审核任务。 */
    default List<ScoreProposalReviewTask> selectPendingByProposalStage(Long proposalId, Integer revisionNo, String stage) {
        return selectList(Wrappers.<ScoreProposalReviewTask>lambdaQuery()
            .eq(ScoreProposalReviewTask::getProposalId, proposalId)
            .eq(ScoreProposalReviewTask::getRevisionNo, revisionNo)
            .eq(ScoreProposalReviewTask::getStage, stage)
            .eq(ScoreProposalReviewTask::getStatus, "PENDING")
            .eq(ScoreProposalReviewTask::getDelFlag, "0"));
    }
}
