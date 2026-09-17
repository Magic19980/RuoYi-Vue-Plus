package org.dromara.department.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 房间预约日历和列表查询参数。 */
@Data
public class RoomBookingQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDateTime beginAt;

    private LocalDateTime endAt;

    private Long roomId;

    private String roomType;

    private String keyword;

    private String status;

    private Boolean mine;

    /** 仅查询当前用户可处理的待审批实例。 */
    private Boolean approvalOnly;
}
