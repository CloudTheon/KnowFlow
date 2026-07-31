package com.cloudtheon.knowflowcore.service;

import com.cloudtheon.knowflowcore.vo.KnowledgeDocumentVO;
import com.cloudtheon.knowflowcore.vo.PageVO;
import org.springframework.ai.document.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库业务接口
 * <p>提供文档上传（解析 → 分块 → 向量化）、列表查询、删除，以及 RAG 语义检索。</p>
 */
public interface KnowledgeService {

    /**
     * 上传文档：解析 → 分块 → 生成向量 → 存入 pgvector
     *
     * @param userId 当前用户 ID
     * @param file   上传文件（pdf / md）
     * @return 文档信息（含处理状态）
     */
    KnowledgeDocumentVO upload(Long userId, MultipartFile file);

    /**
     * 分页查询当前用户的文档列表
     *
     * @param userId   当前用户 ID
     * @param page     页码（从 1 开始）
     * @param pageSize 每页记录数
     * @return 分页数据
     */
    PageVO<KnowledgeDocumentVO> list(Long userId, long page, long pageSize);

    /**
     * 删除文档（同时删除其向量数据）
     *
     * @param userId 当前用户 ID
     * @param docId  文档 ID
     */
    void delete(Long userId, Long docId);

    /**
     * RAG 语义检索：基于用户问题检索知识库中最相关的文档片段
     *
     * @param userId 当前用户 ID（按用户隔离）
     * @param query  检索问题
     * @param topK   返回片段数
     * @return 相关文档片段（含相似度分数）
     */
    List<Document> search(Long userId, String query, int topK);
}
