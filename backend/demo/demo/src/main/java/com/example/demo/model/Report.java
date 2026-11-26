// model/Report.java
package com.example.demo.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mission_id")
    private Mission mission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

    @Column(nullable = false, length = 1000)
    private String details;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderator_id")
    private User moderator;

    @Enumerated(EnumType.STRING)
    private ModerationAction actionTaken;

    @Column(length = 1000)
    private String decisionNote;

    private Instant decidedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Report() {}

    public Report(User reporter, Mission mission, ReportReason reason, String details) {
        this.reporter = reporter;
        this.mission = mission;
        this.reason = reason;
        this.details = details;
    }

    public void decide(User moderator, ModerationAction action, String note, Instant now) {
        if (this.status != ReportStatus.OPEN) {
            throw new IllegalStateException("La segnalazione è già stata gestita");
        }
        this.moderator = moderator;
        this.actionTaken = action;
        this.decisionNote = note;
        this.decidedAt = now;
        this.status = (action == ModerationAction.DISMISS) ? ReportStatus.DISMISSED : ReportStatus.ACTION_TAKEN;
    }

    public Long getId() { return id; }
    public User getReporter() { return reporter; }
    public Mission getMission() { return mission; }
    public ReportReason getReason() { return reason; }
    public String getDetails() { return details; }
    public ReportStatus getStatus() { return status; }
    public User getModerator() { return moderator; }
    public ModerationAction getActionTaken() { return actionTaken; }
    public String getDecisionNote() { return decisionNote; }
    public Instant getDecidedAt() { return decidedAt; }
    public Instant getCreatedAt() { return createdAt; }
}