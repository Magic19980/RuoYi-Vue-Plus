package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 业务科室配置视图。 */
@Data
public class DepartmentConfigVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long deptId;

    /** 泛微组织树中的父部门。 */
    private Long parentId;

    private String deptName;

    /** 是否可以配置为业务科室。 */
    private Boolean selectable;

    /** 是否存在直属下级部门。 */
    private Boolean hasChildren;

    /** 对应的系统部门是否仍为泛微同步后的有效部门。 */
    private Boolean systemDeptAvailable;

    private String status;

    private Long managerUserId;

    private String managerName;

    private Integer sortNum;

    private Long memberCount;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
