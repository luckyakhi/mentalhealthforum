package com.mentalhealthforum.userservice.domain.repository;

import com.mentalhealthforum.userservice.domain.model.User;
import com.mentalhealthforum.userservice.domain.model.UserId;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId id);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
