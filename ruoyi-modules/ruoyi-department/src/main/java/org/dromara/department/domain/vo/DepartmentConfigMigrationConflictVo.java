package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 科室数据迁移时的唯一键冲突信息。 */
@Data
public class DepartmentConfigMigrationConflictVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String dataName;

    private Long conflictCount;
}
