package com.kairos.sports_atlas.common;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.javafaker.Faker;
import com.kairos.core.entity.User;
import com.kairos.core.repository.UserRepository;
import com.kairos.sports_atlas.entities.Activity;
import com.kairos.sports_atlas.entities.Athlete;
import com.kairos.sports_atlas.entities.Facility;
import com.kairos.sports_atlas.entities.Partner;
import com.kairos.sports_atlas.entities.PerformanceRecord;
import com.kairos.sports_atlas.entities.PlayerLfg;
import com.kairos.sports_atlas.entities.ServiceEntity;
import com.kairos.sports_atlas.entities.TrainingOpportunity;
import com.kairos.sports_atlas.facility.service.ActivityService;
import com.kairos.sports_atlas.facility.service.ApprovalService;
import com.kairos.sports_atlas.facility.service.BookingService;
import com.kairos.sports_atlas.facility.service.FacilityService;
import com.kairos.sports_atlas.facility.service.ServiceEntityService;
import com.kairos.sports_atlas.lfg.service.LfgService;
import com.kairos.sports_atlas.partner.service.PartnerService;
import com.kairos.sports_atlas.services.AthleteService;
import com.kairos.sports_atlas.services.PerformanceRecordService;
import com.kairos.sports_atlas.training.service.TrainingOpportunityService;
import com.kairos.vector_search.service.VectorStoreService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Profile("dev") // This service will only run when the 'dev' Spring profile is active
@RequiredArgsConstructor
@Slf4j
public class SyntheticDataService {

	private final FacilityService facilityRepository;
	private final AthleteService athleteRepository;
	private final PerformanceRecordService performanceRecordRepository;
	private final GeocodingService geocodingService;
	private final ActivityService activityRepository;
	private final BookingService bookingRepository;
	private final UserRepository userRepository;
	private final LfgService playerLfgRepository;
	private final PasswordEncoder passwordEncoder;
	private final PartnerService partnerService;
	private final TrainingOpportunityService opportunityRepository;
	private final ServiceEntityService serviceEntityRepository;
	private final ApprovalService approvalService;
	
	private final VectorStoreService openSearchIndexService;

	@PostConstruct
	public void generateData() {
		log.warn("DEV PROFILE ACTIVE: Generating synthetic data...");
		
		
		Map<String, Activity> activities = (activityRepository.count() == 0) ? generateActivities() : loadActivities();
		if (userRepository.count() <= 1)
			generateUsersAndAthletes();

		if (serviceEntityRepository.count() == 0) {
			try {
				openSearchIndexService.createIndexWithMapping(ServiceEntity.class);
			}catch(Exception e) {
				e.printStackTrace();
			}
			log.info("Generating and approving a large set of services...");
			generateAndApprovePublicFacilities(activities);
			generateAndApprovePartners(activities);
			generateAndApproveTrainingOpportunities(activities);
		} else {
			log.info("ServiceEntity records already exist. Skipping generation.");
		}

		List<Athlete> athletes = athleteRepository.findAll();
		if (performanceRecordRepository.count() == 0 && !athletes.isEmpty())
			generatePerformanceRecords(athletes);
		List<User> users = userRepository.findAll();
		if (playerLfgRepository.count() == 0 && !users.isEmpty())
			generateLfgEntries(users, activities);

		log.warn("Synthetic data generation complete. Total services manifested: {}", serviceEntityRepository.count());

		
		log.warn("Synthetic data generation complete.");
	}

	private Map<String, Activity> loadActivities() {
		return activityRepository.findAll().stream().collect(Collectors.toMap(Activity::getName, a -> a));
	}

	private void generateAndApprovePublicFacilities(Map<String, Activity> activities) {
        log.info("Generating public facilities...");
        // This method now creates the original Facility entities
        List<Facility> facilitiesToApprove = new ArrayList<>();
        facilitiesToApprove.add(facilityRepository.createFacility("Stade George V","Football Pitch", "Curepipe", 10000, Set.of(activities.get("Football"), activities.get("Athletics"))));
        facilitiesToApprove.add(facilityRepository.createFacility("Anjalay Stadium","Football Pitch", "Belle Vue", 15000, Set.of(activities.get("Football"), activities.get("Athletics"))));
        facilitiesToApprove.add(facilityRepository.createFacility("Piscine de Serge Alfred","Swimming Pool", "Beau Bassin", 200, Set.of(activities.get("Swimming"))));
        facilitiesToApprove.add(facilityRepository.createFacility("National Tennis Center","Tennis Court", "Petit Camp", 50, Set.of(activities.get("Tennis"))));
        facilitiesToApprove.add(facilityRepository.createFacility("Gymnase Pandit Sahadeo", "Volleyball Court","Vacoas", 100, Set.of(activities.get("Volleyball"), activities.get("Badminton"), activities.get("Handball"))));
        facilitiesToApprove.add(facilityRepository.createFacility("Maryse Justin Stadium","Athletics", "Reduit", 5000, Set.of(activities.get("Athletics"))));
        
        // Now, simulate the approval process for each one
        facilitiesToApprove.forEach(facility -> approvalService.approveFacility(facility.getId()));
    }
	
	
    
    private void generateAndApprovePartners(Map<String, Activity> activities) {
        log.info("Generating private partners...");
        List<Partner> partnersToApprove = new ArrayList<>();
        partnersToApprove.add(partnerService.createPartner("Mauritius BJJ Academy", "Jiu-Jitsu", "Moka", "contact@bjjmauritius.com", "57159027","Premier Brazilian Jiu-Jitsu academy for all ages."));
        partnersToApprove.add(partnerService.createPartner("Yoga Spirit Mauritius", "Yoga", "Flic en Flac", "babaya@yoga.mu", "57159027","Beachfront yoga classes and sunset meditation sessions."));
        partnersToApprove.add(partnerService.createPartner("Club Hippique de Maurice", "Equestrian", "Forest Side", "info@clubhippique.mu", "57159027","Historic horse riding club with lessons and competitions."));
        partnersToApprove.add(partnerService.createPartner("SYP Futsal Center", "Football", "Trianon", "futsal@fusal.mu", "57159027","Rentable 5-a-side indoor football pitches with modern amenities."));

        partnersToApprove.forEach(partner -> approvalService.approvePartner(partner.getId()));
    }
    
    private void generateAndApproveTrainingOpportunities(Map<String, Activity> activities) {
        log.info("Generating training opportunities...");
        List<TrainingOpportunity> oppsToApprove = new ArrayList<>();
        oppsToApprove.add(opportunityRepository.createOpportunity("Full-Stack Web Development Bootcamp", "Le Wagon Mauritius", "Coding", 9, 150000.00, "Moka"));
        oppsToApprove.add(opportunityRepository.createOpportunity("Advanced Barista Skills", "Mauritius Hospitality School", "Hospitality", 4, 25000.00, "Ebene"));
        oppsToApprove.add(opportunityRepository.createOpportunity("Introduction to Graphic Design", "Creative Hub Institute", "Graphic Design", 12, 40000.00, "Port Louis"));
        oppsToApprove.add(opportunityRepository.createOpportunity("Emergency First Aid Certification", "St John Ambulance", "First Aid", 2, 5000.00, "Vacoas"));

        oppsToApprove.forEach(opp -> approvalService.approveTrainingOpportunity(opp.getId()));
    }

	

	private void generateLfgEntries(List<User> users, Map<String, Activity> activities) {
		log.info("Generating 'Looking for Game' entries...");
		List<PlayerLfg> lfgEntries = new ArrayList<>();
		Random random = new Random();
		List<String> locations = List.of("Port Louis", "Curepipe", "Quatre Bornes", "Vacoas", "Flic en Flac");
		List<Activity> activityList = new ArrayList<>(activities.values());

		// Let's have about 15 players looking for a game
		int lfgCount = Math.min(15, users.size());
		Collections.shuffle(users); // Shuffle to pick random users

		for (int i = 0; i < lfgCount; i++) {
			User user = users.get(i);
			Activity activity = activityList.get(random.nextInt(activityList.size()));
			String location = locations.get(random.nextInt(locations.size()));

			PlayerLfg lfg = new PlayerLfg();
			lfg.setUser(user);
			lfg.setActivity(activity);
			try {
				lfg.setLocation(geocodingService.geocode(location));
				lfgEntries.add(lfg);
			} catch (Exception e) {
				log.warn("Could not geocode location '{}' for LFG entry. Skipping.", location);
			}
		}
		playerLfgRepository.saveAll(lfgEntries);
	}

	private List<User> generateUsersAndAthletes() {
		log.info("Generating users and corresponding athletes...");
		Faker faker = new Faker(new Locale("fr"));
		List<User> users = new ArrayList<>();
		List<Athlete> athletes = new ArrayList<>();

		for (int i = 0; i < 50; i++) {
			// Create the Athlete
			String fullName = faker.name().fullName();
			Athlete athlete = new Athlete(fullName,
					faker.date().birthday(14, 18).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
					i % 2 == 0 ? "Athletics" : "Swimming");
			athletes.add(athlete);

			// Create a corresponding User for the Athlete
			User user = new User();
			user.setUsername(fullName.toLowerCase().replace(" ", ".") + "." + i); // Ensure username is unique
			user.setPassword(passwordEncoder.encode("password123")); // Use a default password
			user.setRole("ROLE_USER");
			users.add(user);
		}
		athleteRepository.saveAll(athletes);
		return userRepository.saveAll(users);
	}

	

	private Map<String, Activity> generateActivities() {
		if (activityRepository.count() > 0) {
			log.info("Activities already exist, loading them.");
			return activityRepository.findAll().stream()
					.collect(Collectors.toMap(Activity::getName, activity -> activity));
		}
		log.info("Generating activities...");
		List<Activity> activities = List.of(new Activity("Football"), new Activity("Swimming"), new Activity("Tennis"),
				new Activity("Volleyball"), new Activity("Badminton"), new Activity("Athletics"),
				new Activity("Handball"));
		return activityRepository.saveAll(activities).stream()
				.collect(Collectors.toMap(Activity::getName, activity -> activity));
	}

	

	private void generatePerformanceRecords(List<Athlete> athletes) {
		log.info("Generating performance records for {} athletes...", athletes.size());
		Faker faker = new Faker();
		Random random = new Random();
		List<PerformanceRecord> allRecords = new ArrayList<>();

		// Define some realistic events and units
		Map<String, String> athleticsEvents = Map.of("100m Sprint", "seconds", "Long Jump", "meters", "Shot Put",
				"meters");
		Map<String, String> swimmingEvents = Map.of("50m Freestyle", "seconds", "100m Butterfly", "seconds");

		for (Athlete athlete : athletes) {
			int recordsToGenerate = random.nextInt(4) + 2; // 2 to 5 records per athlete
			Map<String, String> events = athlete.getSport().equals("Athletics") ? athleticsEvents : swimmingEvents;
			List<String> eventNames = new ArrayList<>(events.keySet());

			for (int i = 0; i < recordsToGenerate; i++) {
				String eventName = eventNames.get(random.nextInt(eventNames.size()));
				String unit = events.get(eventName);

				PerformanceRecord record = new PerformanceRecord();
				record.setAthlete(athlete);
				record.setEventName(eventName);
				record.setUnit(unit);
				record.setEventDate(faker.date().past(365 * 2, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault())
						.toLocalDate());

				// Generate realistic-looking results based on the event
				String result;
				switch (eventName) {
				case "100m Sprint":
					result = String.format("%.2f", faker.number().randomDouble(2, 11, 14));
					break;
				case "Long Jump":
					result = String.format("%.2f", faker.number().randomDouble(2, 5, 7));
					break;
				case "Shot Put":
					result = String.format("%.2f", faker.number().randomDouble(2, 10, 15));
					break;
				case "50m Freestyle":
					result = String.format("%.2f", faker.number().randomDouble(2, 24, 29));
					break;
				case "100m Butterfly":
					result = String.format("%.2f", faker.number().randomDouble(2, 58, 65));
					break;
				default:
					result = String.valueOf(faker.number().numberBetween(1, 100));
				}
				record.setResult(result);
				allRecords.add(record);
			}
		}
		performanceRecordRepository.saveAll(allRecords);
	}

	
}