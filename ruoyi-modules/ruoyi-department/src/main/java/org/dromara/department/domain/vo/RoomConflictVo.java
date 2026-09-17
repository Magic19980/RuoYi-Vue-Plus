package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 单个预约冲突说明。 */
@Data
public class RoomConflictVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long roomId;
    private String roomName;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String reason;
}
