package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 预约高峰时段。 */
@Data
public class RoomAnalyticsHotSlotVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer hourOfDay;
    private Long bookingCount;
    private Long bookedMinutes;
}
