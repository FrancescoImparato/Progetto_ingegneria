package com.example.demo.dto;
// dto/AuthResponse.java

public record AuthResponse(String token, UserProfileResponse user) {}