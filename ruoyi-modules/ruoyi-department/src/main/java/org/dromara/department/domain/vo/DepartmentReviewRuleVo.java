package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** 审核人配置视图。 */
@Data
public class DepartmentReviewRuleVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long deptId;
    private String taskType;
    private Long reviewerUserId;
    private String reviewerName;
    private Long backupReviewerUserId;
    private String backupReviewerName;
    private LocalDate effectiveStart;
    private LocalDate effectiveEnd;
    private String status;
    private String remark;
}
