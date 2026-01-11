package com.scheduleengine.field.service;

import com.scheduleengine.field.domain.FieldUsageBlock;
import com.scheduleengine.field.domain.Field;
import com.scheduleengine.field.repository.FieldUsageBlockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;

@Service
public class FieldUsageBlockService {
    private final FieldUsageBlockRepository repo;

    public FieldUsageBlockService(FieldUsageBlockRepository repo) {
        this.repo = repo;
    }

    public List<FieldUsageBlock> findByField(Field field) {
        return repo.findByField(field);
    }

    public List<FieldUsageBlock> findByFieldAndDayOfWeek(Field field, DayOfWeek day) {
        return repo.findByFieldAndDayOfWeek(field, day);
    }

    @Transactional
    public FieldUsageBlock save(FieldUsageBlock block) { return repo.save(block); }

    @Transactional
    public void delete(Long id) { repo.deleteById(id); }
}
