package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 周报快照中的日报条目。
 */
@Data
public class WeeklyReportItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate reportDate;

    private String userName;

    private String todayWork;

    private String tomorrowPlan;

    private String coordinationNote;
}
