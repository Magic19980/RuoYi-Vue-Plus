package org.dromara.department.service;

import java.time.LocalDateTime;
import java.time.LocalTime;

/** 房间范围与 ACL 的纯规则判断，供服务层和权限回归测试复用。 */
public final class RoomBookingPermissionEvaluator {

    private RoomBookingPermissionEvaluator() {
    }

    public static boolean canAccess(String scopeType,
                                    Long roomDeptId,
                                    Long currentDeptId,
                                    boolean aclConfigured,
                                    boolean aclGranted) {
        if (aclConfigured) {
            return aclGranted;
        }
        return "PUBLIC".equalsIgnoreCase(scopeType) || (roomDeptId != null && roomDeptId.equals(currentDeptId));
    }

    public static boolean isBlacklisted(boolean matched) {
        return matched;
    }

    /**
     * 权限通过不代表资源状态允许使用，维护中和停用房间必须在服务端统一拦截。
     */
    public static boolean isBookableStatus(String status) {
        return "ENABLED".equalsIgnoreCase(status == null ? "" : status.trim());
    }

    /** 当前营业时间配置使用同一自然日窗口，因此结束时间必须晚于开始时间。 */
    public static boolean isValidOpeningWindow(LocalTime openTime, LocalTime closeTime) {
        return openTime != null && closeTime != null && closeTime.isAfter(openTime);
    }

    /**
     * 检查预约起止时间；跨日预约必须由房间显式开启，且起止时刻都要落在营业窗口内。
     * 跨日中间的闭馆时段属于房间被连续占用的策略范围，不会被拆成多个预约实例。
     */
    public static boolean isValidBookingWindow(LocalDateTime startAt,
                                               LocalDateTime endAt,
                                               LocalTime openTime,
                                               LocalTime closeTime,
                                               boolean allowCrossDay) {
        if (startAt == null || endAt == null || !endAt.isAfter(startAt)
            || !isValidOpeningWindow(openTime, closeTime)) {
            return false;
        }
        boolean crossDay = !startAt.toLocalDate().equals(endAt.toLocalDate());
        if (crossDay && !allowCrossDay) {
            return false;
        }
        return isWithin(openTime, closeTime, startAt.toLocalTime())
            && isWithin(openTime, closeTime, endAt.toLocalTime());
    }

    private static boolean isWithin(LocalTime openTime, LocalTime closeTime, LocalTime value) {
        return !value.isBefore(openTime) && !value.isAfter(closeTime);
    }
}
