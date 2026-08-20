package com.bili.demo.controller;

import com.bili.demo.common.Result;
import com.bili.demo.dto.LoginDTO;
import com.bili.demo.dto.LoginVO;
import com.bili.demo.dto.RegisterDTO;
import com.bili.demo.entity.VUser;
import com.bili.demo.interceptor.JwtInterceptor;
import com.bili.demo.mapper.VUserMapper;
import com.bili.demo.service.VUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证 Controller
 * 路径前缀: /auth
 * 对外接口:
 *   POST /auth/register   注册
 *   POST /auth/login      登录
 *   GET  /auth/me         获取当前登录用户(需登录)
 *
 * 注: /auth/** 已在 WebMvcConfig 中放行(无需 token)
 *     仅 /auth/me 需要登录,这里通过手动取 request 中的属性实现
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final VUserService vUserService;
    private final VUserMapper vUserMapper;

    /** 注册 */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterDTO dto) {
        Integer id = vUserService.register(dto);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return Result.success("注册成功", data);
    }

    /** 登录 */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(vUserService.login(dto));
    }

    /**
     * 获取当前登录用户信息
     * 该接口需要 token(JwtInterceptor 拦截后会把 userId 放入 request 属性)
     * 注意: /auth/** 已被放行,所以这里通过手动判断 request 中是否有 userId
     */
    @GetMapping("/me")
    public Result<VUser> me(HttpServletRequest request) {
        Object userId = request.getAttribute(JwtInterceptor.ATTR_USER_ID);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        VUser user = vUserMapper.selectById((Integer) userId);
        // 脱敏:不返回密码
        if (user != null) {
            user.setUserPswd(null);
        }
        return Result.success(user);
    }
}
