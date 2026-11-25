// dto/MissionResponse.java
package com.example.demo.dto;

import com.example.demo.model.*;
import java.time.Instant;
import java.util.List;

public record MissionResponse(
    Long id,
    String title,
    String description,
    MissionCategory category,
    MissionType type,
    MissionStatus status,
    Integer durationMinutes,
    int points,
    String participationConditions,
    Double latitude,
    Double longitude,
    String locationName,
    Long authorId,
    String authorNickname,
    int objectivesCount,
    List<ObjectiveResponse> objectives,
    Instant createdAt
) {
    public static MissionResponse from(Mission m, boolean includeObjectives) {
        List<ObjectiveResponse> objList = includeObjectives
            ? m.getObjectives().stream().map(ObjectiveResponse::from).toList()
            : List.of(); // R03/R04: MAI inviare gli obiettivi segreti al client prima dell'accettazione!

        return new MissionResponse(
            m.getId(),
            m.getTitle(),
            m.getDescription(),
            m.getCategory(),
            m.getType(),
            m.getStatus(),
            m.getDurationMinutes(),
            m.getPoints(),
            m.getParticipationConditions(),
            m.getLatitude(),
            m.getLongitude(),
            m.getLocationName(),
            m.getAuthor().getId(),
            m.getAuthor().getNickname(),
            m.getObjectives().size(),
            objList,
            m.getCreatedAt()
        );
    }
}