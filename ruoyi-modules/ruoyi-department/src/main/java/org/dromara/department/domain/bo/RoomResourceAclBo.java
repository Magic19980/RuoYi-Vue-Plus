package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;

/** 房间级授权新增、修改参数。 */
@Data
public class RoomResourceAclBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "授权主键不能为空", groups = EditGroup.class)
    private Long id;

    @NotNull(message = "房间主键不能为空")
    private Long roomId;

    @NotBlank(message = "授权主体类型不能为空")
    private String subjectType;

    /** ALL 主体不需要填写 subjectId。 */
    private Long subjectId;

    @NotBlank(message = "授权动作不能为空")
    private String permissionType;
}
