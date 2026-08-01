package com.cloudtheon.knowflowcore.service;

import com.cloudtheon.knowflowcore.dto.AgentTaskRequest;

/**
 * ReAct Agent 业务接口：多步骤任务自主规划与执行
 */
public interface AgentService {

    /**
     * 执行一个多步骤任务（ReAct 模式：拆解 → 逐步执行/调用工具 → 汇总）
     *
     * @param userId 当前用户 ID
     * @param req    任务请求
     * @return 任务执行结果（Markdown）
     */
    String runTask(Long userId, AgentTaskRequest req);
}
