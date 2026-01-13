package com.scheduleengine.field.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class FieldUsageBlockTest {

    private FieldUsageBlock usageBlock;
    private Field field;

    @BeforeEach
    void setUp() {
        field = new Field("Test Field");
        usageBlock = new FieldUsageBlock(
            field,
            DayOfWeek.MONDAY,
            FieldUsageBlock.UsageType.LEAGUE,
            LocalTime.of(18, 0),
            LocalTime.of(20, 0),
            "Evening league play"
        );
    }

    @Test
    void shouldCreateFieldUsageBlockWithAllFields() {
        assertEquals(field, usageBlock.getField());
        assertEquals(DayOfWeek.MONDAY, usageBlock.getDayOfWeek());
        assertEquals(FieldUsageBlock.UsageType.LEAGUE, usageBlock.getUsageType());
        assertEquals(LocalTime.of(18, 0), usageBlock.getStartTime());
        assertEquals(LocalTime.of(20, 0), usageBlock.getEndTime());
        assertEquals("Evening league play", usageBlock.getNotes());
    }

    @Test
    void shouldSetAndGetId() {
        usageBlock.setId(1L);
        assertEquals(1L, usageBlock.getId());
    }

    @Test
    void shouldSetAndGetField() {
        Field newField = new Field("New Field");
        usageBlock.setField(newField);
        assertEquals(newField, usageBlock.getField());
    }

    @Test
    void shouldSetAndGetDayOfWeek() {
        usageBlock.setDayOfWeek(DayOfWeek.FRIDAY);
        assertEquals(DayOfWeek.FRIDAY, usageBlock.getDayOfWeek());
    }

    @Test
    void shouldSetAndGetUsageType() {
        usageBlock.setUsageType(FieldUsageBlock.UsageType.TOURNAMENT);
        assertEquals(FieldUsageBlock.UsageType.TOURNAMENT, usageBlock.getUsageType());
    }

    @Test
    void shouldSetAndGetStartTime() {
        LocalTime newTime = LocalTime.of(19, 0);
        usageBlock.setStartTime(newTime);
        assertEquals(newTime, usageBlock.getStartTime());
    }

    @Test
    void shouldSetAndGetEndTime() {
        LocalTime newTime = LocalTime.of(21, 0);
        usageBlock.setEndTime(newTime);
        assertEquals(newTime, usageBlock.getEndTime());
    }

    @Test
    void shouldSetAndGetNotes() {
        String notes = "Updated notes";
        usageBlock.setNotes(notes);
        assertEquals(notes, usageBlock.getNotes());
    }

    @Test
    void shouldCreateWithDefaultConstructor() {
        FieldUsageBlock block = new FieldUsageBlock();
        assertNull(block.getField());
        assertNull(block.getDayOfWeek());
        assertNull(block.getUsageType());
        assertNull(block.getStartTime());
        assertNull(block.getEndTime());
        assertNull(block.getNotes());
    }

    @Test
    void shouldHandleAllUsageTypes() {
        for (FieldUsageBlock.UsageType type : FieldUsageBlock.UsageType.values()) {
            usageBlock.setUsageType(type);
            assertEquals(type, usageBlock.getUsageType());
        }
    }

    @Test
    void shouldHandlePracticeBlock() {
        usageBlock.setUsageType(FieldUsageBlock.UsageType.PRACTICE);
        usageBlock.setStartTime(LocalTime.of(17, 0));
        usageBlock.setEndTime(LocalTime.of(18, 0));
        usageBlock.setNotes("Team practice");

        assertEquals(FieldUsageBlock.UsageType.PRACTICE, usageBlock.getUsageType());
        assertEquals("Team practice", usageBlock.getNotes());
    }

    @Test
    void shouldHandleTournamentBlock() {
        usageBlock.setUsageType(FieldUsageBlock.UsageType.TOURNAMENT);
        usageBlock.setDayOfWeek(DayOfWeek.SATURDAY);
        usageBlock.setStartTime(LocalTime.of(9, 0));
        usageBlock.setEndTime(LocalTime.of(18, 0));

        assertEquals(FieldUsageBlock.UsageType.TOURNAMENT, usageBlock.getUsageType());
        assertEquals(DayOfWeek.SATURDAY, usageBlock.getDayOfWeek());
    }

    @Test
    void shouldHandleClosedBlock() {
        usageBlock.setUsageType(FieldUsageBlock.UsageType.CLOSED);
        usageBlock.setNotes("Maintenance scheduled");

        assertEquals(FieldUsageBlock.UsageType.CLOSED, usageBlock.getUsageType());
        assertEquals("Maintenance scheduled", usageBlock.getNotes());
    }

    @Test
    void shouldHandleEmptyNotes() {
        usageBlock.setNotes("");
        assertEquals("", usageBlock.getNotes());
    }

}

