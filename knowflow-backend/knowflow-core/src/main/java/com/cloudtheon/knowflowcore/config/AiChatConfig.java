package com.cloudtheon.knowflowcore.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 对话配置
 * <p>
 * 通过 {@link MessageChatMemoryAdvisor} 自动管理对话上下文：
 * 每次请求按 conversationId 自动注入历史消息，无需手动拼接。
 * </p>
 */
@Configuration
public class AiChatConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
