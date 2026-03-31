package com.mentalhealthforum.forumservice.infrastructure.web;

import com.mentalhealthforum.forumservice.application.dto.CommentDto;
import com.mentalhealthforum.forumservice.application.dto.CreateCommentRequest;
import com.mentalhealthforum.forumservice.application.service.ForumApplicationService;
import com.mentalhealthforum.forumservice.infrastructure.security.ForumUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final ForumApplicationService forumService;

    public CommentController(ForumApplicationService forumService) {
        this.forumService = forumService;
    }

    @GetMapping("/thread/{threadId}")
    public ResponseEntity<List<CommentDto>> getCommentsByThread(
            @PathVariable UUID threadId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(forumService.getCommentsByThread(threadId, page, size));
    }

    @PostMapping("/thread/{threadId}")
    public ResponseEntity<CommentDto> addComment(
            @PathVariable UUID threadId,
            @Valid @RequestBody CreateCommentRequest request) {
        ForumUserDetails user = SecurityContextHelper.getCurrentUser();
        CommentDto comment = forumService.addComment(threadId, request, user.getUserId(), user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCommentBody body) {
        String userId = SecurityContextHelper.getCurrentUserId();
        CommentDto comment = forumService.updateComment(id, body.content(), userId);
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID id) {
        String userId = SecurityContextHelper.getCurrentUserId();
        String userRole = SecurityContextHelper.getCurrentUserRole();
        forumService.deleteComment(id, userId, userRole);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/upvote")
    public ResponseEntity<CommentDto> upvoteComment(@PathVariable UUID id) {
        CommentDto comment = forumService.upvoteComment(id);
        return ResponseEntity.ok(comment);
    }

    // -------------------------------------------------------------------------
    // Inner records for request bodies
    // -------------------------------------------------------------------------

    public record UpdateCommentBody(
            @NotBlank(message = "Content is required")
            @Size(min = 1, max = 5000, message = "Content must be between 1 and 5000 characters")
            String content
    ) {}
}
