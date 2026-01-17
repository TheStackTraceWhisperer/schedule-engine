package com.scheduleengine.field.repository;

import com.scheduleengine.field.domain.Field;
import com.scheduleengine.field.domain.FieldAvailability;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class FieldAvailabilityRepositoryTest {

  @Autowired
  private FieldRepository fieldRepository;

  @Autowired
  private FieldAvailabilityRepository availabilityRepository;

  private Field testField;

  @BeforeEach
  void setUp() {
    testField = new Field("Test Field");
    testField.setLocation("Downtown");
    testField = fieldRepository.save(testField);
  }

  @Test
  void shouldSaveAndRetrieveFieldAvailability() {
    FieldAvailability availability = new FieldAvailability(
      testField,
      DayOfWeek.MONDAY,
      LocalTime.of(9, 0),
      LocalTime.of(17, 0)
    );

    FieldAvailability saved = availabilityRepository.save(availability);

    assertNotNull(saved.getId());
    assertEquals(DayOfWeek.MONDAY, saved.getDayOfWeek());
  }

  @Test
  void shouldFindByField() {
    FieldAvailability monday = new FieldAvailability(testField, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
    FieldAvailability tuesday = new FieldAvailability(testField, DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));

    availabilityRepository.save(monday);
    availabilityRepository.save(tuesday);

    List<FieldAvailability> result = availabilityRepository.findByField(testField);

    assertEquals(2, result.size());
  }

  @Test
  void shouldFindByFieldAndDayOfWeek() {
    FieldAvailability monday = new FieldAvailability(testField, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
    availabilityRepository.save(monday);

    List<FieldAvailability> result = availabilityRepository.findByFieldAndDayOfWeek(testField, DayOfWeek.MONDAY);

    assertEquals(1, result.size());
    assertEquals(DayOfWeek.MONDAY, result.get(0).getDayOfWeek());
  }

  @Test
  void shouldReturnEmptyListWhenNoAvailabilityForDay() {
    List<FieldAvailability> result = availabilityRepository.findByFieldAndDayOfWeek(testField, DayOfWeek.SUNDAY);

    assertTrue(result.isEmpty());
  }

  @Test
  void shouldFindByDayOfWeek() {
    FieldAvailability monday = new FieldAvailability(testField, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
    availabilityRepository.save(monday);

    List<FieldAvailability> result = availabilityRepository.findByDayOfWeek(DayOfWeek.MONDAY);

    assertEquals(1, result.size());
  }

  @Test
  void shouldHandleMultipleFieldsWithSameDayAvailability() {
    Field field2 = new Field("Field 2");
    field2 = fieldRepository.save(field2);

    FieldAvailability avail1 = new FieldAvailability(testField, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
    FieldAvailability avail2 = new FieldAvailability(field2, DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(16, 0));

    availabilityRepository.save(avail1);
    availabilityRepository.save(avail2);

    List<FieldAvailability> result = availabilityRepository.findByDayOfWeek(DayOfWeek.MONDAY);

    assertEquals(2, result.size());
  }

  @Test
  void shouldDeleteAvailability() {
    FieldAvailability availability = new FieldAvailability(testField, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
    FieldAvailability saved = availabilityRepository.save(availability);

    availabilityRepository.deleteById(saved.getId());

    List<FieldAvailability> result = availabilityRepository.findByField(testField);
    assertTrue(result.isEmpty());
  }

}

