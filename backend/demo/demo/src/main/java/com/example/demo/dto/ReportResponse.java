// dto/ReportResponse.java
package com.example.demo.dto;

import com.example.demo.model.*;
import java.time.Instant;

public record ReportResponse(
    Long id,
    Long missionId,
    String missionTitle,
    MissionStatus missionStatus,
    Long reporterId,
    String reporterNickname,
    ReportReason reason,
    String details,
    ReportStatus status,
    ModerationAction actionTaken,
    String decisionNote,
    Instant decidedAt,
    Instant createdAt
) {
    public static ReportResponse from(Report r) {
        return new ReportResponse(
            r.getId(),
            r.getMission().getId(),
            r.getMission().getTitle(),
            r.getMission().getStatus(),
            r.getReporter().getId(),
            r.getReporter().getNickname(),
            r.getReason(),
            r.getDetails(),
            r.getStatus(),
            r.getActionTaken(),
            r.getDecisionNote(),
            r.getDecidedAt(),
            r.getCreatedAt()
        );
    }
}