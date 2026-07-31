package com.cloudtheon.knowflowinfrastructure.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库文档实体，对应数据库 knowledge_docs 表
 */
@Data
@TableName("knowledge_docs")
public class KnowledgeDoc {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户 ID */
    private Long userId;

    /** 文档标题（可选） */
    private String title;

    /** 原始文件名 */
    private String fileName;

    /** 文件类型：pdf / md */
    private String fileType;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 处理状态：processing / ready / failed */
    private String status;

    /** 处理失败原因 */
    private String errorMsg;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
