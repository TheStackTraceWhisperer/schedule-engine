package com.scheduleengine.field.repository;

import com.scheduleengine.field.domain.FieldAvailability;
import com.scheduleengine.field.domain.Field;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface FieldAvailabilityRepository extends JpaRepository<FieldAvailability, Long> {
    List<FieldAvailability> findByField(Field field);
    List<FieldAvailability> findByFieldAndDayOfWeek(Field field, DayOfWeek dayOfWeek);
    List<FieldAvailability> findByDayOfWeek(DayOfWeek dayOfWeek);
    List<FieldAvailability> findByFieldInAndDayOfWeek(List<Field> fields, DayOfWeek dayOfWeek);
}
