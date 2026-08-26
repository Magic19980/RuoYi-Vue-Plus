package org.dromara.department.api.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户当前服务的科室关系。
 */
@Data
@NoArgsConstructor
public class DepartmentMembershipDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户 ID。 */
    private Long userId;

    /** 用户登录账号。 */
    private String userName;

    /** 科室 ID。 */
    private Long deptId;

    /** 科室名称。 */
    private String deptName;
}
