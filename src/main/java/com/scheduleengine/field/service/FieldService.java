package com.scheduleengine.field.service;

import com.scheduleengine.field.domain.Field;
import com.scheduleengine.field.repository.FieldRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FieldService {

  private final FieldRepository fieldRepository;

  public FieldService(FieldRepository fieldRepository) {
    this.fieldRepository = fieldRepository;
  }

  public List<Field> findAll() {
    return fieldRepository.findAll();
  }

  public Optional<Field> findById(Long id) {
    return fieldRepository.findById(id);
  }

  public Field save(Field field) {
    return fieldRepository.save(field);
  }

  public Field update(Long id, Field field) {
    field.setId(id);
    return fieldRepository.save(field);
  }

  public void deleteById(Long id) {
    fieldRepository.deleteById(id);
  }
}
