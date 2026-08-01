package com.cloudtheon.knowflowcore.config;

import com.cloudtheon.knowflowcore.tool.CodeExecutorTool;
import com.cloudtheon.knowflowcore.tool.WebSearchTool;
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
 * 同时注入工具（Tool Calling）：AI 可在对话中调用代码执行 / Web 搜索工具。
 * </p>
 */
@Configuration
public class AiChatConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory,
                                 CodeExecutorTool codeExecutorTool, WebSearchTool webSearchTool) {
        return builder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(codeExecutorTool, webSearchTool)
                .build();
    }
}
