package com.kairos.sports_atlas.entities;

import org.locationtech.jts.geom.Point;

import com.kairos.agentic_framework.conversational_ingestion.annotations.ConversationalEntity;
import com.kairos.agentic_framework.conversational_ingestion.annotations.ConversationalField;
import com.kairos.core.entity.BaseEntity;
import com.kairos.sports_atlas.services.Manifestable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "training_opportunities")
@Getter
@Setter
@NoArgsConstructor
@ConversationalEntity(
    name = "Training Opportunity",
    description = "A course, bootcamp, or training program that teaches a specific skill.",
    introMessage = "AI: provide the user a message telling him that you can register his offer (training opportunity) and the advantages that it implies",
    outroMessage = "AI: thank the user politely and tell him that the data has been saved, and he will be informed once our officials has reviewed and validated the application"
)
public class TrainingOpportunity extends BaseEntity implements Manifestable{

    @NotBlank(message = "The title cannot be empty.")
    @Column(nullable = false)
    @ConversationalField(prompt = "What is the official title of the training course?")
    private String title;

    @NotBlank(message = "The provider name cannot be empty.")
    @Column(nullable = false)
    @ConversationalField(prompt = "Which organization or person is providing this training?")
    private String providerName;

    @NotBlank(message = "Please specify a skill category.")
    @Column(nullable = false)
    @ConversationalField(prompt = "What general category does this skill fall into? (e.g., IT, Hospitality, Design, Finance)")
    private String skillCategory;

    @Min(value = 1, message = "Duration must be at least 1 week.")
    @Column(nullable = false)
    @ConversationalField(prompt = "How many weeks does the training last?")
    private int durationWeeks;

    @Min(value = 0, message = "Cost cannot be negative.")
    @Column(nullable = false)
    @ConversationalField(prompt = "What is the total cost of the course in Mauritian Rupees? (Enter 0 if it's free)")
    private double cost;

    @NotBlank(message = "The location cannot be empty.")
    @Column(nullable = false)
    @ConversationalField(prompt = "Where will the training take place? Please provide a specific address or landmark.")
    private String location;

    @Column(columnDefinition = "geography(Point, 4326)")
    private Point coordinates;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus reviewStatus = ReviewStatus.PENDING;
    
    @Override
    public ServiceEntity toServiceEntity() {
    	 ServiceEntity service = new ServiceEntity();
         service.setName(this.getTitle());
         service.setDescription(String.format(
             "A %d-week course in %s offered by %s. Cost: Rs %.2f",
             this.getDurationWeeks(), this.getSkillCategory(), this.getProviderName(), this.getCost()
         ));
         service.setLocation(this.getLocation());
         service.setCoordinates(this.getCoordinates());
         service.setServiceType("Educational Course");
         service.setBookable(false); // Courses have registrations, not ad-hoc bookings
         service.setOwnershipType(OwnershipType.PRIVATE); // Assume most courses are private

         // The crucial link
         service.setOriginEntityId(this.getId());
         service.setOriginEntityType(TrainingOpportunity.class.getSimpleName());
         return service;
    }

	
}