package com.bili.demo.dto;

import com.bili.demo.entity.Video;
import lombok.Data;

/**
 * 视频详情返回 VO
 * 在原 Video 基础上扩展:
 *   - authorName: 作者昵称(关联 v_user 查询)
 *   - liked:     当前登录用户是否已点赞
 *   - favorited: 当前登录用户是否已收藏
 */
@Data
public class VideoDetailVO extends Video {
    /** 作者昵称 */
    private String authorName;
    /** 当前用户是否已点赞 */
    private Boolean liked;
    /** 当前用户是否已收藏 */
    private Boolean favorited;
}
