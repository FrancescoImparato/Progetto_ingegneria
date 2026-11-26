// dto/ObjectiveProgressResponse.java
package com.example.demo.dto;

import com.example.demo.model.ObjectiveProgress;
import com.example.demo.model.VerificationStatus;
import java.time.Instant;

public record ObjectiveProgressResponse(
    Long progressId,
    Long objectiveId,
    String objectiveTitle,
    String objectiveDescription,
    int orderIndex,
    boolean requiresProof,
    boolean completed,
    String proofText,
    String proofImageUrl,
    VerificationStatus verificationStatus,
    String verificationNote,
    Instant submittedAt,
    Instant verifiedAt
) {
    public static ObjectiveProgressResponse from(ObjectiveProgress p) {
        return new ObjectiveProgressResponse(
            p.getId(),
            p.getObjective().getId(),
            p.getObjective().getTitle(),
            p.getObjective().getDescription(),
            p.getObjective().getOrderIndex(),
            p.getObjective().isRequiresProof(),
            p.isCompleted(),
            p.getProofText(),
            p.getProofImageUrl(),
            p.getVerificationStatus(),
            p.getVerificationNote(),
            p.getSubmittedAt(),
            p.getVerifiedAt()
        );
    }
}