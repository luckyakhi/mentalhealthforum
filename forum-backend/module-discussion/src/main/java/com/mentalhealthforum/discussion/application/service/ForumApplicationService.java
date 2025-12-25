package com.mentalhealthforum.discussion.application.service;

import com.mentalhealthforum.discussion.application.dto.ThreadResponse;
import com.mentalhealthforum.discussion.domain.exception.EntityNotFoundException;
import com.mentalhealthforum.discussion.domain.model.AnonymousAuthorId;
import com.mentalhealthforum.discussion.domain.model.ForumThread;
import com.mentalhealthforum.discussion.domain.model.ThreadId;
import com.mentalhealthforum.discussion.domain.repository.ThreadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class ForumApplicationService {

    private final ThreadRepository threadRepository;

    public ForumApplicationService(ThreadRepository threadRepository) {
        this.threadRepository = threadRepository;
    }

    public ThreadResponse createThread(String title, String content, UUID authorId) {
        ThreadId id = ThreadId.generate();
        AnonymousAuthorId author = new AnonymousAuthorId(authorId);
        Instant now = Instant.now();

        ForumThread thread = new ForumThread(id, title, content, author, now);
        threadRepository.save(thread);

        return ThreadResponse.from(thread);
    }

    public void addReplyToThread(String threadIdStr, String content, UUID authorId) {
        ThreadId threadId = new ThreadId(UUID.fromString(threadIdStr));
        AnonymousAuthorId replyAuthor = new AnonymousAuthorId(authorId);

        ForumThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new EntityNotFoundException("Thread not found with ID: " + threadIdStr));

        thread.addReply(content, replyAuthor);
        threadRepository.save(thread);
    }
}
