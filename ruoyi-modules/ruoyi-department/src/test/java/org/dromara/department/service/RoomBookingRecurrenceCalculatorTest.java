package org.dromara.department.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("dev")
class RoomBookingRecurrenceCalculatorTest {

    @Test
    void weeklySeriesKeepsDurationAndSequence() {
        List<RoomBookingRecurrenceCalculator.Slot> slots = RoomBookingRecurrenceCalculator.calculate(
            "WEEKLY", LocalDateTime.of(2026, 9, 1, 9, 0), LocalDateTime.of(2026, 9, 1, 10, 30),
            2, 3, null, 100
        );
        assertEquals(3, slots.size());
        assertEquals(LocalDateTime.of(2026, 9, 15, 9, 0), slots.get(1).startAt());
        assertEquals(LocalDateTime.of(2026, 9, 15, 10, 30), slots.get(1).endAt());
    }

    @Test
    void untilDateLimitsSeries() {
        List<RoomBookingRecurrenceCalculator.Slot> slots = RoomBookingRecurrenceCalculator.calculate(
            "DAILY", LocalDateTime.of(2026, 9, 1, 9, 0), LocalDateTime.of(2026, 9, 1, 10, 0),
            1, null, LocalDate.of(2026, 9, 3), 100
        );
        assertEquals(3, slots.size());
    }

    @Test
    void crossDayDurationIsPreservedForMonthlySeries() {
        List<RoomBookingRecurrenceCalculator.Slot> slots = RoomBookingRecurrenceCalculator.calculate(
            "MONTHLY", LocalDateTime.of(2026, 1, 31, 23, 30), LocalDateTime.of(2026, 2, 1, 1, 0),
            1, 2, null, 100
        );
        assertEquals(LocalDateTime.of(2026, 3, 1, 1, 0), slots.get(1).endAt());
        assertEquals(90, java.time.Duration.between(slots.get(1).startAt(), slots.get(1).endAt()).toMinutes());
    }

    @Test
    void intervalMustStayWithinSupportedRange() {
        assertThrows(RuntimeException.class, () -> RoomBookingRecurrenceCalculator.calculate(
            "DAILY", LocalDateTime.of(2026, 9, 1, 9, 0), LocalDateTime.of(2026, 9, 1, 10, 0),
            31, 2, null, 100
        ));
    }

    @Test
    void recurringSeriesRequiresBound() {
        assertThrows(RuntimeException.class, () -> RoomBookingRecurrenceCalculator.calculate(
            "MONTHLY", LocalDateTime.of(2026, 9, 1, 9, 0), LocalDateTime.of(2026, 9, 1, 10, 0),
            1, null, null, 100
        ));
    }
}
