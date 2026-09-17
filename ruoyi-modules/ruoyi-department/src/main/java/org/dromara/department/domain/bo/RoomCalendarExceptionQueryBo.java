package org.dromara.department.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** 房间日历例外查询参数。 */
@Data
public class RoomCalendarExceptionQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate beginDate;

    private LocalDate endDate;
}
