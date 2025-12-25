package com.mentalhealthforum.discussion.infrastructure.web;

import com.mentalhealthforum.discussion.application.dto.AddReplyRequest;
import com.mentalhealthforum.discussion.application.dto.CreateThreadRequest;
import com.mentalhealthforum.discussion.application.dto.ThreadResponse;
import com.mentalhealthforum.discussion.application.service.ForumApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/threads")
public class ForumController {

    private final ForumApplicationService forumService;

    public ForumController(ForumApplicationService forumService) {
        this.forumService = forumService;
    }

    @PostMapping
    public ResponseEntity<ThreadResponse> createThread(
            @RequestBody CreateThreadRequest request,
            @AuthenticationPrincipal Principal principal) {
        // Simulate extraction of UUID from Principal (assuming getName() returns the
        // UUID string)
        UUID authorId = UUID.fromString(principal.getName());

        ThreadResponse response = forumService.createThread(
                request.title(),
                request.content(),
                authorId);

        return ResponseEntity
                .created(URI.create("/api/threads/" + response.id()))
                .body(response);
    }

    @PostMapping("/{threadId}/reply")
    public ResponseEntity<Void> addReply(
            @PathVariable String threadId,
            @RequestBody AddReplyRequest request,
            @AuthenticationPrincipal Principal principal) {
        UUID authorId = UUID.fromString(principal.getName());

        forumService.addReplyToThread(
                threadId,
                request.content(),
                authorId);

        return ResponseEntity
                .created(URI.create("/api/threads/" + threadId)) // Location of the thread (or could be specific post if
                                                                 // practical)
                .build();
    }
}
