package com.cloudtheon.knowflowcore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改密码请求
 */
@Data
@Schema(description = "修改密码请求")
public class UpdatePasswordRequest {

    @Schema(description = "原密码", example = "oldPassword123")
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @Schema(description = "新密码（6~100 字符）", example = "newPassword123")
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 100, message = "新密码长度需在 6~100 之间")
    private String newPassword;
}
