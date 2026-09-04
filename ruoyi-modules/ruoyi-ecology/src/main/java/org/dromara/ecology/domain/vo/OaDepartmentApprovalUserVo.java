package org.dromara.ecology.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 审批部门配置用户视图。 */
@Data
public class OaDepartmentApprovalUserVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long approvalId;
    private Long localUserId;
    private String stageCode;
    private String stageName;
    private String stageMode;
    private String userName;
    private String nickName;
    private String employeeNo;
    private String oaUserId;
    private String deptName;
    private String participantRole;
    private Integer sortNo;
}
