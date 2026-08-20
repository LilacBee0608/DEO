// 评论相关 API
import request from './request'

/** 查询视频评论列表 */
export const getCommentList = (vId) => request.get(`/comments/list/${vId}`)

/** 发送评论 */
export const sendComment = (data) => request.post('/comments', data)

/** 评论点赞 */
export const likeComment = (cid) => request.post(`/comments/like/${cid}`)
