package com.cloudtheon.knowflowcore.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户反馈 VO（对外展示）
 */
@Data
@AllArgsConstructor
@Schema(description = "用户反馈")
public class FeedbackVO {

    @Schema(description = "反馈 ID", example = "1")
    private Long id;

    @Schema(description = "反馈类型（bug/suggestion/other）", example = "suggestion")
    private String type;

    @Schema(description = "反馈内容", example = "希望支持更多文档格式")
    private String content;

    @Schema(description = "联系方式", example = "zhangsan@example.com")
    private String contact;

    @Schema(description = "处理状态（pending/processing/resolved）", example = "pending")
    private String status;

    @Schema(description = "提交时间", example = "2026-08-01T10:00:00")
    private LocalDateTime createdAt;
}
