package org.dromara.ecology.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/** 泛微申请的动态审批人快照。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_oa_approval_participant")
public class OaApprovalParticipant extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long applicationId;
    private Long processId;
    private String stageCode;
    private String stageName;
    private Long ruleId;
    private String ruleCode;
    private String ruleName;
    private Integer stageOrder;
    private String stageMode;
    private String participantRole;
    private String participantType;
    private Long localUserId;
    private String oaUserId;
    private String oaUserName;
    private String sourceValue;
    private Integer sortNo;
    private Boolean required;

    @TableLogic
    private String delFlag;
}
