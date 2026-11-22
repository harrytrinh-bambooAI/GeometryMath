import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';

/**
 * Cổng lấy từ biến môi trường (app/scripts/env.sh, app/.env.ports):
 * - GEO_WEB_PORT: cổng Vite (dev và preview), mặc định 5173.
 * - GEO_PORT: cổng máy chủ Java mà proxy trỏ tới, mặc định 8080.
 * Trình duyệt chỉ nói chuyện với một nguồn (Vite); Vite chuyển /api, /health, /dev sang máy chủ,
 * nên không cần CORS. Bản build được phục vụ tĩnh (xem app/docs/STACK.md, mục Giao diện).
 */
const webPort = Number(process.env.GEO_WEB_PORT ?? 5173);
const apiTarget = `http://127.0.0.1:${process.env.GEO_PORT ?? 8080}`;
const proxy = {
  '/api': { target: apiTarget, changeOrigin: false },
  '/health': { target: apiTarget, changeOrigin: false },
  '/dev': { target: apiTarget, changeOrigin: false },
};

export default defineConfig({
  plugins: [react()],
  server: { host: '127.0.0.1', port: webPort, strictPort: true, proxy },
  preview: { host: '127.0.0.1', port: webPort, strictPort: true, proxy },
  build: { outDir: 'dist', sourcemap: true },
});
