package com.cloudtheon.knowflowcore.service;

import com.cloudtheon.knowflowcore.dto.LoginRequest;
import com.cloudtheon.knowflowcore.dto.LoginResponse;
import com.cloudtheon.knowflowcore.dto.RegisterRequest;
import com.cloudtheon.knowflowcore.vo.UserInfoVO;

/**
 * 用户业务接口
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param req 注册请求
     * @return 登录响应（含 Token 和用户信息）
     */
    LoginResponse register(RegisterRequest req);

    /**
     * 用户登录
     *
     * @param req 登录请求
     * @return 登录响应（含 Token 和用户信息）
     */
    LoginResponse login(LoginRequest req);

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    UserInfoVO getUserProfile(String username);
}
