package org.dromara.department.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 人员档案新增时的系统用户选择条件。
 */
@Data
public class PersonUserOptionQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 账号、姓名、工号或部门名称关键字。 */
    private String keyword;

    /** 系统部门筛选条件。 */
    private Long deptId;

    /** 用户岗位筛选条件。 */
    private String jobTitle;
}
