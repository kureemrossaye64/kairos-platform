package com.kairos.pulsberry.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kairos.pulsberry.entity.ChatSession;

public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID>{
	
	List<ChatSession> findByUserIdOrderByUpdatedAtDesc(String userId);

}
