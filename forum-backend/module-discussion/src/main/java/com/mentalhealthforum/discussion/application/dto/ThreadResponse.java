package com.mentalhealthforum.discussion.application.dto;

import com.mentalhealthforum.discussion.domain.model.ForumThread;
import com.mentalhealthforum.discussion.domain.model.Post;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ThreadResponse(
        UUID id,
        String title,
        String content,
        UUID authorId,
        String status,
        Instant timestamp,
        List<PostDto> posts) {
    public static ThreadResponse from(ForumThread thread) {
        List<PostDto> postDtos = thread.getPosts().stream()
                .map(PostDto::from)
                .toList();

        return new ThreadResponse(
                thread.getId().id(),
                thread.getTopic(),
                thread.getContent(),
                thread.getAuthorId().id(),
                thread.getStatus().name(),
                thread.getTimestamp(),
                postDtos);
    }

    public record PostDto(
            UUID id,
            String content,
            UUID authorId,
            Instant timestamp) {
        public static PostDto from(Post post) {
            return new PostDto(
                    post.getId().id(),
                    post.getContent(),
                    post.getAuthorId().id(),
                    post.getTimestamp());
        }
    }
}
