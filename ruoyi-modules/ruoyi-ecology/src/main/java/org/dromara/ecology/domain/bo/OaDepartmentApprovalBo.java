package org.dromara.ecology.domain.bo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** 泛微审批方案参数。 */
@Data
public class OaDepartmentApprovalBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "审批人员配置主键不能为空", groups = EditGroup.class)
    private Long id;

    @NotNull(message = "泛微流程配置不能为空")
    private Long workflowConfigId;

    @NotBlank(message = "业务类型不能为空")
    @Size(max = 64, message = "业务类型不能超过64个字符")
    private String businessType;

    @Size(max = 64, message = "来源模块不能超过64个字符")
    private String sourceModule;

    /** 业务需要发起泛微流程的归属组织；为空表示全组织通用。 */
    private Long businessDeptId;

    @NotBlank(message = "审批方案名称不能为空")
    @Size(max = 100, message = "审批方案名称不能超过100个字符")
    private String planName;

    /** 业务字段条件，普通业务用户无需填写。 */
    @Size(max = 20000, message = "匹配条件不能超过20000个字符")
    private String matchConditionJson;

    private Integer priority = 0;

    private String status = "ENABLED";

    @Size(max = 1000, message = "备注不能超过1000个字符")
    private String remark;

    @Valid
    @NotEmpty(message = "至少配置一名审批人")
    private List<OaDepartmentApprovalUserBo> users;
}
