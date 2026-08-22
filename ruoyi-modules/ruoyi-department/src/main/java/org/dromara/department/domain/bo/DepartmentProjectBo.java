package org.dromara.department.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;

/**
 * 科室项目新增、修改参数。
 */
@Data
public class DepartmentProjectBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "项目主键不能为空", groups = EditGroup.class)
    private Long id;

    @Size(max = 50, message = "项目编码不能超过50个字符")
    private String projectCode;

    @NotBlank(message = "项目名称不能为空")
    @Size(max = 150, message = "项目名称不能超过150个字符")
    private String projectName;

    @Size(max = 50, message = "项目类型不能超过50个字符")
    private String projectType;

    @Size(max = 100, message = "负责人不能超过100个字符")
    private String responsiblePerson;

    private String status;

    private Integer sortNum;

    @Size(max = 1000, message = "备注不能超过1000个字符")
    private String remark;
}
