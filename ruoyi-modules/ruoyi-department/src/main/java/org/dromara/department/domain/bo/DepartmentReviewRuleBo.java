package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** 审核人配置参数。 */
@Data
public class DepartmentReviewRuleBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "主键不能为空", groups = EditGroup.class)
    private Long id;
    private String taskType;
    private Long reviewerUserId;
    private Long backupReviewerUserId;
    private LocalDate effectiveStart;
    private LocalDate effectiveEnd;
    private String status;
    private String remark;
}
