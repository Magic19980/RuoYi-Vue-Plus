package org.dromara.department.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * SCORE 提案审核状态及统计口径。
 *
 * <p>提案状态保存在 dm_score_proposal.review_status 中。DRAFT 表示暂存，
 * 不进入本月提案数及状态统计；其余提交后的状态才属于看板统计范围。</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ScoreProposalStatus {

    /** 暂存，尚未提交审核。 */
    public static final String DRAFT = "DRAFT";
    /** 待审核。 */
    public static final String PENDING = "PENDING";
    /** 待现场确认。 */
    public static final String PENDING_CONFIRM = "PENDING_CONFIRM";
    /** 已通过。 */
    public static final String APPROVED = "APPROVED";
    /** 未通过。 */
    public static final String REJECTED = "REJECTED";

    /** 判断提案是否已提交并应计入本月提案统计。 */
    public static boolean countedInMonthlyMetric(String status) {
        return status != null && !DRAFT.equals(status);
    }

    /** 判断提案是否仍允许修改。已提交审核或现场确认中的提案不可直接修改。 */
    public static boolean editable(String status) {
        return DRAFT.equals(status) || REJECTED.equals(status);
    }

    /** 判断提案是否处于需要审核人处理的阶段。 */
    public static boolean pendingReview(String status) {
        return PENDING.equals(status) || PENDING_CONFIRM.equals(status);
    }
}
