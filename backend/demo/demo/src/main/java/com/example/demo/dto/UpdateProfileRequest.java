package com.example.demo.dto;
// dto/UpdateProfileRequest.java
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @NotBlank @Size(min = 3, max = 50) String nickname,
    @Size(max = 500) String bio
) {}