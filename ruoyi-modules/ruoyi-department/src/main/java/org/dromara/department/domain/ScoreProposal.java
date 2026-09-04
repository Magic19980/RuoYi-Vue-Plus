package org.dromara.department.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * SCORE提案记录实体，对应 {@code dm_score_proposal} 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_score_proposal")
public class ScoreProposal extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    /** 提案主键。 */
    private Long id;

    /** 业务科室主键。 */
    private Long deptId;
    /** 提案大类主键。 */
    private Long mainCategoryId;
    /** 提案小类主键。 */
    private Long subCategoryId;
    /** 提案人用户主键。 */
    private Long proposerUserId;
    /** 提案所属企业名称。 */
    private String companyName;
    /** 企业参与人员用户ID JSON，姓名从用户管理实时读取。 */
    private String teamMemberUserIds;
    /** 提案人工号。 */
    private String employeeNo;
    /** 提案人姓名快照。 */
    private String proposerName;

    /**
     * 岗位由用户管理的 sys_user_post/sys_post 实时关联查询，不落入提案表。
     */
    @TableField(exist = false)
    private String proposerRole;
    /** 提案人职位层级字典值。 */
    private String proposerLevel;
    /** 业务科室名称快照。 */
    private String deptName;
    /** 提案大类名称快照。 */
    private String mainCategory;
    /** 提案小类名称快照。 */
    private String subCategory;
    /** 问题描述。 */
    private String problemDescription;
    /** 改善措施。 */
    private String improvementMeasure;
    /** 实施负责人或监督人。 */
    private String implementerSupervisor;
    /** 实施人员用户ID JSON。 */
    private String implementerUserIds;
    /** 改善前图片对象存储文件主键。 */
    private Long beforeOssId;
    /** 改善后图片对象存储文件主键。 */
    private Long afterOssId;
    /** 改善开始日期。 */
    private LocalDate startDate;
    /** 计划完成日期。 */
    private LocalDate plannedCompletionDate;
    /** 实际完成日期。 */
    private LocalDate actualCompletionDate;
    /** 完成状态。 */
    private String completionStatus;
    /** 提案备注。 */
    private String remark;
    /** 审核状态。 */
    private String reviewStatus;
    /** 审核意见。 */
    private String reviewComment;
    /** 审核人用户主键。 */
    private Long reviewerUserId;
    /** 审核时间。 */
    private LocalDateTime reviewedAt;
    /** 审核附件对象存储文件主键。 */
    private Long reviewFileOssId;
    /** 审核附件原始文件名。 */
    private String reviewFileName;
    /** 当前提案修订版本号。 */
    private Integer revisionNo;
    /** 提交时间。 */
    private LocalDateTime submittedAt;
    /** 提交人用户主键。 */
    private Long submittedBy;
    /** 现场确认意见。 */
    private String confirmComment;
    /** 现场确认人用户主键。 */
    private Long confirmerUserId;
    /** 现场确认时间。 */
    private LocalDateTime confirmedAt;

    @Version
    /** 乐观锁版本号。 */
    private Long version;

    @TableLogic
    /** 逻辑删除标记。 */
    private String delFlag;
}
