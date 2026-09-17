package org.dromara.department.service;

import java.util.Locale;
import java.util.Objects;

/** 房间预约审批主体匹配规则，供服务层和权限矩阵测试复用。 */
public final class RoomBookingApprovalEvaluator {

    private RoomBookingApprovalEvaluator() {
    }

    public static boolean isMember(String approverType,
                                   Long approverId,
                                   Long userId,
                                   Long currentDeptId,
                                   boolean roleMember,
                                   boolean globalManager) {
        if (globalManager) {
            return true;
        }
        String type = approverType == null ? "" : approverType.trim().toUpperCase(Locale.ROOT);
        return switch (type) {
            case "ALL" -> true;
            case "USER" -> Objects.equals(approverId, userId);
            case "DEPT" -> Objects.equals(approverId, currentDeptId);
            case "ROLE" -> roleMember;
            default -> false;
        };
    }
}
