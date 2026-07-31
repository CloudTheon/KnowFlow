package com.cloudtheon.knowflowcore.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库文档 VO（对外展示）
 */
@Data
@AllArgsConstructor
@Schema(description = "知识库文档")
public class KnowledgeDocumentVO {

    @Schema(description = "文档 ID", example = "1")
    private Long id;

    @Schema(description = "文档标题", example = "Spring AI 入门指南")
    private String title;

    @Schema(description = "原始文件名", example = "spring-ai-guide.pdf")
    private String fileName;

    @Schema(description = "文件类型（pdf/md）", example = "pdf")
    private String fileType;

    @Schema(description = "文件大小（字节）", example = "2048576")
    private Long fileSize;

    @Schema(description = "处理状态（processing/ready/failed）", example = "ready")
    private String status;

    @Schema(description = "处理失败原因", example = "null")
    private String errorMsg;

    @Schema(description = "上传时间", example = "2026-07-31T10:00:00")
    private LocalDateTime createdAt;
}
