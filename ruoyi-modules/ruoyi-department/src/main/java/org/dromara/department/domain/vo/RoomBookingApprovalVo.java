package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 预约审批/变更历史视图。 */
@Data
public class RoomBookingApprovalVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long bookingId;
    private Integer occurrenceNo;
    private String action;
    private String fromStatus;
    private String toStatus;
    private Long operatorId;
    private String operatorName;
    private String reason;
    private LocalDateTime createTime;
}
