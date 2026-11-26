// model/Participation.java
package com.example.demo.model;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "participations")
public class Participation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mission_id")
    private Mission mission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipationStatus status = ParticipationStatus.IN_PROGRESS;

    @Column(nullable = false, updatable = false)
    private Instant acceptedAt;

    private Instant expiresAt; // valorizzato solo per SECRET_TIMED
    private Instant completedAt;

    @Column(nullable = false)
    private int scoreAwarded = 0;

    @OneToMany(mappedBy = "participation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ObjectiveProgress> progresses = new ArrayList<>();

    protected Participation() {}

    public Participation(User user, Mission mission, Instant now) {
        this.user = user;
        this.mission = mission;
        this.acceptedAt = now;
        if (mission.isSecretTimed() && mission.getDurationMinutes() != null) {
            this.expiresAt = now.plus(Duration.ofMinutes(mission.getDurationMinutes()));
        }
        for (Objective obj : mission.getObjectives()) {
            this.progresses.add(new ObjectiveProgress(this, obj));
        }
    }

    // Metodi di dominio OOP
    public boolean refreshExpirationIfNeeded(Instant now) {
        if (this.status == ParticipationStatus.IN_PROGRESS
            && this.expiresAt != null
            && now.isAfter(this.expiresAt)) {
            this.status = ParticipationStatus.EXPIRED;
            return true;
        }
        return false;
    }

    public long getRemainingSeconds(Instant now) {
        if (this.expiresAt == null || this.status != ParticipationStatus.IN_PROGRESS) {
            return 0;
        }
        long seconds = Duration.between(now, this.expiresAt).getSeconds();
        return Math.max(0, seconds);
    }

    public void abandon() {
        if (this.status != ParticipationStatus.IN_PROGRESS) {
            throw new IllegalStateException("Puoi abbandonare solo una missione in corso");
        }
        this.status = ParticipationStatus.ABANDONED;
    }

    public void evaluateOutcome(Instant now) {
        boolean allCompleted = progresses.stream().allMatch(ObjectiveProgress::isCompleted);
        if (!allCompleted) {
            return;
        }
        boolean anyRejected = progresses.stream()
            .anyMatch(p -> p.getVerificationStatus() == VerificationStatus.REJECTED);
        if (anyRejected) {
            this.status = ParticipationStatus.FAILED;
            this.scoreAwarded = 0;
            this.completedAt = now;
            return;
        }
        boolean anyPending = progresses.stream()
            .anyMatch(p -> p.getVerificationStatus() == VerificationStatus.PENDING);
        if (anyPending) {
            this.status = ParticipationStatus.PENDING_VERIFICATION;
            return;
        }
        this.status = ParticipationStatus.COMPLETED;
        this.scoreAwarded = this.mission.getPoints();
        this.completedAt = now;
    }

    public void overrideOutcomeAfterAppeal(boolean accepted, Instant now) {
        if (accepted) {
            this.status = ParticipationStatus.COMPLETED;
            this.scoreAwarded = this.mission.getPoints();
            this.completedAt = now;
        }
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Mission getMission() { return mission; }
    public ParticipationStatus getStatus() { return status; }
    public Instant getAcceptedAt() { return acceptedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCompletedAt() { return completedAt; }
    public int getScoreAwarded() { return scoreAwarded; }
    public List<ObjectiveProgress> getProgresses() { return Collections.unmodifiableList(progresses); }
}