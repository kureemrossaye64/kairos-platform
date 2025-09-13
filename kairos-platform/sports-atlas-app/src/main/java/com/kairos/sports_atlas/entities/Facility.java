package com.kairos.sports_atlas.entities;

import java.util.Set;

import org.locationtech.jts.geom.Point;

import com.kairos.core.entity.BaseEntity;
import com.kairos.search.annotation.GeoPointField;
import com.kairos.search.annotation.SearchableField;
import com.kairos.sports_atlas.services.Manifestable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "facilities")
@Getter
@Setter
@NoArgsConstructor
public class Facility extends BaseEntity implements Manifestable {
	
	@Column(columnDefinition = "geography(Point, 4326)") // Store as a geography Point
	@GeoPointField
    private Point coordinates;

    @Column(nullable = false)
    @SearchableField
    private String name;

    @Column(nullable = false)
    @SearchableField
    private String type; // e.g., "Football Pitch", "Swimming Pool", "Tennis Court"

    @Column(nullable = false)
    @SearchableField
    private String location; // e.g., "Curepipe", "Port Louis"

    
    private int capacity;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "facility_activities",
        joinColumns = @JoinColumn(name = "facility_id"),
        inverseJoinColumns = @JoinColumn(name = "activity_id")
    )
    
    private Set<Activity> supportedActivities;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus reviewStatus = ReviewStatus.PENDING;
    
    public String toString() {
    	return name;
    }

	@Override
	public ServiceEntity toServiceEntity() {
		ServiceEntity service = new ServiceEntity();
        service.setName(this.getName());
        service.setDescription(String.format(
            "A Facility for %s offered by the municipality", type
        ));
        service.setLocation(this.getLocation());
        service.setCoordinates(this.getCoordinates());
        service.setServiceType(type);
        service.setBookable(true); // Courses have registrations, not ad-hoc bookings
        service.setOwnershipType(OwnershipType.PUBLIC); // Assume most courses are private

        // The crucial link
        service.setOriginEntityId(this.getId());
        service.setOriginEntityType(Facility.class.getSimpleName());
        return service;
	}
}