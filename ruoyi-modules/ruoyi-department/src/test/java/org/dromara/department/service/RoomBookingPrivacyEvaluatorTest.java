package org.dromara.department.service;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("dev")
class RoomBookingPrivacyEvaluatorTest {

    static Stream<Arguments> privacyMatrix() {
        return Stream.of(
            Arguments.of("公开预约对外可见", "PUBLIC", false, false, false, false),
            Arguments.of("私密预约组织人可见", "PRIVATE", true, false, false, false),
            Arguments.of("私密预约参与者可见", "PRIVATE", false, true, false, false),
            Arguments.of("私密预约全局管理员可见", "PRIVATE", false, false, true, false),
            Arguments.of("私密预约普通旁观者脱敏", "PRIVATE", false, false, false, true),
            Arguments.of("私密标识大小写不影响规则", "private", false, false, false, true),
            Arguments.of("空可见范围不误判为私密", null, false, false, false, false)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("privacyMatrix")
    void evaluatesPrivateBookingMaskingMatrix(String scenario, String visibility,
                                              boolean organizer, boolean attendee,
                                              boolean globalManager, boolean expected) {
        assertEquals(expected, RoomBookingPrivacyEvaluator.shouldMaskPrivate(
            visibility, organizer, attendee, globalManager
        ), scenario);
    }
}
