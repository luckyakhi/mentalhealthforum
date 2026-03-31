package com.mentalhealthforum.forumservice.application.dto;

import jakarta.validation.constraints.Size;

public record UpdateThreadRequest(
        @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
        String title,
        @Size(min = 10, max = 10000, message = "Content must be between 10 and 10000 characters")
        String content
) {}
