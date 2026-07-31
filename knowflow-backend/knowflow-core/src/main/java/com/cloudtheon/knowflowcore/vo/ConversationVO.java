package com.cloudtheon.knowflowcore.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对话概要 VO
 */
@Data
@AllArgsConstructor
@Schema(description = "对话概要信息")
public class ConversationVO {

    @Schema(description = "对话 ID", example = "1")
    private Long id;

    @Schema(description = "对话标题", example = "Java 并发编程问题")
    private String title;

    @Schema(description = "创建时间", example = "2026-07-31T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "最后更新时间", example = "2026-07-31T11:30:00")
    private LocalDateTime updatedAt;
}
