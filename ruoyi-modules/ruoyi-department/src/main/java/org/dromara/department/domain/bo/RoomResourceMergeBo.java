package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** 房间组合/合并参数。 */
@Data
public class RoomResourceMergeBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "至少选择两个房间")
    @Size(min = 2, max = 20, message = "房间组合必须包含2至20个房间")
    private List<Long> roomIds;

    @NotBlank(message = "组合编码不能为空")
    @Size(max = 64, message = "组合编码不能超过64个字符")
    private String mergeGroup;
}
