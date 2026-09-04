package org.dromara.ecology.domain.bo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** 业务类型与泛微表单审批方式的绑定参数。 */
@Data
public class OaBusinessWorkflowBindingBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "泛微表单不能为空")
    private Long formId;

    @NotEmpty(message = "至少选择一种审批方式")
    private List<Long> optionIds;

    @NotNull(message = "默认审批方式不能为空")
    private Long defaultOptionId;
}
