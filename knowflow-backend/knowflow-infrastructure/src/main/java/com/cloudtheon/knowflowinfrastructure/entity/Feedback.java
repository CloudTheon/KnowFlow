package com.cloudtheon.knowflowinfrastructure.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户反馈实体，对应数据库 feedback 表
 */
@Data
@TableName("feedback")
public class Feedback {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户 ID */
    private Long userId;

    /** 反馈类型：bug / suggestion / other */
    private String type;

    /** 反馈内容 */
    private String content;

    /** 联系方式（可选） */
    private String contact;

    /** 处理状态：pending / processing / resolved */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
