package com.mentalhealthforum.forumservice.application.service;

import com.mentalhealthforum.forumservice.application.dto.*;
import com.mentalhealthforum.forumservice.domain.model.*;
import com.mentalhealthforum.forumservice.domain.repository.*;
import com.mentalhealthforum.forumservice.infrastructure.exception.AccessDeniedException;
import com.mentalhealthforum.forumservice.infrastructure.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ForumApplicationService {

    private static final String ANONYMOUS_SALT = "mhf-forum-2024";

    private final ThreadRepository threadRepository;
    private final CommentRepository commentRepository;
    private final CategoryRepository categoryRepository;

    public ForumApplicationService(ThreadRepository threadRepository,
                                   CommentRepository commentRepository,
                                   CategoryRepository categoryRepository) {
        this.threadRepository = threadRepository;
        this.commentRepository = commentRepository;
        this.categoryRepository = categoryRepository;
    }

    // =========================================================================
    // Categories
    // =========================================================================

    public CategoryDto createCategory(CreateCategoryRequest req) {
        if (categoryRepository.existsByName(req.name())) {
            throw new IllegalArgumentException("Category with name '" + req.name() + "' already exists");
        }
        Category category = Category.create(req.name(), req.description(), req.displayOrder());
        Category saved = categoryRepository.save(category);
        return toCategoryDto(saved);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAllOrderedByDisplayOrder()
                .stream()
                .map(this::toCategoryDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategory(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        return toCategoryDto(category);
    }

    public void deleteCategory(UUID id) {
        categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        categoryRepository.delete(id);
    }

    // =========================================================================
    // Threads
    // =========================================================================

    public ThreadDto createThread(CreateThreadRequest req, String userId, String username) {
        categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", req.categoryId()));

        AnonymousAuthorId authorId = AnonymousAuthorId.from(userId, ANONYMOUS_SALT);
        ForumThread thread = ForumThread.create(req.categoryId(), req.title(), req.content(),
                authorId, username);
        ForumThread saved = threadRepository.save(thread);

        // Increment category thread count
        categoryRepository.findById(req.categoryId()).ifPresent(cat -> {
            cat.incrementThreadCount();
            categoryRepository.save(cat);
        });

        long commentCount = commentRepository.countByThreadId(saved.getId());
        return toThreadDto(saved, commentCount);
    }

    public ThreadDto getThread(UUID threadId) {
        ForumThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Thread", threadId));
        thread.incrementViewCount();
        ForumThread saved = threadRepository.save(thread);
        long commentCount = commentRepository.countByThreadId(threadId);
        return toThreadDto(saved, commentCount);
    }

    @Transactional(readOnly = true)
    public List<ThreadSummaryDto> getThreadsByCategory(UUID categoryId, int page, int size) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
        Pageable pageable = PageRequest.of(page, size);
        Page<ForumThread> threads = threadRepository.findByCategoryId(categoryId, pageable);
        return threads.getContent().stream()
                .map(t -> toThreadSummaryDto(t, commentRepository.countByThreadId(t.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ThreadSummaryDto> searchThreads(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ForumThread> threads = threadRepository.search(keyword, pageable);
        return threads.getContent().stream()
                .map(t -> toThreadSummaryDto(t, commentRepository.countByThreadId(t.getId())))
                .toList();
    }

    public ThreadDto updateThread(UUID threadId, UpdateThreadRequest req, String userId) {
        ForumThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Thread", threadId));

        verifyThreadOwnership(userId, thread);

        thread.updateContent(req.title(), req.content());
        ForumThread saved = threadRepository.save(thread);
        long commentCount = commentRepository.countByThreadId(threadId);
        return toThreadDto(saved, commentCount);
    }

    public void deleteThread(UUID threadId, String userId, String userRole) {
        ForumThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Thread", threadId));

        boolean isAdminOrMod = "ADMIN".equals(userRole) || "MODERATOR".equals(userRole);
        if (!isAdminOrMod) {
            verifyThreadOwnership(userId, thread);
        }

        // Decrement category thread count
        categoryRepository.findById(thread.getCategoryId()).ifPresent(cat -> {
            cat.decrementThreadCount();
            categoryRepository.save(cat);
        });

        threadRepository.delete(threadId);
    }

    public void pinThread(UUID threadId) {
        ForumThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Thread", threadId));
        thread.pin();
        threadRepository.save(thread);
    }

    public void lockThread(UUID threadId) {
        ForumThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Thread", threadId));
        thread.lock();
        threadRepository.save(thread);
    }

    // =========================================================================
    // Comments
    // =========================================================================

    public CommentDto addComment(UUID threadId, CreateCommentRequest req,
                                 String userId, String username) {
        threadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Thread", threadId));

        AnonymousAuthorId authorId = AnonymousAuthorId.from(userId, ANONYMOUS_SALT);
        Comment comment = Comment.create(threadId, req.content(), authorId, username,
                req.parentCommentId());
        Comment saved = commentRepository.save(comment);
        return toCommentDto(saved);
    }

    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByThread(UUID threadId, int page, int size) {
        threadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Thread", threadId));
        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = commentRepository.findByThreadId(threadId, pageable);
        return comments.getContent().stream()
                .map(this::toCommentDto)
                .toList();
    }

    public CommentDto updateComment(UUID commentId, String content, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));

        verifyCommentOwnership(userId, comment);

        comment.updateContent(content);
        Comment saved = commentRepository.save(comment);
        return toCommentDto(saved);
    }

    public void deleteComment(UUID commentId, String userId, String userRole) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));

        boolean isAdminOrMod = "ADMIN".equals(userRole) || "MODERATOR".equals(userRole);
        if (!isAdminOrMod) {
            verifyCommentOwnership(userId, comment);
        }

        commentRepository.delete(commentId);
    }

    public CommentDto upvoteComment(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));
        comment.upvote();
        Comment saved = commentRepository.save(comment);
        return toCommentDto(saved);
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private void verifyThreadOwnership(String userId, ForumThread thread) {
        AnonymousAuthorId expectedAuthorId = AnonymousAuthorId.from(userId, ANONYMOUS_SALT);
        if (!expectedAuthorId.value().equals(thread.getAuthorId().value())) {
            throw new AccessDeniedException("You do not have permission to modify this thread");
        }
    }

    private void verifyCommentOwnership(String userId, Comment comment) {
        AnonymousAuthorId expectedAuthorId = AnonymousAuthorId.from(userId, ANONYMOUS_SALT);
        if (!expectedAuthorId.value().equals(comment.getAuthorId().value())) {
            throw new AccessDeniedException("You do not have permission to modify this comment");
        }
    }

    private CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getDisplayOrder(),
                category.getThreadCount()
        );
    }

    private ThreadDto toThreadDto(ForumThread thread, long commentCount) {
        return new ThreadDto(
                thread.getId(),
                thread.getCategoryId(),
                thread.getTitle(),
                thread.getContent(),
                thread.getAuthorDisplayName(),
                thread.getAuthorId().value(),
                thread.getStatus(),
                thread.getViewCount(),
                thread.getCreatedAt(),
                thread.getUpdatedAt(),
                commentCount
        );
    }

    private ThreadSummaryDto toThreadSummaryDto(ForumThread thread, long commentCount) {
        return new ThreadSummaryDto(
                thread.getId(),
                thread.getCategoryId(),
                thread.getTitle(),
                thread.getAuthorDisplayName(),
                thread.getStatus(),
                thread.getViewCount(),
                thread.getCreatedAt(),
                commentCount
        );
    }

    private CommentDto toCommentDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getThreadId(),
                comment.getContent(),
                comment.getAuthorDisplayName(),
                comment.getAuthorId().value(),
                comment.getParentCommentId(),
                comment.getUpvotes(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
