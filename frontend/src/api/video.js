// 视频相关 API
import request from './request'

/** 视频分页查询(支持 title/tags 模糊) */
export const getVideoList = (params) => request.get('/videos/list', { params })

/** 视频详情 */
export const getVideoDetail = (vId) => request.get(`/videos/detail/${vId}`)

/** 上传视频(创建记录) */
export const createVideo = (data) => request.post('/videos', data)

/** 修改视频 */
export const updateVideo = (vId, data) => request.put(`/videos/${vId}`, data)

/** 删除视频 */
export const deleteVideo = (vId) => request.delete(`/videos/${vId}`)

/** 增加播放量 */
export const incrPlay = (vId) => request.post(`/videos/play/${vId}`)

/** 点赞/取消点赞(切换) */
export const toggleLike = (vId) => request.post(`/videos/like/${vId}`)

/** 收藏/取消收藏(切换) */
export const toggleFavorite = (vId) => request.post(`/videos/favorite/${vId}`)
