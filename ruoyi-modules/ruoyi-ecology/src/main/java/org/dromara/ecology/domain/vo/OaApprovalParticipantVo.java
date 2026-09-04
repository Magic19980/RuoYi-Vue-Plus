package org.dromara.ecology.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 泛微动态审批人视图。 */
@Data
public class OaApprovalParticipantVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
}
