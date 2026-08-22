package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** 日报日期例外规则视图。 */
@Data
public class DailyCalendarOverrideVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String userName;
    private String nickName;
    private LocalDate calendarDate;
    private String dayType;
    private String remark;
}
