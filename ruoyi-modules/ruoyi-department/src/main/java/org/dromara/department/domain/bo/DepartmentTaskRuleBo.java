package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/** 周期任务规则参数。 */
@Data
public class DepartmentTaskRuleBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "主键不能为空", groups = EditGroup.class)
    private Long id;
    @NotBlank(message = "任务名称不能为空")
    private String taskName;
    @NotBlank(message = "任务类型不能为空")
    private String taskType;
    @NotBlank(message = "周期不能为空")
    private String cycleType;
    @NotNull(message = "要求次数不能为空")
    private Integer requiredCount;
    private Integer deadlineDay;
    private LocalTime deadlineTime;
    private String countMode;
    private Integer remindHours;
    private LocalDate effectiveStart;
    private LocalDate effectiveEnd;
    private String status;
    private String remark;
}
