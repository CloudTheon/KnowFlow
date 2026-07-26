# 📚 KnowFlow（智能学习助手平台）

> 基于 Spring AI 2.0 与 RAG 技术的个性化编程辅导全栈应用

---

## 1. 📖 项目背景与定位

KnowFlow 是一个面向程序员/学生的 AI 驱动学习辅助平台。核心解决用户在自学过程中遇到的“即时答疑难”、“资料整理乱”、“缺乏模拟面试”等痛点。

项目采用 **Spring AI 2.0** 官方最新框架，深度融合了 **RAG（检索增强生成）** 与 **ReAct Agent（自主规划）** 两大前沿技术，构建了一个具备“记忆、检索、推理、调用工具”能力的智能学习助手。

---

## 2. 🛠 技术栈选型

| 层级 | 技术选型 | 选型理由 |
| :--- | :--- | :--- |
| **后端框架** | Spring Boot 4.1 + Java 21 | Spring AI 2.0 的运行基座；Java 21 支持虚拟线程，极大提升 IO 密集型任务吞吐量 |
| **AI 核心** | **Spring AI 2.0 GA** | 官方推出的统一 AI 抽象层，原生支持 ReAct Agent 和 Tool Calling |
| **向量数据库** | PostgreSQL + pgvector | pgvector 允许在单一数据库同时管理业务数据和向量，架构最轻量 |
| **缓存/会话** | Redis + Redisson | 用于缓存对话历史（ChatMemory）和分布式会话管理 |
| **前端框架** | Vue 3 + TypeScript + Vite | 现代化组合式 API，静态类型加持，构建速度快 |
| **UI 组件** | TDesign | 腾讯企业级设计体系，风格专业，组件丰富 |
| **部署** | Docker Compose | 一条命令启动所有中间件（PG/Redis/App），极简运维 |

---

## 3. 🧩 核心功能模块

| 模块 | 功能描述 | 关键技术点 |
| :--- | :--- | :--- |
| **智能对话引擎** | 支持多轮上下文连续对话，具备记忆能力 | `ChatClient` + `ChatMemoryAdvisor` |
| **RAG 知识库** | 上传 PDF/Markdown，AI 基于私有资料精准问答 | `PgVectorStore` + `QuestionAnswerAdvisor` |
| **Tool Calling** | AI 自动调用外部工具（搜索、代码执行） | `ToolCallingAdvisor` + `@Tool` 注解 |
| **ReAct Agent** | AI 自主规划多步骤任务（如：生成学习路线） | 基于 `ReAct` 模式的智能体编排 |
| **流式交互** | 逐字展示 AI 回复，降低等待焦虑 | SSE（Server-Sent Events） |
| **用户管理** | JWT 登录鉴权，个人对话/文档管理 | Spring Security + JWT |

---

## 4. 🏗 系统架构图

```text
┌─────────────────────────────────────────────────────────────┐
│                      前端层 (Vue 3 + TS)                    │
│              [对话界面]  [知识库管理]  [用户中心]             │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP / SSE
┌──────────────────────────▼──────────────────────────────────┐
│                     后端层 (Spring Boot 4.1)                │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Controller (REST API + SSE Emitter)                 │   │
│  ├──────────────────────────────────────────────────────┤   │
│  │  Service (Chat / Knowledge / Agent)                  │   │
│  ├──────────────────────────────────────────────────────┤   │
│  │  Spring AI 2.0 核心组件                              │   │
│  │  ┌────────────┐ ┌────────────┐ ┌─────────────────┐ │   │
│  │  │ ChatClient │ │EmbeddingCli│ │ToolCallingAdvisor│ │   │
│  │  └────────────┘ └────────────┘ └─────────────────┘ │   │
│  └──────────────────────────────────────────────────────┘   │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                    数据层 (Docker 编排)                      │
│  ┌──────────┐  ┌──────────────┐  ┌──────────┐             │
│  │  MySQL   │  │ PostgreSQL + │  │  Redis   │             │
│  │(用户/对话)│  │  pgvector    │  │  (缓存)  │             │
│  └──────────┘  └──────────────┘  └──────────┘             │
└─────────────────────────────────────────────────────────────┘
```

---

## 5. 🗄 核心数据库设计（关键表）

| 表名 | 核心字段 | 说明 |
| :--- | :--- | :--- |
| `users` | `id`, `username`, `password` | 用户基础信息 |
| `conversations` | `id`, `user_id`, `title` | 对话会话容器 |
| `messages` | `id`, `conversation_id`, `role`, `content` | 历史消息记录 |
| `knowledge_docs` | `id`, `user_id`, `file_name`, `status` | 用户上传的原始文档 |
| **`vector_store`** | **`id`**, **`embedding`(vector)**, **`metadata`(jsonb)** | **pgvector 管理的向量数据（核心）** |

---

## 6. 📂 后端工程结构

```text
knowflow-backend/
├── pom.xml                              # 父POM (Spring Boot 4.1)
├── knowflow-common/                     # 公共工具类、统一响应封装
├── knowflow-infrastructure/             # 基础设施层
│   └── src/main/java/.../infra/
│       ├── config/                      # AI配置、PG向量配置、Redis配置
│       ├── repository/                  # JPA/MyBatis Mapper
│       └── entity/                      # 数据库实体类
├── knowflow-core/                       # 核心业务层
│   └── src/main/java/.../core/
│       ├── service/                     # ChatService, KnowledgeService, AgentService
│       └── tool/                        # WebSearchTool, CodeExecutorTool
└── knowflow-web/                        # Web控制层
    └── src/main/java/.../web/
        ├── controller/                  # ChatController, DocumentController
        └── KnowFlowApplication.java     # Spring Boot 启动类
```

---

## 7. 🚀 快速启动指令（开发阶段）

```bash
# 1. 启动所有中间件
docker-compose up -d

# 2. 启动后端 (Maven)
mvn spring-boot:run

# 3. 启动前端 (Vue)
npm install && npm run dev
```
---