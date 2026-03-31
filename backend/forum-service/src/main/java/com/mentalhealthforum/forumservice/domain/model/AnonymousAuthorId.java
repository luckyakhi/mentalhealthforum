package com.mentalhealthforum.forumservice.domain.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Value object for privacy - derived from userId+salt, not reversible to real userId.
 */
public record AnonymousAuthorId(String value) {

    public static AnonymousAuthorId from(String userId, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String input = userId + salt;
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02x", hash[i]));
            }
            return new AnonymousAuthorId("anon-" + sb);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
