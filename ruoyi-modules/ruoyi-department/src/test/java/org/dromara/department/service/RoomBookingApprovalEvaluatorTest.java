package org.dromara.department.service;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("dev")
class RoomBookingApprovalEvaluatorTest {

    static Stream<Arguments> approvalMatrix() {
        return Stream.of(
            Arguments.of("用户审批人命中", "USER", 101L, 101L, 20L, false, false, true),
            Arguments.of("用户审批人未命中", "USER", 101L, 202L, 20L, false, false, false),
            Arguments.of("科室审批人命中", "DEPT", 20L, 202L, 20L, false, false, true),
            Arguments.of("科室审批人未命中", "DEPT", 10L, 202L, 20L, false, false, false),
            Arguments.of("角色审批人命中", "ROLE", 7L, 202L, 20L, true, false, true),
            Arguments.of("角色审批人未命中", "ROLE", 7L, 202L, 20L, false, false, false),
            Arguments.of("全公司审批人", "ALL", null, 202L, 20L, false, false, true),
            Arguments.of("管理员绕过主体匹配", "USER", 999L, 202L, 20L, false, true, true),
            Arguments.of("全局审批权限绕过当前主体", "ROLE", 7L, 202L, 20L, false, true, true),
            Arguments.of("未知主体默认拒绝", "UNKNOWN", 999L, 202L, 20L, true, false, false),
            Arguments.of("缺少审批规则默认拒绝", null, null, 202L, 20L, false, false, false),
            Arguments.of("主体类型大小写和空格归一", " dept ", 20L, 202L, 20L, false, false, true)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("approvalMatrix")
    void evaluatesApprovalSubjectMatrix(String scenario, String type, Long approverId, Long userId,
                                        Long deptId, boolean roleMember, boolean globalManager, boolean expected) {
        assertEquals(expected, RoomBookingApprovalEvaluator.isMember(
            type, approverId, userId, deptId, roleMember, globalManager
        ), scenario);
    }
}
