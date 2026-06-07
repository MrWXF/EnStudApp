import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api/user':       { target: 'http://localhost:8081', changeOrigin: true, rewrite: p => p.replace(/^\/api/, '') },
      '/api/word':       { target: 'http://localhost:8082', changeOrigin: true, rewrite: p => p.replace(/^\/api/, '') },
      '/api/chat':       { target: 'http://localhost:8083', changeOrigin: true, rewrite: p => p.replace(/^\/api/, '') },
      '/api/writing':    { target: 'http://localhost:8084', changeOrigin: true, rewrite: p => p.replace(/^\/api/, '') },
      '/api/translate':  { target: 'http://localhost:8085', changeOrigin: true, rewrite: p => p.replace(/^\/api/, '') },
      '/api/forum':      { target: 'http://localhost:8086', changeOrigin: true, rewrite: p => p.replace(/^\/api/, '') },
    },
  },
})
