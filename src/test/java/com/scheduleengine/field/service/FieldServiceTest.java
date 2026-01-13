package com.scheduleengine.field.service;

import com.scheduleengine.field.domain.Field;
import com.scheduleengine.field.repository.FieldRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FieldServiceTest {

    @Mock
    private FieldRepository fieldRepository;

    private FieldService fieldService;
    private Field testField;

    @BeforeEach
    void setUp() {
        fieldService = new FieldService(fieldRepository);
        testField = new Field("Memorial Stadium");
        testField.setId(1L);
    }

    @Test
    void shouldFindAllFields() {
        Field field2 = new Field("Central Park");
        field2.setId(2L);

        when(fieldRepository.findAll()).thenReturn(Arrays.asList(testField, field2));

        List<Field> fields = fieldService.findAll();

        assertEquals(2, fields.size());
        assertEquals("Memorial Stadium", fields.get(0).getName());
        assertEquals("Central Park", fields.get(1).getName());
        verify(fieldRepository, times(1)).findAll();
    }

    @Test
    void shouldFindFieldById() {
        when(fieldRepository.findById(1L)).thenReturn(Optional.of(testField));

        Optional<Field> found = fieldService.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("Memorial Stadium", found.get().getName());
        verify(fieldRepository, times(1)).findById(1L);
    }

    @Test
    void shouldReturnEmptyOptionalWhenFieldNotFound() {
        when(fieldRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Field> found = fieldService.findById(999L);

        assertFalse(found.isPresent());
        verify(fieldRepository, times(1)).findById(999L);
    }

    @Test
    void shouldSaveField() {
        when(fieldRepository.save(any(Field.class))).thenReturn(testField);

        Field saved = fieldService.save(testField);

        assertNotNull(saved);
        assertEquals("Memorial Stadium", saved.getName());
        verify(fieldRepository, times(1)).save(testField);
    }

    @Test
    void shouldUpdateField() {
        Field updatedField = new Field("Updated Stadium");

        when(fieldRepository.save(any(Field.class))).thenReturn(updatedField);

        Field result = fieldService.update(1L, updatedField);

        assertEquals(1L, result.getId());
        assertEquals("Updated Stadium", result.getName());
        verify(fieldRepository, times(1)).save(any(Field.class));
    }

    @Test
    void shouldDeleteFieldById() {
        fieldService.deleteById(1L);

        verify(fieldRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldHandleEmptyFieldList() {
        when(fieldRepository.findAll()).thenReturn(Arrays.asList());

        List<Field> fields = fieldService.findAll();

        assertTrue(fields.isEmpty());
        verify(fieldRepository, times(1)).findAll();
    }

    @Test
    void shouldHandleMultipleFieldsWithDifferentProperties() {
        Field field2 = new Field("Riverside Park");
        field2.setId(2L);
        field2.setLocation("Downtown");
        field2.setAddress("456 River Road");

        Field field3 = new Field("North Complex");
        field3.setId(3L);
        field3.setLocation("North End");
        field3.setAddress("789 North Avenue");

        when(fieldRepository.findAll()).thenReturn(Arrays.asList(testField, field2, field3));

        List<Field> fields = fieldService.findAll();

        assertEquals(3, fields.size());
        assertEquals("Downtown", fields.get(1).getLocation());
        assertEquals("North End", fields.get(2).getLocation());
    }

}

