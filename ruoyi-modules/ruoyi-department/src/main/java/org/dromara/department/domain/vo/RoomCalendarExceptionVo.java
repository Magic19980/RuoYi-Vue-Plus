package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 房间日历例外视图。 */
@Data
public class RoomCalendarExceptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long deptId;
    private String deptName;
    private LocalDate exceptionDate;
    private String exceptionType;
    private String exceptionName;
    private String remark;
    private LocalDateTime createTime;
}
