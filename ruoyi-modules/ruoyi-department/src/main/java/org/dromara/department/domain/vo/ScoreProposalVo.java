package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** SCORE提案列表和详情视图。 */
@Data
public class ScoreProposalVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long deptId;
    private String companyName;
    private String teamMembers;
    private String employeeNo;
    private String proposerName;
    private String proposerRole;
    private String proposerLevel;
    private String deptName;
    private Long mainCategoryId;
    private Long subCategoryId;
    private Long proposerUserId;
    private String mainCategory;
    private String subCategory;
    private String problemDescription;
    private String improvementMeasure;
    private String implementerSupervisor;
    private Long beforeOssId;
    private Long afterOssId;
    private LocalDate startDate;
    private LocalDate plannedCompletionDate;
    private LocalDate actualCompletionDate;
    private String completionStatus;
    private String remark;
    private String reviewStatus;
    private String reviewComment;
    private Long reviewerUserId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
