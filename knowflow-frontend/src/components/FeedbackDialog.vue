<template>
  <t-dialog :visible="visible" header="帮助与反馈" :footer="false" width="520px" @close="close">
    <t-tabs v-model="activeTab">
      <t-tab-panel value="submit" label="提交反馈">
        <div class="feedback-form">
          <t-radio-group v-model="feedbackType">
            <t-radio value="bug">问题反馈</t-radio>
            <t-radio value="suggestion">功能建议</t-radio>
            <t-radio value="other">其他</t-radio>
          </t-radio-group>
          <t-textarea
            v-model="feedbackContent"
            placeholder="请描述你遇到的问题或建议…"
            :maxlength="2000"
            :rows="4"
          />
          <t-input v-model="feedbackContact" placeholder="联系方式（选填，便于我们回复你）" clearable />
        </div>
        <div class="dialog-footer">
          <t-button variant="outline" @click="close">取消</t-button>
          <t-button theme="primary" :loading="submitting" @click="handleSubmit">提交</t-button>
        </div>
      </t-tab-panel>

      <t-tab-panel value="mine" label="我的反馈">
        <div v-if="list.length" class="feedback-list">
          <div v-for="f in list" :key="f.id" class="feedback-item">
            <div class="fb-head">
              <t-tag variant="light" size="small">{{ typeText(f.type) }}</t-tag>
              <t-tag variant="light" size="small" :theme="statusTheme(f.status)">
                {{ statusText(f.status) }}
              </t-tag>
              <span class="fb-time">{{ formatTime(f.createdAt) }}</span>
            </div>
            <div class="fb-content">{{ f.content }}</div>
          </div>
          <t-button v-if="hasMore" variant="text" class="load-more" @click="loadMore">加载更多</t-button>
        </div>
        <t-empty v-else description="暂无反馈记录" />
      </t-tab-panel>
    </t-tabs>
  </t-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { feedbackApi } from '@/api'
import type { Feedback } from '@/types'

const props = defineProps<{ visible: boolean }>()
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
}>()

const activeTab = ref('submit')
const feedbackType = ref('suggestion')
const feedbackContent = ref('')
const feedbackContact = ref('')
const submitting = ref(false)

const list = ref<Feedback[]>([])
const page = ref(1)
const total = ref(0)
const pageSize = 10

const hasMore = () => list.value.length < total.value

// 打开时重置表单并加载我的反馈
watch(
  () => props.visible,
  (v) => {
    if (v) {
      feedbackType.value = 'suggestion'
      feedbackContent.value = ''
      feedbackContact.value = ''
      loadMine()
    }
  },
)

function close() {
  emit('update:visible', false)
}

async function handleSubmit() {
  if (!feedbackContent.value.trim()) {
    MessagePlugin.error('请填写反馈内容')
    return
  }
  submitting.value = true
  try {
    const res = await feedbackApi.submit({
      type: feedbackType.value,
      content: feedbackContent.value.trim(),
      contact: feedbackContact.value.trim() || undefined,
    })
    if (res.code === 200) {
      MessagePlugin.success('反馈已提交，感谢你的支持！')
      feedbackContent.value = ''
      feedbackContact.value = ''
      loadMine()
    } else {
      MessagePlugin.error(res.message)
    }
  } catch {
    MessagePlugin.error('提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

async function loadMine() {
  try {
    const res = await feedbackApi.mine(1, pageSize)
    list.value = res.data.records
    total.value = res.data.total
    page.value = 1
  } catch {
    list.value = []
  }
}

async function loadMore() {
  page.value += 1
  try {
    const res = await feedbackApi.mine(page.value, pageSize)
    list.value = list.value.concat(res.data.records)
  } catch {
    page.value -= 1
  }
}

function typeText(t: string): string {
  return { bug: '问题反馈', suggestion: '功能建议', other: '其他' }[t] ?? t
}
function statusText(s: string): string {
  return { pending: '待处理', processing: '处理中', resolved: '已解决' }[s] ?? s
}
function statusTheme(s: string): 'default' | 'warning' | 'success' {
  return s === 'resolved' ? 'success' : s === 'processing' ? 'warning' : 'default'
}
function formatTime(t: string): string {
  return t ? t.replace('T', ' ').slice(0, 16) : ''
}
</script>

<style scoped>
.feedback-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 320px;
  overflow: auto;
}

.feedback-item {
  border: 1px solid var(--td-border-level-1-color);
  border-radius: var(--td-radius-default);
  padding: 12px;
}

.fb-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.fb-time {
  margin-left: auto;
  font-size: 12px;
  color: var(--td-text-color-placeholder);
}

.fb-content {
  font-size: 14px;
  color: var(--td-text-color-primary);
  word-break: break-word;
  white-space: pre-wrap;
}

.load-more {
  align-self: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
