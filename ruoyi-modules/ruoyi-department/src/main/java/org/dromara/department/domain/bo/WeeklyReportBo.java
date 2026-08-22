package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 周报生成参数。
 */
@Data
public class WeeklyReportBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "周开始日期不能为空", groups = AddGroup.class)
    private LocalDate weekStart;

    private LocalDate weekEnd;

    @Size(max = 200, message = "周报标题不能超过200个字符")
    private String title;
}
