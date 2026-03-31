package com.mentalhealthforum.userservice.application.service;

import com.mentalhealthforum.userservice.application.dto.*;
import com.mentalhealthforum.userservice.domain.model.User;
import com.mentalhealthforum.userservice.domain.model.UserId;
import com.mentalhealthforum.userservice.domain.repository.UserRepository;
import com.mentalhealthforum.userservice.infrastructure.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserApplicationService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserApplicationService(UserRepository userRepository,
                                   JwtService jwtService,
                                   BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already in use: " + request.email());
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username is already taken: " + request.username());
        }

        String passwordHash = passwordEncoder.encode(request.password());
        User user = User.register(request.username(), request.email(), passwordHash);
        User saved = userRepository.save(user);

        String token = jwtService.generateToken(
                saved.getId().toString(),
                saved.getUsername(),
                saved.getRole().name()
        );

        return new AuthResponse(
                token,
                saved.getId().toString(),
                saved.getUsername(),
                saved.getRole().name()
        );
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new BadCredentialsException("Account is deactivated");
        }

        String token = jwtService.generateToken(
                user.getId().toString(),
                user.getUsername(),
                user.getRole().name()
        );

        return new AuthResponse(
                token,
                user.getId().toString(),
                user.getUsername(),
                user.getRole().name()
        );
    }

    @Transactional(readOnly = true)
    public UserProfileDto getProfile(String userId) {
        User user = userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return toProfileDto(user);
    }

    public UserProfileDto updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // If username is changing, check uniqueness
        if (request.username() != null && !request.username().isBlank()
                && !request.username().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.username())) {
                throw new IllegalArgumentException("Username is already taken: " + request.username());
            }
        }

        user.updateProfile(request.username(), request.bio(), request.avatarUrl());
        User saved = userRepository.save(user);
        return toProfileDto(saved);
    }

    @Transactional(readOnly = true)
    public UserProfileDto getCurrentUser(String userId) {
        return getProfile(userId);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private UserProfileDto toProfileDto(User user) {
        return new UserProfileDto(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getBio(),
                user.getAvatarUrl(),
                user.getCreatedAt() != null ? user.getCreatedAt().toString() : null
        );
    }
}
