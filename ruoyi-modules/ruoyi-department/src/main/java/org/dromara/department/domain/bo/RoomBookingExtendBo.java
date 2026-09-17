package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 使用中预约延长参数。 */
@Data
public class RoomBookingExtendBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "预约主键不能为空")
    private Long id;

    private Integer occurrenceNo;

    @NotNull(message = "新的结束时间不能为空")
    private LocalDateTime endAt;
}
