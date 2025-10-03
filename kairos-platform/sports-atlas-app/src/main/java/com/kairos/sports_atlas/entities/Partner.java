package com.kairos.sports_atlas.entities;

import org.locationtech.jts.geom.Point;

import com.kairos.agentic.conversational.annotations.ConversationalEntity;
import com.kairos.agentic.conversational.annotations.ConversationalField;
import com.kairos.sports_atlas.services.Manifestable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
// ...

@Entity
@Table(name = "partners")
@Data
@ConversationalEntity(
    name = "Partner",
    description = "A private company or individual offering a sports-related service."
)

public class Partner extends BaseEntity implements Manifestable {
	
	@Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus reviewStatus = ReviewStatus.PENDING;

    @Column(nullable = false)
    @ConversationalField(prompt = "What is the official name of your service or organization?")
    private String name;

    @ManyToOne @JoinColumn(name = "activity_id")
    @ConversationalField(prompt = "What is the primary sport or activity you offer? (e.g., Football, Jiu-Jitsu)")
    private Activity activity;

    @Column(nullable = false)
    @ConversationalField(prompt = "Great! And where are you located? Please provide a specific address or landmark.")
    private String location;

    @Column(nullable = true)
    @ConversationalField(prompt = "Perfect. Could you provide a email for interested people?")
    @Email
    private String contactEmail;
    
    @Column(nullable = true)
    @ConversationalField(prompt = "Perfect. Could you provide a phone for interested people?")
    private String contactPhone;
    
    @ConversationalField(prompt = "Great. Could you tell me if your service / facility require booking")
    private Boolean bookable = true;

    @Column(columnDefinition = "TEXT")
    @ConversationalField(prompt = "Excellent. Finally, could you give me a short description? What makes your service special?")
    private String description;

    @Column(columnDefinition = "geography(Point, 4326)")
    private Point coordinates;
    
    
    
    @Override
    public ServiceEntity toServiceEntity() {
        ServiceEntity service = new ServiceEntity();
        service.setName(this.getName());
        service.setDescription(this.getDescription());
        service.setLocation(this.getLocation());
        service.setCoordinates(this.getCoordinates());
        service.setServiceType("Private Service Provider"); // Standardized type
        service.setBookable(bookable != null? bookable :false); // Let's assume all partners offer bookable services
        service.setOwnershipType(OwnershipType.PRIVATE);

        // The crucial link
        service.setOriginEntityId(this.getId());
        service.setOriginEntityType(Partner.class.getSimpleName());
        return service;
    }
    
    
    public String getTitle() {
    	return name;
    }
}