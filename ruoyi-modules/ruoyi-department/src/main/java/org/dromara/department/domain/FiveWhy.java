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
 * 5WHY分析记录实体，对应 {@code dm_five_why} 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_five_why")
public class FiveWhy extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    /** 5WHY分析记录主键。 */
    private Long id;

    /** 业务科室主键。 */
    private Long deptId;
    /** 业务组织或公司名称。 */
    private String companyDept;
    /** 分析人员工号。 */
    private String employeeNo;
    /** 分析人员用户主键。 */
    private Long analystUserId;
    /** 分析人员姓名快照。 */
    private String analystName;
    /** 分析日期。 */
    private LocalDate analysisDate;
    /** 问题名称。 */
    private String problemName;
    /** 问题描述。 */
    private String problemDescription;
    /** 影响范围。 */
    private String impactScope;
    /** 五个为什么分析内容JSON。 */
    private String whysJson;
    /** 改善措施内容JSON。 */
    private String improvementsJson;
    /** 改善前图片对象存储文件主键。 */
    private Long beforeOssId;
    /** 改善后图片对象存储文件主键。 */
    private Long afterOssId;
    /** 效果验证结果。 */
    private String effectVerification;
    /** 标准化计划。 */
    private String standardizationPlan;
    /** 标准化执行情况。 */
    private String standardizationExecution;
    /** 审核状态。 */
    private String reviewStatus;
    /** 审核意见。 */
    private String reviewComment;
    /** 审核人用户主键。 */
    private Long reviewerUserId;

    @Version
    /** 乐观锁版本号。 */
    private Long version;

    @TableLogic
    /** 逻辑删除标记。 */
    private String delFlag;
}
