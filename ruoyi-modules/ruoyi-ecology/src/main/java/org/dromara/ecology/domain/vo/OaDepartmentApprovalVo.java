package org.dromara.ecology.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.ecology.domain.OaDepartmentApproval;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/** 泛微审批方案视图。 */
@Data
@AutoMapper(target = OaDepartmentApproval.class)
public class OaDepartmentApprovalVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long workflowConfigId;
    private String businessType;
    private String sourceModule;
    private Long businessDeptId;
    private String businessDeptName;
    private String planName;
    private String matchConditionJson;
    private Integer priority;
    private String status;
    private String remark;
    private String workflowName;
    private String formName;
    private String approvalCode;
    private String approvalName;
    private String processType;
    private String participantMappingJson;
    private List<OaDepartmentApprovalUserVo> users;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
