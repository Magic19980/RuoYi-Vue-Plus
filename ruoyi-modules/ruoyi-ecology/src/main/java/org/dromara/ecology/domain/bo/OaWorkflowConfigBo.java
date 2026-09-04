package org.dromara.ecology.domain.bo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;

/** 泛微流程配置参数。 */
@Data
public class OaWorkflowConfigBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "流程配置主键不能为空", groups = EditGroup.class)
    private Long id;

    @NotBlank(message = "泛微 workflowId 不能为空")
    @Size(max = 64, message = "泛微 workflowId 不能超过64个字符")
    private String workflowId;

    @NotBlank(message = "流程名称不能为空")
    @Size(max = 100, message = "流程名称不能超过100个字符")
    private String workflowName;

    @NotBlank(message = "泛微表单名称不能为空")
    @Size(max = 100, message = "泛微表单名称不能超过100个字符")
    private String formName;

    @NotBlank(message = "泛微审批方式值不能为空")
    @Size(max = 64, message = "泛微审批方式值不能超过64个字符")
    private String approvalCode;

    @NotBlank(message = "审批方式名称不能为空")
    @Size(max = 100, message = "审批方式名称不能超过100个字符")
    private String approvalName;

    @NotBlank(message = "流程审批方式不能为空")
    @Size(max = 20, message = "流程审批方式不能超过20个字符")
    private String processType;

    @Size(max = 100, message = "原始泛微流程名称不能超过100个字符")
    private String sourceWorkflowName;

    @Size(max = 200, message = "请求名称模板不能超过200个字符")
    private String requestNameTemplate;

    @Size(max = 20000, message = "字段映射不能超过20000个字符")
    private String fieldMappingJson;

    private String status;

    @Size(max = 1000, message = "备注不能超过1000个字符")
    private String remark;
}
