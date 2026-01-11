package com.scheduleengine.field.service;

import com.scheduleengine.field.domain.FieldAvailability;
import com.scheduleengine.field.domain.Field;
import com.scheduleengine.field.repository.FieldAvailabilityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;

@Service
public class FieldAvailabilityService {
    private final FieldAvailabilityRepository repo;

    public FieldAvailabilityService(FieldAvailabilityRepository repo) {
        this.repo = repo;
    }

    public List<FieldAvailability> findByField(Field field) {
        return repo.findByField(field);
    }

    public List<FieldAvailability> findByFieldAndDayOfWeek(Field field, DayOfWeek day) {
        return repo.findByFieldAndDayOfWeek(field, day);
    }

    @Transactional
    public FieldAvailability save(FieldAvailability fa) { return repo.save(fa); }

    @Transactional
    public void delete(Long id) { repo.deleteById(id); }
}
