package org.dromara.department.service;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("dev")
class RoomFloorPlanMapDataBindingValidatorTest {

    @Test
    void collectsUniqueRoomBindings() {
        String mapData = "{\"elements\":["
            + "{\"id\":\"room-1\",\"type\":\"ROOM\",\"roomId\":101},"
            + "{\"id\":\"wall-1\",\"type\":\"WALL\",\"x\":0,\"y\":0,\"width\":100,\"height\":0}]}";

        assertEquals(Set.of("101"), RoomFloorPlanMapDataBindingValidator.collectPublishedRoomIds(mapData));
    }

    @Test
    void rejectsUnboundAndDuplicateRoomElements() {
        assertThrows(IllegalArgumentException.class, () -> RoomFloorPlanMapDataBindingValidator.collectPublishedRoomIds(
            "{\"elements\":[{\"id\":\"room-1\",\"type\":\"ROOM\"}]}"));
        assertThrows(IllegalArgumentException.class, () -> RoomFloorPlanMapDataBindingValidator.collectPublishedRoomIds(
            "{\"elements\":["
                + "{\"id\":\"room-1\",\"type\":\"ROOM\",\"roomId\":101},"
                + "{\"id\":\"room-2\",\"type\":\"ROOM\",\"roomId\":101}]}"));
    }

    @Test
    void rejectsMissingOrForeignRoomBindings() {
        String mapData = "{\"elements\":[{\"id\":\"room-1\",\"type\":\"ROOM\",\"roomId\":101}]}";

        assertDoesNotThrow(() -> RoomFloorPlanMapDataBindingValidator.validatePublishedBindings(mapData, Set.of("101")));
        assertThrows(IllegalArgumentException.class, () -> RoomFloorPlanMapDataBindingValidator.validatePublishedBindings(mapData, Set.of("101", "102")));
        assertThrows(IllegalArgumentException.class, () -> RoomFloorPlanMapDataBindingValidator.validatePublishedBindings(mapData, Set.of("102")));
    }
}
