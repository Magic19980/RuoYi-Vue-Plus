package org.dromara.ecology.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 申请提交前解析出的审批链预览。 */
@Data
public class OaApprovalRulePreviewVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long ruleId;
    private String ruleCode;
    private String ruleName;
    private String stageCode;
    private String stageName;
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
