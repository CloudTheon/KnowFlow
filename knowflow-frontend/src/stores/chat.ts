import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ChatMessage } from '@/types'

export const useChatStore = defineStore('chat', () => {
  const messages = ref<ChatMessage[]>([])
  const loading = ref(false)
  const conversationId = ref<number | null>(null)

  function addMessage(msg: ChatMessage) {
    messages.value.push(msg)
  }

  function clearMessages() {
    messages.value = []
  }

  function setConversationId(id: number | null) {
    conversationId.value = id
  }

  return {
    messages,
    loading,
    conversationId,
    addMessage,
    clearMessages,
    setConversationId,
  }
})
