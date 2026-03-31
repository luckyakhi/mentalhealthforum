import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      // Dev-time proxy: forward /api calls to user-service
      // In production, API URLs are baked in at build time via VITE_ env vars
      '/api': {
        target: 'http://localhost:30081',
        changeOrigin: true,
      },
    },
  },
});
