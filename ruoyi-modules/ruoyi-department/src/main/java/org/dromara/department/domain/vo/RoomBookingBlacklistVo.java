package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 房间预约黑名单视图。 */
@Data
public class RoomBookingBlacklistVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long roomId;
    private String roomName;
    private String subjectType;
    private Long subjectId;
    private String subjectName;
    private String reason;
    private Boolean enabled;
    private LocalDateTime createTime;
}
