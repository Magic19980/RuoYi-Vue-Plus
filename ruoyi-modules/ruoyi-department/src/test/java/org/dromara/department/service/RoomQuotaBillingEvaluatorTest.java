package org.dromara.department.service;

import org.dromara.department.domain.RoomQuotaPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("dev")
class RoomQuotaBillingEvaluatorTest {

    @Test
    void choosesMostSpecificPricedPolicy() {
        RoomQuotaPolicy allCompany = policy(40L, "ALL", "DAY", "10");
        RoomQuotaPolicy department = policy(20L, "DEPT", "WEEK", "20");
        RoomQuotaPolicy user = policy(10L, "USER", "MONTH", "30");

        assertEquals(user, RoomQuotaBillingEvaluator.selectBillingPolicy(List.of(allCompany, department, user)));
    }

    @Test
    void choosesDailyPolicyWhenOneSubjectHasMultipleBillingPeriods() {
        RoomQuotaPolicy month = policy(20L, "DEPT", "MONTH", "30");
        RoomQuotaPolicy week = policy(10L, "DEPT", "WEEK", "20");
        RoomQuotaPolicy day = policy(30L, "DEPT", "DAY", "10");

        assertEquals(day, RoomQuotaBillingEvaluator.selectBillingPolicy(List.of(month, week, day)));
    }

    @Test
    void ignoresNonPricedPolicies() {
        RoomQuotaPolicy noPrice = policy(1L, "USER", "DAY", "0");

        assertNull(RoomQuotaBillingEvaluator.selectBillingPolicy(List.of(noPrice)));
        assertEquals(new BigDecimal("0.00"), RoomQuotaBillingEvaluator.calculate(noPrice, 60, "CONSUME"));
    }

    @Test
    void calculatesHourlyChargeWithHalfUpRounding() {
        RoomQuotaPolicy policy = policy(1L, "ALL", "DAY", "100");

        assertEquals(new BigDecimal("50.00"), RoomQuotaBillingEvaluator.calculate(policy, 30, "CONSUME"));
        assertEquals(new BigDecimal("6.25"), RoomQuotaBillingEvaluator.calculate(policy, 15, "REFUND"));
    }

    @Test
    void usesFullRefundWhenRateIsMissing() {
        RoomQuotaPolicy policy = policy(1L, "ALL", "DAY", "100");
        policy.setRefundRate(null);

        assertEquals(new BigDecimal("100.00"), RoomQuotaBillingEvaluator.calculate(policy, 60, "REFUND"));
    }

    private static RoomQuotaPolicy policy(Long id, String subjectType, String periodType, String unitPrice) {
        RoomQuotaPolicy policy = new RoomQuotaPolicy();
        policy.setId(id);
        policy.setSubjectType(subjectType);
        policy.setPeriodType(periodType);
        policy.setUnitPrice(new BigDecimal(unitPrice));
        policy.setRefundRate(new BigDecimal("25"));
        return policy;
    }
}
