package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 任务周期实例视图。 */
@Data
public class DepartmentTaskInstanceVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long ruleId;
    private Long deptId;
    private Long userId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDateTime deadline;
    private Integer requiredCount;
    private Integer completedCount;
    private String status;
    private LocalDateTime generatedAt;
    private LocalDateTime completedAt;
}
