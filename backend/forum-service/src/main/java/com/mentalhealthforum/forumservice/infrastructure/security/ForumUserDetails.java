package com.mentalhealthforum.forumservice.infrastructure.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

/**
 * Holds the authenticated user's details extracted from JWT.
 * Used as the principal in the SecurityContext.
 */
public class ForumUserDetails {

    private final String userId;
    private final String username;
    private final Collection<? extends GrantedAuthority> authorities;

    public ForumUserDetails(String userId, String username,
                            Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.username = username;
        this.authorities = authorities != null ? authorities : List.of();
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String toString() {
        return "ForumUserDetails{userId='" + userId + "', username='" + username + "'}";
    }
}
