package com.kairos.sports_atlas.events;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kairos.agentic_framework.feedback.entity.PendingFeedbackRequest;
import com.kairos.agentic_framework.feedback.repository.PendingFeedbackRepository;
import com.kairos.core.entity.User;
import com.kairos.core.repository.UserRepository;
import com.kairos.notification.NotificationService;

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