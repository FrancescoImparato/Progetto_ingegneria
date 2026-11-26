// dto/DecideAppealRequest.java
package com.example.demo.dto;

import jakarta.validation.constraints.*;

public record DecideAppealRequest(
    boolean accepted,
    @NotBlank @Size(min = 5, max = 1000) String decisionNote
) {}