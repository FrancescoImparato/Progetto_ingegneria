// dto/ObjectiveRequest.java
package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ObjectiveRequest(
    @NotBlank @Size(max = 150) String title,
    @Size(max = 1000) String description,
    boolean requiresProof
) {}