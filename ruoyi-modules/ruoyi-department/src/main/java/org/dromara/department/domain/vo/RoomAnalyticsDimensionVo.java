package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/** 预约分析的科室或用户维度。 */
@Data
public class RoomAnalyticsDimensionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long dimensionId;
    private String dimensionName;
    private Long bookingCount;
    private Long bookedMinutes;
    private Long noShowCount;
    private BigDecimal utilizationRate;
}
