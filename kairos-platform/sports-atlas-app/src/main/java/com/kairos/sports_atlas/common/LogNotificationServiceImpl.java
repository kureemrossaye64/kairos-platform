package com.kairos.sports_atlas.common;

import com.kairos.notification.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LogNotificationServiceImpl implements NotificationService {
    @Override
    public void send(String recipient, String subject, String body) {
        log.info("--- SIMULATED NOTIFICATION ---");
        log.info("Recipient: {}", recipient);
        log.info("Subject: {}", subject);
        log.info("Body:\n{}", body);
        log.info("--- END SIMULATED NOTIFICATION ---");
    }
}