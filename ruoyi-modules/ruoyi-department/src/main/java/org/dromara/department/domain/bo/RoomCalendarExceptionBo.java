package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** 房间日历例外新增、修改参数。 */
@Data
public class RoomCalendarExceptionBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "例外主键不能为空", groups = EditGroup.class)
    private Long id;

    @NotNull(message = "例外日期不能为空")
    private LocalDate exceptionDate;

    private String exceptionType;

    @Size(max = 100, message = "例外名称不能超过100个字符")
    private String exceptionName;

    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;
}
