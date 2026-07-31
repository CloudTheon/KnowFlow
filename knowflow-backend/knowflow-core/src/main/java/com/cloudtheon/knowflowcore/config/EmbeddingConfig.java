package com.cloudtheon.knowflowcore.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

/**
 * Embedding 模型配置（RAG 向量化）
 * <p>
 * 使用 {@link TransformersEmbeddingModel}（ONNX 本地推理，无需外部 API Key）。
 * 模型来源由 {@code knowflow.embedding.model-uri} / {@code tokenizer-uri} 指定
 * （支持 {@code file:}、{@code classpath:}、{@code http(s):} 前缀），留空则使用内置默认模型
 * （all-MiniLM-L6-v2，384 维，首次使用自动下载并缓存）。
 * {@link VectorStore} Bean 标记为 {@link Lazy}：首次使用知识库时才初始化并加载模型，
 * 避免应用启动时被模型加载阻塞。
 * </p>
 */
@Configuration
public class EmbeddingConfig {

    /**
     * 本地 Embedding 模型（懒加载：首次使用知识库时才初始化并加载模型）
     */
    @Bean
    @Lazy
    public EmbeddingModel embeddingModel(
            @Value("${knowflow.embedding.model-uri:}") String modelUri,
            @Value("${knowflow.embedding.tokenizer-uri:}") String tokenizerUri,
            @Value("${knowflow.embedding.cache-dir:./data/embedding-cache}") String cacheDir) {
        TransformersEmbeddingModel model = new TransformersEmbeddingModel();
        if (StringUtils.hasText(modelUri)) {
            model.setModelResource(modelUri);
        }
        if (StringUtils.hasText(tokenizerUri)) {
            model.setTokenizerResource(tokenizerUri);
        }
        model.setResourceCacheDirectory(cacheDir);
        return model;
    }

    /**
     * pgvector 向量存储（懒加载：首次使用知识库时才初始化）
     */
    @Bean
    @Lazy
    public VectorStore vectorStore(
            JdbcTemplate jdbcTemplate,
            EmbeddingModel embeddingModel,
            @Value("${spring.ai.vectorstore.pgvector.dimensions:512}") int dimensions,
            @Value("${spring.ai.vectorstore.pgvector.index-type:HNSW}") String indexType,
            @Value("${spring.ai.vectorstore.pgvector.distance-type:COSINE_DISTANCE}") String distanceType) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(dimensions)
                .indexType(PgVectorStore.PgIndexType.valueOf(indexType))
                .distanceType(PgVectorStore.PgDistanceType.valueOf(distanceType))
                .initializeSchema(true)
                .build();
    }
}
