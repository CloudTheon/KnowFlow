package com.cloudtheon.knowflowinfrastructure.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于 Redis 的对话记忆实现（ChatMemory）
 * <p>
 * 存储最近 N 条消息，供 {@code MessageChatMemoryAdvisor} 自动注入对话上下文。
 * 相比默认的内存实现 {@code MessageWindowChatMemory}，Redis 存储可跨服务重启保留。
 * </p>
 */
@Slf4j
@Component
public class RedisChatMemory implements ChatMemory {

    private static final String KEY_PREFIX = "chat:memory:";

    /** 每个会话最多保留的消息条数 */
    private static final int MAX_MESSAGES = 100;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisChatMemory(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        List<Map<String, String>> all = read(conversationId);
        for (Message m : messages) {
            Map<String, String> entry = new HashMap<>();
            entry.put("type", m.getMessageType().name().toLowerCase());
            entry.put("content", m.getText());
            all.add(entry);
        }
        // 截断为最近 MAX_MESSAGES 条
        if (all.size() > MAX_MESSAGES) {
            all = new ArrayList<>(all.subList(all.size() - MAX_MESSAGES, all.size()));
        }
        write(conversationId, all);
    }

    @Override
    public List<Message> get(String conversationId) {
        List<Message> result = new ArrayList<>();
        for (Map<String, String> entry : read(conversationId)) {
            String type = entry.get("type");
            String content = entry.getOrDefault("content", "");
            switch (type == null ? "" : type) {
                case "assistant" -> result.add(new AssistantMessage(content));
                case "system" -> result.add(new SystemMessage(content));
                default -> result.add(new UserMessage(content));
            }
        }
        return result;
    }

    @Override
    public void clear(String conversationId) {
        redisTemplate.delete(KEY_PREFIX + conversationId);
    }

    // ==================== 私有方法 ====================

    private List<Map<String, String>> read(String conversationId) {
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + conversationId);
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.warn("读取 Redis 对话记忆失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private void write(String conversationId, List<Map<String, String>> messages) {
        try {
            redisTemplate.opsForValue().set(KEY_PREFIX + conversationId,
                    objectMapper.writeValueAsString(messages));
        } catch (Exception e) {
            log.warn("写入 Redis 对话记忆失败: {}", e.getMessage());
        }
    }
}
