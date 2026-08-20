package com.bili.demo.service;

import com.bili.demo.dto.CommentSendDTO;
import com.bili.demo.dto.CommentVO;

import java.util.List;

/**
 * 评论服务接口
 */
public interface VCommentService {

    /** 查询视频全部评论(带评论者昵称,游客可见) */
    List<CommentVO> listByVideo(String vId);

    /** 发送评论(需登录) */
    Integer send(CommentSendDTO dto, Integer userId);

    /** 评论点赞(需登录,简单计数,不做去重) */
    void like(Integer cid);
}
