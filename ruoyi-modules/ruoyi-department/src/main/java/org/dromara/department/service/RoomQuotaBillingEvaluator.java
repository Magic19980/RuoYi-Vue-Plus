package org.dromara.department.service;

import org.dromara.department.domain.RoomQuotaPolicy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

/**
 * 配额流水计费规则。配额限制和计费策略共用同一组有效策略，但计费只能命中一条最具体的价格策略。
 */
public final class RoomQuotaBillingEvaluator {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private RoomQuotaBillingEvaluator() {
    }

    /**
     * 从已经按用户、科室和角色筛选过的有效策略中选择计费策略。
     * 用户策略优先于科室、角色和全公司策略；同一主体同时配置多个周期时，日周期优先，保证结果唯一且稳定。
     */
    public static RoomQuotaPolicy selectBillingPolicy(List<RoomQuotaPolicy> policies) {
        if (policies == null || policies.isEmpty()) {
            return null;
        }
        return policies.stream()
            .filter(policy -> policy != null && policy.getUnitPrice() != null
                && policy.getUnitPrice().compareTo(BigDecimal.ZERO) > 0)
            .min(Comparator
                .comparingInt((RoomQuotaPolicy policy) -> subjectPriority(policy.getSubjectType()))
                .thenComparingInt(policy -> periodPriority(policy.getPeriodType()))
                .thenComparing(policy -> policy.getId() == null ? Long.MAX_VALUE : policy.getId()))
            .orElse(null);
    }

    public static BigDecimal calculate(RoomQuotaPolicy policy, int minutes, String action) {
        if (policy == null || minutes <= 0 || policy.getUnitPrice() == null
            || policy.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return ZERO;
        }
        BigDecimal amount = policy.getUnitPrice().multiply(BigDecimal.valueOf(minutes))
            .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        if ("REFUND".equalsIgnoreCase(action)) {
            BigDecimal refundRate = policy.getRefundRate() == null ? BigDecimal.valueOf(100) : policy.getRefundRate();
            amount = amount.multiply(refundRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return amount;
    }

    private static int subjectPriority(String subjectType) {
        return switch (subjectType == null ? "" : subjectType.toUpperCase()) {
            case "USER" -> 1;
            case "DEPT" -> 2;
            case "ROLE" -> 3;
            default -> 4;
        };
    }

    private static int periodPriority(String periodType) {
        return switch (periodType == null ? "" : periodType.toUpperCase()) {
            case "DAY" -> 1;
            case "WEEK" -> 2;
            case "MONTH" -> 3;
            default -> 4;
        };
    }
}
