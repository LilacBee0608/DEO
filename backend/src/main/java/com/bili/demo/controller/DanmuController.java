package com.bili.demo.controller;

import com.bili.demo.common.Result;
import com.bili.demo.dto.DanmuSendDTO;
import com.bili.demo.entity.Danmu;
import com.bili.demo.interceptor.JwtInterceptor;
import com.bili.demo.service.DanmuService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 弹幕 Controller
 * 路径前缀: /danmu
 * 接口列表:
 *   GET  /danmu/list/{vId}    查询视频弹幕(游客可见)
 *   POST /danmu               发送弹幕(需登录)
 */
@RestController
@RequestMapping("/danmu")
@RequiredArgsConstructor
public class DanmuController {

    private final DanmuService danmuService;

    /** 查询视频弹幕(游客可见,WebMvcConfig 已放行) */
    @GetMapping("/list/{vId}")
    public Result<List<Danmu>> list(@PathVariable String vId) {
        return Result.success(danmuService.listByVideo(vId));
    }

    /** 发送弹幕(需登录) */
    @PostMapping
    public Result<Integer> send(@Valid @RequestBody DanmuSendDTO dto, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(JwtInterceptor.ATTR_USER_ID);
        Integer did = danmuService.send(dto, userId);
        return Result.success("发送成功", did);
    }
}
