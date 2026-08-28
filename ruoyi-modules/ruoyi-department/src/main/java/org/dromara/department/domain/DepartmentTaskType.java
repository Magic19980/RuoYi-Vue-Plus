package org.dromara.department.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** 科室周期任务业务类型。 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DepartmentTaskType {

    /** SCORE 提案任务。 */
    public static final String SCORE_PROPOSAL = "SCORE_PROPOSAL";
    /** 5WHY 分析任务。 */
    public static final String FIVE_WHY = "FIVE_WHY";
    /** 日报任务。 */
    public static final String DAILY_REPORT = "DAILY_REPORT";
}
