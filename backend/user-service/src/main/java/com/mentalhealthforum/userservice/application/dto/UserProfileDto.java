package com.mentalhealthforum.userservice.application.dto;

public record UserProfileDto(
        String id,
        String username,
        String email,
        String role,
        String bio,
        String avatarUrl,
        String createdAt
) {}
