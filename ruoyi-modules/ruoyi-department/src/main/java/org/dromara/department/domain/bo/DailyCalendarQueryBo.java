package org.dromara.department.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** 日报日历查询参数。 */
@Data
public class DailyCalendarQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate month;
}
