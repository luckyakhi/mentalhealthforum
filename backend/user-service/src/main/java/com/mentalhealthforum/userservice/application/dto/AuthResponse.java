package com.mentalhealthforum.userservice.application.dto;

public record AuthResponse(
        String token,
        String userId,
        String username,
        String role
) {}
