package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 配额消费和退款历史视图。 */
@Data
public class RoomQuotaUsageRecordVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long roomId;
    private Long bookingId;
    private Integer occurrenceNo;
    private Long userId;
    private String userName;
    private String action;
    private Integer minutes;
    private Integer bookingCount;
    private BigDecimal amount;
    private String reason;
    private LocalDateTime createTime;
}
