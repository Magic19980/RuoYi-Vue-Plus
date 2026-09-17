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
import java.math.BigDecimal;

/** 房间预约配额策略新增、修改参数。 */
@Data
public class RoomQuotaPolicyBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "配额策略主键不能为空", groups = EditGroup.class)
    private Long id;

    @NotNull(message = "房间主键不能为空")
    private Long roomId;

    @NotBlank(message = "配额主体类型不能为空")
    private String subjectType;

    private Long subjectId;

    @NotBlank(message = "配额周期不能为空")
    private String periodType;

    @NotNull(message = "配额分钟数不能为空")
    @Min(value = 0, message = "配额分钟数不能小于0")
    @Max(value = 44640, message = "配额分钟数不能超过31天")
    private Integer quotaMinutes;

    @NotNull(message = "配额次数不能为空")
    @Min(value = 0, message = "配额次数不能小于0")
    @Max(value = 9999, message = "配额次数不能超过9999")
    private Integer quotaCount;

    private String overQuotaAction;

    private BigDecimal unitPrice;

    private BigDecimal refundRate;

    private Boolean enabled;

    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;
}
