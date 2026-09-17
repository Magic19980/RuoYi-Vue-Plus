package org.dromara.department.service;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("dev")
class RoomFloorPlanMapDataSanitizerTest {

    @Test
    void removesRoomsOutsideViewScopeAndKeepsStructuralElements() {
        String mapData = "{\"version\":3,\"elements\":["
            + "{\"id\":\"room-visible\",\"type\":\"ROOM\",\"roomId\":101},"
            + "{\"id\":\"room-hidden\",\"type\":\"ROOM\",\"roomId\":202},"
            + "{\"id\":\"wall-1\",\"type\":\"WALL\"}]}";

        String sanitized = RoomFloorPlanMapDataSanitizer.sanitize(mapData, Set.of("101"));

        assertTrue(sanitized.contains("room-visible"));
        assertTrue(sanitized.contains("wall-1"));
        assertFalse(sanitized.contains("room-hidden"));
    }

    @Test
    void invalidMapDataDoesNotLeakOriginalContent() {
        String sanitized = RoomFloorPlanMapDataSanitizer.sanitize("not-json", Set.of());

        assertTrue(sanitized.contains("\"elements\":[]"));
        assertFalse(sanitized.contains("not-json"));
    }

    @Test
    void detectsWhenBackgroundImageCouldExposeAnInaccessibleRoom() {
        String mapData = "{\"elements\":["
            + "{\"id\":\"room-visible\",\"type\":\"ROOM\",\"roomId\":101},"
            + "{\"id\":\"room-hidden\",\"type\":\"ROOM\",\"roomId\":202}]}";

        assertTrue(RoomFloorPlanMapDataSanitizer.hasInaccessibleRooms(mapData, Set.of("101")));
        assertFalse(RoomFloorPlanMapDataSanitizer.hasInaccessibleRooms(mapData, Set.of("101", "202")));
        assertTrue(RoomFloorPlanMapDataSanitizer.hasInaccessibleRooms("not-json", Set.of("101")));
    }
}
