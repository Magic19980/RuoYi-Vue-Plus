package org.dromara.department.domain.bo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;

/** 房间预约审批规则新增、修改参数。 */
@Data
public class RoomApprovalRuleBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "审批规则主键不能为空", groups = EditGroup.class)
    private Long id;

    @NotNull(message = "房间主键不能为空")
    private Long roomId;

    @NotNull(message = "审批级别不能为空")
    @Min(value = 1, message = "审批级别必须从1开始")
    @Max(value = 20, message = "最多配置20级审批")
    private Integer stepNo;

    @NotBlank(message = "审批主体类型不能为空")
    private String approverType;

    /** ALL 主体不需要填写审批主体主键。 */
    private Long approverId;

    @NotNull(message = "审批超时时间不能为空")
    @Min(value = 0, message = "审批超时时间不能小于0")
    @Max(value = 43200, message = "审批超时时间不能超过30天")
    private Integer timeoutMinutes;

    private Boolean enabled;

    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;
}
