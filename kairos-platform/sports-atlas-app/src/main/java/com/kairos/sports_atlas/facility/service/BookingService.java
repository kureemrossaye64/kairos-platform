package com.kairos.sports_atlas.facility.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.kairos.sports_atlas.common.GenericCrudService;
import com.kairos.sports_atlas.entities.Booking;
import com.kairos.sports_atlas.events.BookingCompletedEvent;
import com.kairos.sports_atlas.repositories.BookingRepository;

@Service
public class BookingService extends GenericCrudService<Booking, BookingRepository> {
	private final ApplicationEventPublisher eventPublisher;
	public BookingService(BookingRepository repository, ApplicationEventPublisher eventPublisher) {
		super(repository);
		this.eventPublisher = eventPublisher;
	}
	
	// In your method that creates and saves a booking...
    public Booking createBooking(Booking booking) {
        Booking savedBooking = this.save(booking);

        // Publish the event after the transaction is successfully committed
        eventPublisher.publishEvent(new BookingCompletedEvent(
            savedBooking.getService().getId(),
            savedBooking.getUser().getId()
        ));
        
        return savedBooking;
    }
}