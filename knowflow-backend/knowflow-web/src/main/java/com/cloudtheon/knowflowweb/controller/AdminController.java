package com.cloudtheon.knowflowweb.controller;

import com.cloudtheon.knowflowcommon.result.ApiResponse;
import com.cloudtheon.knowflowcore.dto.UpdateUserStatusRequest;
import com.cloudtheon.knowflowcore.service.AdminService;
import com.cloudtheon.knowflowcore.vo.AdminUserVO;
import com.cloudtheon.knowflowcore.vo.PageVO;
import com.cloudtheon.knowflowinfrastructure.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统管理后台控制器（仅管理员）
 */
@Tag(name = "06-系统管理", description = "平台数据概览、用户管理、系统配置（仅管理员）")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "平台数据概览", description = "统计用户数/对话数/文档数/反馈数")
    @GetMapping("/overview")
    public ApiResponse<Map<String, Long>> overview(@AuthenticationPrincipal LoginUser loginUser) {
        return ApiResponse.success(adminService.overview(loginUser.getUserId()));
    }

    @Operation(summary = "用户列表", description = "分页查询所有用户，支持用户名模糊搜索")
    @GetMapping("/users")
    public ApiResponse<PageVO<AdminUserVO>> users(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(adminService.listUsers(loginUser.getUserId(), page, pageSize, keyword));
    }

    @Operation(summary = "启用/禁用用户", description = "设置用户状态（enabled/disabled），不能操作自己")
    @PutMapping("/users/{id}/status")
    public ApiResponse<Void> updateUserStatus(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest req) {
        adminService.updateUserStatus(loginUser.getUserId(), id, req.getStatus());
        return ApiResponse.success();
    }

    @Operation(summary = "获取系统配置", description = "读取系统配置（存储于 Redis）")
    @GetMapping("/config")
    public ApiResponse<Map<String, Object>> config(@AuthenticationPrincipal LoginUser loginUser) {
        return ApiResponse.success(adminService.getConfig(loginUser.getUserId()));
    }

    @Operation(summary = "更新系统配置", description = "写入/更新系统配置键值对（存储于 Redis）")
    @PutMapping("/config")
    public ApiResponse<Void> updateConfig(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody Map<String, Object> config) {
        adminService.updateConfig(loginUser.getUserId(), config);
        return ApiResponse.success();
    }
}
