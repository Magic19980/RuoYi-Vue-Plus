package org.dromara.department.service;

/** 预约详情的私密信息脱敏规则，供列表、详情和权限回归测试复用。 */
public final class RoomBookingPrivacyEvaluator {

    private RoomBookingPrivacyEvaluator() {
    }

    public static boolean shouldMaskPrivate(String visibility,
                                             boolean organizer,
                                             boolean attendee,
                                             boolean globalManager) {
        return "PRIVATE".equalsIgnoreCase(visibility)
            && !organizer
            && !attendee
            && !globalManager;
    }
}
