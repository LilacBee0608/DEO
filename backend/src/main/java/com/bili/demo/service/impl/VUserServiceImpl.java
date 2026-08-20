package com.bili.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bili.demo.common.BusinessException;
import com.bili.demo.dto.LoginDTO;
import com.bili.demo.dto.LoginVO;
import com.bili.demo.dto.RegisterDTO;
import com.bili.demo.entity.VUser;
import com.bili.demo.mapper.VUserMapper;
import com.bili.demo.service.VUserService;
import com.bili.demo.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 * - 注册: 校验用户名是否已存在,插入新用户
 * - 登录: 校验用户名密码,生成 JWT token
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VUserServiceImpl implements VUserService {

    private final VUserMapper vUserMapper;
    private final JwtUtils jwtUtils;

    @Override
    public Integer register(RegisterDTO dto) {
        // 1. 校验用户名是否已被占用
        VUser exist = vUserMapper.selectOne(
                new LambdaQueryWrapper<VUser>()
                        .eq(VUser::getUserName, dto.getUserName()));
        if (exist != null) {
            throw new BusinessException("用户名已被占用");
        }

        // 2. 构造实体并插入(实训简化:密码明文存储;生产应使用 BCryptPasswordEncoder)
        VUser user = new VUser();
        user.setUserName(dto.getUserName());
        user.setUserPswd(dto.getUserPswd());
        vUserMapper.insert(user);
        log.info("用户注册成功: id={}, name={}", user.getId(), user.getUserName());
        return user.getId();
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        // 1. 按用户名查询
        VUser user = vUserMapper.selectOne(
                new LambdaQueryWrapper<VUser>()
                        .eq(VUser::getUserName, dto.getUserName()));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 2. 校验密码(明文比对)
        if (!dto.getUserPswd().equals(user.getUserPswd())) {
            throw new BusinessException("密码错误");
        }
        // 3. 生成 JWT token
        String token = jwtUtils.generateToken(user.getId(), user.getUserName());
        log.info("用户登录成功: id={}, name={}", user.getId(), user.getUserName());
        return new LoginVO(user.getId(), user.getUserName(), token);
    }
}
