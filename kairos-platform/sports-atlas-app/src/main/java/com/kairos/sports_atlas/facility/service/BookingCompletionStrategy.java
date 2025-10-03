package com.kairos.sports_atlas.facility.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.kairos.agentic.conversational.FormCompletionStrategy;
import com.kairos.agentic.form.Form;
import com.kairos.agentic.transactional.TransactionCompletionStrategy;
import com.kairos.agentic.transactional.TransactionContext;
import com.kairos.sports_atlas.entities.Booking;
import com.kairos.sports_atlas.entities.Facility;
import com.kairos.sports_atlas.entities.ServiceEntity;
import com.kairos.sports_atlas.entities.User;
import com.kairos.sports_atlas.repositories.UserRepository;
import com.kairos.sports_atlas.util.Util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingCompletionStrategy implements TransactionCompletionStrategy {

	private final BookingService bookingService;
	
	private final UserRepository userRepository;

	@Override
	public String getTransactionName() {
		return "Booking";
	}

	@Override
	public String execute(TransactionContext completedForm) {
		log.info("Executing completion strategy for a new Partner form.");
		String userId = Util.getConversationId();
		User currentUser = userRepository.findByUsername(userId).orElseThrow(()-> new IllegalStateException("cannot find user with username:" + userId));
		ServiceEntity facility = (ServiceEntity) completedForm.getFieldValue("service");
		LocalDateTime startTime = (LocalDateTime)completedForm.getFieldValue("startTime");
		Double durationHours = Double.parseDouble((String) completedForm.getFieldValue("durationHours"));
		String purpose = (String)completedForm.getFieldValue("purpose");
		Booking booking = new Booking();
		booking.setService(facility);
		booking.setStartTime(startTime);
		booking.setEndTime(startTime.plusMinutes((long) (durationHours * 60)));
		booking.setUser(currentUser);
		booking.setPurpose(purpose);

		bookingService.createBooking(booking); // This will also fire the feedback event
		
		return String.format(
	            "You're all set! Your booking for '%s' is confirmed for %s from %s to %s. Enjoy!",
	            facility.getName(),
	            startTime.toLocalDate(),
	            startTime.toLocalTime(),
	            booking.getEndTime().toLocalTime()
	        );

	}

}
