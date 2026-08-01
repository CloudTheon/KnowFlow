package com.cloudtheon.knowflowweb.controller;

import com.cloudtheon.knowflowcommon.result.ApiResponse;
import com.cloudtheon.knowflowcore.dto.AgentTaskRequest;
import com.cloudtheon.knowflowcore.service.AgentService;
import com.cloudtheon.knowflowcore.vo.AgentTaskResponse;
import com.cloudtheon.knowflowinfrastructure.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * ReAct Agent 控制器：多步骤任务自主规划
 */
@Tag(name = "05-Agent 智能体", description = "多步骤任务自主规划、学习路径生成")
@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @Operation(summary = "执行 Agent 任务", description = "ReAct 模式执行多步骤任务（可调用代码执行 / Web 搜索工具），支持通用任务与学习路径规划")
    @PostMapping("/task")
    public ApiResponse<AgentTaskResponse> task(
            @AuthenticationPrincipal LoginUser loginUser,
            @Valid @RequestBody AgentTaskRequest req) {
        String result = agentService.runTask(loginUser.getUserId(), req);
        return ApiResponse.success(new AgentTaskResponse(result));
    }
}
