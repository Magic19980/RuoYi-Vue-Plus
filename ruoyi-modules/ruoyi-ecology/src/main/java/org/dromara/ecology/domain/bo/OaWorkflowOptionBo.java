package org.dromara.ecology.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 可跨泛微表单复用的审批方式配置。 */
@Data
public class OaWorkflowOptionBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "审批方式编码不能为空")
    @Size(max = 64, message = "审批方式编码不能超过64个字符")
    private String optionCode;

    @NotBlank(message = "审批方式名称不能为空")
    @Size(max = 100, message = "审批方式名称不能超过100个字符")
    private String optionName;

    @Size(max = 20, message = "旧审批模式不能超过20个字符")
    private String processType;

    @Size(max = 20000, message = "审批节点映射不能超过20000个字符")
    private String participantMappingJson;

    private Integer sortNo;

    private String status;

    @Size(max = 1000, message = "备注不能超过1000个字符")
    private String remark;
}
