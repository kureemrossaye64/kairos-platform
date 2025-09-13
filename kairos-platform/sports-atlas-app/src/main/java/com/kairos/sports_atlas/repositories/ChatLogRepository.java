package com.kairos.sports_atlas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.kairos.sports_atlas.entities.ChatLog;

import java.time.Instant;
import java.util.List;
import java.util.UUID;



public interface ChatLogRepository extends JpaRepository<ChatLog, UUID> {
	@Query("SELECT cl FROM ChatLog cl WHERE cl.createdAt >= :since")
	List<ChatLog> findLogsSince(Instant since);
}