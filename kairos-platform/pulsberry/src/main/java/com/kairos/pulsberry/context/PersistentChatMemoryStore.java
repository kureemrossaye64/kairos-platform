package com.kairos.pulsberry.context;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.kairos.pulsberry.entity.ChatMessageEntity;
import com.kairos.pulsberry.entity.ChatSession;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.CustomMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PersistentChatMemoryStore implements ChatMemoryStore {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        UUID sessionId = UUID.fromString((String) memoryId);
        List<ChatMessageEntity> entities = em.createQuery(
                "SELECT m FROM ChatMessageEntity m WHERE m.session.id = :sid ORDER BY m.createdAt ASC", ChatMessageEntity.class)
                .setParameter("sid", sessionId)
                .getResultList();

        return entities.stream().map(e -> {
            // Simplified mapping
            switch (e.getType()) {
                case USER: return new UserMessage(e.getContent());
                case AI: return new AiMessage(e.getContent());
                case SYSTEM: return new SystemMessage(e.getContent());
                case TOOL_EXECUTION_RESULT: return new ToolExecutionResultMessage(e.getId().toString(), e.getToolName(), e.getContent());
                default: return CustomMessage.from(Map.of("id", e.getId(), "text", e.getContent()));
            }
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        UUID sessionId;
        try {
            sessionId = UUID.fromString((String) memoryId);
        } catch (IllegalArgumentException e) {
            log.error("Invalid Session UUID: {}", memoryId);
            return;
        }

        ChatSession session = em.find(ChatSession.class, sessionId);
        if (session == null) {
            log.error("Attempted to save chat messages for non-existent session: {}", sessionId);
            throw new IllegalArgumentException("Session not found");
        }

        // 1. Fetch the Last Persisted Message for this session to find our sync point.
        // We order by sequenceNumber (or ID) desc to get the very last one.
        ChatMessageEntity lastDbMessage = null;
        try {
            lastDbMessage = em.createQuery(
                    "SELECT m FROM ChatMessageEntity m WHERE m.session.id = :sid ORDER BY m.id DESC", 
                    ChatMessageEntity.class)
                    .setParameter("sid", sessionId)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException e) {
            // DB is empty for this session, we will persist all incoming messages.
        }

        // 2. Identify the Delta (New Messages)
        int startIndex = 0;
        if (lastDbMessage != null) {
            // We iterate backwards through the incoming list to find the lastDbMessage.
            // Once found, everything *after* that index is new.
            boolean matchFound = false;
            for (int i = messages.size() - 1; i >= 0; i--) {
                ChatMessage memoryMsg = messages.get(i);
                
                // Content comparison check. 
                // Note: In a heavily concurrent env, we might generate a unique hash ID for every message
                // but textual comparison + type is usually sufficient for single-user-per-session streams.
                if (isSameMessage(lastDbMessage, memoryMsg)) {
                    startIndex = i + 1; // Start inserting from the next message
                    matchFound = true;
                    break;
                }
            }
            
            // Safety: If we didn't find the DB message in the window (e.g. window slid forward),
            // we technically assume all in 'messages' are newer or part of a different window.
            // However, strictly appending is safer to avoid duplication if the window overlaps weirdly.
            // For this specific implementation, if no match is found, we might be in a scenario 
            // where we assume they are new, OR we might want to check the DB count. 
            // Let's stick to the matched index logic as it handles 99% of "continue conversation" flows.
            if (!matchFound) {
               // Fallback: If the window completely moved past the last DB message, we assume all are new.
               // (This happens if maxMessages is small and conversation is long).
               startIndex = 0; 
            }
        }

        // 3. Persist only the new messages
        for (int i = startIndex; i < messages.size(); i++) {
            ChatMessage msg = messages.get(i);
            saveEntity(session, msg);
        }
    }

    private void saveEntity(ChatSession session, ChatMessage msg) {
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setSession(session);
        entity.setType(msg.type());
        
        // 4. Robust Content Mapping
        // Different message types handle content differently.
        // We map complex types to a string representation for storage.
        
        String contentToSave = extractContent(msg);
        // Handle case where AI message is purely a tool request (text is null)
       // if (contentToSave == null && msg.type().equals(dev.langchain4j.data.message.ChatMessageType.AI)) {
            // contentToSave = "[Tool Request Executed]"; 
        //}

        entity.setContent(contentToSave != null ? contentToSave : "");
        
        em.persist(entity);
        log.debug("Persisted new message type: {} for session: {}", msg.type(), session.getId());
    }
    
    private String extractContent(ChatMessage msg) {
    	String contentToSave = "";
        
        switch (msg.type()) {
            case USER:
            	contentToSave = ((UserMessage)msg).singleText();
            	break;
            case SYSTEM:
            	contentToSave = ((SystemMessage)msg).text();
            	break;
            case AI:
                contentToSave = ((AiMessage)msg).text();
                break;
            case TOOL_EXECUTION_RESULT:
                // We want to see the result of the tool in the history
                contentToSave = ((ToolExecutionResultMessage) msg).text();
                break;
            // Note: TOOL_EXECUTION_REQUEST is inside AiMessage in newer LC4J versions, 
            // or a separate type depending on version. If AiMessage, msg.text() might be null.
        }
        return contentToSave;
    }

    /**
     * Helper to compare a DB entity with a LangChain memory object.
     */
    private boolean isSameMessage(ChatMessageEntity dbEntity, ChatMessage memoryMsg) {
        if (dbEntity.getType() != memoryMsg.type()) return false;
        
        String memoryContent = extractContent(memoryMsg);
        if (memoryMsg.type() == dev.langchain4j.data.message.ChatMessageType.TOOL_EXECUTION_RESULT) {
            memoryContent = ((ToolExecutionResultMessage) memoryMsg).text();
        }
        
        // Handle nulls gracefully
        String dbContent = dbEntity.getContent() == null ? "" : dbEntity.getContent();
        String memContentStr = memoryContent == null ? "" : memoryContent;
        
        // If content matches, we assume it's the same message.
        // (In extremely rare cases where a user types the exact same thing twice in a row,
        // LangChain usually handles this logic before calling update, but even if we skip one,
        // it doesn't break the flow significantly).
        return dbContent.equals(memContentStr);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        UUID sessionId = UUID.fromString((String) memoryId);
        em.createQuery("DELETE FROM ChatMessageEntity m WHERE m.session.id = :sid")
          .setParameter("sid", sessionId)
          .executeUpdate();
    }
}