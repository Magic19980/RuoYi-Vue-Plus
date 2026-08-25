package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.time.LocalDate;

/** 日报日历人员行。 */
@Data
public class DailyCalendarMemberVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String userName;
    private String nickName;
    private String jobTitle;
    /** 系统用户的原所属部门；用于标识跨科室协作人员。 */
    private String sourceDeptName;
    private LocalDate joinDate;
    private LocalDate leaveDate;
    private List<DailyCalendarCellVo> cells;
}
