package com.mentalhealthforum.userservice.application.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @Size(max = 500, message = "Bio must not exceed 500 characters")
        String bio,

        String avatarUrl
) {}
