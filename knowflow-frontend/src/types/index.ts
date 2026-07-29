/** 统一 API 响应结构 */
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

/** 分页参数 */
export interface PaginationParams {
  page: number
  pageSize: number
}

/** 分页响应 */
export interface PaginatedData<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
}

/** 用户信息 */
export interface UserInfo {
  id: number
  username: string
  avatar?: string
}

/** 对话消息 */
export interface ChatMessage {
  id?: number
  role: 'user' | 'assistant'
  content: string
  createdAt?: string
}

/** 知识库文档 */
export interface KnowledgeDocument {
  id: number
  title: string
  fileName: string
  fileType: string
  createdAt: string
  status: 'processing' | 'ready' | 'failed'
}
