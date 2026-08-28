package org.dromara.department.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** 科室人员服务关系状态。 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DepartmentMemberStatus {

    /** 当前仍在科室服务。 */
    public static final String ACTIVE = "ACTIVE";
    /** 服务关系已结束，历史记录保留。 */
    public static final String ENDED = "ENDED";
}
