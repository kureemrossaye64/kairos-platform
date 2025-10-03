package com.kairos.sports_atlas.entities;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "athletes")
@Getter
@Setter
@NoArgsConstructor
public class Athlete extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private String sport;
    
    public Athlete(String name, LocalDate dateOfBirth, String sport) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.sport = sport;
    }
}