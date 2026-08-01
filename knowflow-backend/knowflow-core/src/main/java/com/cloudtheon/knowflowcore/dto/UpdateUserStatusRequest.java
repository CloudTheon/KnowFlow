package com.cloudtheon.knowflowcore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 更新用户状态请求
 */
@Data
@Schema(description = "更新用户状态请求")
public class UpdateUserStatusRequest {

    @Schema(description = "状态：enabled=启用 / disabled=禁用", example = "disabled")
    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "enabled|disabled", message = "状态不合法")
    private String status;
}
