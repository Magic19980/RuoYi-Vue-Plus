package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** 日报日期例外规则维护参数。 */
@Data
public class DailyCalendarOverrideBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "主键不能为空", groups = EditGroup.class)
    private Long id;

    /** 调休上班人员；休息日为空。 */
    private Long userId;

    @NotNull(message = "日期不能为空", groups = {AddGroup.class, EditGroup.class})
    private LocalDate calendarDate;

    @NotNull(message = "日期类型不能为空", groups = {AddGroup.class, EditGroup.class})
    private String dayType;

    private String remark;
}
