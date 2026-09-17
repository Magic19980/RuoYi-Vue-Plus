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

/** 房间设施目录新增、修改参数。 */
@Data
public class RoomAmenityBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "设施主键不能为空", groups = EditGroup.class)
    private Long id;

    @NotBlank(message = "设施名称不能为空")
    @Size(max = 100, message = "设施名称不能超过100个字符")
    private String amenityName;

    @Size(max = 50, message = "设施分类不能超过50个字符")
    private String category;

    @Size(max = 100, message = "设施图标不能超过100个字符")
    private String icon;

    @Min(value = 0, message = "排序值不能小于0")
    @Max(value = 9999, message = "排序值不能超过9999")
    private Integer sortNo;

    private String status;

    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;
}
