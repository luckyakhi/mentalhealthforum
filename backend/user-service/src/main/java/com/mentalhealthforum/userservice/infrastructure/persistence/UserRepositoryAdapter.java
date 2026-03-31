package com.mentalhealthforum.userservice.infrastructure.persistence;

import com.mentalhealthforum.userservice.domain.model.User;
import com.mentalhealthforum.userservice.domain.model.UserId;
import com.mentalhealthforum.userservice.domain.model.UserRole;
import com.mentalhealthforum.userservice.domain.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    public UserRepositoryAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = toEntity(user);
        UserJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return jpaRepository.findById(id.toString()).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    // -----------------------------------------------------------------------
    // Mapping helpers
    // -----------------------------------------------------------------------

    private UserJpaEntity toEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId().toString());
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setRole(user.getRole().name());
        entity.setBio(user.getBio());
        entity.setAvatarUrl(user.getAvatarUrl());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setActive(user.isActive());
        return entity;
    }

    private User toDomain(UserJpaEntity entity) {
        return new User(
                UserId.of(entity.getId()),
                entity.getUsername(),
                entity.getEmail(),
                entity.getPasswordHash(),
                UserRole.valueOf(entity.getRole()),
                entity.getBio(),
                entity.getAvatarUrl(),
                entity.getCreatedAt(),
                entity.isActive()
        );
    }
}
