package com.cloudtheon.knowflowcommon.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一业务状态码
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请重新登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 业务状态码
    USERNAME_EXISTS(40001, "用户名已存在"),
    USERNAME_OR_PASSWORD_ERROR(40002, "用户名或密码错误"),
    USER_NOT_FOUND(40003, "用户不存在"),
    OLD_PASSWORD_ERROR(40004, "原密码错误"),
    TOKEN_EXPIRED(40010, "Token 已过期"),
    TOKEN_INVALID(40011, "Token 无效"),
    USER_DISABLED(40012, "账号已被禁用，请联系管理员"),
    FILE_TYPE_NOT_SUPPORTED(40100, "不支持的文件类型，仅支持 PDF 和 Markdown 格式"),
    FILE_SIZE_EXCEEDED(40101, "文件大小超过限制"),
    CONVERSATION_NOT_FOUND(40200, "对话不存在"),
    DOCUMENT_NOT_FOUND(40300, "文档不存在"),
    ;

    private final int code;
    private final String message;
}
