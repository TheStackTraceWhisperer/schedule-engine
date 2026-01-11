package com.scheduleengine.field.domain;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "field_usage_block")
public class FieldUsageBlock {
    public enum UsageType { LEAGUE, TOURNAMENT, PRACTICE, CLOSED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "field_id")
    private Field field;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Enumerated(EnumType.STRING)
    @Column(name = "usage_type", nullable = false)
    private UsageType usageType;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "notes")
    private String notes;

    public FieldUsageBlock() {}

    public FieldUsageBlock(Field field, DayOfWeek dayOfWeek, UsageType usageType, LocalTime startTime, LocalTime endTime, String notes) {
        this.field = field;
        this.dayOfWeek = dayOfWeek;
        this.usageType = usageType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Field getField() { return field; }
    public void setField(Field field) { this.field = field; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public UsageType getUsageType() { return usageType; }
    public void setUsageType(UsageType usageType) { this.usageType = usageType; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

