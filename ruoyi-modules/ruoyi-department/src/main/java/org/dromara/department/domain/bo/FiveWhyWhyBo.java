package org.dromara.department.domain.bo;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 5WHY问题与原因。 */
@Data
public class FiveWhyWhyBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer level;

    @Size(max = 2000, message = "WHY问题不能超过2000个字符")
    private String question;

    @Size(max = 2000, message = "WHY原因不能超过2000个字符")
    private String cause;
}
