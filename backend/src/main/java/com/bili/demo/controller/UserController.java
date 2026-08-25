package com.bili.demo.controller;

import com.bili.demo.common.Result;
import com.bili.demo.interceptor.JwtInterceptor;
import com.bili.demo.mapper.VFavoriteMapper;
import com.bili.demo.service.HistoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户 Controller (用户主页相关)
 * 路径前缀: /user
 * 路径规则与项目统一: /auth 已在 WebMvcConfig 放行, /user 默认走 JwtInterceptor(需登录)
 * 接口列表:
 *   POST /user/history/{vId}  记录观看历史(去重:重复观看只更新时间)
 *   GET  /user/history         查询观看历史列表(按观看时间倒序)
 *   DELETE /user/history       清空观看历史
 *   GET  /user/favorites       查询收藏夹列表(收藏表天然去重,联合主键约束)
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final HistoryService historyService;
    private final VFavoriteMapper vFavoriteMapper;

    /** 记录观看历史(后端保证同用户+同视频不重复,重复观看仅更新 watch_time) */
    @PostMapping("/history/{vId}")
    public Result<Void> recordHistory(@PathVariable String vId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(JwtInterceptor.ATTR_USER_ID);
        historyService.recordWatch(userId, vId);
        return Result.success();
    }

    /** 查询当前用户观看历史列表(含视频封面、标题、UP主名等展示字段) */
    @GetMapping("/history")
    public Result<List<Map<String, Object>>> getHistory(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(JwtInterceptor.ATTR_USER_ID);
        return Result.success(historyService.getHistoryList(userId));
    }

    /** 清空当前用户观看历史 */
    @DeleteMapping("/history")
    public Result<Void> clearHistory(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(JwtInterceptor.ATTR_USER_ID);
        historyService.clearHistory(userId);
        return Result.success();
    }

    /** 查询当前用户收藏夹列表(收藏表本身联合主键已保证不重复) */
    @GetMapping("/favorites")
    public Result<List<Map<String, Object>>> getFavorites(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(JwtInterceptor.ATTR_USER_ID);
        return Result.success(vFavoriteMapper.selectFavoriteList(userId));
    }
}
