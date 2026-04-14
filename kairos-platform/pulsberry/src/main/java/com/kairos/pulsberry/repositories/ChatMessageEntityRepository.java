package com.kairos.pulsberry.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kairos.pulsberry.entity.ChatMessageEntity;

public interface ChatMessageEntityRepository extends JpaRepository<ChatMessageEntity, UUID>{
	
	List<ChatMessageEntity> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);

}
