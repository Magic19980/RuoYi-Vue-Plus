package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;

/** 楼层平面图新增、修改参数。 */
@Data
public class RoomFloorPlanBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "平面图主键不能为空", groups = EditGroup.class)
    private Long id;

    @NotBlank(message = "平面图名称不能为空")
    @Size(max = 100, message = "平面图名称不能超过100个字符")
    private String planName;

    @NotBlank(message = "建筑物名称不能为空")
    @Size(max = 100, message = "建筑物名称不能超过100个字符")
    private String buildingName;

    @NotBlank(message = "楼层名称不能为空")
    @Size(max = 100, message = "楼层名称不能超过100个字符")
    private String floorName;

    @Min(value = 1, message = "楼层序号必须大于等于1")
    private Integer floorNo;

    @Size(max = 500, message = "平面图地址不能超过500个字符")
    private String mapImage;

    @Size(max = 1000000, message = "平面图数据不能超过1000000个字符")
    private String mapData;

    private String status;

    @Size(max = 1000, message = "平面图备注不能超过1000个字符")
    private String remark;
}
