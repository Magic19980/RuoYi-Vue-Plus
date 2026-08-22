package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** 日报日历日期列。 */
@Data
public class DailyCalendarDayVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate date;
    private Integer dayOfWeek;
    private String weekLabel;
    private Boolean workday;
    /** 是否存在科室统一休息日例外；个人周工作日规则仍可单独计算。 */
    private Boolean departmentRest;
    private String dayType;
    private String label;
    private String remark;
}
