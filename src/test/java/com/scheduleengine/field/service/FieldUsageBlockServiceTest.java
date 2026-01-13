package com.scheduleengine.field.service;

import com.scheduleengine.field.domain.Field;
import com.scheduleengine.field.domain.FieldUsageBlock;
import com.scheduleengine.field.repository.FieldUsageBlockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FieldUsageBlockServiceTest {

    @Mock
    private FieldUsageBlockRepository repository;

    private FieldUsageBlockService service;
    private Field testField;
    private FieldUsageBlock testBlock;

    @BeforeEach
    void setUp() {
        service = new FieldUsageBlockService(repository);
        testField = new Field("Test Field");
        testField.setId(1L);
        testBlock = new FieldUsageBlock(
            testField,
            DayOfWeek.MONDAY,
            FieldUsageBlock.UsageType.LEAGUE,
            LocalTime.of(18, 0),
            LocalTime.of(20, 0),
            "Evening league"
        );
        testBlock.setId(1L);
    }

    @Test
    void shouldFindByField() {
        when(repository.findByField(testField)).thenReturn(Arrays.asList(testBlock));

        List<FieldUsageBlock> result = service.findByField(testField);

        assertEquals(1, result.size());
        assertEquals(FieldUsageBlock.UsageType.LEAGUE, result.get(0).getUsageType());
        verify(repository, times(1)).findByField(testField);
    }

    @Test
    void shouldFindByFieldAndDayOfWeek() {
        when(repository.findByFieldAndDayOfWeek(testField, DayOfWeek.MONDAY))
            .thenReturn(Arrays.asList(testBlock));

        List<FieldUsageBlock> result = service.findByFieldAndDayOfWeek(testField, DayOfWeek.MONDAY);

        assertEquals(1, result.size());
        assertEquals(LocalTime.of(18, 0), result.get(0).getStartTime());
        verify(repository, times(1)).findByFieldAndDayOfWeek(testField, DayOfWeek.MONDAY);
    }

    @Test
    void shouldReturnEmptyListWhenNoBlocksFound() {
        when(repository.findByFieldAndDayOfWeek(testField, DayOfWeek.SUNDAY))
            .thenReturn(Arrays.asList());

        List<FieldUsageBlock> result = service.findByFieldAndDayOfWeek(testField, DayOfWeek.SUNDAY);

        assertTrue(result.isEmpty());
        verify(repository, times(1)).findByFieldAndDayOfWeek(testField, DayOfWeek.SUNDAY);
    }

    @Test
    void shouldSaveBlock() {
        when(repository.save(any(FieldUsageBlock.class))).thenReturn(testBlock);

        FieldUsageBlock result = service.save(testBlock);

        assertNotNull(result);
        assertEquals(FieldUsageBlock.UsageType.LEAGUE, result.getUsageType());
        verify(repository, times(1)).save(testBlock);
    }

    @Test
    void shouldDeleteBlock() {
        service.delete(1L);

        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void shouldHandleMultipleBlocksForSameDay() {
        FieldUsageBlock morning = new FieldUsageBlock(
            testField,
            DayOfWeek.MONDAY,
            FieldUsageBlock.UsageType.PRACTICE,
            LocalTime.of(6, 0),
            LocalTime.of(8, 0),
            "Morning practice"
        );

        FieldUsageBlock afternoon = new FieldUsageBlock(
            testField,
            DayOfWeek.MONDAY,
            FieldUsageBlock.UsageType.LEAGUE,
            LocalTime.of(14, 0),
            LocalTime.of(17, 0),
            "Afternoon league"
        );

        when(repository.findByFieldAndDayOfWeek(testField, DayOfWeek.MONDAY))
            .thenReturn(Arrays.asList(morning, afternoon, testBlock));

        List<FieldUsageBlock> result = service.findByFieldAndDayOfWeek(testField, DayOfWeek.MONDAY);

        assertEquals(3, result.size());
        assertEquals(FieldUsageBlock.UsageType.PRACTICE, result.get(0).getUsageType());
        assertEquals(FieldUsageBlock.UsageType.LEAGUE, result.get(1).getUsageType());
        assertEquals(FieldUsageBlock.UsageType.LEAGUE, result.get(2).getUsageType());
    }

    @Test
    void shouldHandleAllUsageTypes() {
        FieldUsageBlock league = new FieldUsageBlock(testField, DayOfWeek.MONDAY, FieldUsageBlock.UsageType.LEAGUE, LocalTime.of(18, 0), LocalTime.of(20, 0), "");
        FieldUsageBlock tournament = new FieldUsageBlock(testField, DayOfWeek.SATURDAY, FieldUsageBlock.UsageType.TOURNAMENT, LocalTime.of(9, 0), LocalTime.of(17, 0), "");
        FieldUsageBlock practice = new FieldUsageBlock(testField, DayOfWeek.TUESDAY, FieldUsageBlock.UsageType.PRACTICE, LocalTime.of(17, 0), LocalTime.of(18, 0), "");
        FieldUsageBlock closed = new FieldUsageBlock(testField, DayOfWeek.SUNDAY, FieldUsageBlock.UsageType.CLOSED, LocalTime.of(0, 0), LocalTime.of(23, 59), "");

        when(repository.findByField(testField))
            .thenReturn(Arrays.asList(league, tournament, practice, closed));

        List<FieldUsageBlock> result = service.findByField(testField);

        assertEquals(4, result.size());
        assertTrue(result.stream().anyMatch(b -> b.getUsageType() == FieldUsageBlock.UsageType.LEAGUE));
        assertTrue(result.stream().anyMatch(b -> b.getUsageType() == FieldUsageBlock.UsageType.TOURNAMENT));
        assertTrue(result.stream().anyMatch(b -> b.getUsageType() == FieldUsageBlock.UsageType.PRACTICE));
        assertTrue(result.stream().anyMatch(b -> b.getUsageType() == FieldUsageBlock.UsageType.CLOSED));
    }

    @Test
    void shouldHandleMultipleFieldsWithBlocks() {
        Field field2 = new Field("Field 2");
        field2.setId(2L);

        FieldUsageBlock block2 = new FieldUsageBlock(
            field2,
            DayOfWeek.MONDAY,
            FieldUsageBlock.UsageType.TOURNAMENT,
            LocalTime.of(9, 0),
            LocalTime.of(18, 0),
            "Tournament"
        );

        when(repository.findByField(testField)).thenReturn(Arrays.asList(testBlock));
        when(repository.findByField(field2)).thenReturn(Arrays.asList(block2));

        List<FieldUsageBlock> result1 = service.findByField(testField);
        List<FieldUsageBlock> result2 = service.findByField(field2);

        assertEquals(1, result1.size());
        assertEquals(1, result2.size());
        assertEquals(FieldUsageBlock.UsageType.LEAGUE, result1.get(0).getUsageType());
        assertEquals(FieldUsageBlock.UsageType.TOURNAMENT, result2.get(0).getUsageType());
    }

}

