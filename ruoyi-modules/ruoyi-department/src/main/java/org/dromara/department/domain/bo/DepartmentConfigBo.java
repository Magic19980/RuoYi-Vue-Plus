package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;

/** 业务科室配置新增、修改参数。 */
@Data
public class DepartmentConfigBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "科室ID不能为空")
    private Long deptId;

    @NotNull(message = "配置主键不能为空", groups = EditGroup.class)
    private Long id;

    private String status;

    private Long managerUserId;

    private Integer sortNum;

    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;
}
