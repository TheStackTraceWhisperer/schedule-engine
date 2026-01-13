package com.scheduleengine.field.service;

import com.scheduleengine.field.domain.Field;
import com.scheduleengine.field.domain.FieldAvailability;
import com.scheduleengine.field.repository.FieldAvailabilityRepository;
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
class FieldAvailabilityServiceTest {

    @Mock
    private FieldAvailabilityRepository repository;

    private FieldAvailabilityService service;
    private Field testField;
    private FieldAvailability testAvailability;

    @BeforeEach
    void setUp() {
        service = new FieldAvailabilityService(repository);
        testField = new Field("Test Field");
        testField.setId(1L);
        testAvailability = new FieldAvailability(
            testField,
            DayOfWeek.MONDAY,
            LocalTime.of(9, 0),
            LocalTime.of(17, 0)
        );
        testAvailability.setId(1L);
    }

    @Test
    void shouldFindByField() {
        when(repository.findByField(testField)).thenReturn(Arrays.asList(testAvailability));

        List<FieldAvailability> result = service.findByField(testField);

        assertEquals(1, result.size());
        assertEquals(DayOfWeek.MONDAY, result.get(0).getDayOfWeek());
        verify(repository, times(1)).findByField(testField);
    }

    @Test
    void shouldFindByFieldAndDayOfWeek() {
        when(repository.findByFieldAndDayOfWeek(testField, DayOfWeek.MONDAY))
            .thenReturn(Arrays.asList(testAvailability));

        List<FieldAvailability> result = service.findByFieldAndDayOfWeek(testField, DayOfWeek.MONDAY);

        assertEquals(1, result.size());
        assertEquals(LocalTime.of(9, 0), result.get(0).getOpenTime());
        verify(repository, times(1)).findByFieldAndDayOfWeek(testField, DayOfWeek.MONDAY);
    }

    @Test
    void shouldReturnEmptyListWhenNoAvailabilityFound() {
        when(repository.findByFieldAndDayOfWeek(testField, DayOfWeek.SUNDAY))
            .thenReturn(Arrays.asList());

        List<FieldAvailability> result = service.findByFieldAndDayOfWeek(testField, DayOfWeek.SUNDAY);

        assertTrue(result.isEmpty());
        verify(repository, times(1)).findByFieldAndDayOfWeek(testField, DayOfWeek.SUNDAY);
    }

    @Test
    void shouldSaveAvailability() {
        when(repository.save(any(FieldAvailability.class))).thenReturn(testAvailability);

        FieldAvailability result = service.save(testAvailability);

        assertNotNull(result);
        assertEquals(DayOfWeek.MONDAY, result.getDayOfWeek());
        verify(repository, times(1)).save(testAvailability);
    }

    @Test
    void shouldDeleteAvailability() {
        service.delete(1L);

        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void shouldHandleMultipleAvailabilitiesForSameField() {
        FieldAvailability tuesday = new FieldAvailability(
            testField,
            DayOfWeek.TUESDAY,
            LocalTime.of(9, 0),
            LocalTime.of(17, 0)
        );

        FieldAvailability wednesday = new FieldAvailability(
            testField,
            DayOfWeek.WEDNESDAY,
            LocalTime.of(10, 0),
            LocalTime.of(18, 0)
        );

        when(repository.findByField(testField))
            .thenReturn(Arrays.asList(testAvailability, tuesday, wednesday));

        List<FieldAvailability> result = service.findByField(testField);

        assertEquals(3, result.size());
        assertEquals(DayOfWeek.MONDAY, result.get(0).getDayOfWeek());
        assertEquals(DayOfWeek.TUESDAY, result.get(1).getDayOfWeek());
        assertEquals(DayOfWeek.WEDNESDAY, result.get(2).getDayOfWeek());
    }

    @Test
    void shouldHandleDifferentTimeRanges() {
        FieldAvailability earlyMorning = new FieldAvailability(
            testField,
            DayOfWeek.MONDAY,
            LocalTime.of(6, 0),
            LocalTime.of(8, 0)
        );

        FieldAvailability evening = new FieldAvailability(
            testField,
            DayOfWeek.MONDAY,
            LocalTime.of(18, 0),
            LocalTime.of(22, 0)
        );

        when(repository.findByField(testField))
            .thenReturn(Arrays.asList(earlyMorning, testAvailability, evening));

        List<FieldAvailability> result = service.findByField(testField);

        assertEquals(3, result.size());
        assertEquals(LocalTime.of(6, 0), result.get(0).getOpenTime());
        assertEquals(LocalTime.of(9, 0), result.get(1).getOpenTime());
        assertEquals(LocalTime.of(18, 0), result.get(2).getOpenTime());
    }

}

