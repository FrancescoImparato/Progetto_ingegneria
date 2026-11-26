// model/Appeal.java
package com.example.demo.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "appeals",
    uniqueConstraints = @UniqueConstraint(columnNames = {"target_type", "target_id"})
)
public class Appeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id")
    private User requester;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private AppealTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppealStatus status = AppealStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @Column(length = 1000)
    private String decisionNote;

    private Instant decidedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Appeal() {}

    public Appeal(User requester, AppealTargetType targetType, Long targetId, String reason) {
        this.requester = requester;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
    }

    public void decide(User reviewer, boolean accepted, String note, Instant now) {
        if (this.status != AppealStatus.PENDING) {
            throw new IllegalStateException("La richiesta di riesame è già stata chiusa e non può essere modificata");
        }
        this.reviewer = reviewer;
        this.status = accepted ? AppealStatus.ACCEPTED : AppealStatus.REJECTED;
        this.decisionNote = note;
        this.decidedAt = now;
    }

    public Long getId() { return id; }
    public User getRequester() { return requester; }
    public AppealTargetType getTargetType() { return targetType; }
    public Long getTargetId() { return targetId; }
    public String getReason() { return reason; }
    public AppealStatus getStatus() { return status; }
    public User getReviewer() { return reviewer; }
    public String getDecisionNote() { return decisionNote; }
    public Instant getDecidedAt() { return decidedAt; }
    public Instant getCreatedAt() { return createdAt; }
}