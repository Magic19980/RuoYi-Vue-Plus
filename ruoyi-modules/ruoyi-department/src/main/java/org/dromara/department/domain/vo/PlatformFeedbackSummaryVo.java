package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 平台问题与建议汇总视图。
 */
@Data
public class PlatformFeedbackSummaryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer totalCount;

    private Integer pendingCount;

    private Integer processingCount;

    private Integer waitingCount;

    private Integer closedCount;

    private Integer suggestionCount;
}
