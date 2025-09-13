package com.kairos.sports_atlas.logging.service;

import com.kairos.core.entity.User;
import com.kairos.core.repository.UserRepository;
import com.kairos.sports_atlas.entities.ChatLog;
import com.kairos.sports_atlas.repositories.ChatLogRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatLogService {
    private final ChatLogRepository chatLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public void logConversation(String username, String userMessage, String agentResponse) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found for logging: " + username));
        
        ChatLog log = new ChatLog(user, userMessage, agentResponse);
        
        // Simple heuristic to flag an unmet need for analysis
        if (agentResponse.toLowerCase().contains("i'm sorry, i could not find") ||
            agentResponse.toLowerCase().contains("i couldn't find any")) {
            log.setUnmetNeedFlag(true);
        }
        
        chatLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public String getLogsForAnalysis(LocalDate date) {
        Instant since = date.atStartOfDay().toInstant(ZoneOffset.UTC);
        List<ChatLog> logs = chatLogRepository.findLogsSince(since);
        
        return logs.stream()
                .map(log -> String.format(
                        "User '%s' asked: '%s' | Kaya responded: '%s' | Unmet Need Flag: %b",
                        log.getUser().getUsername(),
                        log.getUserMessage(),
                        log.getAgentResponse(),
                        log.isUnmetNeedFlag()
                ))
                .collect(Collectors.joining("\n---\n"));
    }
}