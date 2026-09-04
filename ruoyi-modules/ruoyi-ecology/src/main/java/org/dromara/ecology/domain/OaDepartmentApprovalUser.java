package org.dromara.ecology.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/** 泛微审批方案中的有序审批人/抄送人。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_oa_approval_plan_user")
public class OaDepartmentApprovalUser extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long approvalId;
    private Long localUserId;
    /** MIXED 流程阶段：LEVEL_1、COUNTERSIGN、LEVEL_3；普通流程为 APPROVAL。 */
    private String stageCode;
    private String stageName;
    private String stageMode;
    private String participantRole;
    private Integer sortNo;

    @TableLogic
    private String delFlag;
}
