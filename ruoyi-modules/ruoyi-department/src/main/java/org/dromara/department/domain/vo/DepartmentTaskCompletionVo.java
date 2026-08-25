package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 任务完成记录视图。 */
@Data
public class DepartmentTaskCompletionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long instanceId;
    private String taskType;
    private Long sourceId;
    private LocalDateTime completedAt;
}
