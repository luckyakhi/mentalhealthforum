package com.mentalhealthforum.userservice.infrastructure.web;

import com.mentalhealthforum.userservice.application.dto.UpdateProfileRequest;
import com.mentalhealthforum.userservice.application.dto.UserProfileDto;
import com.mentalhealthforum.userservice.application.service.UserApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserApplicationService userApplicationService;

    public UserController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    /**
     * GET /api/users/me — returns the currently authenticated user's profile.
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUser(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        UserProfileDto profile = userApplicationService.getCurrentUser(userId);
        return ResponseEntity.ok(profile);
    }

    /**
     * GET /api/users/{userId}/profile — publicly accessible user profile.
     */
    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileDto> getProfile(@PathVariable String userId) {
        UserProfileDto profile = userApplicationService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }

    /**
     * PUT /api/users/me — update the authenticated user's profile.
     */
    @PutMapping("/me")
    public ResponseEntity<UserProfileDto> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        UserProfileDto updated = userApplicationService.updateProfile(userId, request);
        return ResponseEntity.ok(updated);
    }
}
