package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 当前登录人待处理的SCORE提案任务。 */
@Data
public class ScoreProposalReviewTaskVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long proposalId;
    private Long deptId;
    private Integer revisionNo;
    private String stage;
    private String stageLabel;
    private Long assigneeUserId;
    private String status;
    private LocalDateTime deadline;
    private String proposerName;
    private String mainCategory;
    private String subCategory;
    private String taskTitle;
    private String path;
    private LocalDateTime createTime;
}
