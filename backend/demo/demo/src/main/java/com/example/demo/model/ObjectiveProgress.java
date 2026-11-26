// model/ObjectiveProgress.java
package com.example.demo.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "objective_progress")
public class ObjectiveProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participation_id")
    private Participation participation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "objective_id")
    private Objective objective;

    @Column(nullable = false)
    private boolean completed = false;

    @Column(length = 1000)
    private String proofText;

    @Column(length = 500)
    private String proofImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.NOT_SUBMITTED;

    @Column(length = 500)
    private String verificationNote;

    private Instant submittedAt;
    private Instant verifiedAt;

    protected ObjectiveProgress() {}

    public ObjectiveProgress(Participation participation, Objective objective) {
        this.participation = participation;
        this.objective = objective;
    }

    public void submit(String proofText, String proofImageUrl, Instant now) {
        this.completed = true;
        this.proofText = proofText;
        this.proofImageUrl = proofImageUrl;
        this.submittedAt = now;
        if (this.objective.isRequiresProof()) {
            this.verificationStatus = VerificationStatus.PENDING;
        } else {
            this.verificationStatus = VerificationStatus.APPROVED;
            this.verifiedAt = now;
        }
    }

    public void verify(boolean approved, String note, Instant now) {
        this.verificationStatus = approved ? VerificationStatus.APPROVED : VerificationStatus.REJECTED;
        this.verificationNote = note;
        this.verifiedAt = now;
    }

    public Long getId() { return id; }
    public Participation getParticipation() { return participation; }
    public Objective getObjective() { return objective; }
    public boolean isCompleted() { return completed; }
    public String getProofText() { return proofText; }
    public String getProofImageUrl() { return proofImageUrl; }
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public String getVerificationNote() { return verificationNote; }
    public Instant getSubmittedAt() { return submittedAt; }
    public Instant getVerifiedAt() { return verifiedAt; }
}