import { forumServiceApi } from './config';

export interface Comment {
  id: string;
  threadId: string;
  content: string;
  authorDisplayName: string;
  authorId: string;
  parentCommentId: string | null;
  upvotes: number;
  createdAt: string;
  updatedAt: string;
}

export const getComments = (threadId: string, page = 0, size = 50) =>
  forumServiceApi.get<Comment[]>(`/api/comments/thread/${threadId}?page=${page}&size=${size}`);

export const addComment = (threadId: string, data: { content: string; parentCommentId?: string }) =>
  forumServiceApi.post<Comment>(`/api/comments/thread/${threadId}`, data);

export const upvoteComment = (id: string) => forumServiceApi.post<Comment>(`/api/comments/${id}/upvote`);

export const deleteComment = (id: string) => forumServiceApi.delete(`/api/comments/${id}`);
