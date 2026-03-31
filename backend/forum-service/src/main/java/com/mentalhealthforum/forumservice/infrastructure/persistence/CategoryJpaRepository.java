package com.mentalhealthforum.forumservice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, String> {

    List<CategoryJpaEntity> findAllByOrderByDisplayOrderAsc();

    boolean existsByName(String name);
}
