package org.dromara.ecology.domain.bo;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/** 通用导入批次提交参数。 */
@Data
public class OaImportSubmitBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 为空时使用业务模板默认流程。 */
    private Long workflowConfigId;

    /** 页面参数，不要求业务方修改表单 JSON。 */
    private Map<String, Object> parameters;

    /** AUTO_RULE 自动匹配，PLAN 选择审批方案，MANUAL 本次临时指定人员。 */
    @Size(max = 20, message = "审批方式不能超过20个字符")
    private String approvalMode = "AUTO_RULE";

    /** 选择方案时填写；自动匹配时可为空。 */
    private Long approvalPlanId;

    @Size(max = 20, message = "流程方式不能超过20个字符")
    private String processType = "SEQUENTIAL";

    private List<OaApprovalParticipantBo> participants;
}
