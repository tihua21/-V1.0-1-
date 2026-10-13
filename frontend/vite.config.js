import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// Vite 配置
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    open: true,
    // 开发环境代理：前端 /api 请求转发到后端 8080
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
