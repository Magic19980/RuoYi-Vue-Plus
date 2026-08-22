package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** 人员休假安排维护参数。 */
@Data
public class DailyLeaveBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "主键不能为空", groups = EditGroup.class)
    private Long id;

    @NotNull(message = "人员不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long userId;

    @NotNull(message = "休假开始日期不能为空", groups = {AddGroup.class, EditGroup.class})
    private LocalDate startDate;

    @NotNull(message = "休假结束日期不能为空", groups = {AddGroup.class, EditGroup.class})
    private LocalDate endDate;

    private String leaveType;

    private String reason;
}
