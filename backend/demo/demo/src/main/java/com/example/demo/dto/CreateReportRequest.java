// dto/CreateReportRequest.java
package com.example.demo.dto;

import com.example.demo.model.ReportReason;
import jakarta.validation.constraints.*;

public record CreateReportRequest(
    @NotNull Long missionId,
    @NotNull ReportReason reason,
    @NotBlank @Size(min = 5, max = 1000) String details
) {}