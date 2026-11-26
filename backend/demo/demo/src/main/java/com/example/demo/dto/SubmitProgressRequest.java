// dto/SubmitProgressRequest.java
package com.example.demo.dto;

import jakarta.validation.constraints.Size;

public record SubmitProgressRequest(
    @Size(max = 1000) String proofText,
    @Size(max = 500) String proofImageUrl
) {}