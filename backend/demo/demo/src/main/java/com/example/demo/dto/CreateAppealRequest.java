// dto/CreateAppealRequest.java
package com.example.demo.dto;

import com.example.demo.model.AppealTargetType;
import jakarta.validation.constraints.*;

public record CreateAppealRequest(
    @NotNull AppealTargetType targetType,
    @NotNull Long targetId,
    @NotBlank @Size(min = 5, max = 1000) String reason
) {}