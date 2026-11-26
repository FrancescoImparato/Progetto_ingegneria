// dto/ParticipationResponse.java
package com.example.demo.dto;

import com.example.demo.model.*;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

public record ParticipationResponse(
    Long id,
    Long missionId,
    String missionTitle,
    MissionType missionType,
    MissionCategory missionCategory,
    Long participantId,
    String participantNickname,
    ParticipationStatus status,
    Instant acceptedAt,
    Instant expiresAt,
    long remainingSeconds,
    Instant completedAt,
    int scoreAwarded,
    int missionMaxPoints,
    List<ObjectiveProgressResponse> objectives
) {
    public static ParticipationResponse from(Participation p, Instant now) {
        List<ObjectiveProgressResponse> objs = p.getProgresses().stream()
            .sorted(Comparator.comparingInt(op -> op.getObjective().getOrderIndex()))
            .map(ObjectiveProgressResponse::from)
            .toList();

        return new ParticipationResponse(
            p.getId(),
            p.getMission().getId(),
            p.getMission().getTitle(),
            p.getMission().getType(),
            p.getMission().getCategory(),
            p.getUser().getId(),
            p.getUser().getNickname(),
            p.getStatus(),
            p.getAcceptedAt(),
            p.getExpiresAt(),
            p.getRemainingSeconds(now),
            p.getCompletedAt(),
            p.getScoreAwarded(),
            p.getMission().getPoints(),
            objs
        );
    }
}