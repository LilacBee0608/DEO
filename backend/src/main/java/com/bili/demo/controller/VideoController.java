package com.bili.demo.controller;

import com.bili.demo.common.Result;
import com.bili.demo.dto.VideoDetailVO;
import com.bili.demo.dto.VideoQueryDTO;
import com.bili.demo.dto.VideoUploadDTO;
import com.bili.demo.entity.Video;
import com.bili.demo.interceptor.JwtInterceptor;
import com.bili.demo.service.VideoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 视频 Controller
 * 路径前缀: /videos
 * 接口列表:
 *   GET  /videos/list              分页查询(游客可见)
 *   GET  /videos/detail/{vId}      视频详情(游客可见,登录后返回点赞/收藏状态)
 *   POST /videos                   上传视频(需登录)
 *   PUT  /videos/{vId}             修改视频(需登录,作者本人)
 *   DELETE /videos/{vId}           删除视频(需登录,作者本人)
 *   POST /videos/play/{vId}        增加播放量
 *   POST /videos/like/{vId}        点赞/取消点赞(需登录)
 *   POST /videos/favorite/{vId}    收藏/取消收藏(需登录)
 */
@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    /** 分页查询视频列表(游客可见) */
    @GetMapping("/list")
    public Result<Map<String, Object>> list(VideoQueryDTO dto) {
        var page = videoService.queryPage(dto);
        Map<String, Object> data = new HashMap<>();
        data.put("records", page.getRecords());
        data.put("total", page.getTotal());
        data.put("page", page.getCurrent());
        data.put("size", page.getSize());
        return Result.success(data);
    }

    /** 视频详情(游客可见,登录后返回点赞/收藏状态) */
    @GetMapping("/detail/{vId}")
    public Result<VideoDetailVO> detail(@PathVariable String vId, HttpServletRequest request) {
        // 未登录时 currentUserId 为 null(游客场景)
        Integer userId = (Integer) request.getAttribute(JwtInterceptor.ATTR_USER_ID);
        return Result.success(videoService.detail(vId, userId));
    }

    /** 上传视频(需登录) */
    @PostMapping
    public Result<Map<String, String>> create(@Valid @RequestBody VideoUploadDTO dto,
                                              HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(JwtInterceptor.ATTR_USER_ID);
        String vId = videoService.create(dto, userId);
        Map<String, String> data = new HashMap<>();
        data.put("vId", vId);
        return Result.success("上传成功", data);
    }

    /** 修改视频(需登录,作者本人) */
    @PutMapping("/{vId}")
    public Result<Void> update(@PathVariable String vId,
                               @Valid @RequestBody VideoUploadDTO dto,
                               HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(JwtInterceptor.ATTR_USER_ID);
        videoService.update(vId, dto, userId);
        return Result.success();
    }

    /** 删除视频(需登录,作者本人) */
    @DeleteMapping("/{vId}")
    public Result<Void> delete(@PathVariable String vId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(JwtInterceptor.ATTR_USER_ID);
        videoService.delete(vId, userId);
        return Result.success();
    }

    /** 增加播放量 */
    @PostMapping("/play/{vId}")
    public Result<Void> play(@PathVariable String vId) {
        videoService.incrPlay(vId);
        return Result.success();
    }

    /** 点赞/取消点赞(切换状态) */
    @PostMapping("/like/{vId}")
    public Result<Map<String, Boolean>> like(@PathVariable String vId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(JwtInterceptor.ATTR_USER_ID);
        boolean liked = videoService.toggleLike(vId, userId);
        Map<String, Boolean> data = new HashMap<>();
        data.put("liked", liked);
        return Result.success(data);
    }

    /** 收藏/取消收藏(切换状态) */
    @PostMapping("/favorite/{vId}")
    public Result<Map<String, Boolean>> favorite(@PathVariable String vId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(JwtInterceptor.ATTR_USER_ID);
        boolean favorited = videoService.toggleFavorite(vId, userId);
        Map<String, Boolean> data = new HashMap<>();
        data.put("favorited", favorited);
        return Result.success(data);
    }
}
