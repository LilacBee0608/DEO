package com.bili.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录成功返回 VO
 * 包含用户基本信息 + JWT token
 * 前端将 token 存入 localStorage,后续请求携带在 Authorization 头中
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {
    /** 用户id */
    private Integer id;
    /** 用户昵称 */
    private String userName;
    /** JWT token */
    private String token;
}
