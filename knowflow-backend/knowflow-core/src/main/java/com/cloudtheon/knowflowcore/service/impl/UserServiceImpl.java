package com.cloudtheon.knowflowcore.service.impl;

import com.cloudtheon.knowflowcommon.exception.BusinessException;
import com.cloudtheon.knowflowcommon.result.ResultCode;
import com.cloudtheon.knowflowcore.dto.LoginRequest;
import com.cloudtheon.knowflowcore.dto.LoginResponse;
import com.cloudtheon.knowflowcore.dto.RegisterRequest;
import com.cloudtheon.knowflowcore.service.UserService;
import com.cloudtheon.knowflowcore.vo.UserInfoVO;
import com.cloudtheon.knowflowinfrastructure.entity.User;
import com.cloudtheon.knowflowinfrastructure.mapper.UserMapper;
import com.cloudtheon.knowflowinfrastructure.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户业务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Override
    public LoginResponse register(RegisterRequest req) {
        // 校验用户名是否已存在
        User existing = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, req.getUsername()));
        if (existing != null) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        // 创建新用户
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));

        userMapper.insert(user);

        // 生成 Token
        String token = jwtProvider.generateToken(user.getId(), user.getUsername());
        UserInfoVO userInfo = new UserInfoVO(user.getId(), user.getUsername(), user.getAvatar());

        return new LoginResponse(token, userInfo);
    }

    @Override
    public LoginResponse login(LoginRequest req) {
        // 查询用户
        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, req.getUsername()));
        if (user == null) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }

        // 校验密码
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }

        // 生成 Token
        String token = jwtProvider.generateToken(user.getId(), user.getUsername());
        UserInfoVO userInfo = new UserInfoVO(user.getId(), user.getUsername(), user.getAvatar());

        return new LoginResponse(token, userInfo);
    }

    @Override
    public UserInfoVO getUserProfile(String username) {
        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return new UserInfoVO(user.getId(), user.getUsername(), user.getAvatar());
    }
}
