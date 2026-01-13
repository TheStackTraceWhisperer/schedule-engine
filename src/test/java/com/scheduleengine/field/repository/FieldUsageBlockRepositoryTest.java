package com.scheduleengine.field.repository;

import com.scheduleengine.field.domain.Field;
import com.scheduleengine.field.domain.FieldUsageBlock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class FieldUsageBlockRepositoryTest {

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private FieldUsageBlockRepository blockRepository;

    private Field testField;

    @BeforeEach
    void setUp() {
        testField = new Field("Test Field");
        testField = fieldRepository.save(testField);
    }

    @Test
    void shouldSaveAndRetrieveFieldUsageBlock() {
        FieldUsageBlock block = new FieldUsageBlock(
            testField,
            DayOfWeek.MONDAY,
            FieldUsageBlock.UsageType.LEAGUE,
            LocalTime.of(18, 0),
            LocalTime.of(20, 0),
            "Evening league"
        );

        FieldUsageBlock saved = blockRepository.save(block);

        assertNotNull(saved.getId());
        assertEquals(FieldUsageBlock.UsageType.LEAGUE, saved.getUsageType());
    }

    @Test
    void shouldFindByField() {
        FieldUsageBlock league = new FieldUsageBlock(testField, DayOfWeek.MONDAY, FieldUsageBlock.UsageType.LEAGUE, LocalTime.of(18, 0), LocalTime.of(20, 0), "");
        FieldUsageBlock practice = new FieldUsageBlock(testField, DayOfWeek.TUESDAY, FieldUsageBlock.UsageType.PRACTICE, LocalTime.of(17, 0), LocalTime.of(18, 0), "");

        blockRepository.save(league);
        blockRepository.save(practice);

        List<FieldUsageBlock> result = blockRepository.findByField(testField);

        assertEquals(2, result.size());
    }

    @Test
    void shouldFindByFieldAndDayOfWeek() {
        FieldUsageBlock monday = new FieldUsageBlock(testField, DayOfWeek.MONDAY, FieldUsageBlock.UsageType.LEAGUE, LocalTime.of(18, 0), LocalTime.of(20, 0), "");
        blockRepository.save(monday);

        List<FieldUsageBlock> result = blockRepository.findByFieldAndDayOfWeek(testField, DayOfWeek.MONDAY);

        assertEquals(1, result.size());
        assertEquals(DayOfWeek.MONDAY, result.get(0).getDayOfWeek());
    }

    @Test
    void shouldReturnEmptyListWhenNoBlocksForDay() {
        List<FieldUsageBlock> result = blockRepository.findByFieldAndDayOfWeek(testField, DayOfWeek.SUNDAY);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldHandleMultipleBlocksForSameDay() {
        FieldUsageBlock morning = new FieldUsageBlock(testField, DayOfWeek.MONDAY, FieldUsageBlock.UsageType.PRACTICE, LocalTime.of(6, 0), LocalTime.of(8, 0), "");
        FieldUsageBlock afternoon = new FieldUsageBlock(testField, DayOfWeek.MONDAY, FieldUsageBlock.UsageType.LEAGUE, LocalTime.of(14, 0), LocalTime.of(17, 0), "");
        FieldUsageBlock evening = new FieldUsageBlock(testField, DayOfWeek.MONDAY, FieldUsageBlock.UsageType.LEAGUE, LocalTime.of(18, 0), LocalTime.of(20, 0), "");

        blockRepository.save(morning);
        blockRepository.save(afternoon);
        blockRepository.save(evening);

        List<FieldUsageBlock> result = blockRepository.findByFieldAndDayOfWeek(testField, DayOfWeek.MONDAY);

        assertEquals(3, result.size());
    }

    @Test
    void shouldPersistAllUsageTypes() {
        FieldUsageBlock league = new FieldUsageBlock(testField, DayOfWeek.MONDAY, FieldUsageBlock.UsageType.LEAGUE, LocalTime.of(18, 0), LocalTime.of(20, 0), "");
        FieldUsageBlock tournament = new FieldUsageBlock(testField, DayOfWeek.SATURDAY, FieldUsageBlock.UsageType.TOURNAMENT, LocalTime.of(9, 0), LocalTime.of(17, 0), "");
        FieldUsageBlock practice = new FieldUsageBlock(testField, DayOfWeek.WEDNESDAY, FieldUsageBlock.UsageType.PRACTICE, LocalTime.of(17, 0), LocalTime.of(18, 0), "");
        FieldUsageBlock closed = new FieldUsageBlock(testField, DayOfWeek.SUNDAY, FieldUsageBlock.UsageType.CLOSED, LocalTime.of(0, 0), LocalTime.of(23, 59), "Closed");

        blockRepository.save(league);
        blockRepository.save(tournament);
        blockRepository.save(practice);
        blockRepository.save(closed);

        List<FieldUsageBlock> result = blockRepository.findByField(testField);

        assertEquals(4, result.size());
        assertTrue(result.stream().anyMatch(b -> b.getUsageType() == FieldUsageBlock.UsageType.LEAGUE));
        assertTrue(result.stream().anyMatch(b -> b.getUsageType() == FieldUsageBlock.UsageType.TOURNAMENT));
        assertTrue(result.stream().anyMatch(b -> b.getUsageType() == FieldUsageBlock.UsageType.PRACTICE));
        assertTrue(result.stream().anyMatch(b -> b.getUsageType() == FieldUsageBlock.UsageType.CLOSED));
    }

    @Test
    void shouldDeleteBlock() {
        FieldUsageBlock block = new FieldUsageBlock(testField, DayOfWeek.MONDAY, FieldUsageBlock.UsageType.LEAGUE, LocalTime.of(18, 0), LocalTime.of(20, 0), "");
        FieldUsageBlock saved = blockRepository.save(block);

        blockRepository.deleteById(saved.getId());

        List<FieldUsageBlock> result = blockRepository.findByField(testField);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldHandleMultipleFieldsWithBlocks() {
        Field field2 = new Field("Field 2");
        field2 = fieldRepository.save(field2);

        FieldUsageBlock block1 = new FieldUsageBlock(testField, DayOfWeek.MONDAY, FieldUsageBlock.UsageType.LEAGUE, LocalTime.of(18, 0), LocalTime.of(20, 0), "");
        FieldUsageBlock block2 = new FieldUsageBlock(field2, DayOfWeek.MONDAY, FieldUsageBlock.UsageType.TOURNAMENT, LocalTime.of(9, 0), LocalTime.of(17, 0), "");

        blockRepository.save(block1);
        blockRepository.save(block2);

        List<FieldUsageBlock> field1Blocks = blockRepository.findByField(testField);
        List<FieldUsageBlock> field2Blocks = blockRepository.findByField(field2);

        assertEquals(1, field1Blocks.size());
        assertEquals(1, field2Blocks.size());
        assertEquals(FieldUsageBlock.UsageType.LEAGUE, field1Blocks.get(0).getUsageType());
        assertEquals(FieldUsageBlock.UsageType.TOURNAMENT, field2Blocks.get(0).getUsageType());
    }

}

