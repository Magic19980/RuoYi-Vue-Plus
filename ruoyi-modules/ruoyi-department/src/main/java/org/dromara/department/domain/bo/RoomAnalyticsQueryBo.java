package org.dromara.department.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 房间使用分析查询参数。 */
@Data
public class RoomAnalyticsQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDateTime beginAt;

    private LocalDateTime endAt;
}
