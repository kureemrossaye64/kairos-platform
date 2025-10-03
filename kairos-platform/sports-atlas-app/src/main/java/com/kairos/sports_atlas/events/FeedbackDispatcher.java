package com.kairos.sports_atlas.events;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kairos.notification.NotificationService;
import com.kairos.sports_atlas.entities.PendingFeedbackRequest;
import com.kairos.sports_atlas.entities.User;
import com.kairos.sports_atlas.repositories.PendingFeedbackRepository;
import com.kairos.sports_atlas.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedbackDispatcher {

    private final PendingFeedbackRepository pendingRepo;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Scheduled(fixedRate = 3600000) // Run once every hour
    @Transactional
    public void dispatchPendingFeedbackRequests() {
        log.debug("Running feedback dispatcher job...");
        List<PendingFeedbackRequest> requests = pendingRepo.findByIsDispatchedFalseAndDispatchAtBefore(LocalDateTime.now());
        if (requests.isEmpty()) {
            log.debug("No pending feedback requests to dispatch.");
            return;
        }

        log.info("Found {} feedback requests to dispatch.", requests.size());
        for (PendingFeedbackRequest request : requests) {
            User user = userRepository.findById(request.getUserId()).orElse(null);
            if (user != null) {
                String subject = "How was your experience?";
                String body = String.format(
                    "Hi %s!\n\nWe hope you had a great time. To help us improve, " +
                    "you can provide feedback to Kaya using the following information:\n\n" +
                    "Token: %s\nRating: [1-5]\nComment: [Your comment]\n\n" +
                    "Example: \"submit feedback with token %s, rating 5, comment The pitch was perfect!\"",
                    user.getUsername().split("\\.")[0], // Get first name
                    request.getUniqueToken(),
                    request.getUniqueToken()
                );
                notificationService.send(user.getUsername(), subject, body); // Using username as email for demo
                request.setDispatched(true);
            } else {
                log.warn("User not found for feedback request {}. Marking as dispatched to prevent retries.", request.getId());
                request.setDispatched(true); // Mark as dispatched to avoid spamming logs
            }
        }
        pendingRepo.saveAll(requests);
    }
}