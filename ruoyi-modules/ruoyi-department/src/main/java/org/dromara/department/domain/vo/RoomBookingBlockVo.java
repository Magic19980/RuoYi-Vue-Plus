package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 房间封锁时段展示视图。 */
@Data
public class RoomBookingBlockVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long roomId;
    private String roomName;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String reason;
    private String status;

    private String recurrenceType;

    private Integer recurrenceInterval;

    private Integer recurrenceCount;

    private java.time.LocalDate recurrenceUntil;
    private LocalDateTime createTime;
}
