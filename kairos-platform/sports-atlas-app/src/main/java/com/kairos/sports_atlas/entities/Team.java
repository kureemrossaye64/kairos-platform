package com.kairos.sports_atlas.entities;

import java.util.Set;

import com.kairos.agentic_framework.transactional_chat.annotations.TransactionalEntity;
import com.kairos.agentic_framework.transactional_chat.annotations.TransactionalField;
import com.kairos.core.entity.BaseEntity;
import com.kairos.core.entity.User;
import com.kairos.sports_atlas.facility.service.ActivityFieldProcessor;
import com.kairos.sports_atlas.facility.service.UserFieldProcessor;
import com.kairos.sports_atlas.facility.service.UserListFieldProcessor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
@TransactionalEntity(
    name = "Team",
    description = "The process of creating a new sports team and inviting players."
)
public class Team extends BaseEntity {

    @Column(nullable = false, unique = true)
    @TransactionalField(
        description = "The official name for the new team.",
        processor = com.kairos.agentic_framework.conversational_ingestion.DefaultFieldProcessor.class
    )
    private String name;

    @ManyToOne
    @JoinColumn(name = "activity_id", nullable = false)
    @TransactionalField(
        description = "The primary sport or activity this team plays, e.g., 'Football'.",
        processor = ActivityFieldProcessor.class
    )
    private Activity activity;

    @ManyToOne
    @JoinColumn(name = "captain_id", nullable = false)
    @TransactionalField(
        description = "The designated team captain. Must be a registered user.",
        processor = UserFieldProcessor.class
    )
    private User captain;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "team_members",
        joinColumns = @JoinColumn(name = "team_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @TransactionalField(
        description = "A comma-separated list of usernames for the players to be invited.",
        processor = UserListFieldProcessor.class
    )
    private Set<User> members;
}