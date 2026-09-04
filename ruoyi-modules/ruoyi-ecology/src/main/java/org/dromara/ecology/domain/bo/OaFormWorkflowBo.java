package org.dromara.ecology.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.EditGroup;

import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;

/** 泛微表单主配置。 */
@Data
public class OaFormWorkflowBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "表单配置主键不能为空", groups = EditGroup.class)
    private Long id;

    @NotBlank(message = "泛微 workflowId 不能为空")
    @Size(max = 64, message = "泛微 workflowId 不能超过64个字符")
    private String workflowId;

    @NotBlank(message = "表单名称不能为空")
    @Size(max = 100, message = "表单名称不能超过100个字符")
    private String formName;

    @Size(max = 200, message = "请求名称模板不能超过200个字符")
    private String requestNameTemplate;

    @Size(max = 50000, message = "字段定义不能超过50000个字符")
    private String fieldMappingJson;

    @Size(max = 20000, message = "表单专属字段映射不能超过20000个字符")
    private String specificFieldMappingJson;

    /** 可视化表单字段定义 JSON。字段的 oaFieldCode 为泛微真实字段编码。 */
    @Size(max = 50000, message = "表单字段定义不能超过50000个字符")
    private String fieldSchemaJson;

    private String status;

    @Size(max = 1000, message = "备注不能超过1000个字符")
    private String remark;

}
