package com.bili.demo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bili.demo.dto.VideoDetailVO;
import com.bili.demo.dto.VideoQueryDTO;
import com.bili.demo.dto.VideoUploadDTO;
import com.bili.demo.entity.Video;

/**
 * 视频服务接口
 */
public interface VideoService {

    /** 分页查询视频(游客可见) */
    IPage<Video> queryPage(VideoQueryDTO dto);

    /** 视频详情(包含作者昵称、当前用户是否点赞/收藏) */
    VideoDetailVO detail(String vId, Integer currentUserId);

    /** 上传/创建视频(需登录) */
    String create(VideoUploadDTO dto, Integer userId);

    /** 修改视频(需登录,作者本人) */
    void update(String vId, VideoUploadDTO dto, Integer userId);

    /** 删除视频(需登录,作者本人) */
    void delete(String vId, Integer userId);

    /** 增加播放量(每次播放+1) */
    void incrPlay(String vId);

    /** 点赞/取消点赞(切换状态,需登录) */
    boolean toggleLike(String vId, Integer userId);

    /** 收藏/取消收藏(切换状态,需登录) */
    boolean toggleFavorite(String vId, Integer userId);
}
