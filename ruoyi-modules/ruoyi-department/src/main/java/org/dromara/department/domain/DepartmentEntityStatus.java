package org.dromara.department.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** 科室业务配置及规则的启停状态。 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DepartmentEntityStatus {

    /** 已启用。 */
    public static final String ENABLED = "ENABLED";
    /** 已停用。 */
    public static final String DISABLED = "DISABLED";
}
