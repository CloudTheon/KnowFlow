package com.cloudtheon.knowflowcore.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Agent 任务响应
 *
 * @param result 任务执行结果（Markdown）
 */
@Schema(description = "Agent 任务响应")
public record AgentTaskResponse(
        @Schema(description = "任务执行结果（Markdown 格式）") String result) {
}
