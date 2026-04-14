package com.kairos.pulsberry.entity;

import dev.langchain4j.data.message.ChatMessageType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "chat_messages")
@Getter @Setter
public class ChatMessageEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatMessageType type;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    // Simple ordering
    private long sequenceNumber;
    
    @Column(nullable = true)
    private String toolName;
}
