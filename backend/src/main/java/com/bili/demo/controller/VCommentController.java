package com.bili.demo.controller;

import com.bili.demo.common.Result;
import com.bili.demo.dto.CommentSendDTO;
import com.bili.demo.dto.CommentVO;
import com.bili.demo.interceptor.JwtInterceptor;
import com.bili.demo.service.VCommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论 Controller
 * 路径前缀: /comments
 * 接口列表:
 *   GET  /comments/list/{vId}    查询视频评论(游客可见)
 *   POST /comments               发送评论(需登录)
 *   POST /comments/like/{cid}    评论点赞(需登录)
 */
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class VCommentController {

    private final VCommentService vCommentService;

    /** 查询视频评论(游客可见) */
    @GetMapping("/list/{vId}")
    public Result<List<CommentVO>> list(@PathVariable String vId) {
        return Result.success(vCommentService.listByVideo(vId));
    }

    /** 发送评论(需登录) */
    @PostMapping
    public Result<Integer> send(@Valid @RequestBody CommentSendDTO dto, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(JwtInterceptor.ATTR_USER_ID);
        Integer cid = vCommentService.send(dto, userId);
        return Result.success("发送成功", cid);
    }

    /** 评论点赞(需登录) */
    @PostMapping("/like/{cid}")
    public Result<Void> like(@PathVariable Integer cid) {
        vCommentService.like(cid);
        return Result.success();
    }
}
