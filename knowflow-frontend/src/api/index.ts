import { get, post, put, del } from '@/utils/request'
import { getToken } from '@/utils/auth'
import type {
  ApiResponse,
  ChatMessage,
  Conversation,
  Feedback,
  KnowledgeDocument,
  PaginatedData,
  UserInfo,
} from '@/types'

/** ======== 用户 ======== */
export const userApi = {
  login(username: string, password: string) {
    return post<{ token: string; user: UserInfo }>('/auth/login', { username, password })
  },
  register(username: string, password: string) {
    return post<{ token: string; user: UserInfo }>('/auth/register', { username, password })
  },
  profile() {
    return get<UserInfo>('/auth/profile')
  },
  /** 更新个人资料（头像） */
  updateProfile(avatar: string) {
    return put<UserInfo>('/auth/profile', { avatar })
  },
  /** 修改密码 */
  updatePassword(oldPassword: string, newPassword: string) {
    return put<void>('/auth/password', { oldPassword, newPassword })
  },
}

/** ======== 帮助与反馈 ======== */
export const feedbackApi = {
  /** 提交反馈 */
  submit(data: { type: string; content: string; contact?: string }) {
    return post<Feedback>('/feedback', data)
  },
  /** 我的反馈列表 */
  mine(page = 1, pageSize = 10) {
    return get<PaginatedData<Feedback>>('/feedback/mine', { params: { page, pageSize } })
  },
}

/** ======== 对话 ======== */
export const chatApi = {
  /** 发送消息（普通） */
  sendMessage(conversationId: number | null, content: string) {
    return post<ChatMessage>('/chat/send', { conversationId, content })
  },

  /** 流式对话（SSE，基于 fetch 解析，可携带 Authorization 头） */
  async stream(
    conversationId: number | null,
    content: string,
    handlers: {
      onToken: (chunk: string) => void
      onDone: (conversationId: number) => void
      onError: (message: string) => void
    },
  ) {
    const token = getToken()
    const params = new URLSearchParams({ content })
    if (conversationId != null) params.set('conversationId', String(conversationId))

    const response = await fetch(`/api/chat/stream?${params}`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    })

    if (!response.ok || !response.body) {
      handlers.onError(`请求失败（${response.status}）`)
      return
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })

      // 按 SSE 规范用空行分割事件
      const events = buffer.split('\n\n')
      buffer = events.pop() ?? ''

      for (const raw of events) {
        let event = 'message'
        let data = ''
        for (const line of raw.split('\n')) {
          if (line.startsWith('event:')) event = line.slice(6).trim()
          else if (line.startsWith('data:')) data += line.slice(5).trim()
        }
        if (!data) continue

        try {
          const payload = JSON.parse(data)
          if (event === 'token') {
            handlers.onToken(payload.content ?? '')
          } else if (event === 'done') {
            handlers.onDone(payload.conversationId)
          } else if (event === 'error') {
            handlers.onError(payload.message ?? '服务异常')
          }
        } catch {
          // 忽略无法解析的数据
        }
      }
    }
  },

  /** 获取对话列表 */
  conversations() {
    return get<Conversation[]>('/chat/conversations')
  },

  /** 获取历史消息 */
  history(conversationId: number) {
    return get<ChatMessage[]>(`/chat/${conversationId}/messages`)
  },

  /** 删除对话 */
  deleteConversation(conversationId: number) {
    return del<void>(`/chat/${conversationId}`)
  },
}

/** ======== 知识库 ======== */
export const knowledgeApi = {
  /** 分页获取文档列表 */
  list(page = 1, pageSize = 20) {
    return get<PaginatedData<KnowledgeDocument>>('/knowledge/list', { params: { page, pageSize } })
  },
  /** 上传文档 */
  upload(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return post<KnowledgeDocument>('/knowledge/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
  /** 删除文档 */
  delete(id: number) {
    return del<void>(`/knowledge/${id}`)
  },
}
