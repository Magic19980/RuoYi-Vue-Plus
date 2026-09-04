package org.dromara.ecology.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 审批部门配置中的本地用户参数。 */
@Data
public class OaDepartmentApprovalUserBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "审批人员不能为空")
    private Long localUserId;

    /** MIXED 流程阶段；依次签/会签使用 APPROVAL。 */
    private String stageCode = "APPROVAL";

    private String stageName;

    private String stageMode;

    /** APPROVER 或 COPY。 */
    private String participantRole = "APPROVER";

    private Integer sortNo;
}
