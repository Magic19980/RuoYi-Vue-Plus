package org.dromara.ecology.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** 通用导入批次按结算部门解析出的审批配置预览。 */
@Data
public class OaImportApprovalPreviewVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String groupKey;
    private String groupName;
    private Long businessDeptId;
    private String businessDeptName;
    private Integer recordCount;
    /** MATCHED、MISSING_CONFIG、INVALID_CONFIG、AMBIGUOUS_DEPT。 */
    private String status;
    private String message;
    private Long approvalPlanId;
    private String planName;
    private Long workflowConfigId;
    private String workflowId;
    private String workflowName;
    private String formName;
    private String approvalCode;
    private String approvalName;
    private String processType;
    private List<OaDepartmentApprovalUserVo> approvers;
    private List<OaDepartmentApprovalUserVo> copyUsers;
}
