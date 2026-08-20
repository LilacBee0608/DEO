// 弹幕相关 API
import request from './request'

/** 查询视频弹幕列表 */
export const getDanmuList = (vId) => request.get(`/danmu/list/${vId}`)

/** 发送弹幕 */
export const sendDanmu = (data) => request.post('/danmu', data)
