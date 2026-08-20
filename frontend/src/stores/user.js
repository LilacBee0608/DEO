// 用户状态管理 (Pinia)
// 维护 token 和 用户信息,持久化到 localStorage
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi, getMe } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  // 计算属性: 是否已登录
  const isLoggedIn = computed(() => !!token.value)

  // 持久化工具
  const persist = () => {
    if (token.value) {
      localStorage.setItem('token', token.value)
    } else {
      localStorage.removeItem('token')
    }
    if (userInfo.value) {
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    } else {
      localStorage.removeItem('userInfo')
    }
  }

  // 登录
  const login = async (payload) => {
    const res = await loginApi(payload)
    token.value = res.data.token
    userInfo.value = { id: res.data.id, userName: res.data.userName }
    persist()
    return res
  }

  // 注册
  const register = async (payload) => {
    return await registerApi(payload)
  }

  // 拉取当前用户信息(刷新页面后恢复)
  const fetchMe = async () => {
    if (!token.value) return null
    try {
      const res = await getMe()
      userInfo.value = { id: res.data.id, userName: res.data.userName }
      persist()
      return res.data
    } catch (e) {
      // token 无效则清空
      logout()
      return null
    }
  }

  // 退出登录
  const logout = () => {
    token.value = ''
    userInfo.value = null
    persist()
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    login,
    register,
    fetchMe,
    logout
  }
})
