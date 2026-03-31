import axios from 'axios';

const USER_SERVICE_URL = process.env.REACT_APP_USER_SERVICE_URL || 'http://localhost:30081';
const FORUM_SERVICE_URL = process.env.REACT_APP_FORUM_SERVICE_URL || 'http://localhost:30082';

export const userServiceApi = axios.create({
  baseURL: USER_SERVICE_URL,
  headers: { 'Content-Type': 'application/json' },
});

export const forumServiceApi = axios.create({
  baseURL: FORUM_SERVICE_URL,
  headers: { 'Content-Type': 'application/json' },
});

// Add auth token interceptor to both
[userServiceApi, forumServiceApi].forEach(api => {
  api.interceptors.request.use(config => {
    const token = localStorage.getItem('token');
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
  });
});
