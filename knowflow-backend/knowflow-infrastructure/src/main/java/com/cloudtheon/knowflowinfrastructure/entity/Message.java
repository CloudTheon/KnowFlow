package com.cloudtheon.knowflowinfrastructure.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息实体，对应数据库 messages 表
 */
@Data
@TableName("messages")
public class Message {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long conversationId;

    /** 角色：user / assistant */
    private String role;

    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
