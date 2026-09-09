import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      // 前端 /api/** 请求代理到 Spring Boot 后端（1200 端口）
      '/api': {
        target: 'http://127.0.0.1:1200',
        changeOrigin: true
      }
    }
  }
})
