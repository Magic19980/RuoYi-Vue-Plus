package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 人员档案绑定用的系统用户选项。
 */
@Data
public class PersonUserOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long deptId;

    private String userName;

    private String nickName;

    /** 用户管理中的印尼语姓名。 */
    private String indonesianName;

    private String deptName;

    /** 用户所属部门的印尼语名称。 */
    private String deptIndonesianName;

    private String employeeNo;

    /** 用户管理中的当前岗位。 */
    private String jobTitle;

    /** 用户当前岗位的印尼语名称。 */
    private String jobTitleIndonesianName;
}
