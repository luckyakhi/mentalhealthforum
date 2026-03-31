package com.mentalhealthforum.forumservice.infrastructure.web;

import com.mentalhealthforum.forumservice.application.dto.CreateThreadRequest;
import com.mentalhealthforum.forumservice.application.dto.ThreadDto;
import com.mentalhealthforum.forumservice.application.dto.ThreadSummaryDto;
import com.mentalhealthforum.forumservice.application.dto.UpdateThreadRequest;
import com.mentalhealthforum.forumservice.application.service.ForumApplicationService;
import com.mentalhealthforum.forumservice.infrastructure.security.ForumUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/threads")
public class ThreadController {

    private final ForumApplicationService forumService;

    public ThreadController(ForumApplicationService forumService) {
        this.forumService = forumService;
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ThreadSummaryDto>> getThreadsByCategory(
            @PathVariable UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(forumService.getThreadsByCategory(categoryId, page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ThreadSummaryDto>> searchThreads(
            @RequestParam("q") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(forumService.searchThreads(keyword, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ThreadDto> getThread(@PathVariable UUID id) {
        return ResponseEntity.ok(forumService.getThread(id));
    }

    @PostMapping
    public ResponseEntity<ThreadDto> createThread(@Valid @RequestBody CreateThreadRequest request) {
        ForumUserDetails user = SecurityContextHelper.getCurrentUser();
        ThreadDto thread = forumService.createThread(request, user.getUserId(), user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(thread);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ThreadDto> updateThread(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateThreadRequest request) {
        String userId = SecurityContextHelper.getCurrentUserId();
        ThreadDto thread = forumService.updateThread(id, request, userId);
        return ResponseEntity.ok(thread);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteThread(@PathVariable UUID id) {
        String userId = SecurityContextHelper.getCurrentUserId();
        String userRole = SecurityContextHelper.getCurrentUserRole();
        forumService.deleteThread(id, userId, userRole);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/pin")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Void> pinThread(@PathVariable UUID id) {
        forumService.pinThread(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/lock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Void> lockThread(@PathVariable UUID id) {
        forumService.lockThread(id);
        return ResponseEntity.ok().build();
    }
}
