package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/** 房间使用分析明细。 */
@Data
public class RoomAnalyticsRoomVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long roomId;

    private String roomName;

    private String roomCode;

    private Long bookingCount;

    private Long bookedMinutes;

    private Long completedCount;

    private Long cancelledCount;

    private Long noShowCount;

    private BigDecimal utilizationRate;
}
