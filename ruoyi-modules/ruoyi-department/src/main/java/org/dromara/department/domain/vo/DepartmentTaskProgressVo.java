package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 当前用户周期任务进度视图。 */
@Data
public class DepartmentTaskProgressVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long ruleId;
    private Long instanceId;
    private Long assignmentId;
    private Long userId;
    private String userName;
    private String taskName;
    private String taskType;
    private String cycleType;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDateTime deadline;
    private Integer requiredCount;
    private Integer completedCount;
    private LocalDateTime completedAt;
    private String status;
    private String statusLabel;
    private String reminderText;
}
