package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 房间封锁时段新增、修改参数。 */
@Data
public class RoomBookingBlockBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "封锁记录主键不能为空", groups = EditGroup.class)
    private Long id;

    @NotNull(message = "房间主键不能为空")
    private Long roomId;

    @NotNull(message = "封锁开始时间不能为空")
    private LocalDateTime startAt;

    @NotNull(message = "封锁结束时间不能为空")
    private LocalDateTime endAt;

    private String recurrenceType;

    private Integer recurrenceInterval;

    private Integer recurrenceCount;

    private java.time.LocalDate recurrenceUntil;

    @Size(max = 500, message = "封锁原因不能超过500个字符")
    private String reason;
}
