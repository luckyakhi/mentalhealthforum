import { userServiceApi } from './config';

export interface AuthResponse {
  token: string;
  userId: string;
  username: string;
  role: string;
}

export const register = (data: { username: string; email: string; password: string }) =>
  userServiceApi.post<AuthResponse>('/api/auth/register', data);

export const login = (data: { email: string; password: string }) =>
  userServiceApi.post<AuthResponse>('/api/auth/login', data);
