package org.dromara.department.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** 周期任务实例及进度状态。 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DepartmentTaskStatus {

    /** 尚未完成任何要求。 */
    public static final String NOT_STARTED = "NOT_STARTED";
    /** 已完成部分要求。 */
    public static final String IN_PROGRESS = "IN_PROGRESS";
    /** 已达到要求次数。 */
    public static final String COMPLETED = "COMPLETED";
    /** 已超过截止时间且未完成。 */
    public static final String OVERDUE = "OVERDUE";
}
