package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/** 周期任务规则视图。 */
@Data
public class DepartmentTaskRuleVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long deptId;
    private String taskName;
    private String taskType;
    private String cycleType;
    private Integer requiredCount;
    private Integer deadlineDay;
    private LocalTime deadlineTime;
    private String countMode;
    private Integer remindHours;
    private LocalDate effectiveStart;
    private LocalDate effectiveEnd;
    private String status;
    private String remark;
    private Long assignmentCount;
}
