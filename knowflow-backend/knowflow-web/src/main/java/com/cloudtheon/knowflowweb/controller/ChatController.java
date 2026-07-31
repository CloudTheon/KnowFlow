package com.cloudtheon.knowflowweb.controller;

import com.cloudtheon.knowflowcommon.result.ApiResponse;
import com.cloudtheon.knowflowcore.dto.ChatStream;
import com.cloudtheon.knowflowcore.dto.SendMessageRequest;
import com.cloudtheon.knowflowcore.service.ChatService;
import com.cloudtheon.knowflowcore.vo.ChatMessageVO;
import com.cloudtheon.knowflowcore.vo.ConversationVO;
import com.cloudtheon.knowflowinfrastructure.security.LoginUser;
import tools.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * 智能对话控制器
 */
@Slf4j
@Tag(name = "02-智能对话", description = "多轮连续对话、SSE 流式输出、对话历史管理")
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    @Operation(summary = "发送消息（非流式）", description = "向 AI 发送消息并获取完整回复")
    @PostMapping("/send")
    public ApiResponse<ChatMessageVO> send(
            @AuthenticationPrincipal LoginUser loginUser,
            @Valid @RequestBody SendMessageRequest req) {
        ChatMessageVO reply = chatService.sendMessage(loginUser.getUserId(), req);
        return ApiResponse.success(reply);
    }

    @Operation(summary = "SSE 流式对话", description = "通过 Server-Sent Events 实现 AI 回复的流式输出（POST）")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream(
            @AuthenticationPrincipal LoginUser loginUser,
            @Valid @RequestBody SendMessageRequest req) {

        ChatStream stream = chatService.streamChat(loginUser.getUserId(), req.getConversationId(), req.getContent());
        Long actualConversationId = stream.conversationId();

        return stream.content()
                .map(chunk -> ServerSentEvent.<String>builder()
                        .event("token")
                        .data(toJson(Map.of("content", chunk)))
                        .build())
                .concatWith(Mono.just(ServerSentEvent.<String>builder()
                        .event("done")
                        .data(toJson(Map.of("conversationId", actualConversationId)))
                        .build()));
    }

    @Operation(summary = "获取对话列表", description = "获取当前用户的所有历史对话列表，按最后更新时间倒序")
    @GetMapping("/conversations")
    public ApiResponse<List<ConversationVO>> conversations(@AuthenticationPrincipal LoginUser loginUser) {
        return ApiResponse.success(chatService.listConversations(loginUser.getUserId()));
    }

    @Operation(summary = "获取历史消息", description = "获取指定对话的全部历史消息，按发送时间正序")
    @GetMapping("/{conversationId}/messages")
    public ApiResponse<List<ChatMessageVO>> messages(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long conversationId) {
        return ApiResponse.success(chatService.getMessages(loginUser.getUserId(), conversationId));
    }

    @Operation(summary = "删除对话", description = "删除指定对话及其所有消息记录")
    @DeleteMapping("/{conversationId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long conversationId) {
        chatService.deleteConversation(loginUser.getUserId(), conversationId);
        return ApiResponse.success();
    }

    // ==================== 私有方法 ====================

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("序列化 SSE 数据失败: {}", e.getMessage(), e);
            return "{}";
        }
    }
}
