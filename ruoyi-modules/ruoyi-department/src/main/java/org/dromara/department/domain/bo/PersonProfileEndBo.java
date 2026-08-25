package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** 结束人员服务关系参数。 */
@Data
public class PersonProfileEndBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "离开生效日不能为空")
    private LocalDate leaveDate;

    private String reason;
}
