package org.dromara.ecology.domain;

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
import java.time.LocalDateTime;
import java.util.List;

/** 泛微通用审批申请。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_oa_application")
public class OaApplication extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String applicationNo;

    private String businessType;

    private String sourceModule;

    private String businessId;

    private String businessNo;

    private String title;

    private String content;

    private String urgency;

    private String formDataJson;

    private Long applicantUserId;

    private String applicantName;

    private Long deptId;

    /** 多部门申请的部门明细，实际存储在 dm_oa_application_dept。 */
    @TableField(exist = false)
    private List<Long> deptIds;

    /** 业务申请使用的公司/组织 ID，来自泛微同步组织树。 */
    private Long companyId;

    /** SEQUENTIAL、COUNTERSIGN、MIXED。 */
    private String processType;

    /** 提交时实际使用的审批方案。 */
    private Long approvalPlanId;

    /** AUTO_RULE 自动匹配 / PLAN 选择方案 / MANUAL 本次临时指定。 */
    private String approvalMode;

    private Long workflowConfigId;

    private String status;

    private LocalDateTime submittedAt;

    @Version
    private Long version;

    @TableLogic
    private String delFlag;
}
