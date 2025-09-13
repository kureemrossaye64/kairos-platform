package com.kairos.sports_atlas.entities;

import com.kairos.core.entity.BaseEntity;
import com.kairos.core.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chat_logs")
@Getter
@Setter
@NoArgsConstructor
public class ChatLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "user_message", columnDefinition = "TEXT", nullable = false)
    private String userMessage;

    @Column(name = "agent_response", columnDefinition = "TEXT", nullable = false)
    private String agentResponse;

    @Column(name = "unmet_need_flag", nullable = false)
    private boolean unmetNeedFlag = false;

    public ChatLog(User user, String userMessage, String agentResponse) {
        this.user = user;
        this.userMessage = userMessage;
        this.agentResponse = agentResponse;
    }
}