package com.cloudtheon.knowflowcore.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对话消息 VO（对外展示）
 */
@Data
@AllArgsConstructor
@Schema(description = "对话消息")
public class ChatMessageVO {

    @Schema(description = "消息 ID", example = "1")
    private Long id;

    @Schema(description = "角色（user=用户, assistant=AI）", example = "user")
    private String role;

    @Schema(description = "消息内容", example = "请解释 Java 中的 volatile 关键字")
    private String content;

    @Schema(description = "发送时间", example = "2026-07-31T10:00:00")
    private LocalDateTime createdAt;
}
