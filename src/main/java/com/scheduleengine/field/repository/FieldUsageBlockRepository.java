package com.scheduleengine.field.repository;

import com.scheduleengine.field.domain.FieldUsageBlock;
import com.scheduleengine.field.domain.Field;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface FieldUsageBlockRepository extends JpaRepository<FieldUsageBlock, Long> {
    List<FieldUsageBlock> findByField(Field field);
    List<FieldUsageBlock> findByFieldAndDayOfWeek(Field field, DayOfWeek dayOfWeek);
    List<FieldUsageBlock> findByDayOfWeek(DayOfWeek dayOfWeek);
    List<FieldUsageBlock> findByFieldInAndDayOfWeek(List<Field> fields, DayOfWeek dayOfWeek);
}
