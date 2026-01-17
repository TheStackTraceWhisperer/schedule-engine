package com.scheduleengine.field.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FieldTest {

  private Field field;

  @BeforeEach
  void setUp() {
    field = new Field("Memorial Stadium");
  }

  @Test
  void shouldCreateFieldWithName() {
    assertEquals("Memorial Stadium", field.getName());
  }

  @Test
  void shouldSetAndGetId() {
    field.setId(1L);
    assertEquals(1L, field.getId());
  }

  @Test
  void shouldSetAndGetName() {
    field.setName("Central Park Field");
    assertEquals("Central Park Field", field.getName());
  }

  @Test
  void shouldSetAndGetLocation() {
    field.setLocation("Downtown");
    assertEquals("Downtown", field.getLocation());
  }

  @Test
  void shouldSetAndGetAddress() {
    field.setAddress("123 Main Street");
    assertEquals("123 Main Street", field.getAddress());
  }

  @Test
  void shouldSetAndGetFacilities() {
    String facilities = "Parking, Lights, Restrooms";
    field.setFacilities(facilities);
    assertEquals(facilities, field.getFacilities());
  }

  @Test
  void shouldInitializeGamesAsEmptyList() {
    assertNotNull(field.getGames());
    assertTrue(field.getGames().isEmpty());
  }

  @Test
  void shouldCreateFieldWithDefaultConstructor() {
    Field newField = new Field();
    assertNull(newField.getName());
    assertNull(newField.getId());
  }

  @Test
  void shouldEqualAnotherFieldWithSameName() {
    Field field2 = new Field("Memorial Stadium");
    field.setId(1L);
    field2.setId(1L);
    assertEquals(field.getId(), field2.getId());
  }

}

