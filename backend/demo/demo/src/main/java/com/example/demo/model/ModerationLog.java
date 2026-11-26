// model/ModerationLog.java
package com.example.demo.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "moderation_logs")
public class ModerationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(nullable = false, length = 50)
    private String targetType;

    @Column(nullable = false)
    private Long targetId;

    @Column(length = 1000)
    private String note;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected ModerationLog() {}

    public ModerationLog(User actor, String action, String targetType, Long targetId, String note) {
        this.actor = actor;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.note = note;
    }

    public Long getId() { return id; }
    public User getActor() { return actor; }
    public String getAction() { return action; }
    public String getTargetType() { return targetType; }
    public Long getTargetId() { return targetId; }
    public String getNote() { return note; }
    public Instant getCreatedAt() { return createdAt; }
}