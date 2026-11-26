// dto/ModerationLogResponse.java
package com.example.demo.dto;

import com.example.demo.model.ModerationLog;
import java.time.Instant;

public record ModerationLogResponse(
    Long id, String actorNickname, String action, String targetType, Long targetId, String note, Instant createdAt
) {
    public static ModerationLogResponse from(ModerationLog log) {
        return new ModerationLogResponse(
            log.getId(), log.getActor().getNickname(), log.getAction(),
            log.getTargetType(), log.getTargetId(), log.getNote(), log.getCreatedAt()
        );
    }
}