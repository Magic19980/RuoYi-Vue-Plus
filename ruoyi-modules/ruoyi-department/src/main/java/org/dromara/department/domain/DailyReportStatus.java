package org.dromara.department.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 日报状态常量。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DailyReportStatus {

    /** 已提交状态。 */
    public static final String SUBMITTED = "SUBMITTED";

    /**
     * 判断日报是否允许编辑。
     *
     * @param status 日报状态
     * @return 是否允许编辑
     */
    public static boolean editable(String status) {
        return SUBMITTED.equals(status);
    }
}
