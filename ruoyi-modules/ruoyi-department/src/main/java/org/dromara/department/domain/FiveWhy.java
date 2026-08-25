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
 * 5WHY分析记录对象 dm_five_why。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_five_why")
public class FiveWhy extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long deptId;
    private String companyDept;
    private String employeeNo;
    private Long analystUserId;
    private String analystName;
    private LocalDate analysisDate;
    private String problemName;
    private String problemDescription;
    private String impactScope;
    private String whysJson;
    private String improvementsJson;
    private Long beforeOssId;
    private Long afterOssId;
    private String effectVerification;
    private String standardizationPlan;
    private String standardizationExecution;
    private String reviewStatus;
    private String reviewComment;
    private Long reviewerUserId;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
