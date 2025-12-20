package com.mentalhealthforum.discussion.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ForumThreadTest {

    @Test
    @DisplayName("Should successfully create a thread and add a valid reply (Happy Path)")
    void shouldCreateThreadAndAddReply() {
        // Arrange
        ThreadId threadId = ThreadId.generate();
        AnonymousAuthorId authorId = AnonymousAuthorId.generate();
        Instant now = Instant.now();
        ForumThread thread = new ForumThread(threadId, "Anxiety Support", "I need help", authorId, now);

        AnonymousAuthorId replierId = AnonymousAuthorId.generate();
        String replyContent = "We are here for you.";

        // Act
        thread.addReply(replyContent, replierId);

        // Assert
        assertThat(thread.getId()).isEqualTo(threadId);
        assertThat(thread.getTopic()).isEqualTo("Anxiety Support");
        assertThat(thread.getContent()).isEqualTo("I need help");
        assertThat(thread.getStatus()).isEqualTo(ThreadStatus.ACTIVE);
        assertThat(thread.getSafetyFlag()).isEqualTo(SafetyFlag.SAFE);
        assertThat(thread.getTimestamp()).isEqualTo(now);

        assertThat(thread.getPosts()).hasSize(1);
        Post addedPost = thread.getPosts().get(0);
        assertThat(addedPost.getContent()).isEqualTo(replyContent);
        assertThat(addedPost.getAuthorId()).isEqualTo(replierId);
    }

    @Test
    @DisplayName("Should transition state to NEEDS_MODERATION when flagged for potential harm")
    void shouldFlagPotentialHarm() {
        // Arrange
        ForumThread thread = createValidThread();

        // Act
        thread.flagPotentialHarm();

        // Assert
        assertThat(thread.getSafetyFlag()).isEqualTo(SafetyFlag.NEEDS_MODERATION);
    }

    @Test
    @DisplayName("Should throw exception when replying to a LOCKED thread")
    void shouldThrowExceptionWhenReplyingToLockedThread() {
        // Arrange
        ForumThread thread = createValidThread();
        thread.lock();

        assertThat(thread.getStatus()).isEqualTo(ThreadStatus.LOCKED);

        // Act & Assert
        assertThatThrownBy(() -> thread.addReply("Illegal reply", AnonymousAuthorId.generate()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot add reply to a thread that is LOCKED");
    }

    @Test
    @DisplayName("Should throw exception when replying to an ARCHIVED thread")
    void shouldThrowExceptionWhenReplyingToArchivedThread() {
        // Arrange
        ForumThread thread = createValidThread();
        thread.archive();

        assertThat(thread.getStatus()).isEqualTo(ThreadStatus.ARCHIVED);

        // Act & Assert
        assertThatThrownBy(() -> thread.addReply("Illegal reply", AnonymousAuthorId.generate()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot add reply to a thread that is ARCHIVED");
    }

    @Test
    @DisplayName("Should correctly associate AnonymousAuthorId with the thread (Privacy Check)")
    void shouldCorrectlyAssociateAnonymousAuthorId() {
        // Arrange
        AnonymousAuthorId expectedAuthorId = AnonymousAuthorId.generate();
        ForumThread thread = new ForumThread(
                ThreadId.generate(),
                "Topic",
                "Content",
                expectedAuthorId,
                Instant.now());

        // Act & Assert
        assertThat(thread.getAuthorId()).isEqualTo(expectedAuthorId);
        assertThat(thread.getAuthorId().id()).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception when creating thread with invalid arguments")
    void shouldThrowExceptionForInvalidConstructorArguments() {
        ThreadId validId = ThreadId.generate();
        AnonymousAuthorId validAuthor = AnonymousAuthorId.generate();
        Instant validTime = Instant.now();

        // Null checks
        assertThatThrownBy(() -> new ForumThread(null, "Topic", "Content", validAuthor, validTime))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("ThreadId cannot be null");

        assertThatThrownBy(() -> new ForumThread(validId, null, "Content", validAuthor, validTime))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Topic cannot be null");

        assertThatThrownBy(() -> new ForumThread(validId, "Topic", null, validAuthor, validTime))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Content cannot be null");

        assertThatThrownBy(() -> new ForumThread(validId, "Topic", "Content", null, validTime))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("AuthorId cannot be null");

        assertThatThrownBy(() -> new ForumThread(validId, "Topic", "Content", validAuthor, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Timestamp cannot be null");

        // Empty checks
        assertThatThrownBy(() -> new ForumThread(validId, "", "Content", validAuthor, validTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Topic cannot be empty");

        assertThatThrownBy(() -> new ForumThread(validId, "   ", "Content", validAuthor, validTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Topic cannot be empty");

        assertThatThrownBy(() -> new ForumThread(validId, "Topic", "", validAuthor, validTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Content cannot be empty");

        assertThatThrownBy(() -> new ForumThread(validId, "Topic", "   ", validAuthor, validTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Content cannot be empty");
    }

    // Helper method to create a valid thread
    private ForumThread createValidThread() {
        return new ForumThread(
                ThreadId.generate(),
                "Valid Topic",
                "Valid Content",
                AnonymousAuthorId.generate(),
                Instant.now());
    }
}
