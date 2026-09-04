package org.dromara.ecology.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 泛微 HRM 新人员初始密码设置参数。 */
@Data
public class OaHrmUserPasswordBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "人员初始密码不能为空")
    @Size(min = 5, max = 20, message = "人员初始密码长度必须在{min}到{max}个字符之间")
    @Pattern(regexp = "^[^<>\"'|\\\\]+$", message = "人员初始密码包含非法字符")
    private String password;
}
