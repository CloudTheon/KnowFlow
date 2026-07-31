<template>
  <div class="chat-container">
    <!-- 左侧：对话列表 -->
    <aside class="chat-sidebar">
      <div class="sidebar-header">
        <t-button theme="primary" block @click="newConversation">＋ 新建对话</t-button>
      </div>
      <div class="sidebar-list">
        <div
          v-for="conv in conversations"
          :key="conv.id"
          class="conversation-item"
          :class="{ active: conv.id === currentId }"
          @click="switchConversation(conv.id)"
        >
          <span class="conv-title">{{ conv.title || '新对话' }}</span>
          <t-popconfirm content="确认删除该对话？" @confirm="removeConversation(conv.id)">
            <t-button shape="circle" variant="text" size="small" class="conv-delete" @click.stop>
              <template #icon><t-icon name="delete" /></template>
            </t-button>
          </t-popconfirm>
        </div>
        <t-empty v-if="!loading && conversations.length === 0" description="暂无对话" />
      </div>
    </aside>

    <!-- 右侧：TDesign Chatbot -->
    <main class="chat-main">
      <Chatbot
        ref="chatbotRef"
        :default-messages="defaultMessages"
        :message-props="messageProps"
        :sender-props="senderProps"
        :chat-service-config="chatServiceConfig"
        class="chat-bot"
      />
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { Chatbot } from '@tdesign-vue-next/chat'
import type {
  AIMessageContent,
  ChatMessagesData,
  ChatServiceConfig,
  SSEChunkData,
  TdChatbotApi,
} from '@tdesign-vue-next/chat'
import { chatApi } from '@/api'
import { getToken } from '@/utils/auth'
import type { ChatMessage, Conversation } from '@/types'

const conversations = ref<Conversation[]>([])
const currentId = ref<number | null>(null)
const loading = ref(false)
const chatbotRef = ref<TdChatbotApi | null>(null)

// 初始欢迎消息
const defaultMessages: ChatMessagesData[] = [
  {
    id: 'welcome',
    role: 'assistant',
    status: 'complete',
    content: [
      {
        type: 'text',
        data: '你好！我是你的编程学习助手 👋\n可以问我编程问题、解释概念或制定学习计划。',
      },
    ],
  },
]

// 消息展示配置
const messageProps = {
  user: {
    variant: 'base',
    placement: 'right',
  },
  assistant: {
    placement: 'left',
    actions: ['copy'] as string[],
  },
}

// 输入框配置
const senderProps = {
  placeholder: '输入你的问题，Enter 发送，Shift+Enter 换行',
}

// 聊天服务配置：对接后端 POST SSE
const chatServiceConfig: ChatServiceConfig = {
  endpoint: '/api/chat/stream',
  stream: true,
  // 请求发送前：添加鉴权头 + POST body
  onRequest: (params) => {
    const token = getToken()
    return {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: JSON.stringify({
        conversationId: currentId.value,
        content: params.prompt,
      }),
    }
  },
  // 解析后端 SSE：token → markdown 追加；done → 记录新对话 ID
  onMessage: (chunk: SSEChunkData): AIMessageContent | null => {
    if (chunk.event === 'token') {
      return {
        type: 'markdown',
        data: chunk.data?.content || '',
        strategy: 'merge',
      }
    }
    if (chunk.event === 'done') {
      const id = chunk.data?.conversationId
      if (id) {
        currentId.value = id
        loadConversations()
      }
      return null
    }
    return null
  },
  onError: (err) => {
    console.error('对话请求失败:', err)
    MessagePlugin.error('对话请求失败，请稍后重试')
  },
}

onMounted(async () => {
  await loadConversations()
})

async function loadConversations() {
  loading.value = true
  try {
    const res = await chatApi.conversations()
    conversations.value = res.data
  } catch {
    // 错误已由拦截器处理
  } finally {
    loading.value = false
  }
}

// 切换对话：加载历史消息到组件
async function switchConversation(id: number) {
  currentId.value = id
  loading.value = true
  try {
    const res = await chatApi.history(id)
    chatbotRef.value?.setMessages(toChatMessages(res.data), 'replace')
  } catch {
    MessagePlugin.error('加载历史消息失败')
  } finally {
    loading.value = false
  }
}

// 新建对话
function newConversation() {
  currentId.value = null
  chatbotRef.value?.clearMessages()
}

// 删除对话
async function removeConversation(id: number) {
  try {
    await chatApi.deleteConversation(id)
    conversations.value = conversations.value.filter((c) => c.id !== id)
    if (currentId.value === id) newConversation()
    MessagePlugin.success('对话已删除')
  } catch {
    MessagePlugin.error('删除失败')
  }
}

// 后端消息 → TDesign Chat 消息格式
function toChatMessages(list: ChatMessage[]): ChatMessagesData[] {
  return list.map((m, i) => ({
    id: String(m.id ?? `msg-${i}`),
    role: m.role,
    status: 'complete',
    datetime: m.createdAt,
    content: [{ type: 'text', data: m.content }],
  }))
}
</script>

<style scoped>
.chat-container {
  display: flex;
  flex: 1;
  min-height: 0;
  background: var(--td-bg-color-container);
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--td-component-stroke);
}

/* 左侧栏 */
.chat-sidebar {
  width: 240px;
  border-right: 1px solid var(--td-component-stroke);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid var(--td-component-stroke);
}

.sidebar-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.conversation-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  color: var(--td-text-color-primary);
  margin-bottom: 4px;
  transition: background 0.2s;
}

.conversation-item:hover {
  background: var(--td-bg-color-container-hover);
}

.conversation-item.active {
  background: var(--td-brand-color-light);
  color: var(--td-brand-color);
}

.conv-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
}

.conv-delete {
  opacity: 0;
  transition: opacity 0.2s;
}

.conversation-item:hover .conv-delete {
  opacity: 1;
}

/* 右侧聊天区 */
.chat-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.chat-bot {
  flex: 1;
  min-height: 0;
  height: 100%;
}
</style>

<!-- 定制 t-chatbot 内部留白（Web Component 需用 ::part，非 scoped） -->
<style>
.chat-bot::part(t-chat) {
  padding: 24px;
}
</style>
