package com.kairos.sports_atlas.entities;

import com.kairos.core.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "activities")
@Getter
@Setter
@NoArgsConstructor
public class Activity extends BaseEntity {

	@Column(nullable = false, unique = true)
	private String name; // "Football", "Badminton", "Table Tennis"

	public Activity(String name) {
		this.name = name;
	}
}