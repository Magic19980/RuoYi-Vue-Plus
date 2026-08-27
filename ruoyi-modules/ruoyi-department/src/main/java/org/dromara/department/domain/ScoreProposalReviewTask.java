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

/** SCORE提案的事件型审核任务，不与周期提交任务混用。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_score_proposal_review_task")
public class ScoreProposalReviewTask extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private Long proposalId;
    private Long deptId;
    private Integer revisionNo;
    private String stage;
    private Long assigneeUserId;
    private String status;
    private LocalDateTime deadline;
    private Long completedBy;
    private LocalDateTime completedAt;
    private String result;
    private String comment;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
