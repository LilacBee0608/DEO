package com.bili.demo.interceptor;

import com.bili.demo.common.BusinessException;
import com.bili.demo.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 拦截器
 * 拦截需要登录的接口,从请求头 Authorization 中取 token
 * 校验通过则将 userId / userName 存入 request,供 Controller 使用
 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    public static final String ATTR_USER_ID = "currentUserId";
    public static final String ATTR_USER_NAME = "currentUserName";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 放行 OPTIONS 预检请求(跨域时浏览器会先发 OPTIONS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String header = request.getHeader("Authorization");
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            throw new BusinessException(401, "未登录,请先登录");
        }
        String token = header.substring(7);
        if (!jwtUtils.validateToken(token)) {
            throw new BusinessException(401, "登录已过期,请重新登录");
        }
        // 将解析出的用户信息存入 request,后续 Controller 可直接获取
        request.setAttribute(ATTR_USER_ID, jwtUtils.getUserId(token));
        request.setAttribute(ATTR_USER_NAME, jwtUtils.getUserName(token));
        return true;
    }
}
