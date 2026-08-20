// Axios 实例封装
// 功能:
//   1) baseURL: /api (由 vite 代理转发到后端 http://localhost:8080/api)
//   2) 请求拦截: 自动在请求头携带 JWT token
//   3) 响应拦截: 统一处理后端返回的 Result 结构,自动抛出错误
//   4) 401 自动跳转登录
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截: 添加 Authorization 头
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截: 解包 Result,错误统一弹窗
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 后端统一返回结构: { code, message, data }
    if (res.code === 200) {
      return res
    } else {
      ElMessage.error(res.message || '请求失败')
      // 401: 未登录或 token 过期
      if (res.code === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  (error) => {
    // HTTP 层错误(网络、超时、500等)
    let msg = '网络错误'
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        msg = '登录已过期,请重新登录'
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
      } else if (status === 403) {
        msg = '没有权限'
      } else if (status === 500) {
        msg = '服务器异常'
      } else {
        msg = error.response.data?.message || `请求失败(${status})`
      }
    } else if (error.message?.includes('timeout')) {
      msg = '请求超时'
    }
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default service
