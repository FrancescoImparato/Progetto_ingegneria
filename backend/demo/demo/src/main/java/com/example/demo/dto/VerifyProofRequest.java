// dto/VerifyProofRequest.java
package com.example.demo.dto;

import jakarta.validation.constraints.Size;

public record VerifyProofRequest(
    boolean approved,
    @Size(max = 500) String note
) {}