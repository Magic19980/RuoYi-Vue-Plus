package org.dromara.department.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** 校验写入接口的语义化平面图数据，避免仅依赖前端编辑器校验。 */
public final class RoomFloorPlanMapDataValidator {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();
    private static final Set<String> ELEMENT_TYPES = Set.of(
        "ROOM", "WALL", "DOOR", "WINDOW", "STAIR", "ELEVATOR", "RECEPTION", "POI", "LANDSCAPE"
    );
    private static final Set<String> ROOM_PURPOSES = Set.of(
        "WORKSPACE", "MEETING", "TRAINING", "RECEPTION", "LOUNGE", "STORAGE"
    );
    private static final int MAX_ELEMENTS = 2000;
    private static final double MAX_CANVAS_SIZE = 10000;
    private static final double MAX_COORDINATE = 100000;
    private static final int MAX_CONNECTOR_ID_LENGTH = 50;
    private static final int MAX_MODEL_KEY_LENGTH = 120;
    private static final Pattern MODEL_KEY_PATTERN = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{0,119}");
    private static final Set<String> REGISTERED_MODEL_KEYS = Set.of(
        "office.room.standard", "office.room.meeting", "office.furniture.desk", "office.furniture.meeting-table",
        "office.furniture.chair", "office.furniture.reception", "office.furniture.lounge", "office.furniture.storage",
        "office.prop.plant", "office.structure.stair",
        "office.structure.elevator", "animal.room.cabin", "animal.furniture.market", "animal.furniture.reception",
        "animal.prop.tree", "animal.prop.lamp", "animal.prop.island-decor", "animal.character.resident",
        "animal.furniture.lounge", "animal.furniture.storage",
        "animal.structure.stair", "animal.structure.elevator"
    );
    private static final Map<String, Set<String>> MODEL_KEYS_BY_ELEMENT_TYPE = Map.of(
        "ROOM", Set.of("office.room.standard", "office.room.meeting", "animal.room.cabin"),
        "RECEPTION", Set.of("office.furniture.reception", "animal.furniture.reception"),
        "STAIR", Set.of("office.structure.stair", "animal.structure.stair"),
        "ELEVATOR", Set.of("office.structure.elevator", "animal.structure.elevator"),
        "POI", Set.of(
            "office.furniture.desk", "office.furniture.meeting-table", "office.furniture.chair",
            "office.furniture.reception", "office.prop.plant", "animal.furniture.market",
            "animal.furniture.reception", "animal.prop.tree", "animal.prop.lamp",
            "animal.prop.island-decor", "animal.character.resident", "office.furniture.lounge",
            "office.furniture.storage", "animal.furniture.lounge", "animal.furniture.storage"
        ),
        "LANDSCAPE", Set.of(
            "office.prop.plant", "animal.prop.tree", "animal.prop.lamp", "animal.prop.island-decor",
            "animal.character.resident"
        )
    );

    private RoomFloorPlanMapDataValidator() {
    }

    public static void validate(String mapData) {
        validate(mapData, false);
    }

    /** 发布前执行完整的空间关系校验；草稿保存允许暂时保留未完成的门窗布局。 */
    public static void validateForPublish(String mapData) {
        validate(mapData, true);
    }

    private static void validate(String mapData, boolean requireOpeningsNearWall) {
        if (mapData == null || mapData.isBlank()) {
            return;
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

        double width = readCanvasSize(document, "width", 1200);
        double height = readCanvasSize(document, "height", 680);
        JsonNode elementsNode = document.get("elements");
        if (elementsNode == null || elementsNode.isNull()) {
            return;
        }
        if (!(elementsNode instanceof ArrayNode elements)) {
            throw new IllegalArgumentException("平面图 elements 必须是数组");
        }
        if (elements.size() > MAX_ELEMENTS) {
            throw new IllegalArgumentException("平面图元素数量不能超过" + MAX_ELEMENTS + "个");
        }

        Set<String> ids = new HashSet<>();
        List<WallSegment> walls = new ArrayList<>();
        List<OpeningCenter> openings = new ArrayList<>();
        List<RoomBounds> rooms = new ArrayList<>();
        for (int index = 0; index < elements.size(); index++) {
            JsonNode node = elements.get(index);
            if (!(node instanceof ObjectNode element)) {
                throw invalidElement(index, "必须是对象");
            }
            String id = text(element.get("id"));
            if (id.isBlank()) {
                throw invalidElement(index, "缺少 id");
            }
            if (!ids.add(id)) {
                throw invalidElement(index, "id 重复：" + id);
            }

            String type = text(element.get("type")).toUpperCase(Locale.ROOT);
            if (!ELEMENT_TYPES.contains(type)) {
                throw invalidElement(index, "不支持的元素类型：" + type);
            }
            validateSemanticMetadata(element, type, index);
            double x = readCoordinate(element, "x", index);
            double y = readCoordinate(element, "y", index);
            double elementWidth = readCoordinate(element, "width", index);
            double elementHeight = readCoordinate(element, "height", index);
            double rotation = readOptionalNumber(element.get("rotation"), "rotation", index, 0);
            if ("WALL".equals(type)) {
                if (elementWidth == 0 && elementHeight == 0) {
                    throw invalidElement(index, "墙体线段长度不能为 0");
                }
                WallSegment wall = rotatedWallSegment(x, y, elementWidth, elementHeight, rotation);
                if (isOutOfBounds(wall, width, height)) {
                    throw invalidElement(index, "超出平面图范围");
                }
                walls.add(wall);
            } else if (elementWidth <= 0 || elementHeight <= 0) {
                throw invalidElement(index, "宽度和高度必须大于 0");
            }
            if ("DOOR".equals(type) || "WINDOW".equals(type)) {
                openings.add(new OpeningCenter(index, x + elementWidth / 2, y + elementHeight / 2));
            }
            if ("ROOM".equals(type)) {
                rooms.add(new RoomBounds(index, x, y, elementWidth, elementHeight, rotation));
            }
            if (!"WALL".equals(type) && isOutOfBounds(x, y, elementWidth, elementHeight, rotation, width, height)) {
                throw invalidElement(index, "超出平面图范围");
            }
        }
        if (requireOpeningsNearWall && !openings.isEmpty() && walls.isEmpty()) {
            throw invalidElement(openings.get(0).index(), "门窗必须贴近墙体，当前平面图没有墙体");
        }
        if (requireOpeningsNearWall && !walls.isEmpty()) {
            for (OpeningCenter opening : openings) {
                boolean nearWall = walls.stream().anyMatch(wall -> distanceToSegment(
                    opening.x(), opening.y(), wall.x1(), wall.y1(), wall.x2(), wall.y2()
                ) <= 42);
                if (!nearWall) {
                    throw invalidElement(opening.index(), "门窗必须贴近墙体");
                }
            }
        }
        if (requireOpeningsNearWall) {
            for (int firstIndex = 0; firstIndex < rooms.size(); firstIndex++) {
                RoomBounds first = rooms.get(firstIndex);
                    for (int secondIndex = firstIndex + 1; secondIndex < rooms.size(); secondIndex++) {
                        RoomBounds second = rooms.get(secondIndex);
                    if (rectanglesOverlap(first, second, 4)) {
                        throw invalidElement(first.index(), "房间与其他房间存在重叠");
                    }
                }
            }
        }
    }

    private static void validateSemanticMetadata(ObjectNode element, String type, int index) {
        String roomPurpose = text(element.get("roomPurpose")).toUpperCase(Locale.ROOT);
        if (!roomPurpose.isBlank() && !"ROOM".equals(type)) {
            throw invalidElement(index, "只有房间图元可以设置空间用途");
        }
        if (!roomPurpose.isBlank() && !ROOM_PURPOSES.contains(roomPurpose)) {
            throw invalidElement(index, "不支持的空间用途：" + roomPurpose);
        }
        String modelKey = text(element.get("modelKey"));
        if (!modelKey.isBlank() && (modelKey.length() > MAX_MODEL_KEY_LENGTH || !MODEL_KEY_PATTERN.matcher(modelKey).matches())) {
            throw invalidElement(index, "modelKey 格式不合法");
        }
        if (!modelKey.isBlank() && !REGISTERED_MODEL_KEYS.contains(modelKey)) {
            throw invalidElement(index, "modelKey 未注册，请选择已有模型或使用自动匹配");
        }
        if (!modelKey.isBlank() && !MODEL_KEYS_BY_ELEMENT_TYPE.getOrDefault(type, Set.of()).contains(modelKey)) {
            throw invalidElement(index, "modelKey 与图元语义不匹配，请选择当前类型的模型或使用自动匹配");
        }
        String connectorId = text(element.get("connectorId"));
        boolean verticalConnector = "STAIR".equals(type) || "ELEVATOR".equals(type);
        if (!connectorId.isBlank() && (!verticalConnector || connectorId.length() > MAX_CONNECTOR_ID_LENGTH)) {
            throw invalidElement(index, verticalConnector ? "连接编号长度不能超过" + MAX_CONNECTOR_ID_LENGTH + "个字符" : "只有楼梯和电梯可以设置连接编号");
        }
        String connectorType = text(element.get("connectorType")).toUpperCase(Locale.ROOT);
        if (!connectorType.isBlank() && (!"STAIR".equals(connectorType) && !"ELEVATOR".equals(connectorType))) {
            throw invalidElement(index, "不支持的连接类型：" + connectorType);
        }
        if (!connectorType.isBlank() && !connectorType.equals(type)) {
            throw invalidElement(index, "连接类型必须与图元类型一致");
        }
    }

    private static double distanceToSegment(double px, double py, double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        if (dx == 0 && dy == 0) {
            return Math.hypot(px - x1, py - y1);
        }
        double ratio = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy);
        ratio = Math.max(0, Math.min(1, ratio));
        return Math.hypot(px - (x1 + ratio * dx), py - (y1 + ratio * dy));
    }

    private static WallSegment rotatedWallSegment(double x, double y, double width, double height, double rotation) {
        Point midpoint = new Point(x + width / 2, y + height / 2);
        double radians = Math.toRadians(rotation);
        return new WallSegment(
            rotatePoint(new Point(x, y), midpoint, radians),
            rotatePoint(new Point(x + width, y + height), midpoint, radians)
        );
    }

    private static Point rotatePoint(Point point, Point center, double radians) {
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double x = point.x() - center.x();
        double y = point.y() - center.y();
        return new Point(center.x() + x * cos - y * sin, center.y() + x * sin + y * cos);
    }

    /** Oriented-rectangle overlap check; AABB overlap is too strict for rotated rooms. */
    private static boolean rectanglesOverlap(RoomBounds first, RoomBounds second, double minimumOverlap) {
        List<Point> firstCorners = rotatedCorners(first);
        List<Point> secondCorners = rotatedCorners(second);
        List<Axis> axes = new ArrayList<>(8);
        axes.addAll(edgeAxes(firstCorners));
        axes.addAll(edgeAxes(secondCorners));
        for (Axis axis : axes) {
            Projection firstProjection = project(firstCorners, axis);
            Projection secondProjection = project(secondCorners, axis);
            double overlap = Math.min(firstProjection.max(), secondProjection.max())
                - Math.max(firstProjection.min(), secondProjection.min());
            if (overlap <= minimumOverlap) {
                return false;
            }
        }
        return true;
    }

    private static List<Point> rotatedCorners(RoomBounds room) {
        double centerX = room.x() + room.width() / 2;
        double centerY = room.y() + room.height() / 2;
        double radians = Math.toRadians(room.rotation());
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double halfWidth = room.width() / 2;
        double halfHeight = room.height() / 2;
        List<Point> corners = List.of(
            new Point(-halfWidth, -halfHeight), new Point(halfWidth, -halfHeight),
            new Point(halfWidth, halfHeight), new Point(-halfWidth, halfHeight)
        );
        return corners.stream()
            .map(point -> new Point(
                centerX + point.x() * cos - point.y() * sin,
                centerY + point.x() * sin + point.y() * cos
            ))
            .toList();
    }

    private static List<Axis> edgeAxes(List<Point> corners) {
        List<Axis> axes = new ArrayList<>(corners.size());
        for (int index = 0; index < corners.size(); index++) {
            Point current = corners.get(index);
            Point next = corners.get((index + 1) % corners.size());
            double dx = next.x() - current.x();
            double dy = next.y() - current.y();
            double length = Math.hypot(dx, dy);
            axes.add(new Axis(-dy / length, dx / length));
        }
        return axes;
    }

    private static Projection project(List<Point> corners, Axis axis) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (Point corner : corners) {
            double value = corner.x() * axis.x() + corner.y() * axis.y();
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        return new Projection(min, max);
    }

    private record Point(double x, double y) {
    }

    private record WallSegment(Point start, Point end) {
        double x1() {
            return start.x();
        }

        double y1() {
            return start.y();
        }

        double x2() {
            return end.x();
        }

        double y2() {
            return end.y();
        }
    }

    private record Axis(double x, double y) {
    }

    private record Projection(double min, double max) {
    }

    private record OpeningCenter(int index, double x, double y) {
    }

    private record RoomBounds(int index, double x, double y, double width, double height, double rotation) {
    }

    private record Bounds(double minX, double minY, double maxX, double maxY) {
    }

    private static boolean isOutOfBounds(double x,
                                         double y,
                                         double elementWidth,
                                         double elementHeight,
                                         double rotation,
                                         double canvasWidth,
                                         double canvasHeight) {
        Bounds bounds = rotatedBounds(x, y, elementWidth, elementHeight, rotation);
        return bounds.minX() < 0 || bounds.maxX() > canvasWidth || bounds.minY() < 0 || bounds.maxY() > canvasHeight;
    }

    private static boolean isOutOfBounds(WallSegment wall, double canvasWidth, double canvasHeight) {
        return isOutOfBounds(wall.start(), canvasWidth, canvasHeight)
            || isOutOfBounds(wall.end(), canvasWidth, canvasHeight);
    }

    private static boolean isOutOfBounds(Point point, double canvasWidth, double canvasHeight) {
        return point.x() < 0 || point.x() > canvasWidth || point.y() < 0 || point.y() > canvasHeight;
    }

    private static Bounds rotatedBounds(double x, double y, double elementWidth, double elementHeight, double rotation) {
        double centerX = x + elementWidth / 2;
        double centerY = y + elementHeight / 2;
        double radians = Math.toRadians(rotation);
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);
        double halfWidth = Math.abs(elementWidth) / 2;
        double halfHeight = Math.abs(elementHeight) / 2;
        double[][] corners = {
            {-halfWidth, -halfHeight}, {halfWidth, -halfHeight},
            {halfWidth, halfHeight}, {-halfWidth, halfHeight}
        };
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (double[] corner : corners) {
            double rotatedX = centerX + corner[0] * cos - corner[1] * sin;
            double rotatedY = centerY + corner[0] * sin + corner[1] * cos;
            minX = Math.min(minX, rotatedX);
            minY = Math.min(minY, rotatedY);
            maxX = Math.max(maxX, rotatedX);
            maxY = Math.max(maxY, rotatedY);
        }
        return new Bounds(minX, minY, maxX, maxY);
    }

    private static double readCanvasSize(ObjectNode document, String field, double defaultValue) {
        JsonNode node = document.get(field);
        if (node == null || node.isNull()) {
            return defaultValue;
        }
        double value = readNumber(node, field);
        if (value <= 0 || value > MAX_CANVAS_SIZE) {
            throw new IllegalArgumentException("平面图" + field + "必须在 0 到 " + MAX_CANVAS_SIZE + "之间");
        }
        return value;
    }

    private static double readCoordinate(ObjectNode element, String field, int index) {
        JsonNode node = element.get(field);
        if (node == null || node.isNull()) {
            throw invalidElement(index, "缺少 " + field);
        }
        double value = readNumber(node, field);
        if (Math.abs(value) > MAX_COORDINATE) {
            throw invalidElement(index, field + "超出允许范围");
        }
        return value;
    }

    private static double readNumber(JsonNode node, String field) {
        if (!node.isNumber()) {
            throw new IllegalArgumentException(field + "必须是数字");
        }
        double value = node.asDouble();
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(field + "必须是有限数字");
        }
        return value;
    }

    private static double readOptionalNumber(JsonNode node, String field, int index, double defaultValue) {
        if (node == null || node.isNull()) {
            return defaultValue;
        }
        try {
            return readNumber(node, field);
        } catch (IllegalArgumentException exception) {
            throw invalidElement(index, exception.getMessage());
        }
    }

    private static String text(JsonNode node) {
        return node == null || node.isNull() ? "" : node.asText("").trim();
    }

    private static IllegalArgumentException invalidElement(int index, String message) {
        return new IllegalArgumentException("平面图第" + (index + 1) + "个元素" + message);
    }
}
