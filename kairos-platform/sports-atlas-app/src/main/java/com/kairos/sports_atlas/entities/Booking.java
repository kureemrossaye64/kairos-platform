package com.kairos.sports_atlas.entities;

import java.time.LocalDateTime;

import com.kairos.agentic_framework.conversational_ingestion.annotations.ConversationalField;
import com.kairos.agentic_framework.transactional_chat.annotations.TransactionalEntity;
import com.kairos.agentic_framework.transactional_chat.annotations.TransactionalField;
import com.kairos.core.entity.BaseEntity;
import com.kairos.core.entity.User; // Reuse the core User entity
import com.kairos.sports_atlas.facility.service.ServiceFieldProcessor;
import com.kairos.sports_atlas.facility.service.StartTimeFieldProcessor;

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
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@TransactionalEntity(
	    name = "Booking",
	    description = "The process of reserving a bookable service like a sports facility for a specific time."
	    ,instructions = ""
	)
public class Booking extends BaseEntity {
	
	@TransactionalField(description = "What is the purpose of the booking?")
	private String purpose; // e.g., "U-17 Team Training"

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "service_id", nullable = false)
	@TransactionalField(
	        description = "The specific service the user wants to book. It must be an service, approved facility. "
	        		+ "You should use the tool `findServices` to get the exact service the user want to book.",
	        processor = ServiceFieldProcessor.class
	    )
	private ServiceEntity service;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user; // The person who booked

	@Column(nullable = false)
	@TransactionalField(
	        description = "The desired start date and time for the booking, in 'YYYY-MM-DD HH:mm' format. This must be a future time and during the facility's opening hours. AI instruction: You understand the user input properly and do your best to convert the input to the format 'YYYY-MM-DD HH:mm'",
	        processor = StartTimeFieldProcessor.class
	    )
	private LocalDateTime startTime;

	@TransactionalField(
	        description = "The duration of the booking in hours, e.g., 1 or 1.5 for 90 minutes. AI Instruction: You should understand the user input properly and convert it to minutes",
	        processor = com.kairos.agentic_framework.conversational_ingestion.DefaultFieldProcessor.class // Use default for simple number
	    )
	private transient Double durationHours;

	@Column(nullable = false)
	private LocalDateTime endTime;

	
}