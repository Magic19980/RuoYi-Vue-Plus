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
    /** 全体成员生效的日期例外类型字典值。 */
    private String dayType;
    private String label;
    private String remark;
}
