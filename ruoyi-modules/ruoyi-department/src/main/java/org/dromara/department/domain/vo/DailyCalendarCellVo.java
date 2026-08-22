package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** 日报日历单元格。 */
@Data
public class DailyCalendarCellVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate date;
    private Boolean workday;
    private String dayType;
    private String label;
    private String state;
    private Long reportId;
    private String sourceType;
    private String todayWork;
    private String tomorrowPlan;
    private String coordinationNote;
    private Long leaveId;
    private String leaveType;
}
