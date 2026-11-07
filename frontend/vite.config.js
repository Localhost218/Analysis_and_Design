import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Dev: /api được chuyển tới backend (mặc định http://localhost:8080)
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': process.env.VITE_API_TARGET || 'http://localhost:8080',
    },
  },
});
