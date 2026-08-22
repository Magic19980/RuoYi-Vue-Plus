package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 日报周工作日规则维护参数。 */
@Data
public class DailyCalendarConfigBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "每周工作日不能为空")
    private String workDays;

    private String remark;
}
