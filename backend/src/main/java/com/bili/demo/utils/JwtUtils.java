package com.bili.demo.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 功能:
 *   1) 生成 token  (登录成功后调用)
 *   2) 解析 token  (从 token 取出 userId、userName)
 *   3) 校验 token  (是否过期、是否合法)
 *
 * 使用 jjwt 0.12.x 新版 API
 */
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey key;

    /** 初始化密钥(将配置中的字符串密钥转为 SecretKey 对象) */
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT
     * @param userId   用户id
     * @param userName 用户昵称(也可放入 token,减少后续查库)
     * @return JWT 字符串
     */
    public String generateToken(Integer userId, String userName) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userName", userName);
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * 解析 token,返回 Claims
     * 如果 token 非法或过期会抛异常
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 从 token 取出 userId */
    public Integer getUserId(String token) {
        Object v = parseToken(token).get("userId");
        // 兼容 Integer / String 类型
        if (v instanceof Number n) return n.intValue();
        return Integer.parseInt(String.valueOf(v));
    }

    /** 从 token 取出 userName */
    public String getUserName(String token) {
        return String.valueOf(parseToken(token).get("userName"));
    }

    /**
     * 校验 token 是否合法且未过期
     * @return true=有效
     */
    public boolean validateToken(String token) {
        try {
            Date expiration = parseToken(token).getExpiration();
            return expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
