package org.dromara.department.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.LinkedHashSet;
import java.util.Set;

/** 发布平面图时校验 ROOM 图元与系统房间资源的一致性。 */
public final class RoomFloorPlanMapDataBindingValidator {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    private RoomFloorPlanMapDataBindingValidator() {
    }

    /** 返回图纸中所有已绑定的房间 ID；未绑定 ROOM 图元和重复绑定会阻止发布。 */
    public static Set<String> collectPublishedRoomIds(String mapData) {
        Set<String> roomIds = new LinkedHashSet<>();
        if (mapData == null || mapData.isBlank()) {
            return roomIds;
        }
        final JsonNode root;
        try {
            root = JSON_MAPPER.readTree(mapData);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("平面图图形数据必须是合法的 JSON 对象", exception);
        }
        if (!(root instanceof ObjectNode document)) {
            throw new IllegalArgumentException("平面图图形数据必须是 JSON 对象");
        }
        JsonNode elementsNode = document.get("elements");
        if (elementsNode == null || elementsNode.isNull()) {
            return roomIds;
        }
        if (!(elementsNode instanceof ArrayNode elements)) {
            throw new IllegalArgumentException("平面图 elements 必须是数组");
        }
        for (int index = 0; index < elements.size(); index++) {
            JsonNode node = elements.get(index);
            if (!(node instanceof ObjectNode element)
                || !"ROOM".equalsIgnoreCase(text(element.get("type")))) {
                continue;
            }
            String roomId = text(element.get("roomId"));
            if (roomId.isBlank()) {
                throw invalidElement(index, "房间图元必须绑定系统房间后才能发布");
            }
            if (!roomIds.add(roomId)) {
                throw invalidElement(index, "房间资源重复绑定：" + roomId);
            }
        }
        return roomIds;
    }

    public static void validatePublishedBindings(String mapData, Set<String> expectedRoomIds) {
        Set<String> actualRoomIds = collectPublishedRoomIds(mapData);
        Set<String> expected = expectedRoomIds == null ? Set.of() : new LinkedHashSet<>(expectedRoomIds);
        Set<String> missing = new LinkedHashSet<>(expected);
        missing.removeAll(actualRoomIds);
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("已分配到当前平面图的房间尚未放置图元：" + String.join(", ", missing));
        }
        Set<String> unknown = new LinkedHashSet<>(actualRoomIds);
        unknown.removeAll(expected);
        if (!unknown.isEmpty()) {
            throw new IllegalArgumentException("图纸绑定了不属于当前平面图的房间：" + String.join(", ", unknown));
        }
    }

    private static String text(JsonNode node) {
        return node == null || node.isNull() ? "" : node.asText("").trim();
    }

    private static IllegalArgumentException invalidElement(int index, String message) {
        return new IllegalArgumentException("平面图第" + (index + 1) + "个元素" + message);
    }
}
