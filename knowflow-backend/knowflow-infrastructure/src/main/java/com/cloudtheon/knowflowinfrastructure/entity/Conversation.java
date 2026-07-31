package com.cloudtheon.knowflowinfrastructure.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对话实体，对应数据库 conversations 表
 */
@Data
@TableName("conversations")
public class Conversation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 对话标题（可自动生成） */
    private String title;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
