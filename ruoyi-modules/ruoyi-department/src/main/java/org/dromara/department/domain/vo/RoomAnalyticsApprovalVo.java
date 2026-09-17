package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/** 审批时长和驳回原因统计。 */
@Data
public class RoomAnalyticsApprovalVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long submitCount;
    private Long approveCount;
    private Long rejectCount;
    private BigDecimal averageMinutes;
    private String rejectReasons;
}
