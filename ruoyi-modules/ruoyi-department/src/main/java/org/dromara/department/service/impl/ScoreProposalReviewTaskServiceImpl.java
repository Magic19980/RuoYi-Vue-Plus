package org.dromara.department.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.utils.IdGeneratorUtil;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.department.domain.ScoreProposalReviewTask;
import org.dromara.department.domain.vo.ScoreProposalReviewTaskVo;
import org.dromara.department.mapper.ScoreProposalReviewTaskMapper;
import org.dromara.department.service.IScoreProposalReviewTaskService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/** SCORE提案审核/现场确认任务实现。 */
@RequiredArgsConstructor
@Service
public class ScoreProposalReviewTaskServiceImpl implements IScoreProposalReviewTaskService {

    private final ScoreProposalReviewTaskMapper taskMapper;

    @Override
    public List<ScoreProposalReviewTaskVo> queryMyPendingTasks() {
        Long userId = LoginHelper.getUserId();
        return taskMapper.selectPendingByUserId(userId).stream().peek(item -> {
            item.setStageLabel("REVIEW".equals(item.getStage()) ? "提案审核" : "现场确认");
            item.setTaskTitle(("REVIEW".equals(item.getStage()) ? "审核SCORE提案：" : "现场确认SCORE提案：")
                + (item.getProposerName() == null ? String.valueOf(item.getProposalId()) : item.getProposerName()));
            item.setPath("/department/scoreProposal/proposal?id=" + item.getProposalId() + "&mode=review&stage=" + item.getStage());
        }).toList();
    }

    @Override
    public void createStageTasks(Long proposalId, Long deptId, Integer revisionNo, String stage,
                                 Collection<Long> assigneeUserIds, LocalDateTime deadline) {
        if (assigneeUserIds == null || assigneeUserIds.isEmpty()) {
            throw new ServiceException("没有可用的SCORE提案审核人");
        }
        for (Long userId : new LinkedHashSet<>(assigneeUserIds)) {
            if (userId == null) continue;
            ScoreProposalReviewTask task = new ScoreProposalReviewTask();
            task.setId(IdGeneratorUtil.nextLongId());
            task.setProposalId(proposalId);
            task.setDeptId(deptId);
            task.setRevisionNo(revisionNo);
            task.setStage(stage);
            task.setAssigneeUserId(userId);
            task.setStatus("PENDING");
            task.setDeadline(deadline);
            taskMapper.insert(task);
        }
    }

    @Override
    public ScoreProposalReviewTask requirePending(Long proposalId, Integer revisionNo, String stage, Long userId) {
        ScoreProposalReviewTask task = taskMapper.selectPending(proposalId, revisionNo, stage, userId);
        if (task == null) {
            throw new ServiceException("当前提案没有分配给您的待处理任务，或任务已处理");
        }
        return task;
    }

    @Override
    public void complete(Long taskId, Long userId, String result, String comment) {
        ScoreProposalReviewTask task = taskMapper.selectById(taskId);
        if (task == null || !Objects.equals(task.getAssigneeUserId(), userId) || !"PENDING".equals(task.getStatus())) {
            throw new ServiceException("审核任务不存在或已经处理");
        }
        task.setStatus("COMPLETED");
        task.setCompletedBy(userId);
        task.setCompletedAt(LocalDateTime.now());
        task.setResult(result);
        task.setComment(comment);
        taskMapper.updateById(task);
    }

    @Override
    public void cancelOtherTasks(Long proposalId, Integer revisionNo, String stage, Long exceptTaskId) {
        taskMapper.update(null, Wrappers.<ScoreProposalReviewTask>lambdaUpdate()
            .eq(ScoreProposalReviewTask::getProposalId, proposalId)
            .eq(ScoreProposalReviewTask::getRevisionNo, revisionNo)
            .eq(ScoreProposalReviewTask::getStage, stage)
            .eq(ScoreProposalReviewTask::getStatus, "PENDING")
            .ne(exceptTaskId != null, ScoreProposalReviewTask::getId, exceptTaskId)
            .set(ScoreProposalReviewTask::getStatus, "CANCELLED")
            .set(ScoreProposalReviewTask::getUpdateTime, LocalDateTime.now()));
    }
}
