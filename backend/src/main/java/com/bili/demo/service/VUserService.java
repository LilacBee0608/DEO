package com.bili.demo.service;

import com.bili.demo.dto.LoginDTO;
import com.bili.demo.dto.LoginVO;
import com.bili.demo.dto.RegisterDTO;

/**
 * 用户服务接口
 */
public interface VUserService {

    /**
     * 用户注册
     * @return 新用户id
     */
    Integer register(RegisterDTO dto);

    /**
     * 用户登录
     * @return 登录返回信息(含 token)
     */
    LoginVO login(LoginDTO dto);
}
