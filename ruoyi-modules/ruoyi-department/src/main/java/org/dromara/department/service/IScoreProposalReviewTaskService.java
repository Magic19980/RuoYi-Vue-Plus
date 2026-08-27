package org.dromara.department.service;

import org.dromara.department.domain.ScoreProposalReviewTask;
import org.dromara.department.domain.vo.ScoreProposalReviewTaskVo;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/** SCORE提案审核/现场确认任务服务。 */
public interface IScoreProposalReviewTaskService {

    List<ScoreProposalReviewTaskVo> queryMyPendingTasks();

    void createStageTasks(Long proposalId, Long deptId, Integer revisionNo, String stage,
                          Collection<Long> assigneeUserIds, LocalDateTime deadline);

    ScoreProposalReviewTask requirePending(Long proposalId, Integer revisionNo, String stage, Long userId);

    void complete(Long taskId, Long userId, String result, String comment);

    void cancelOtherTasks(Long proposalId, Integer revisionNo, String stage, Long exceptTaskId);
}
