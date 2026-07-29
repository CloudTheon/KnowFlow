import { get, post } from '@/utils/request'
import type { ApiResponse, ChatMessage, KnowledgeDocument, UserInfo } from '@/types'

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
}

/** ======== 对话 ======== */
export const chatApi = {
  /** 发送消息（普通） */
  sendMessage(conversationId: number | null, content: string) {
    return post<ChatMessage>('/chat/send', { conversationId, content })
  },
  /** 流式对话（SSE） */
  streamUrl(conversationId: number | null) {
    return `/chat/stream?conversationId=${conversationId ?? ''}`
  },
  /** 获取历史消息 */
  history(conversationId: number) {
    return get<ChatMessage[]>(`/chat/${conversationId}/messages`)
  },
}

/** ======== 知识库 ======== */
export const knowledgeApi = {
  list() {
    return get<KnowledgeDocument[]>('/knowledge/list')
  },
  upload(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return post<KnowledgeDocument>('/knowledge/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
  delete(id: number) {
    return post<void>(`/knowledge/${id}/delete`)
  },
}
