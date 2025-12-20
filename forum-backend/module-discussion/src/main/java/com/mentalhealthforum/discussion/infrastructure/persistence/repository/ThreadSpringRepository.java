package com.mentalhealthforum.discussion.infrastructure.persistence.repository;

import com.mentalhealthforum.discussion.infrastructure.persistence.entity.ThreadJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ThreadSpringRepository extends JpaRepository<ThreadJpaEntity, UUID> {
}
