package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 日报新增、修改参数。
 */
@Data
public class DailyReportBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "主键不能为空", groups = EditGroup.class)
    private Long id;

    @NotNull(message = "日报日期不能为空", groups = {AddGroup.class, EditGroup.class})
    private LocalDate reportDate;

    @NotBlank(message = "今日工作不能为空", groups = {AddGroup.class, EditGroup.class})
    private String todayWork;

    private String tomorrowPlan;

    private String coordinationNote;
}
