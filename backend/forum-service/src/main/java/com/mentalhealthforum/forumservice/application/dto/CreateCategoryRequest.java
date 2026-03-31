package com.mentalhealthforum.forumservice.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(
        @NotBlank(message = "Category name is required") String name,
        @NotBlank(message = "Category description is required") String description,
        int displayOrder
) {}
