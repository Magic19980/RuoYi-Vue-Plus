package org.dromara.department.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.lock.annotation.Lock4j;
import org.dromara.department.domain.bo.RoomBookingBo;
import org.dromara.department.domain.bo.RoomBookingExtendBo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 并发预约入口必须保持分布式锁，防止同一房间同一时间被重复占用。 */
@Tag("dev")
class RoomBookingConcurrencyTest {

    @Test
    void bookingMutationEndpointsAreLockGuarded() throws NoSuchMethodException {
        List<Method> methods = List.of(
            RoomBookingController.class.getDeclaredMethod("saveBooking", RoomBookingBo.class),
            RoomBookingController.class.getDeclaredMethod("editBooking", RoomBookingBo.class),
            RoomBookingController.class.getDeclaredMethod("cancelBooking", Long[].class, String.class, Integer.class, String.class),
            RoomBookingController.class.getDeclaredMethod("approve", Long.class, Integer.class),
            RoomBookingController.class.getDeclaredMethod("reject", Long.class, Integer.class, String.class),
            RoomBookingController.class.getDeclaredMethod("checkIn", Long.class, Integer.class),
            RoomBookingController.class.getDeclaredMethod("checkOut", Long.class, Integer.class),
            RoomBookingController.class.getDeclaredMethod("release", Long.class, Integer.class, String.class),
            RoomBookingController.class.getDeclaredMethod("extend", RoomBookingExtendBo.class),
            RoomBookingController.class.getDeclaredMethod("publishFloorPlan", Long.class),
            RoomBookingController.class.getDeclaredMethod("unpublishFloorPlan", Long.class),
            RoomBookingController.class.getDeclaredMethod("restoreFloorPlanVersion", Long.class)
        );
        methods.forEach(method -> assertNotNull(method.getAnnotation(Lock4j.class), method.getName()));
    }

    @Test
    void approvalEndpointsKeepBaseQueryGuardForAclOnlyApprovers() throws NoSuchMethodException {
        for (Method method : List.of(
            RoomBookingController.class.getDeclaredMethod("approve", Long.class, Integer.class),
            RoomBookingController.class.getDeclaredMethod("reject", Long.class, Integer.class, String.class)
        )) {
            SaCheckPermission permission = method.getAnnotation(SaCheckPermission.class);
            assertNotNull(permission, method.getName());
            List<String> values = Arrays.asList(permission.value());
            assertTrue(values.contains("department:room:query"), method.getName());
            assertTrue(values.contains("department:room:approve"), method.getName());
        }
    }

    @Test
    void globalManagerCanEnterBookingMutationEndpoints() throws NoSuchMethodException {
        for (Method method : List.of(
            RoomBookingController.class.getDeclaredMethod("saveBooking", RoomBookingBo.class),
            RoomBookingController.class.getDeclaredMethod("editBooking", RoomBookingBo.class)
        )) {
            SaCheckPermission permission = method.getAnnotation(SaCheckPermission.class);
            assertNotNull(permission, method.getName());
            List<String> values = Arrays.asList(permission.value());
            assertTrue(values.contains("department:room:book"), method.getName());
            assertTrue(values.contains("department:room:manageAll"), method.getName());
        }
    }

    @Test
    void readOnlyRoomAndFloorPlanEntrypointsAcceptQueryOrBookingPermission() throws NoSuchMethodException {
        for (Method method : List.of(
            RoomBookingController.class.getDeclaredMethod("resourceOptions"),
            RoomBookingController.class.getDeclaredMethod("resourceViewOptions"),
            RoomBookingController.class.getDeclaredMethod("floorPlanOptions"),
            RoomBookingController.class.getDeclaredMethod("floorPlanInfo", Long.class)
        )) {
            SaCheckPermission permission = method.getAnnotation(SaCheckPermission.class);
            assertNotNull(permission, method.getName());
            List<String> values = Arrays.asList(permission.value());
            assertTrue(values.contains("department:room:query"), method.getName());
            assertTrue(values.contains("department:room:book"), method.getName());
            assertTrue(values.contains("department:room:manageAll"), method.getName());
        }
    }

    @Test
    void floorPlanVersionEntrypointsAreManagerOnly() throws NoSuchMethodException {
        for (Method method : List.of(
            RoomBookingController.class.getDeclaredMethod("floorPlanVersions", Long.class),
            RoomBookingController.class.getDeclaredMethod("publishFloorPlan", Long.class),
            RoomBookingController.class.getDeclaredMethod("unpublishFloorPlan", Long.class),
            RoomBookingController.class.getDeclaredMethod("restoreFloorPlanVersion", Long.class)
        )) {
            SaCheckPermission permission = method.getAnnotation(SaCheckPermission.class);
            assertNotNull(permission, method.getName());
            List<String> values = Arrays.asList(permission.value());
            assertTrue(values.contains("department:room:manage"), method.getName());
            assertTrue(values.contains("department:room:manageAll"), method.getName());
        }
    }
}
