package com.kairos.sports_atlas.entities;

import org.locationtech.jts.geom.Point;

import com.kairos.core.entity.BaseEntity;
import com.kairos.core.entity.User;

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
@Table(name = "player_lfg")
@Getter
@Setter
@NoArgsConstructor
public class PlayerLfg extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "activity_id", nullable = false)
	private Activity activity;

	@Column(columnDefinition = "geography(Point, 4326)")
	private Point location;
}