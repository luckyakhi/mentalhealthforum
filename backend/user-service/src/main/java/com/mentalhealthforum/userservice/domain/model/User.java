package com.mentalhealthforum.userservice.domain.model;

import java.time.LocalDateTime;

public class User {

    private UserId id;
    private String username;
    private String email;
    private String passwordHash;
    private UserRole role;
    private String bio;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private boolean active;

    // Private no-arg constructor for factory/mapper use
    private User() {}

    // All-args constructor
    public User(UserId id, String username, String email, String passwordHash,
                UserRole role, String bio, String avatarUrl,
                LocalDateTime createdAt, boolean active) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
        this.active = active;
    }

    /**
     * Factory method: creates a new user during registration with safe defaults.
     */
    public static User register(String username, String email, String passwordHash) {
        User user = new User();
        user.id = UserId.generate();
        user.username = username;
        user.email = email;
        user.passwordHash = passwordHash;
        user.role = UserRole.USER;
        user.bio = null;
        user.avatarUrl = null;
        user.createdAt = LocalDateTime.now();
        user.active = true;
        return user;
    }

    /**
     * Domain method: update mutable profile fields.
     */
    public void updateProfile(String username, String bio, String avatarUrl) {
        if (username != null && !username.isBlank()) {
            this.username = username;
        }
        this.bio = bio;
        this.avatarUrl = avatarUrl;
    }

    // -----------------------------------------------------------------------
    // Getters (no setters — domain object mutated only through domain methods)
    // -----------------------------------------------------------------------

    public UserId getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public String getBio() {
        return bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return active;
    }
}
