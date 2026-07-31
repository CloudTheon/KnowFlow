package com.cloudtheon.knowflowcore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudtheon.knowflowcommon.exception.BusinessException;
import com.cloudtheon.knowflowcommon.result.ResultCode;
import com.cloudtheon.knowflowcore.service.KnowledgeService;
import com.cloudtheon.knowflowcore.vo.KnowledgeDocumentVO;
import com.cloudtheon.knowflowcore.vo.PageVO;
import com.cloudtheon.knowflowinfrastructure.entity.KnowledgeDoc;
import com.cloudtheon.knowflowinfrastructure.mapper.KnowledgeDocMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 知识库业务实现
 * <p>
 * 文档处理流水线：Tika 解析 → TokenTextSplitter 分块 → TransformersEmbeddingModel 向量化 → 存入 pgvector。
 * 检索时按 {@code user_id} 过滤，实现用户数据隔离。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    /** 文件大小上限：20MB */
    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024;
    private static final int DEFAULT_TOP_K = 5;

    private final KnowledgeDocMapper knowledgeDocMapper;

    /**
     * 懒获取向量存储：避免应用启动时创建（加载 Embedding 模型），首次使用知识库时才初始化
     */
    private final ObjectProvider<VectorStore> vectorStoreProvider;

    /** 文本分块器（默认每块约 500 token） */
    private final TokenTextSplitter textSplitter = TokenTextSplitter.builder()
            .withChunkSize(500)
            .withMinChunkSizeChars(100)
            .withMinChunkLengthToEmbed(50)
            .build();

    @Override
    public KnowledgeDocumentVO upload(Long userId, MultipartFile file) {
        // 1. 校验文件
        String fileType = resolveFileType(file);
        if (file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "文件内容为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ResultCode.FILE_SIZE_EXCEEDED);
        }
        String fileName = file.getOriginalFilename();

        // 2. 写入元信息（初始状态 processing）
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setUserId(userId);
        doc.setFileName(fileName);
        doc.setFileType(fileType);
        doc.setFileSize(file.getSize());
        doc.setStatus("processing");
        knowledgeDocMapper.insert(doc);

        try {
            // 3. Tika 解析文档内容
            byte[] bytes = file.getBytes();
            ByteArrayResource resource = new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            };
            List<Document> parsed = new TikaDocumentReader(resource).get();

            // 4. 文本分块
            List<Document> chunks = textSplitter.split(parsed);

            // 5. 标记元数据（doc_id / user_id / chunk_index 用于检索过滤与删除），id 使用 UUID（PgVectorStore 默认）
            for (int i = 0; i < chunks.size(); i++) {
                Document c = chunks.get(i);
                chunks.set(i, c.mutate()
                        .id(UUID.randomUUID().toString())
                        .metadata("doc_id", doc.getId())
                        .metadata("user_id", userId)
                        .metadata("chunk_index", i)
                        .metadata("file_name", fileName)
                        .build());
            }

            // 6. 生成向量并写入 pgvector
            vectorStore().add(chunks);

            // 7. 更新状态为 ready
            doc.setStatus("ready");
            knowledgeDocMapper.updateById(doc);
            log.info("文档上传并向量化成功: id={}, chunks={}, file={}", doc.getId(), chunks.size(), fileName);
        } catch (Exception e) {
            log.error("文档处理失败: file={}, error={}", fileName, e.getMessage(), e);
            doc.setStatus("failed");
            doc.setErrorMsg(e.getMessage());
            knowledgeDocMapper.updateById(doc);
        }
        return toVO(doc);
    }

    @Override
    public PageVO<KnowledgeDocumentVO> list(Long userId, long page, long pageSize) {
        Page<KnowledgeDoc> p = knowledgeDocMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<KnowledgeDoc>()
                        .eq(KnowledgeDoc::getUserId, userId)
                        .orderByDesc(KnowledgeDoc::getCreatedAt));
        List<KnowledgeDocumentVO> records = p.getRecords().stream().map(this::toVO).toList();
        return new PageVO<>(records, p.getTotal(), p.getCurrent(), p.getSize());
    }

    @Override
    public void delete(Long userId, Long docId) {
        KnowledgeDoc doc = getOwnedDoc(userId, docId);
        knowledgeDocMapper.deleteById(doc.getId());
        // 删除该文档对应的向量数据
        try {
            vectorStore().delete(new FilterExpressionBuilder().eq("doc_id", doc.getId()).build());
            log.info("文档及其向量已删除: id={}", doc.getId());
        } catch (Exception e) {
            log.warn("删除文档向量失败: id={}, error={}", doc.getId(), e.getMessage());
        }
    }

    @Override
    public List<Document> search(Long userId, String query, int topK) {
        return vectorStore().similaritySearch(SearchRequest.builder()
                .query(query)
                .topK(topK > 0 ? topK : DEFAULT_TOP_K)
                .similarityThreshold(0.2)
                .filterExpression(new FilterExpressionBuilder().eq("user_id", userId).build())
                .build());
    }

    // ==================== 私有方法 ====================

    /**
     * 懒获取向量存储实例（首次调用时创建并初始化 Embedding 模型）
     */
    private VectorStore vectorStore() {
        VectorStore vs = vectorStoreProvider.getIfAvailable();
        if (vs == null) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR.getCode(), "向量存储未初始化");
        }
        return vs;
    }

    /**
     * 根据扩展名判定支持的文件类型
     */
    private String resolveFileType(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null) {
            throw new BusinessException(ResultCode.FILE_TYPE_NOT_SUPPORTED);
        }
        String lower = name.toLowerCase();
        if (lower.endsWith(".pdf")) {
            return "pdf";
        }
        if (lower.endsWith(".md")) {
            return "md";
        }
        throw new BusinessException(ResultCode.FILE_TYPE_NOT_SUPPORTED);
    }

    /**
     * 校验文档归属并返回
     */
    private KnowledgeDoc getOwnedDoc(Long userId, Long docId) {
        KnowledgeDoc doc = knowledgeDocMapper.selectById(docId);
        if (doc == null || !Objects.equals(doc.getUserId(), userId)) {
            throw new BusinessException(ResultCode.DOCUMENT_NOT_FOUND);
        }
        return doc;
    }

    private KnowledgeDocumentVO toVO(KnowledgeDoc d) {
        return new KnowledgeDocumentVO(d.getId(), d.getTitle(), d.getFileName(), d.getFileType(),
                d.getFileSize(), d.getStatus(), d.getErrorMsg(), d.getCreatedAt());
    }
}
