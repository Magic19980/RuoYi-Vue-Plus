package org.dromara.department.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 周报状态常量。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WeeklyReportStatus {

    /** 已生成状态。 */
    public static final String GENERATED = "GENERATED";
}
