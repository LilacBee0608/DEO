// 用户相关 API (观看历史 + 收藏夹)
import request from './request'

/** 记录观看历史(需登录,后端自动去重) */
export const recordHistory = (vId) => request.post(`/user/history/${vId}`)

/** 获取观看历史列表(按观看时间倒序) */
export const getHistoryList = () => request.get('/user/history')

/** 清空观看历史 */
export const clearHistory = () => request.delete('/user/history')

/** 获取收藏夹列表(按视频播放量倒序) */
export const getFavoriteList = () => request.get('/user/favorites')
