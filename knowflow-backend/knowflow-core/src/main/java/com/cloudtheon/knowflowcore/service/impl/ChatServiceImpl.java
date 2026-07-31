package com.cloudtheon.knowflowcore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudtheon.knowflowcommon.exception.BusinessException;
import com.cloudtheon.knowflowcommon.result.ResultCode;
import com.cloudtheon.knowflowcore.dto.ChatStream;
import com.cloudtheon.knowflowcore.dto.SendMessageRequest;
import com.cloudtheon.knowflowcore.service.ChatService;
import com.cloudtheon.knowflowcore.service.KnowledgeService;
import com.cloudtheon.knowflowcore.vo.ChatMessageVO;
import com.cloudtheon.knowflowcore.vo.ConversationVO;
import com.cloudtheon.knowflowinfrastructure.entity.Conversation;
import com.cloudtheon.knowflowinfrastructure.entity.Message;
import com.cloudtheon.knowflowinfrastructure.mapper.ConversationMapper;
import com.cloudtheon.knowflowinfrastructure.mapper.MessageMapper;
import com.cloudtheon.knowflowinfrastructure.memory.RedisChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;

/**
 * 智能对话业务实现
 * <p>
 * 上下文方案：
 * <ul>
 *   <li>数据库（messages 表）持久化完整对话历史</li>
 *   <li>{@link MessageChatMemoryAdvisor}（配置于 ChatClient）自动管理上下文注入，
 *       底层由 {@link RedisChatMemory} 存储近期消息</li>
 * </ul>
 */
@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    private static final String SYSTEM_PROMPT = """
            你是一个专业的编程学习助手，帮助用户解决编程问题、解释技术概念、提供学习建议。
            回答要准确、清晰、有条理，尽量给出代码示例。
            当提供了「知识库参考内容」时：
            - 如果用户的问题与参考内容相关，请优先基于参考内容回答，并可在回答中说明依据；
            - 如果参考内容与问题无关，请忽略参考内容，正常回答。
            """;

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final ChatClient chatClient;
    private final RedisChatMemory chatMemory;
    private final KnowledgeService knowledgeService;

    public ChatServiceImpl(
            ConversationMapper conversationMapper,
            MessageMapper messageMapper,
            ChatClient chatClient,
            RedisChatMemory chatMemory,
            KnowledgeService knowledgeService) {
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
        this.knowledgeService = knowledgeService;
    }

    // ==================== 非流式对话 ====================

    @Override
    public ChatMessageVO sendMessage(Long userId, SendMessageRequest req) {
        Conversation conversation = resolveConversation(userId, req.getConversationId(), req.getContent());

        // 持久化用户消息
        saveMessage(conversation.getId(), "user", req.getContent());

        // 调用 AI（MessageChatMemoryAdvisor 自动注入该会话历史上下文，RAG 检索知识库）
        String aiContent;
        try {
            aiContent = chatClient.prompt()
                    .system(buildSystemPrompt(userId, req.getContent()))
                    .user(req.getContent())
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, String.valueOf(conversation.getId())))
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("调用 AI 失败: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR.getCode(),
                    "AI 服务调用失败，请检查模型配置或稍后重试");
        }

        // 持久化 AI 回复
        Message aiMsg = saveMessage(conversation.getId(), "assistant", aiContent);
        return toVO(aiMsg);
    }

    // ==================== 流式对话（SSE） ====================

    @Override
    public ChatStream streamChat(Long userId, Long conversationId, String content) {
        Conversation conversation = resolveConversation(userId, conversationId, content);

        // 持久化用户消息
        saveMessage(conversation.getId(), "user", content);

        // 流式调用 AI（Advisor 自动注入上下文，RAG 检索知识库）
        StringBuilder fullReply = new StringBuilder();
        Flux<String> contentFlux = chatClient.prompt()
                .system(buildSystemPrompt(userId, content))
                .user(content)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, String.valueOf(conversation.getId())))
                .stream()
                .content()
                .doOnNext(fullReply::append)
                .doOnComplete(() ->
                        // 流结束后持久化完整回复
                        saveMessage(conversation.getId(), "assistant", fullReply.toString()))
                .onErrorResume(e -> {
                    log.error("流式对话失败: {}", e.getMessage(), e);
                    return Flux.just("抱歉，AI 服务暂时不可用，请稍后重试。");
                });

        return new ChatStream(conversation.getId(), contentFlux);
    }

    // ==================== 对话管理 ====================

    @Override
    public List<ConversationVO> listConversations(Long userId) {
        List<Conversation> list = conversationMapper.selectList(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getUserId, userId)
                        .orderByDesc(Conversation::getUpdatedAt));
        return list.stream().map(this::toVO).toList();
    }

    @Override
    public List<ChatMessageVO> getMessages(Long userId, Long conversationId) {
        Conversation conversation = getOwnedConversation(userId, conversationId);
        List<Message> messages = messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversation.getId())
                        .orderByAsc(Message::getId));
        return messages.stream().map(this::toVO).toList();
    }

    @Override
    public void deleteConversation(Long userId, Long conversationId) {
        Conversation conversation = getOwnedConversation(userId, conversationId);
        // 删除消息
        messageMapper.delete(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversation.getId()));
        // 删除对话
        conversationMapper.deleteById(conversation.getId());
        // 清理 Redis 对话记忆
        chatMemory.clear(String.valueOf(conversation.getId()));
    }

    // ==================== 私有方法 ====================

    /**
     * 构建系统提示词：基础提示 + RAG 知识库检索到的相关片段
     */
    private String buildSystemPrompt(Long userId, String userContent) {
        StringBuilder sb = new StringBuilder(SYSTEM_PROMPT);
        try {
            List<Document> docs = knowledgeService.search(userId, userContent, 5);
            if (docs != null && !docs.isEmpty()) {
                sb.append("\n\n# 知识库参考内容\n");
                sb.append("以下内容来自用户的知识库文档：\n");
                for (int i = 0; i < docs.size(); i++) {
                    sb.append("\n【片段 ").append(i + 1).append("】").append(docs.get(i).getText()).append("\n");
                }
            }
        } catch (Exception e) {
            // 检索失败不影响正常对话
            log.warn("知识库检索失败，忽略 RAG 上下文: {}", e.getMessage());
        }
        return sb.toString();
    }

    /**
     * 解析对话：已存在则返回，否则创建新对话
     */
    private Conversation resolveConversation(Long userId, Long conversationId, String firstContent) {
        if (conversationId != null) {
            return getOwnedConversation(userId, conversationId);
        }
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setTitle(generateTitle(firstContent));
        conversationMapper.insert(conversation);
        return conversation;
    }

    /**
     * 校验对话归属并返回
     */
    private Conversation getOwnedConversation(Long userId, Long conversationId) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !Objects.equals(conversation.getUserId(), userId)) {
            throw new BusinessException(ResultCode.CONVERSATION_NOT_FOUND);
        }
        return conversation;
    }

    /**
     * 从用户首条消息生成对话标题
     */
    private String generateTitle(String content) {
        if (content == null || content.isBlank()) {
            return "新对话";
        }
        String trimmed = content.trim();
        return trimmed.length() > 20 ? trimmed.substring(0, 20) + "…" : trimmed;
    }

    /**
     * 持久化一条消息
     */
    private Message saveMessage(Long conversationId, String role, String content) {
        Message m = new Message();
        m.setConversationId(conversationId);
        m.setRole(role);
        m.setContent(content);
        messageMapper.insert(m);
        return m;
    }

    // ==================== 转换 ====================

    private ChatMessageVO toVO(Message m) {
        return new ChatMessageVO(m.getId(), m.getRole(), m.getContent(), m.getCreatedAt());
    }

    private ConversationVO toVO(Conversation c) {
        return new ConversationVO(c.getId(), c.getTitle(), c.getCreatedAt(), c.getUpdatedAt());
    }
}
