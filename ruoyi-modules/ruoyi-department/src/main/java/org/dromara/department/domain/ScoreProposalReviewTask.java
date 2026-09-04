package org.dromara.department.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * SCORE提案的事件型审核任务实体，对应 {@code dm_score_proposal_review_task} 表。
 *
 * <p>该任务用于提案审核和现场确认，不与周期提交任务混用。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_score_proposal_review_task")
public class ScoreProposalReviewTask extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    /** 审核任务主键。 */
    private Long id;
    /** 提案主键。 */
    private Long proposalId;
    /** 业务科室主键。 */
    private Long deptId;
    /** 提案修订版本号。 */
    private Integer revisionNo;
    /** 审核阶段。 */
    private String stage;
    /** 被分配处理人用户主键。 */
    private Long assigneeUserId;
    /** 任务状态。 */
    private String status;
    /** 任务截止时间。 */
    private LocalDateTime deadline;
    /** 实际完成人用户主键。 */
    private Long completedBy;
    /** 实际完成时间。 */
    private LocalDateTime completedAt;
    /** 处理结果。 */
    private String result;
    /** 处理意见。 */
    private String comment;

    @Version
    /** 乐观锁版本号。 */
    private Long version;

    @TableLogic
    /** 逻辑删除标记。 */
    private String delFlag;
}
