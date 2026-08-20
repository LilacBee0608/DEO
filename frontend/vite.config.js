import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// Vite 配置
// 1) 路径别名: @ -> src
// 2) 开发服务器端口: 5173
// 3) 后端代理: /api 转发到 http://localhost:8081,避免跨域
//    注: 后端端口从 8080 改为 8081,以避开 Steam 占用 8080
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    open: true, // 启动后自动打开浏览器
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true
      }
    }
  }
})
