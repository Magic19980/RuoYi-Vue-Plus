package org.dromara.ecology.domain.bo;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 泛微动态审批人参数。优先使用本地用户 ID，提交时解析为泛微用户 ID。 */
@Data
public class OaApprovalParticipantBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 节点编码，如 FIRST_APPROVER、COUNTERSIGN、FINAL_APPROVER。 */
    @Size(max = 64, message = "审批节点编码不能超过64个字符")
    private String stageCode;

    @Size(max = 100, message = "审批节点名称不能超过100个字符")
    private String stageName;

    /** 规则主键与快照信息，手工编排时可以为空。 */
    private Long ruleId;

    @Size(max = 64, message = "规则编码不能超过64个字符")
    private String ruleCode;

    @Size(max = 100, message = "规则名称不能超过100个字符")
    private String ruleName;

    private Integer stageOrder;

    @Size(max = 20, message = "审批方式不能超过20个字符")
    private String stageMode = "SEQUENTIAL";

    @Size(max = 20, message = "参与类型不能超过20个字符")
    private String participantRole = "APPROVER";

    /** USER / OA_USER；后续可扩展 DEPT、ROLE、FORMULA。 */
    @Size(max = 20, message = "审批人类型不能超过20个字符")
    private String participantType = "USER";

    private Long localUserId;

    @Size(max = 64, message = "泛微用户 ID 不能超过64个字符")
    private String oaUserId;

    @Size(max = 100, message = "来源值不能超过100个字符")
    private String sourceValue;

    private Integer sortNo;

    private Boolean required = Boolean.TRUE;
}
