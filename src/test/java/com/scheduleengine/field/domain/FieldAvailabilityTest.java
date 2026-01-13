package com.scheduleengine.field.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class FieldAvailabilityTest {

    private FieldAvailability availability;
    private Field field;

    @BeforeEach
    void setUp() {
        field = new Field("Test Field");
        availability = new FieldAvailability(field, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
    }

    @Test
    void shouldCreateFieldAvailabilityWithAllFields() {
        assertEquals(field, availability.getField());
        assertEquals(DayOfWeek.MONDAY, availability.getDayOfWeek());
        assertEquals(LocalTime.of(9, 0), availability.getOpenTime());
        assertEquals(LocalTime.of(17, 0), availability.getCloseTime());
    }

    @Test
    void shouldSetAndGetId() {
        availability.setId(1L);
        assertEquals(1L, availability.getId());
    }

    @Test
    void shouldSetAndGetField() {
        Field newField = new Field("New Field");
        availability.setField(newField);
        assertEquals(newField, availability.getField());
    }

    @Test
    void shouldSetAndGetDayOfWeek() {
        availability.setDayOfWeek(DayOfWeek.FRIDAY);
        assertEquals(DayOfWeek.FRIDAY, availability.getDayOfWeek());
    }

    @Test
    void shouldSetAndGetOpenTime() {
        LocalTime newTime = LocalTime.of(10, 30);
        availability.setOpenTime(newTime);
        assertEquals(newTime, availability.getOpenTime());
    }

    @Test
    void shouldSetAndGetCloseTime() {
        LocalTime newTime = LocalTime.of(18, 30);
        availability.setCloseTime(newTime);
        assertEquals(newTime, availability.getCloseTime());
    }

    @Test
    void shouldCreateWithDefaultConstructor() {
        FieldAvailability fa = new FieldAvailability();
        assertNull(fa.getField());
        assertNull(fa.getDayOfWeek());
        assertNull(fa.getOpenTime());
        assertNull(fa.getCloseTime());
    }

    @Test
    void shouldHandleMultipleDaysOfWeek() {
        for (DayOfWeek day : DayOfWeek.values()) {
            availability.setDayOfWeek(day);
            assertEquals(day, availability.getDayOfWeek());
        }
    }

    @Test
    void shouldHandleEarlyMorningHours() {
        availability.setOpenTime(LocalTime.of(6, 0));
        availability.setCloseTime(LocalTime.of(8, 0));
        assertEquals(LocalTime.of(6, 0), availability.getOpenTime());
        assertEquals(LocalTime.of(8, 0), availability.getCloseTime());
    }

    @Test
    void shouldHandleLateNightHours() {
        availability.setOpenTime(LocalTime.of(22, 0));
        availability.setCloseTime(LocalTime.of(23, 59));
        assertEquals(LocalTime.of(22, 0), availability.getOpenTime());
        assertEquals(LocalTime.of(23, 59), availability.getCloseTime());
    }

}

