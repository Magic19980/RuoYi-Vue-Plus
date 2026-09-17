package org.dromara.department.service;

import org.dromara.common.core.exception.ServiceException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** 循环预约和维护计划共用的时间展开规则。 */
public final class RoomBookingRecurrenceCalculator {

    private static final Set<String> TYPES = Set.of("NONE", "DAILY", "WEEKLY", "MONTHLY");

    private RoomBookingRecurrenceCalculator() {
    }

    public record Slot(int occurrenceNo, LocalDateTime startAt, LocalDateTime endAt) {
    }

    public static List<Slot> calculate(String recurrenceType,
                                       LocalDateTime startAt,
                                       LocalDateTime endAt,
                                       Integer intervalValue,
                                       Integer countValue,
                                       LocalDate until,
                                       int maxOccurrences) {
        String type = recurrenceType == null || recurrenceType.isBlank() ? "NONE" : recurrenceType.trim().toUpperCase();
        if (!TYPES.contains(type)) {
            throw new ServiceException("不支持的循环类型：" + recurrenceType);
        }
        int interval = intervalValue == null ? 1 : intervalValue;
        if (interval < 1 || interval > 30) {
            throw new ServiceException("循环间隔必须在1至30之间");
        }
        int count = countValue == null ? 0 : countValue;
        if (count < 0 || count > maxOccurrences) {
            throw new ServiceException("循环预约最多支持" + maxOccurrences + "次");
        }
        if (!"NONE".equals(type) && count == 0 && until == null) {
            throw new ServiceException("循环预约请选择结束日期或填写重复次数");
        }
        long durationMinutes = Duration.between(startAt, endAt).toMinutes();
        List<Slot> result = new ArrayList<>();
        LocalDateTime start = startAt;
        for (int index = 0; index < maxOccurrences; index++) {
            if (count > 0 && index >= count) {
                break;
            }
            if (until != null && start.toLocalDate().isAfter(until)) {
                break;
            }
            result.add(new Slot(index + 1, start, start.plusMinutes(durationMinutes)));
            if ("NONE".equals(type)) {
                break;
            }
            start = switch (type) {
                case "DAILY" -> start.plusDays(interval);
                case "WEEKLY" -> start.plusWeeks(interval);
                case "MONTHLY" -> start.plusMonths(interval);
                default -> throw new ServiceException("不支持的循环类型：" + type);
            };
        }
        if (result.isEmpty()) {
            throw new ServiceException("循环预约至少需要生成一个预约实例");
        }
        return result;
    }
}
