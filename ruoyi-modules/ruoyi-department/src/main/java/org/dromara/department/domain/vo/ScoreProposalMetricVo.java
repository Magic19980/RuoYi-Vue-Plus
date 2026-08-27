package org.dromara.department.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * SCORE提案月度精益指标。
 */
@Data
public class ScoreProposalMetricVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 统计月份，格式 yyyy-MM。 */
    private String month;

    /** 所选月份内有效的科室人数。 */
    private Integer memberCount = 0;

    /** 月度提案目标 = 科室人数 × 10%。 */
    private BigDecimal monthlyTarget = BigDecimal.ZERO;

    /** 所选月份内现场确认通过的提案数。 */
    private Integer approvedCount = 0;

    /** 所选月份内除暂存外的有效提案总数。 */
    private Integer totalCount = 0;

    /** 所选月份内当前状态为已通过的提案数。 */
    private Integer statusApprovedCount = 0;

    /** 所选月份内待审核的提案数。 */
    private Integer pendingCount = 0;

    /** 所选月份内待现场确认的提案数。 */
    private Integer pendingConfirmCount = 0;

    /** 所选月份内未通过的提案数。 */
    private Integer rejectedCount = 0;

    /** 完成率，单位为百分比。 */
    private BigDecimal completionRate;

    /** 按完成率规则计算的得分。 */
    private Integer score = 0;
}
