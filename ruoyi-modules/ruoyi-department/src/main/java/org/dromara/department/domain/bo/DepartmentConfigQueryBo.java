package org.dromara.department.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 业务科室配置查询参数。 */
@Data
public class DepartmentConfigQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String deptName;

    private String status;
}
