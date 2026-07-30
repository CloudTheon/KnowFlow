package com.cloudtheon.knowflowweb.controller;

import com.cloudtheon.knowflowcommon.result.ApiResponse;
import com.cloudtheon.knowflowcore.dto.LoginRequest;
import com.cloudtheon.knowflowcore.dto.LoginResponse;
import com.cloudtheon.knowflowcore.dto.RegisterRequest;
import com.cloudtheon.knowflowcore.service.UserService;
import com.cloudtheon.knowflowcore.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 用户认证控制器
 */
@Tag(name = "01-用户认证", description = "用户注册、登录、个人信息管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "用户注册", description = "创建新用户，注册成功后自动返回 JWT Token")
    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest req) {
        LoginResponse response = userService.register(req);
        return ApiResponse.success(response);
    }

    @Operation(summary = "用户登录", description = "使用用户名和密码登录，返回 JWT Token 和用户信息")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        LoginResponse response = userService.login(req);
        return ApiResponse.success(response);
    }

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的基本信息，需携带有效的 JWT Token")
    @GetMapping("/profile")
    public ApiResponse<UserInfoVO> profile(@AuthenticationPrincipal UserDetails userDetails) {
        UserInfoVO userInfo = userService.getUserProfile(userDetails.getUsername());
        return ApiResponse.success(userInfo);
    }
}
