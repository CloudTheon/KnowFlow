package com.cloudtheon.knowflowcore.dto;

import com.cloudtheon.knowflowcore.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录/注册响应 DTO
 */
@Data
@AllArgsConstructor
@Schema(description = "登录/注册响应数据")
public class LoginResponse {

    @Schema(description = "JWT Token", example = "eyJhbGciOiJIUzI1NiIs...")
    private String token;

    @Schema(description = "用户信息")
    private UserInfoVO user;
}
