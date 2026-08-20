// 认证相关 API
import request from './request'

/** 登录 */
export const login = (data) => request.post('/auth/login', data)

/** 注册 */
export const register = (data) => request.post('/auth/register', data)

/** 获取当前登录用户信息 */
export const getMe = () => request.get('/auth/me')
