package com.bili.demo.config;

import com.bili.demo.interceptor.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 1) 注册 JWT 拦截器,指定拦截路径与放行路径
 * 2) 注册静态资源映射,让上传的视频/封面可通过 URL 访问
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    @Value("${storage.video-dir}")
    private String videoDir;

    @Value("${storage.cover-dir}")
    private String coverDir;

    @Value("${storage.url-prefix}")
    private String urlPrefix;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                // 放行: 登录注册、静态资源、首页视频列表、视频详情、弹幕查询、静态文件
                .excludePathPatterns(
                        "/auth/**",          // 登录注册接口
                        "/files/**",         // 静态资源
                        "/videos/list",      // 视频列表(游客可看)
                        "/videos/detail/**", // 视频详情(游客可看)
                        "/danmu/list/**",    // 弹幕列表(游客可看)
                        "/comments/list/**"  // 评论列表(游客可看)
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 视频文件 /files/videos/xxx.mp4 -> 物理目录
        registry.addResourceHandler(urlPrefix + "/videos/**")
                .addResourceLocations("file:" + videoDir + "/");
        // 封面文件 /files/covers/xxx.jpg -> 物理目录
        registry.addResourceHandler(urlPrefix + "/covers/**")
                .addResourceLocations("file:" + coverDir + "/");
    }
}
