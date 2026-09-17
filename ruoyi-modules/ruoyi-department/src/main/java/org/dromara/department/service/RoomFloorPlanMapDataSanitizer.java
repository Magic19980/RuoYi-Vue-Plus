package org.dromara.department.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Set;

/** 过滤平面图中的不可见房间，避免仅依赖前端隐藏造成几何信息泄露。 */
public final class RoomFloorPlanMapDataSanitizer {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    private RoomFloorPlanMapDataSanitizer() {
    }

    public static String sanitize(String mapData, Set<String> accessibleRoomIds) {
        if (mapData == null || mapData.isBlank()) {
            return mapData;
        }
        try {
            JsonNode root = JSON_MAPPER.readTree(mapData);
            if (!(root instanceof ObjectNode document)) {
                return emptyDocument();
            }
            JsonNode elementsNode = document.get("elements");
            if (elementsNode instanceof ArrayNode elements) {
                for (int index = elements.size() - 1; index >= 0; index--) {
                    JsonNode element = elements.get(index);
                    JsonNode typeNode = element == null ? null : element.get("type");
                    if (typeNode == null || !"ROOM".equalsIgnoreCase(typeNode.asText())) {
                        continue;
                    }
                    JsonNode roomIdNode = element.get("roomId");
                    String roomId = roomIdNode == null ? "" : roomIdNode.asText();
                    if (roomId.isBlank() || accessibleRoomIds == null || !accessibleRoomIds.contains(roomId)) {
                        elements.remove(index);
                    }
                }
            }
            return JSON_MAPPER.writeValueAsString(document);
        } catch (RuntimeException exception) {
            return emptyDocument();
        }
    }

    /**
     * 判断平面图原始数据是否包含当前用户不可见的房间。
     *
     * <p>背景图通常是整张楼层截图，无法像 mapData 一样逐个图元裁剪；只要原始
     * 语义数据中存在不可见房间，调用方就应隐藏背景图，避免绕过图元脱敏。</p>
     */
    public static boolean hasInaccessibleRooms(String mapData, Set<String> accessibleRoomIds) {
        if (mapData == null || mapData.isBlank()) {
            return true;
        }
        try {
            JsonNode root = JSON_MAPPER.readTree(mapData);
            if (!(root instanceof ObjectNode document)) {
                return true;
            }
            JsonNode elementsNode = document.get("elements");
            if (!(elementsNode instanceof ArrayNode elements)) {
                return true;
            }
            for (JsonNode element : elements) {
                if (element == null || !"ROOM".equalsIgnoreCase(text(element, "type"))) {
                    continue;
                }
                String roomId = text(element, "roomId");
                if (roomId.isBlank() || accessibleRoomIds == null || !accessibleRoomIds.contains(roomId)) {
                    return true;
                }
            }
            return false;
        } catch (RuntimeException exception) {
            return true;
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null ? "" : value.asText("");
    }

    private static String emptyDocument() {
        return "{\"version\":4,\"width\":1200,\"height\":680,\"unit\":\"px\",\"elements\":[]}";
    }
}
