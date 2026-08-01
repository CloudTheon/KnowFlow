package com.cloudtheon.knowflowcore.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 后台用户管理 VO
 */
@Schema(description = "后台用户信息")
public record AdminUserVO(
        @Schema(description = "用户 ID") Long id,
        @Schema(description = "用户名") String username,
        @Schema(description = "角色（admin/user）") String role,
        @Schema(description = "状态（enabled/disabled）") String status,
        @Schema(description = "注册时间") LocalDateTime createdAt) {
}
