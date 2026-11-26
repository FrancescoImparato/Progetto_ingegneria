// dto/DecideReportRequest.java
package com.example.demo.dto;

import com.example.demo.model.ModerationAction;
import jakarta.validation.constraints.*;

public record DecideReportRequest(
    @NotNull ModerationAction action,
    @NotBlank @Size(min = 5, max = 1000) String decisionNote
) {}