package com.kairos.pulsberry.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "chat_sessions")
@Getter @Setter
public class ChatSession extends BaseEntity {
    @Column(nullable = false)
    private String userId;
    
    private String title;
}