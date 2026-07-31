package com.cloudtheon.knowflowcore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发送消息请求 DTO（非流式 / SSE 共用）
 */
@Data
@Schema(description = "发送消息请求")
public class SendMessageRequest {

    @Schema(description = "对话 ID（null=新建对话，非 null=续接已有对话）", example = "1")
    private Long conversationId;

    @NotBlank(message = "消息内容不能为空")
    @Schema(description = "用户消息内容", example = "请解释 Java 中的 volatile 关键字")
    private String content;
}
