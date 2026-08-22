package org.dromara.department.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 科室项目查询参数。
 */
@Data
public class DepartmentProjectQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String projectCode;

    private String projectName;

    private String projectType;

    private String status;
}
