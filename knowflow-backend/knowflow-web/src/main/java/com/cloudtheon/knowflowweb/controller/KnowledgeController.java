package com.cloudtheon.knowflowweb.controller;

import com.cloudtheon.knowflowcommon.result.ApiResponse;
import com.cloudtheon.knowflowcore.service.KnowledgeService;
import com.cloudtheon.knowflowcore.vo.KnowledgeDocumentVO;
import com.cloudtheon.knowflowcore.vo.PageVO;
import com.cloudtheon.knowflowinfrastructure.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 知识库控制器（RAG）
 */
@Tag(name = "03-RAG 知识库", description = "文档上传解析、知识库管理、基于知识库的精准问答")
@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @Operation(summary = "上传文档", description = "上传学习文档（PDF/Markdown），后端解析、分块并向量化存入知识库")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<KnowledgeDocumentVO> upload(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam("file") MultipartFile file) {
        return ApiResponse.success(knowledgeService.upload(loginUser.getUserId(), file));
    }

    @Operation(summary = "文档列表", description = "获取当前用户的知识库文档列表（分页，按上传时间倒序）")
    @GetMapping("/list")
    public ApiResponse<PageVO<KnowledgeDocumentVO>> list(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.success(knowledgeService.list(loginUser.getUserId(), page, pageSize));
    }

    @Operation(summary = "删除文档", description = "删除指定的知识库文档及其向量数据")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id) {
        knowledgeService.delete(loginUser.getUserId(), id);
        return ApiResponse.success();
    }
}
