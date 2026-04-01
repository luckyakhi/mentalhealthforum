import { forumServiceApi } from './config';

export interface ThreadSummary {
  id: string;
  categoryId: string;
  title: string;
  authorDisplayName: string;
  status: string;
  viewCount: number;
  createdAt: string;
  commentCount: number;
}

export interface Thread {
  id: string;
  categoryId: string;
  title: string;
  content: string;
  authorDisplayName: string;
  authorId: string;
  status: string;
  viewCount: number;
  createdAt: string;
  updatedAt: string;
  commentCount: number;
}

export const getAllThreads = (page = 0, size = 20) =>
  forumServiceApi.get<ThreadSummary[]>(`/api/threads?page=${page}&size=${size}`);

export const getThreadsByCategory = (categoryId: string, page = 0, size = 20) =>
  forumServiceApi.get<ThreadSummary[]>(`/api/threads/category/${categoryId}?page=${page}&size=${size}`);

export const searchThreads = (q: string, page = 0, size = 20) =>
  forumServiceApi.get<ThreadSummary[]>(`/api/threads/search?q=${q}&page=${page}&size=${size}`);

export const getThread = (id: string) => forumServiceApi.get<Thread>(`/api/threads/${id}`);

export const createThread = (data: { title: string; content: string; categoryId?: string }) =>
  forumServiceApi.post<Thread>('/api/threads', data);

export const deleteThread = (id: string) => forumServiceApi.delete(`/api/threads/${id}`);
