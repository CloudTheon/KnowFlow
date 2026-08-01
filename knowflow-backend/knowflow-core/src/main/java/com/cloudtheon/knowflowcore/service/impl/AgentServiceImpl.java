package com.cloudtheon.knowflowcore.service.impl;

import com.cloudtheon.knowflowcore.dto.AgentTaskRequest;
import com.cloudtheon.knowflowcore.service.AgentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

/**
 * ReAct Agent 业务实现
 * <p>
 * 复用已配置工具（代码执行 / Web 搜索）的 {@link ChatClient}，配合 ReAct 风格系统提示，
 * 引导模型对复杂任务进行拆解、逐步执行（调用工具获取信息）、最终汇总输出。
 * </p>
 */
@Slf4j
@Service
public class AgentServiceImpl implements AgentService {

    private static final String GENERAL_PROMPT = """
            你是一个自主任务规划助手（ReAct Agent）。面对用户的复杂任务，你需要：
            1. 把任务拆解为若干清晰的子步骤
            2. 需要实时信息时调用 web_search 工具搜索
            3. 涉及代码验证时调用 execute_java 工具运行
            4. 观察工具结果，继续推进后续步骤
            5. 最后用结构化的 Markdown 给出完整、可执行的最终答案

            要求：
            - 不要凭空编造事实；拿不准的信息用工具核实
            - 最终答案要清晰分层（标题、列表、步骤、示例）
            """;

    private static final String LEARNING_PATH_PROMPT = """
            你是一位资深学习规划专家（ReAct Agent）。根据用户的目标制定一份结构化的个性化学习路径：
            1. 先理解用户的当前基础、目标与时间
            2. 需要了解最新技术栈/资源时调用 web_search 搜索
            3. 将学习过程划分为若干阶段（如：基础入门 → 核心进阶 → 实战精通）
            4. 每个阶段包含：阶段目标、核心知识点清单、推荐资源（书籍/课程/文档）、练习建议与评估标准

            输出使用清晰的分阶段 Markdown 结构，可执行、有节奏感。
            """;

    private final ChatClient chatClient;

    public AgentServiceImpl(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public String runTask(Long userId, AgentTaskRequest req) {
        String system = "learning-path".equals(req.getMode()) ? LEARNING_PATH_PROMPT : GENERAL_PROMPT;
        try {
            String result = chatClient.prompt()
                    .system(system)
                    .user(req.getContent())
                    // 使用用户级独立会话 ID，满足 MessageChatMemoryAdvisor 的 conversationId 要求
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "agent-" + userId))
                    .call()
                    .content();
            return result == null ? "（任务执行未返回结果）" : result;
        } catch (Exception e) {
            log.error("Agent 任务执行失败: {}", e.getMessage(), e);
            return "任务执行失败：" + e.getMessage() + "（请稍后重试）";
        }
    }
}
