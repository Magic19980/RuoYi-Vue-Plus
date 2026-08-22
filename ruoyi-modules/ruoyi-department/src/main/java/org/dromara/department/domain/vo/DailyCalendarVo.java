package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/** 科室日报日历视图。 */
@Data
public class DailyCalendarVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate month;
    private LocalDate beginDate;
    private LocalDate endDate;

    private Boolean futureMonth;
    private String workDays;
    private List<DailyCalendarDayVo> days;
    private List<DailyCalendarMemberVo> members;
    private Integer requiredCount;
    private Integer filledCount;
    private Integer missingCount;
    private Integer leaveCount;
}
