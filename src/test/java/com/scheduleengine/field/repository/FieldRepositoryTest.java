package com.scheduleengine.field.repository;

import com.scheduleengine.field.domain.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class FieldRepositoryTest {

  @Autowired
  private FieldRepository fieldRepository;

  private Field testField;

  @BeforeEach
  void setUp() {
    testField = new Field("Memorial Stadium");
    testField.setLocation("Downtown");
    testField.setAddress("123 Main Street");
  }

  @Test
  void shouldSaveAndRetrieveField() {
    Field saved = fieldRepository.save(testField);

    assertNotNull(saved.getId());
    assertEquals("Memorial Stadium", saved.getName());
  }

  @Test
  void shouldFindFieldById() {
    Field saved = fieldRepository.save(testField);
    Optional<Field> found = fieldRepository.findById(saved.getId());

    assertTrue(found.isPresent());
    assertEquals("Memorial Stadium", found.get().getName());
  }

  @Test
  void shouldReturnEmptyOptionalWhenFieldNotFound() {
    Optional<Field> found = fieldRepository.findById(999L);

    assertFalse(found.isPresent());
  }

  @Test
  void shouldFindAllFields() {
    Field field2 = new Field("Central Park");
    field2.setLocation("North End");

    fieldRepository.save(testField);
    fieldRepository.save(field2);

    List<Field> fields = fieldRepository.findAll();

    assertEquals(2, fields.size());
  }

  @Test
  void shouldUpdateField() {
    Field saved = fieldRepository.save(testField);
    saved.setName("Updated Stadium");
    saved.setLocation("New Location");

    fieldRepository.save(saved);
    Optional<Field> updated = fieldRepository.findById(saved.getId());

    assertTrue(updated.isPresent());
    assertEquals("Updated Stadium", updated.get().getName());
    assertEquals("New Location", updated.get().getLocation());
  }

  @Test
  void shouldDeleteField() {
    Field saved = fieldRepository.save(testField);
    fieldRepository.deleteById(saved.getId());

    Optional<Field> found = fieldRepository.findById(saved.getId());
    assertFalse(found.isPresent());
  }

  @Test
  void shouldHandleFieldWithAllProperties() {
    testField.setFacilities("Parking, Lights, Restrooms");
    Field saved = fieldRepository.save(testField);

    Optional<Field> found = fieldRepository.findById(saved.getId());
    assertTrue(found.isPresent());
    assertEquals("Parking, Lights, Restrooms", found.get().getFacilities());
  }

  @Test
  void shouldHandleMultipleFieldsWithDifferentProperties() {
    Field field1 = new Field("Field 1");
    field1.setLocation("Location 1");
    field1.setAddress("Address 1");

    Field field2 = new Field("Field 2");
    field2.setLocation("Location 2");
    field2.setAddress("Address 2");
    field2.setFacilities("Indoor");

    Field field3 = new Field("Field 3");
    field3.setLocation("Location 3");
    field3.setAddress("Address 3");
    field3.setFacilities("Outdoor");

    fieldRepository.save(field1);
    fieldRepository.save(field2);
    fieldRepository.save(field3);

    List<Field> fields = fieldRepository.findAll();

    assertEquals(3, fields.size());
  }

  @Test
  void shouldHandleEmptyFacilities() {
    testField.setFacilities("");
    Field saved = fieldRepository.save(testField);

    Optional<Field> found = fieldRepository.findById(saved.getId());
    assertTrue(found.isPresent());
    assertEquals("", found.get().getFacilities());
  }

}

