package com.cloudtheon.knowflowweb.controller;

import com.cloudtheon.knowflowcommon.result.ApiResponse;
import com.cloudtheon.knowflowcore.dto.FeedbackRequest;
import com.cloudtheon.knowflowcore.service.FeedbackService;
import com.cloudtheon.knowflowcore.vo.FeedbackVO;
import com.cloudtheon.knowflowcore.vo.PageVO;
import com.cloudtheon.knowflowinfrastructure.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 帮助与反馈控制器
 */
@Tag(name = "04-帮助与反馈", description = "用户反馈提交与查询")
@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @Operation(summary = "提交反馈", description = "提交问题反馈 / 功能建议 / 其他反馈")
    @PostMapping
    public ApiResponse<FeedbackVO> submit(
            @AuthenticationPrincipal LoginUser loginUser,
            @Valid @RequestBody FeedbackRequest req) {
        return ApiResponse.success(feedbackService.submit(loginUser.getUserId(), req));
    }

    @Operation(summary = "我的反馈列表", description = "获取当前用户的反馈列表（分页，按提交时间倒序）")
    @GetMapping("/mine")
    public ApiResponse<PageVO<FeedbackVO>> mine(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.success(feedbackService.listMine(loginUser.getUserId(), page, pageSize));
    }
}
