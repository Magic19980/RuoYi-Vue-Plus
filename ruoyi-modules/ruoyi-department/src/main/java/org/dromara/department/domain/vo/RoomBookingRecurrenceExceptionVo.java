package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 循环预约例外实例视图。 */
@Data
public class RoomBookingRecurrenceExceptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long bookingId;
    private Integer occurrenceNo;
    private LocalDate occurrenceDate;
    private String reason;
    private String status;
    private LocalDateTime createTime;
}
