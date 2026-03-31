import { forumServiceApi } from './config';

export interface Category {
  id: string;
  name: string;
  description: string;
  displayOrder: number;
  threadCount: number;
}

export const getCategories = () => forumServiceApi.get<Category[]>('/api/categories');
export const getCategory = (id: string) => forumServiceApi.get<Category>(`/api/categories/${id}`);
