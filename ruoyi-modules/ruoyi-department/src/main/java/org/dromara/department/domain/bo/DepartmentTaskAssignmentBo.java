package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** 周期任务成员分配参数。 */
@Data
public class DepartmentTaskAssignmentBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "规则ID不能为空")
    private Long ruleId;
    @NotNull(message = "成员不能为空")
    private Long userId;
    private Long id;
    private LocalDate effectiveStart;
    private LocalDate effectiveEnd;
    private String workDays;
    private java.time.LocalTime reminderTime;
    private String status;
    private String remark;
}
