package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 日报周工作日规则视图。 */
@Data
public class DailyCalendarConfigVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long deptId;
    private Long userId;
    private String workDays;
    private String remark;
}
