package com.mentalhealthforum.forumservice.infrastructure.persistence;

import com.mentalhealthforum.forumservice.domain.model.Category;
import com.mentalhealthforum.forumservice.domain.repository.CategoryRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final CategoryJpaRepository jpaRepository;

    public CategoryRepositoryAdapter(CategoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Category save(Category category) {
        CategoryJpaEntity entity = toEntity(category);
        CategoryJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return jpaRepository.findById(id.toString()).map(this::toDomain);
    }

    @Override
    public List<Category> findAllOrderedByDisplayOrder() {
        return jpaRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id.toString());
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    // -------------------------------------------------------------------------
    // Mapping helpers
    // -------------------------------------------------------------------------

    private CategoryJpaEntity toEntity(Category category) {
        return CategoryJpaEntity.builder()
                .id(category.getId().toString())
                .name(category.getName())
                .description(category.getDescription())
                .displayOrder(category.getDisplayOrder())
                .threadCount(category.getThreadCount())
                .build();
    }

    private Category toDomain(CategoryJpaEntity entity) {
        return Category.reconstitute(
                UUID.fromString(entity.getId()),
                entity.getName(),
                entity.getDescription(),
                entity.getDisplayOrder(),
                entity.getThreadCount()
        );
    }
}
