package org.dromara.department.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 房间模块的每个 HTTP 入口都必须显式声明权限，避免新增接口绕过权限链。 */
@Tag("dev")
class RoomBookingEndpointContractTest {

    @Test
    void everyHttpEndpointHasPermissionAndUniqueRoute() {
        List<Method> endpoints = Arrays.stream(RoomBookingController.class.getDeclaredMethods())
            .filter(this::isHttpEndpoint)
            .toList();
        assertFalse(endpoints.isEmpty());

        Set<String> routes = new HashSet<>();
        endpoints.forEach(method -> {
            SaCheckPermission permission = method.getAnnotation(SaCheckPermission.class);
            assertNotNull(permission, method.getName());
            if (permission.value().length > 1) {
                assertEquals(SaMode.OR, permission.mode(), method.getName());
            }
            for (String route : routesOf(method)) {
                assertTrue(routes.add(route), "重复房间接口路由: " + route);
            }
        });
    }

    @Test
    void endpointMethodsDoNotExposeUnmappedPublicHelpers() {
        long publicMethods = Arrays.stream(RoomBookingController.class.getDeclaredMethods())
            .filter(method -> java.lang.reflect.Modifier.isPublic(method.getModifiers()))
            .count();
        assertEquals(publicMethods, Arrays.stream(RoomBookingController.class.getDeclaredMethods())
            .filter(this::isHttpEndpoint)
            .count());
    }

    private boolean isHttpEndpoint(Method method) {
        return method.isAnnotationPresent(GetMapping.class)
            || method.isAnnotationPresent(PostMapping.class)
            || method.isAnnotationPresent(PutMapping.class)
            || method.isAnnotationPresent(DeleteMapping.class);
    }

    private List<String> routesOf(Method method) {
        List<String> routes = new ArrayList<>();
        addRoutes(routes, "GET", method.getAnnotation(GetMapping.class));
        addRoutes(routes, "POST", method.getAnnotation(PostMapping.class));
        addRoutes(routes, "PUT", method.getAnnotation(PutMapping.class));
        addRoutes(routes, "DELETE", method.getAnnotation(DeleteMapping.class));
        return routes;
    }

    private void addRoutes(List<String> routes, String httpMethod, Annotation annotation) {
        if (annotation == null) return;
        String[] values;
        if (annotation instanceof GetMapping mapping) values = mapping.value();
        else if (annotation instanceof PostMapping mapping) values = mapping.value();
        else if (annotation instanceof PutMapping mapping) values = mapping.value();
        else if (annotation instanceof DeleteMapping mapping) values = mapping.value();
        else return;
        for (String value : values) routes.add(httpMethod + " " + value);
    }
}
