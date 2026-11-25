// dto/CreateMissionRequest.java
package com.example.demo.dto;

import com.example.demo.model.MissionCategory;
import com.example.demo.model.MissionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record CreateMissionRequest(
    @NotBlank @Size(min = 3, max = 150) String title,
    @NotBlank @Size(min = 10, max = 2000) String description,
    @NotNull MissionCategory category,
    @NotNull MissionType type,
    @Min(1) @Max(1440) Integer durationMinutes,
    @Min(10) @Max(10000) int points,
    @Size(max = 500) String participationConditions,
    Double latitude,
    Double longitude,
    @Size(max = 200) String locationName,
    boolean publishImmediately,
    @NotEmpty @Valid List<ObjectiveRequest> objectives
) {}