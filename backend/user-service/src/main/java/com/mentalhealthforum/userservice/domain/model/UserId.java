package com.mentalhealthforum.userservice.domain.model;

import java.util.UUID;

public record UserId(UUID value) {

    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }

    public static UserId of(String id) {
        return new UserId(UUID.fromString(id));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
