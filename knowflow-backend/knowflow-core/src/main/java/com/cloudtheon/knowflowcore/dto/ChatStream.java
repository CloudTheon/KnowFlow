package com.cloudtheon.knowflowcore.dto;

import reactor.core.publisher.Flux;

/**
 * 流式对话响应包装：携带实际对话 ID 和 AI 回复文本流
 */
public record ChatStream(Long conversationId, Flux<String> content) {
}
