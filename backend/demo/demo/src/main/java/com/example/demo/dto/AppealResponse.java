// dto/AppealResponse.java
package com.example.demo.dto;

import com.example.demo.model.*;
import java.time.Instant;

public record AppealResponse(
    Long id,
    Long requesterId,
    String requesterNickname,
    AppealTargetType targetType,
    Long targetId,
    String reason,
    AppealStatus status,
    String decisionNote,
    Instant decidedAt,
    Instant createdAt
) {
    public static AppealResponse from(Appeal a) {
        return new AppealResponse(
            a.getId(),
            a.getRequester().getId(),
            a.getRequester().getNickname(),
            a.getTargetType(),
            a.getTargetId(),
            a.getReason(),
            a.getStatus(),
            a.getDecisionNote(),
            a.getDecidedAt(),
            a.getCreatedAt()
        );
    }
}