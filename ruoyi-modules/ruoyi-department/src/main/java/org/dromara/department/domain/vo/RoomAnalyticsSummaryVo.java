package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/** 房间使用分析汇总。 */
@Data
public class RoomAnalyticsSummaryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long totalBookings;

    private Long totalMinutes;

    private Long completedCount;

    private Long cancelledCount;

    private Long noShowCount;

    private Long releasedCount;

    private BigDecimal utilizationRate;

    private Long availableMinutes;

    private Long vacancyMinutes;

    private Long totalDurationMinutes;
}
