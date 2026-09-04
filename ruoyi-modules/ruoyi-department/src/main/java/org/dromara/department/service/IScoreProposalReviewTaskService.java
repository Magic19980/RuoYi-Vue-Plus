package org.dromara.department.service;

import org.dromara.department.domain.ScoreProposalReviewTask;
import org.dromara.department.domain.vo.ScoreProposalReviewTaskVo;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/** SCORE提案审核/现场确认任务服务。 */
public interface IScoreProposalReviewTaskService {

    /**
     * 查询当前登录人的待处理提案审核任务。
     *
     * @return 待处理任务列表
     */
    List<ScoreProposalReviewTaskVo> queryMyPendingTasks();

    /**
     * 为提案审核阶段创建事件型任务。
     *
     * @param proposalId      提案主键
     * @param deptId          业务科室主键
     * @param revisionNo      提案修订版本号
     * @param stage           审核阶段
     * @param assigneeUserIds 待分配的审核人用户主键集合
     * @param deadline        任务截止时间
     */
    void createStageTasks(Long proposalId, Long deptId, Integer revisionNo, String stage,
                          Collection<Long> assigneeUserIds, LocalDateTime deadline);

    /**
     * 获取指定用户的待处理任务，不存在时抛出业务异常。
     *
     * @param proposalId 提案主键
     * @param revisionNo 提案修订版本号
     * @param stage      审核阶段
     * @param userId     处理人用户主键
     * @return 待处理审核任务
     */
    ScoreProposalReviewTask requirePending(Long proposalId, Integer revisionNo, String stage, Long userId);

    /**
     * 完成提案审核任务。
     *
     * @param taskId  任务主键
     * @param userId  当前处理人用户主键
     * @param result  处理结果
     * @param comment 处理意见
     */
    void complete(Long taskId, Long userId, String result, String comment);

    /**
     * 取消同一审核阶段中除指定任务外的其他待处理任务。
     *
     * @param proposalId   提案主键
     * @param revisionNo   提案修订版本号
     * @param stage        审核阶段
     * @param exceptTaskId 需要保留的任务主键
     */
    void cancelOtherTasks(Long proposalId, Integer revisionNo, String stage, Long exceptTaskId);
}
