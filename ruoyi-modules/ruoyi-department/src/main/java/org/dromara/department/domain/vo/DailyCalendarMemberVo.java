package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** 日报日历人员行。 */
@Data
public class DailyCalendarMemberVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String userName;
    private String nickName;
    private String jobTitle;
    private List<DailyCalendarCellVo> cells;
}
