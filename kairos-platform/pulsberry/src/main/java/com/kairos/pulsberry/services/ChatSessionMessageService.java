package com.kairos.pulsberry.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import com.kairos.pulsberry.dto.ChatMessageEntityDto;
import com.kairos.pulsberry.dto.ChatSessionDto;
import com.kairos.pulsberry.entity.ChatSession;
import com.kairos.pulsberry.entity.User;
import com.kairos.pulsberry.repositories.ChatMessageEntityRepository;
import com.kairos.pulsberry.repositories.ChatSessionRepository;
import com.kairos.pulsberry.utils.Util;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ChatSessionMessageService {

	private final ChatMessageEntityRepository messageRepository;

	private final ChatSessionRepository sessionRepository;

	@Transactional
	public ChatSessionDto createSession(String title) {
		User currentUser = Util.getCurrentUser();
		ChatSession session = new ChatSession();
		session.setUserId(currentUser.getUsername());
		session.setTitle(title != null ? title : "New Chat");
		sessionRepository.save(session);
		return ChatSessionDto.from(session);
	}

	@Transactional(readOnly = true)
	public List<ChatSessionDto> getUserSessions() {
		User currentUser = Util.getCurrentUser();
		List<ChatSession> sessions = sessionRepository.findByUserIdOrderByUpdatedAtDesc(currentUser.getUsername());
		return sessions.stream().map(ChatSessionDto::from).collect(Collectors.toList());
	}
	
	@Transactional(readOnly = true)
    public List<ChatMessageEntityDto> getSessionMessages(@PathVariable UUID sessionId) {
       return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream().map(ChatMessageEntityDto::from).filter((f)->{
    	   
    	   return f.getContent() != null 
    			   && !f.getContent().equals("[Tool Request Executed]") 
    			   && !f.getType().equals("TOOL_EXECUTION_RESULT")
    			   && !f.getType().equals("SYSTEM")
    			   ;
       	}
       ).collect(Collectors.toList());
    }

}
