package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** 周期任务成员分配视图。 */
@Data
public class DepartmentTaskAssignmentVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long ruleId;
    private Long deptId;
    private Long userId;
    private String userName;
    private String nickName;
    private LocalDate effectiveStart;
    private LocalDate effectiveEnd;
    private String workDays;
    private java.time.LocalTime reminderTime;
    private String status;
    private String remark;
}
