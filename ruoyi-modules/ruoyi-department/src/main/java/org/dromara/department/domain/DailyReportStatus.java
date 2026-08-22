package org.dromara.department.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 日报状态常量。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DailyReportStatus {

    public static final String SUBMITTED = "SUBMITTED";

    public static boolean editable(String status) {
        return SUBMITTED.equals(status);
    }
}
