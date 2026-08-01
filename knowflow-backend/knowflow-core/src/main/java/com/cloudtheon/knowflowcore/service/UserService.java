package com.cloudtheon.knowflowcore.service;

import com.cloudtheon.knowflowcore.dto.LoginRequest;
import com.cloudtheon.knowflowcore.dto.LoginResponse;
import com.cloudtheon.knowflowcore.dto.RegisterRequest;
import com.cloudtheon.knowflowcore.dto.UpdatePasswordRequest;
import com.cloudtheon.knowflowcore.dto.UpdateProfileRequest;
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

    /**
     * 更新个人资料（头像）
     *
     * @param userId 当前用户 ID
     * @param req    更新请求
     * @return 更新后的用户信息
     */
    UserInfoVO updateProfile(Long userId, UpdateProfileRequest req);

    /**
     * 修改密码
     *
     * @param userId 当前用户 ID
     * @param req    修改密码请求
     */
    void updatePassword(Long userId, UpdatePasswordRequest req);
}
