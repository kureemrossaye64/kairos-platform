package com.kairos.sports_atlas.entities;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "performance_records")
@Getter
@Setter
@NoArgsConstructor
public class PerformanceRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "athlete_id", nullable = false)
    private Athlete athlete;

    @Column(nullable = false)
    private String eventName;

    @Column(nullable = false)
    private String result; // e.g., "10.52", "2.15"

    private String unit; // e.g., "seconds", "meters"

    @Column(nullable = false)
    private LocalDate eventDate;
}