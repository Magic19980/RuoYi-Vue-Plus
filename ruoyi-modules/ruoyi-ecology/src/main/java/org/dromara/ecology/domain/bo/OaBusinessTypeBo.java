package org.dromara.ecology.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;

/** 泛微审批业务类型配置参数。 */
@Data
public class OaBusinessTypeBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "业务类型主键不能为空", groups = EditGroup.class)
    private Long id;

    @NotBlank(message = "业务类型标识不能为空")
    @Size(max = 64, message = "业务类型标识不能超过64个字符")
    private String businessType;

    @NotBlank(message = "业务类型名称不能为空")
    @Size(max = 100, message = "业务类型名称不能超过100个字符")
    private String businessName;

    private String status;

    @Size(max = 1000, message = "备注不能超过1000个字符")
    private String remark;
}
