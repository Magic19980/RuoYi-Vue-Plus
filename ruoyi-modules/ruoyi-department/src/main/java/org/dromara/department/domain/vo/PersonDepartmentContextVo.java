package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 当前登录用户可用的科室业务上下文。
 */
@Data
public class PersonDepartmentContextVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long deptId;

    private String deptName;

    private String memberType;

    private LocalDate joinDate;

    private LocalDate leaveDate;

    private Boolean current;
}
