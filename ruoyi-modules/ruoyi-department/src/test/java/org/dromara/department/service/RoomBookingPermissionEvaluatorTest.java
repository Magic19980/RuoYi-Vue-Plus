package org.dromara.department.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.time.LocalTime;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("dev")
class RoomBookingPermissionEvaluatorTest {

    static Stream<Arguments> accessMatrix() {
        return Stream.of(
            Arguments.of("PUBLIC 同科室", "PUBLIC", 10L, 10L, false, false, true),
            Arguments.of("PUBLIC 跨科室", "PUBLIC", 10L, 20L, false, false, true),
            Arguments.of("PUBLIC 当前科室为空", "PUBLIC", 10L, null, false, false, true),
            Arguments.of("DEPT 同科室", "DEPT", 10L, 10L, false, false, true),
            Arguments.of("DEPT 跨科室", "DEPT", 10L, 20L, false, false, false),
            Arguments.of("DEPT 当前科室为空", "DEPT", 10L, null, false, false, false),
            Arguments.of("ACL 明确放行跨科室", "DEPT", 10L, 20L, true, true, true),
            Arguments.of("ACL 明确拒绝同科室", "DEPT", 10L, 10L, true, false, false),
            Arguments.of("PUBLIC 配置 ACL 后未放行", "PUBLIC", 10L, 20L, true, false, false),
            Arguments.of("未知范围不默认放开", "UNKNOWN", 10L, 20L, false, false, false)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("accessMatrix")
    void evaluatesRoomAccessPermissionMatrix(String scenario, String scopeType, Long roomDeptId, Long currentDeptId,
                                             boolean aclConfigured, boolean aclGranted, boolean expected) {
        assertEquals(expected, RoomBookingPermissionEvaluator.canAccess(
            scopeType, roomDeptId, currentDeptId, aclConfigured, aclGranted), scenario);
    }

    @Test
    void departmentRoomCannotCrossDepartmentWithoutAcl() {
        assertTrue(RoomBookingPermissionEvaluator.canAccess("DEPT", 10L, 10L, false, false));
        assertFalse(RoomBookingPermissionEvaluator.canAccess("DEPT", 10L, 20L, false, false));
    }

    @Test
    void explicitAclReplacesDefaultScope() {
        assertTrue(RoomBookingPermissionEvaluator.canAccess("DEPT", 10L, 20L, true, true));
        assertFalse(RoomBookingPermissionEvaluator.canAccess("PUBLIC", 10L, 20L, true, false));
        assertFalse(RoomBookingPermissionEvaluator.canAccess("DEPT", 10L, 10L, true, false));
    }

    @Test
    void publicRoomCanBeBookedAcrossDepartmentsWithoutAcl() {
        assertTrue(RoomBookingPermissionEvaluator.canAccess("PUBLIC", 10L, 20L, false, false));
        assertTrue(RoomBookingPermissionEvaluator.canAccess("PUBLIC", 20L, 10L, false, false));
    }

    @Test
    void unknownScopeDoesNotAccidentallyBecomeGlobal() {
        assertFalse(RoomBookingPermissionEvaluator.canAccess("UNKNOWN", 10L, 20L, false, false));
        assertTrue(RoomBookingPermissionEvaluator.canAccess("dept", 10L, 10L, false, false));
    }

    @Test
    void publicRoomIsVisibleWithoutAclAndNullDepartmentDoesNotMatch() {
        assertTrue(RoomBookingPermissionEvaluator.canAccess("PUBLIC", null, 20L, false, false));
        assertFalse(RoomBookingPermissionEvaluator.canAccess("DEPT", null, 20L, false, false));
    }

    @Test
    void blacklistAlwaysDeniesBooking() {
        assertTrue(RoomBookingPermissionEvaluator.isBlacklisted(true));
        assertFalse(RoomBookingPermissionEvaluator.isBlacklisted(false));
    }

    @Test
    void onlyEnabledRoomsAreBookable() {
        assertTrue(RoomBookingPermissionEvaluator.isBookableStatus("ENABLED"));
        assertTrue(RoomBookingPermissionEvaluator.isBookableStatus(" enabled "));
        assertFalse(RoomBookingPermissionEvaluator.isBookableStatus("MAINTENANCE"));
        assertFalse(RoomBookingPermissionEvaluator.isBookableStatus("DISABLED"));
        assertFalse(RoomBookingPermissionEvaluator.isBookableStatus(null));
    }

    @Test
    void openingWindowMustBeAValidSameDayRange() {
        assertTrue(RoomBookingPermissionEvaluator.isValidOpeningWindow(LocalTime.of(8, 0), LocalTime.of(22, 0)));
        assertFalse(RoomBookingPermissionEvaluator.isValidOpeningWindow(LocalTime.of(22, 0), LocalTime.of(8, 0)));
        assertFalse(RoomBookingPermissionEvaluator.isValidOpeningWindow(LocalTime.of(8, 0), LocalTime.of(8, 0)));
        assertFalse(RoomBookingPermissionEvaluator.isValidOpeningWindow(null, LocalTime.of(8, 0)));
    }

    @Test
    void crossDayBookingRequiresRoomPolicyAndValidEndpointTimes() {
        LocalTime open = LocalTime.of(8, 0);
        LocalTime close = LocalTime.of(22, 0);
        LocalDateTime sameDayStart = LocalDateTime.of(2026, 9, 16, 9, 0);
        LocalDateTime sameDayEnd = LocalDateTime.of(2026, 9, 16, 10, 0);
        LocalDateTime crossDayEnd = LocalDateTime.of(2026, 9, 17, 10, 0);

        assertTrue(RoomBookingPermissionEvaluator.isValidBookingWindow(sameDayStart, sameDayEnd, open, close, false));
        assertFalse(RoomBookingPermissionEvaluator.isValidBookingWindow(sameDayStart, crossDayEnd, open, close, false));
        assertTrue(RoomBookingPermissionEvaluator.isValidBookingWindow(sameDayStart, crossDayEnd, open, close, true));
        assertFalse(RoomBookingPermissionEvaluator.isValidBookingWindow(
            sameDayStart, LocalDateTime.of(2026, 9, 17, 23, 0), open, close, true));
    }
}
