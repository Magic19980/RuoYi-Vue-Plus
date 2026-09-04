package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 业务科室配置迁移参数。 */
@Data
public class DepartmentConfigMigrationBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "原科室不能为空")
    private Long sourceDeptId;

    @NotNull(message = "目标部门不能为空")
    private Long targetDeptId;
}
