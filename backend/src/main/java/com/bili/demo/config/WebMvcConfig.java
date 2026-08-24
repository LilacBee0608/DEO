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

    @Value("${storage.logo-dir}")
    private String logoDir;

    @Value("${storage.url-prefix}")
    private String urlPrefix;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                // 仅放行: 登录注册接口 + 静态资源访问
                // 注: /videos/detail/** /videos/list /danmu/list/** /comments/list/** 从 exclude 移除
                // 由 JwtInterceptor 内部实现"软解析"——游客不抛错,登录用户回填 userId,
                // 保证视频详情页返回的 liked/favorited/评论点赞态等用户态字段正确。
                .excludePathPatterns(
                        "/auth/**",          // 登录注册接口(全程不需要登录)
                        "/files/**"          // 静态资源(视频/封面/logo)
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
        // Logo 文件 /files/logo/xxx.png -> 物理目录(网站logo资源)
        registry.addResourceHandler(urlPrefix + "/logo/**")
                .addResourceLocations("file:" + logoDir + "/");
    }
}
