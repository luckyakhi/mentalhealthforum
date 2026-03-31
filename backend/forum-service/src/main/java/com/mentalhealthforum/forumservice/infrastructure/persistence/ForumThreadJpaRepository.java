package com.mentalhealthforum.forumservice.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForumThreadJpaRepository extends JpaRepository<ForumThreadJpaEntity, String> {

    Page<ForumThreadJpaEntity> findByCategoryIdOrderByCreatedAtDesc(String categoryId, Pageable pageable);

    Page<ForumThreadJpaEntity> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            String titleKeyword, String contentKeyword, Pageable pageable);

    long countByCategoryId(String categoryId);
}
