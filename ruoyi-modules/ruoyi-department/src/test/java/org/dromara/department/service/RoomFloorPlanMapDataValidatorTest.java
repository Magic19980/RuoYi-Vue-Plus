package org.dromara.department.service;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("dev")
class RoomFloorPlanMapDataValidatorTest {

    @Test
    void acceptsSemanticFloorPlanElements() {
        String mapData = "{\"version\":3,\"width\":1200,\"height\":680,\"elements\":["
            + "{\"id\":\"room-1\",\"type\":\"ROOM\",\"x\":80,\"y\":60,\"width\":240,\"height\":160},"
            + "{\"id\":\"wall-1\",\"type\":\"WALL\",\"x\":0,\"y\":0,\"width\":1200,\"height\":0}]}";

        assertDoesNotThrow(() -> RoomFloorPlanMapDataValidator.validate(mapData));
    }

    @Test
    void rejectsUnknownTypesDuplicateIdsAndOutOfBoundsElements() {
        assertThrows(IllegalArgumentException.class, () -> RoomFloorPlanMapDataValidator.validate(
            "{\"elements\":[{\"id\":\"one\",\"type\":\"UNKNOWN\",\"x\":0,\"y\":0,\"width\":10,\"height\":10}]}"));
        assertThrows(IllegalArgumentException.class, () -> RoomFloorPlanMapDataValidator.validate(
            "{\"elements\":[{\"id\":\"one\",\"type\":\"ROOM\",\"x\":0,\"y\":0,\"width\":10,\"height\":10},{\"id\":\"one\",\"type\":\"POI\",\"x\":20,\"y\":20,\"width\":10,\"height\":10}]}"));
        assertThrows(IllegalArgumentException.class, () -> RoomFloorPlanMapDataValidator.validate(
            "{\"width\":100,\"height\":100,\"elements\":[{\"id\":\"room-1\",\"type\":\"ROOM\",\"x\":95,\"y\":0,\"width\":10,\"height\":10}]}"));
    }

    @Test
    void checksRotatedElementBounds() {
        assertDoesNotThrow(() -> RoomFloorPlanMapDataValidator.validate(
            "{\"width\":200,\"height\":200,\"elements\":[{\"id\":\"room-1\",\"type\":\"ROOM\",\"x\":70,\"y\":70,\"width\":60,\"height\":60,\"rotation\":45}]}"));
        assertThrows(IllegalArgumentException.class, () -> RoomFloorPlanMapDataValidator.validate(
            "{\"width\":200,\"height\":200,\"elements\":[{\"id\":\"room-1\",\"type\":\"ROOM\",\"x\":155,\"y\":70,\"width\":40,\"height\":40,\"rotation\":45}]}"));
        assertThrows(IllegalArgumentException.class, () -> RoomFloorPlanMapDataValidator.validate(
            "{\"elements\":[{\"id\":\"room-1\",\"type\":\"ROOM\",\"x\":10,\"y\":10,\"width\":40,\"height\":40,\"rotation\":\"quarter-turn\"}]}"));
    }

    @Test
    void rejectsInvalidRectangleDimensionsButAllowsDirectedWalls() {
        assertThrows(IllegalArgumentException.class, () -> RoomFloorPlanMapDataValidator.validate(
            "{\"elements\":[{\"id\":\"room-1\",\"type\":\"ROOM\",\"x\":10,\"y\":10,\"width\":-40,\"height\":40}]}"));
        assertThrows(IllegalArgumentException.class, () -> RoomFloorPlanMapDataValidator.validate(
            "{\"elements\":[{\"id\":\"wall-1\",\"type\":\"WALL\",\"x\":10,\"y\":10,\"width\":0,\"height\":0}]}"));
        assertDoesNotThrow(() -> RoomFloorPlanMapDataValidator.validate(
            "{\"elements\":[{\"id\":\"wall-1\",\"type\":\"WALL\",\"x\":100,\"y\":100,\"width\":-60,\"height\":20}]}"));
    }

    @Test
    void rejectsWallsOutsideCanvasAfterRotation() {
        assertThrows(IllegalArgumentException.class, () -> RoomFloorPlanMapDataValidator.validate(
            "{\"width\":200,\"height\":200,\"elements\":[{\"id\":\"wall-1\",\"type\":\"WALL\",\"x\":190,\"y\":100,\"width\":20,\"height\":0}]}"));
        assertThrows(IllegalArgumentException.class, () -> RoomFloorPlanMapDataValidator.validate(
            "{\"width\":200,\"height\":200,\"elements\":[{\"id\":\"wall-1\",\"type\":\"WALL\",\"x\":190,\"y\":100,\"width\":60,\"height\":0,\"rotation\":90}]}"));
    }

    @Test
    void allowsIncompleteDraftOpeningsButRejectsDetachedOpeningsOnPublish() {
        String detachedOpening = "{\"width\":200,\"height\":200,\"elements\":["
            + "{\"id\":\"wall-1\",\"type\":\"WALL\",\"x\":0,\"y\":100,\"width\":200,\"height\":0},"
            + "{\"id\":\"door-1\",\"type\":\"DOOR\",\"x\":80,\"y\":10,\"width\":40,\"height\":20}]}";

        assertDoesNotThrow(() -> RoomFloorPlanMapDataValidator.validate(detachedOpening));
        assertThrows(IllegalArgumentException.class, () -> RoomFloorPlanMapDataValidator.validateForPublish(detachedOpening));
        assertDoesNotThrow(() -> RoomFloorPlanMapDataValidator.validateForPublish(
            "{\"width\":200,\"height\":200,\"elements\":["
                + "{\"id\":\"wall-1\",\"type\":\"WALL\",\"x\":0,\"y\":100,\"width\":200,\"height\":0},"
                + "{\"id\":\"door-1\",\"type\":\"DOOR\",\"x\":80,\"y\":85,\"width\":40,\"height\":20}]}"
        ));
    }

    @Test
    void rejectsOpeningsWhenPublishLayoutHasNoWalls() {
        String openingWithoutWall = "{\"width\":200,\"height\":200,\"elements\":["
            + "{\"id\":\"door-1\",\"type\":\"DOOR\",\"x\":80,\"y\":85,\"width\":40,\"height\":20}]}";

        assertDoesNotThrow(() -> RoomFloorPlanMapDataValidator.validate(openingWithoutWall));
        assertThrows(IllegalArgumentException.class, () -> RoomFloorPlanMapDataValidator.validateForPublish(openingWithoutWall));
    }

    @Test
    void allowsOverlappingDraftRoomsButRejectsThemOnPublish() {
        String overlappingRooms = "{\"width\":300,\"height\":200,\"elements\":["
            + "{\"id\":\"room-1\",\"type\":\"ROOM\",\"x\":20,\"y\":20,\"width\":120,\"height\":80},"
            + "{\"id\":\"room-2\",\"type\":\"ROOM\",\"x\":100,\"y\":50,\"width\":120,\"height\":80}]}";

        assertDoesNotThrow(() -> RoomFloorPlanMapDataValidator.validate(overlappingRooms));
        assertThrows(IllegalArgumentException.class, () -> RoomFloorPlanMapDataValidator.validateForPublish(overlappingRooms));
    }

    @Test
    void usesOrientedRoomGeometryAndRotatedWallsOnPublish() {
        String diagonalRooms = "{\"width\":400,\"height\":300,\"elements\":["
            + "{\"id\":\"room-1\",\"type\":\"ROOM\",\"x\":90,\"y\":144,\"width\":120,\"height\":12,\"rotation\":45},"
            + "{\"id\":\"room-2\",\"type\":\"ROOM\",\"x\":180,\"y\":234,\"width\":120,\"height\":12,\"rotation\":45}]}";
        assertDoesNotThrow(() -> RoomFloorPlanMapDataValidator.validateForPublish(diagonalRooms));

        String rotatedWall = "{\"width\":300,\"height\":300,\"elements\":["
            + "{\"id\":\"wall-1\",\"type\":\"WALL\",\"x\":100,\"y\":140,\"width\":120,\"height\":0,\"rotation\":90},"
            + "{\"id\":\"door-1\",\"type\":\"DOOR\",\"x\":140,\"y\":180,\"width\":40,\"height\":20}]}";
        assertDoesNotThrow(() -> RoomFloorPlanMapDataValidator.validateForPublish(rotatedWall));
    }

    @Test
    void validatesModelKeysAndVerticalConnectorMetadata() {
        assertDoesNotThrow(() -> RoomFloorPlanMapDataValidator.validate(
            "{\"elements\":[{\"id\":\"stair-1\",\"type\":\"STAIR\",\"x\":0,\"y\":0,\"width\":60,\"height\":60,"
                + "\"modelKey\":\"office.structure.stair\",\"connectorId\":\"core-01\",\"connectorType\":\"STAIR\"}]}"));
        assertThrows(IllegalArgumentException.class, () -> RoomFloorPlanMapDataValidator.validate(
            "{\"elements\":[{\"id\":\"poi-1\",\"type\":\"POI\",\"x\":0,\"y\":0,\"width\":20,\"height\":20,\"connectorId\":\"core-01\"}]}"));
        assertThrows(IllegalArgumentException.class, () -> RoomFloorPlanMapDataValidator.validate(
            "{\"elements\":[{\"id\":\"stair-1\",\"type\":\"STAIR\",\"x\":0,\"y\":0,\"width\":60,\"height\":60,\"connectorType\":\"ELEVATOR\"}]}"));
        assertThrows(IllegalArgumentException.class, () -> RoomFloorPlanMapDataValidator.validate(
            "{\"elements\":[{\"id\":\"poi-1\",\"type\":\"POI\",\"x\":0,\"y\":0,\"width\":20,\"height\":20,\"modelKey\":\"https://example.com/model.glb\"}]}"));
        assertThrows(IllegalArgumentException.class, () -> RoomFloorPlanMapDataValidator.validate(
            "{\"elements\":[{\"id\":\"poi-1\",\"type\":\"POI\",\"x\":0,\"y\":0,\"width\":20,\"height\":20,\"modelKey\":\"office.custom.lamp\"}]}"));
        assertDoesNotThrow(() -> RoomFloorPlanMapDataValidator.validate(
            "{\"elements\":[{\"id\":\"garden-1\",\"type\":\"LANDSCAPE\",\"x\":0,\"y\":0,\"width\":120,\"height\":80,\"modelKey\":\"animal.prop.tree\"}]}"));
    }

    @Test
    void validatesExplicitRoomPurpose() {
        assertDoesNotThrow(() -> RoomFloorPlanMapDataValidator.validate(
            "{\"elements\":[{\"id\":\"room-1\",\"type\":\"ROOM\",\"x\":0,\"y\":0,\"width\":80,\"height\":60,\"roomPurpose\":\"TRAINING\"}]}"));
        assertThrows(IllegalArgumentException.class, () -> RoomFloorPlanMapDataValidator.validate(
            "{\"elements\":[{\"id\":\"room-1\",\"type\":\"ROOM\",\"x\":0,\"y\":0,\"width\":80,\"height\":60,\"roomPurpose\":\"GYM\"}]}"));
        assertThrows(IllegalArgumentException.class, () -> RoomFloorPlanMapDataValidator.validate(
            "{\"elements\":[{\"id\":\"poi-1\",\"type\":\"POI\",\"x\":0,\"y\":0,\"width\":20,\"height\":20,\"roomPurpose\":\"LOUNGE\"}]}"));
    }

    @Test
    void rejectsRegisteredModelsWithAnIncompatibleSemanticType() {
        assertThrows(IllegalArgumentException.class, () -> RoomFloorPlanMapDataValidator.validate(
            "{\"elements\":[{\"id\":\"room-1\",\"type\":\"ROOM\",\"x\":0,\"y\":0,\"width\":80,\"height\":60,\"modelKey\":\"office.furniture.desk\"}]}"));
        assertThrows(IllegalArgumentException.class, () -> RoomFloorPlanMapDataValidator.validate(
            "{\"elements\":[{\"id\":\"stair-1\",\"type\":\"STAIR\",\"x\":0,\"y\":0,\"width\":60,\"height\":60,\"modelKey\":\"office.structure.elevator\"}]}"));
        assertThrows(IllegalArgumentException.class, () -> RoomFloorPlanMapDataValidator.validate(
            "{\"elements\":[{\"id\":\"door-1\",\"type\":\"DOOR\",\"x\":0,\"y\":0,\"width\":40,\"height\":20,\"modelKey\":\"office.prop.plant\"}]}"));
        assertDoesNotThrow(() -> RoomFloorPlanMapDataValidator.validate(
            "{\"elements\":[{\"id\":\"poi-1\",\"type\":\"POI\",\"x\":0,\"y\":0,\"width\":20,\"height\":20,\"modelKey\":\"animal.prop.lamp\"}]}"));
    }
}
