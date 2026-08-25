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

    private String deptName;

    private String status;

    private Long managerUserId;

    private String managerName;

    private Integer sortNum;

    private Long memberCount;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
