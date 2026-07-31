package com.cloudtheon.knowflowcore.service;

import com.cloudtheon.knowflowcore.dto.ChatStream;
import com.cloudtheon.knowflowcore.dto.SendMessageRequest;
import com.cloudtheon.knowflowcore.vo.ChatMessageVO;
import com.cloudtheon.knowflowcore.vo.ConversationVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 智能对话业务接口
 */
public interface ChatService {

    /**
     * 发送消息（非流式）
     *
     * @param userId 当前用户 ID
     * @param req    发送消息请求
     * @return AI 回复消息
     */
    ChatMessageVO sendMessage(Long userId, SendMessageRequest req);

    /**
     * 流式对话（SSE）
     *
     * @param userId         当前用户 ID
     * @param conversationId 对话 ID（null=新建）
     * @param content        用户消息
     * @return 流式响应（含实际对话 ID 和 AI 回复文本流）
     */
    ChatStream streamChat(Long userId, Long conversationId, String content);

    /**
     * 获取对话列表
     *
     * @param userId 当前用户 ID
     * @return 对话列表（按更新时间倒序）
     */
    List<ConversationVO> listConversations(Long userId);

    /**
     * 获取对话历史消息
     *
     * @param userId         当前用户 ID
     * @param conversationId 对话 ID
     * @return 消息列表（按时间正序）
     */
    List<ChatMessageVO> getMessages(Long userId, Long conversationId);

    /**
     * 删除对话
     *
     * @param userId         当前用户 ID
     * @param conversationId 对话 ID
     */
    void deleteConversation(Long userId, Long conversationId);
}
