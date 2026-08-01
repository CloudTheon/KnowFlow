package com.cloudtheon.knowflowcore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新个人资料请求
 */
@Data
@Schema(description = "更新个人资料请求")
public class UpdateProfileRequest {

    @Schema(description = "头像 URL", example = "https://example.com/avatar.png")
    @Size(max = 500, message = "头像地址过长")
    private String avatar;
}
