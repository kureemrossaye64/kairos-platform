package com.kairos.sports_atlas.feedback;

import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import com.kairos.agentic_framework.feedback.FeedbackHandler;
import com.kairos.agentic_framework.feedback.entity.PendingFeedbackRequest;
import com.kairos.agentic_framework.feedback.repository.PendingFeedbackRepository;
import com.kairos.agentic_framework.tools.KairosTool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;

@KairosTool
@RequiredArgsConstructor
public class FeedbackTool {
    private final List<FeedbackHandler> handlers;
    private final PendingFeedbackRepository pendingRepo;

    @Tool("Submits user feedback for a service, event, or facility using a unique token provided in a notification.")
    @Transactional
    public String submitFeedback(
            @P("The unique token provided in the feedback request notification.") String token,
            @P("The user's numerical rating on a scale of 1 to 5.") int rating,
            @P("The user's detailed text comment about their experience.") String comment
    ) {
        Optional<PendingFeedbackRequest> requestOpt = pendingRepo.findByUniqueToken(token);
        if (requestOpt.isEmpty()) {
            return "I'm sorry, that is not a valid feedback token. Please check the token and try again.";
        }

        PendingFeedbackRequest request = requestOpt.get();
        handlers.stream()
                .filter(h -> h.getEntityType().equalsIgnoreCase(request.getEntityType()))
                .findFirst()
                .ifPresentOrElse(
                        handler -> handler.processFeedback(request.getEntityId(), rating, comment),
                        () -> { throw new RuntimeException("No feedback handler found for entity type: " + request.getEntityType()); }
                );

        pendingRepo.delete(request); // Clean up the completed request
        return "Thank you so much for your valuable feedback! We've recorded your input.";
    }
}