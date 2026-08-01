-- ============================================================================
-- KnowFlow 数据库初始化脚本
-- 数据库：PostgreSQL 16 + pgvector
-- 说明：本脚本用于初始化 KnowFlow 平台所需的数据库表结构
-- ============================================================================

-- 启用 pgvector 扩展（用于向量相似度检索）
CREATE EXTENSION IF NOT EXISTS vector;

-- ============================================================================
-- 1. 用户表 (users)
--    存储平台注册用户信息
-- ============================================================================
CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL       PRIMARY KEY,
    username    VARCHAR(50)     NOT NULL,
    password    VARCHAR(255)    NOT NULL,       -- BCrypt 加密后的密码
    avatar      VARCHAR(500),                   -- 头像 URL
    role        VARCHAR(20)     NOT NULL DEFAULT 'user',   -- admin / user
    status      VARCHAR(20)     NOT NULL DEFAULT 'enabled', -- enabled / disabled
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_users_username UNIQUE (username)
);

COMMENT ON TABLE  users      IS '用户表';
COMMENT ON COLUMN users.id       IS '主键 ID';
COMMENT ON COLUMN users.username IS '用户名（唯一）';
COMMENT ON COLUMN users.password IS 'BCrypt 加密密码';
COMMENT ON COLUMN users.avatar   IS '头像 URL';
COMMENT ON COLUMN users.role     IS '角色（admin=管理员, user=普通用户）';
COMMENT ON COLUMN users.status   IS '状态（enabled=正常, disabled=禁用）';
COMMENT ON COLUMN users.created_at IS '创建时间';
COMMENT ON COLUMN users.updated_at IS '更新时间';

-- 用户名索引（唯一约束已自动创建索引，此处仅为显式命名）
CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);
-- 按创建时间排序常用
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users (created_at DESC);

-- ============================================================================
-- 2. 对话表 (conversations)
--    存储用户的历史对话记录
-- ============================================================================
CREATE TABLE IF NOT EXISTS conversations (
    id          BIGSERIAL       PRIMARY KEY,
    user_id     BIGINT          NOT NULL,
    title       VARCHAR(200),                   -- 对话标题（可由 AI 自动生成）
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_conversations_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE
);

COMMENT ON TABLE  conversations       IS '对话表';
COMMENT ON COLUMN conversations.id         IS '主键 ID';
COMMENT ON COLUMN conversations.user_id    IS '所属用户 ID';
COMMENT ON COLUMN conversations.title      IS '对话标题';
COMMENT ON COLUMN conversations.created_at IS '创建时间';
COMMENT ON COLUMN conversations.updated_at IS '最后更新时间';

-- 按用户查询对话列表（常用操作）
CREATE INDEX IF NOT EXISTS idx_conversations_user_id ON conversations (user_id);
-- 对话列表通常按更新时间倒序排列
CREATE INDEX IF NOT EXISTS idx_conversations_updated_at ON conversations (user_id, updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_conversations_created_at ON conversations (created_at DESC);

-- ============================================================================
-- 3. 消息表 (messages)
--    存储对话中的每一条消息
-- ============================================================================
CREATE TABLE IF NOT EXISTS messages (
    id              BIGSERIAL       PRIMARY KEY,
    conversation_id BIGINT          NOT NULL,
    role            VARCHAR(10)     NOT NULL,   -- 'user' | 'assistant'
    content         TEXT            NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_messages_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations (id)
        ON DELETE CASCADE,
    CONSTRAINT ck_messages_role
        CHECK (role IN ('user', 'assistant'))
);

COMMENT ON TABLE  messages               IS '消息表';
COMMENT ON COLUMN messages.id                IS '主键 ID';
COMMENT ON COLUMN messages.conversation_id   IS '所属对话 ID';
COMMENT ON COLUMN messages.role              IS '角色（user=用户, assistant=AI）';
COMMENT ON COLUMN messages.content           IS '消息内容';
COMMENT ON COLUMN messages.created_at        IS '发送时间';

-- 按对话查询消息（常用操作，按时间正序）
CREATE INDEX IF NOT EXISTS idx_messages_conversation_id ON messages (conversation_id);
CREATE INDEX IF NOT EXISTS idx_messages_created_at ON messages (conversation_id, created_at ASC);

-- ============================================================================
-- 4. 知识库文档表 (knowledge_docs)
--    存储用户上传的学习文档元信息
-- ============================================================================
CREATE TABLE IF NOT EXISTS knowledge_docs (
    id          BIGSERIAL       PRIMARY KEY,
    user_id     BIGINT          NOT NULL,
    title       VARCHAR(200),                   -- 文档标题（可选）
    file_name   VARCHAR(255)    NOT NULL,       -- 原始文件名
    file_type   VARCHAR(20)     NOT NULL,       -- 文件类型（pdf / md）
    file_size   BIGINT          NOT NULL,       -- 文件大小（字节）
    status      VARCHAR(20)     NOT NULL DEFAULT 'processing',  -- processing / ready / failed
    error_msg   TEXT,                           -- 处理失败原因
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_knowledge_docs_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT ck_knowledge_docs_file_type
        CHECK (file_type IN ('pdf', 'md')),
    CONSTRAINT ck_knowledge_docs_status
        CHECK (status IN ('processing', 'ready', 'failed'))
);

COMMENT ON TABLE  knowledge_docs            IS '知识库文档表';
COMMENT ON COLUMN knowledge_docs.id             IS '主键 ID';
COMMENT ON COLUMN knowledge_docs.user_id        IS '所属用户 ID';
COMMENT ON COLUMN knowledge_docs.title          IS '文档标题';
COMMENT ON COLUMN knowledge_docs.file_name      IS '原始文件名';
COMMENT ON COLUMN knowledge_docs.file_type      IS '文件类型（pdf/md）';
COMMENT ON COLUMN knowledge_docs.file_size      IS '文件大小（字节）';
COMMENT ON COLUMN knowledge_docs.status         IS '处理状态（processing=处理中, ready=已就绪, failed=处理失败）';
COMMENT ON COLUMN knowledge_docs.error_msg      IS '处理失败原因';
COMMENT ON COLUMN knowledge_docs.created_at     IS '上传时间';

-- 按用户查询文档列表
CREATE INDEX IF NOT EXISTS idx_knowledge_docs_user_id ON knowledge_docs (user_id);
-- 文档列表按上传时间倒序排列
CREATE INDEX IF NOT EXISTS idx_knowledge_docs_created_at ON knowledge_docs (user_id, created_at DESC);
-- 按状态查询（用于后台任务轮询待处理文档）
CREATE INDEX IF NOT EXISTS idx_knowledge_docs_status ON knowledge_docs (status) WHERE status = 'processing';

-- ============================================================================
-- 5. 用户反馈表 (feedback)
--    存储用户提交的问题反馈 / 功能建议
-- ============================================================================
CREATE TABLE IF NOT EXISTS feedback (
    id          BIGSERIAL       PRIMARY KEY,
    user_id     BIGINT          NOT NULL,
    type        VARCHAR(20)     NOT NULL,       -- bug / suggestion / other
    content     TEXT            NOT NULL,       -- 反馈内容
    contact     VARCHAR(100),                   -- 联系方式（可选）
    status      VARCHAR(20)     NOT NULL DEFAULT 'pending',  -- pending / processing / resolved
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_feedback_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT ck_feedback_type
        CHECK (type IN ('bug', 'suggestion', 'other')),
    CONSTRAINT ck_feedback_status
        CHECK (status IN ('pending', 'processing', 'resolved'))
);

COMMENT ON TABLE  feedback        IS '用户反馈表';
COMMENT ON COLUMN feedback.id          IS '主键 ID';
COMMENT ON COLUMN feedback.user_id     IS '所属用户 ID';
COMMENT ON COLUMN feedback.type        IS '反馈类型（bug=问题反馈, suggestion=功能建议, other=其他）';
COMMENT ON COLUMN feedback.content     IS '反馈内容';
COMMENT ON COLUMN feedback.contact     IS '联系方式';
COMMENT ON COLUMN feedback.status      IS '处理状态（pending=待处理, processing=处理中, resolved=已解决）';
COMMENT ON COLUMN feedback.created_at  IS '提交时间';

-- 按用户查询反馈列表
CREATE INDEX IF NOT EXISTS idx_feedback_user_id ON feedback (user_id);
-- 反馈列表按提交时间倒序
CREATE INDEX IF NOT EXISTS idx_feedback_created_at ON feedback (user_id, created_at DESC);

-- ============================================================================
-- 6. 向量存储表 (vector_store)
--    由 Spring AI 的 PgVectorStore 自动管理建表，以下定义仅供参考。
--    实际表结构由 Spring AI 运行时自动创建，无需手动执行。
-- ============================================================================
-- 参考 DDL（Spring AI 2.0 自动生成）：
--
-- CREATE TABLE IF NOT EXISTS vector_store (
--     id          UUID            PRIMARY KEY,
--     embedding   vector(1536)    NOT NULL,   -- 向量维度取决于 Embedding 模型
--     metadata    JSONB           NOT NULL    -- 元数据（含 doc_id, chunk_index, user_id, text 等）
-- );
--
-- 索引（Spring AI 会自动创建 IVFFlat 索引）：
-- CREATE INDEX IF NOT EXISTS idx_vector_store_embedding
--     ON vector_store
--     USING ivfflat (embedding vector_cosine_ops)
--     WITH (lists = 100);

-- ============================================================================
-- 7. 自动更新 updated_at 的触发器函数
--    用于 users 和 conversations 表的 updated_at 字段自动更新
-- ============================================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 为 users 表添加触发器
DROP TRIGGER IF EXISTS trg_users_updated_at ON users;
CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 为 conversations 表添加触发器
DROP TRIGGER IF EXISTS trg_conversations_updated_at ON conversations;
CREATE TRIGGER trg_conversations_updated_at
    BEFORE UPDATE ON conversations
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- 8. 初始化完成日志
-- ============================================================================
DO $$
BEGIN
    RAISE NOTICE 'KnowFlow 数据库初始化完成 ✅';
    RAISE NOTICE '  已创建表: users, conversations, messages, knowledge_docs, feedback';
    RAISE NOTICE '  数据库: PostgreSQL 16 + pgvector';
END $$;
