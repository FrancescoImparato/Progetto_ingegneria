package com.example.demo.dto;
// dto/UserProfileResponse.java

import com.example.demo.model.Role;
import com.example.demo.model.User;

public record UserProfileResponse(
    Long id, String email, String nickname, String bio, Role role, boolean active
) {
    public static UserProfileResponse from(User u) {
        return new UserProfileResponse(u.getId(), u.getEmail(), u.getNickname(), u.getBio(), u.getRole(), u.isActive());
    }
}