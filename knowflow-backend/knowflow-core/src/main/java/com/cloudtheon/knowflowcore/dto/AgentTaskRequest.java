package com.cloudtheon.knowflowcore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Agent 任务请求
 */
@Data
@Schema(description = "Agent 任务请求")
public class AgentTaskRequest {

    @Schema(description = "任务描述", example = "帮我制定一个 3 个月的 Java 进阶学习路线")
    @NotBlank(message = "任务描述不能为空")
    @Size(max = 2000, message = "任务描述过长")
    private String content;

    @Schema(description = "任务模式：general=通用多步任务 / learning-path=学习路径规划", example = "learning-path")
    @Pattern(regexp = "general|learning-path", message = "任务模式不合法")
    private String mode = "general";
}
