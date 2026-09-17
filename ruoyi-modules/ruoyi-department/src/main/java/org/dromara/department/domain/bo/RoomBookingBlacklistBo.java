package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;

/** 房间预约黑名单参数。 */
@Data
public class RoomBookingBlacklistBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "黑名单主键不能为空", groups = EditGroup.class)
    private Long id;

    @NotNull(message = "房间主键不能为空")
    private Long roomId;

    @NotBlank(message = "黑名单主体类型不能为空")
    private String subjectType;

    private Long subjectId;

    @Size(max = 500, message = "黑名单原因不能超过500个字符")
    private String reason;

    private Boolean enabled;
}
