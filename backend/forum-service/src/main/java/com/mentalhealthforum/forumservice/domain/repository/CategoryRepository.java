package com.mentalhealthforum.forumservice.domain.repository;

import com.mentalhealthforum.forumservice.domain.model.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port (interface) for Category persistence.
 */
public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findById(UUID id);

    List<Category> findAllOrderedByDisplayOrder();

    void delete(UUID id);

    boolean existsByName(String name);
}
