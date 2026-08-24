package com.bili.demo.interceptor;

import com.bili.demo.common.BusinessException;
import com.bili.demo.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 拦截器
 * 行为分两级(核心改动):
 *   1) 游客可访问路径 (GUEST_ALLOWED):
 *      - 没 token: 正常放行(不注入 userId,Controller 会当成游客)
 *      - 有 token 且有效: 正常解析并注入 userId,使登录用户访问"游客接口"也能拿到自己的 liked/favorited 等用户态
 *      - 有 token 但无效: 静默放行(等价于游客,避免过期 token 影响浏览)
 *   2) 必须登录路径(其余全部):
 *      - 无 token 或 token 无效: 抛 401
 *
 * 这样修改解决的问题:
 *   之前 /videos/detail/** 被完全 exclude,导致登录用户请求该接口时 JwtInterceptor 根本不执行,
 *   userId 永远为 null,detail 返回 liked/favorited 恒为 false,刷新页面收藏/点赞按钮恢复未激活。
 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    public static final String ATTR_USER_ID = "currentUserId";
    public static final String ATTR_USER_NAME = "currentUserName";

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * 游客可访问的"读操作"路径:
     *  - 游客直接看(无需登录)
     *  - 登录用户带上 token 时,拦截器会正常把 userId 注入 request,供后端返回用户态字段(收藏/点赞)
     */
    private static final String[] GUEST_ALLOWED_PATHS = {
            "/videos/list",
            "/videos/detail/**",
            "/danmu/list/**",
            "/comments/list/**"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 放行 OPTIONS 预检请求(跨域时浏览器会先发 OPTIONS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String requestPath = request.getRequestURI().substring(request.getContextPath().length());
        boolean guestAllowed = false;
        for (String pattern : GUEST_ALLOWED_PATHS) {
            if (PATH_MATCHER.match(pattern, requestPath)) {
                guestAllowed = true;
                break;
            }
        }

        String header = request.getHeader("Authorization");
        boolean hasBearer = StringUtils.hasText(header) && header.startsWith("Bearer ");

        // ========= 情况 A: 没有 token =========
        if (!hasBearer) {
            if (guestAllowed) {
                // 游客可访问路径,放行(不注入 userId,Controller 按游客处理)
                return true;
            }
            // 必须登录的路径,抛 401
            throw new BusinessException(401, "未登录,请先登录");
        }

        // ========= 情况 B: 有 token,尝试解析 =========
        String token = header.substring(7);
        if (!jwtUtils.validateToken(token)) {
            if (guestAllowed) {
                // token 无效但路径允许游客: 静默放行(等价游客,避免过期 token 破坏浏览)
                return true;
            }
            throw new BusinessException(401, "登录已过期,请重新登录");
        }

        // ========= 情况 C: token 有效,注入用户信息 =========
        // 无论该路径是 guest-allowed 还是 must-login,都可安全使用
        Integer uid = jwtUtils.getUserId(token);
        String uname = jwtUtils.getUserName(token);
        request.setAttribute(ATTR_USER_ID, uid);
        request.setAttribute(ATTR_USER_NAME, uname);
        return true;
    }
}
