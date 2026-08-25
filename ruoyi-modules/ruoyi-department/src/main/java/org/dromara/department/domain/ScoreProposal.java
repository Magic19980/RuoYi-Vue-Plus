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
import java.time.LocalDate;

/**
 * SCORE提案记录对象 dm_score_proposal。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_score_proposal")
public class ScoreProposal extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long deptId;
    private Long mainCategoryId;
    private Long subCategoryId;
    private Long proposerUserId;
    private String companyName;
    private String teamMembers;
    private String employeeNo;
    private String proposerName;
    private String proposerRole;
    private String proposerLevel;
    private String deptName;
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

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
