import { userServiceApi } from './config';

export interface UserProfile {
  id: string;
  username: string;
  email: string;
  role: string;
  bio: string;
  avatarUrl: string;
  createdAt: string;
}

export const getMe = () => userServiceApi.get<UserProfile>('/api/users/me');
export const getProfile = (userId: string) => userServiceApi.get<UserProfile>(`/api/users/${userId}/profile`);
export const updateProfile = (data: { username?: string; bio?: string; avatarUrl?: string }) =>
  userServiceApi.put<UserProfile>('/api/users/me', data);
