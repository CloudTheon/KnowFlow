<template>
  <div class="knowledge-page">
    <t-card title="知识库" :bordered="false" class="knowledge-card">
      <template #actions>
        <div class="upload-trigger">
          <input
            ref="fileInputRef"
            type="file"
            accept=".pdf,.md"
            class="upload-input"
            @change="onFileSelect"
          />
          <t-button theme="primary" :loading="uploading" class="upload-btn" @click="fileInputRef?.click()">
            <template #icon><upload-icon /></template>
            上传文档
          </t-button>
        </div>
      </template>

      <div v-if="!loading && docs.length === 0" class="kb-empty">
        <t-empty description="暂无文档，上传你的学习资料开始 RAG 问答" />
      </div>

      <t-table
        v-else
        :data="docs"
        :columns="columns"
        :loading="loading"
        row-key="id"
        :pagination="pagination"
        @change="onPageChange"
      >
        <template #fileType="{ row }">
          <t-tag variant="light" :theme="row.fileType === 'pdf' ? 'danger' : 'primary'">
            {{ row.fileType === 'pdf' ? 'PDF' : 'MD' }}
          </t-tag>
        </template>

        <template #fileSize="{ row }">
          {{ formatSize(row.fileSize) }}
        </template>

        <template #status="{ row }">
          <t-tag v-if="row.status === 'ready'" theme="success" variant="light">已就绪</t-tag>
          <t-tag v-else-if="row.status === 'processing'" theme="warning" variant="light" loading>
            处理中
          </t-tag>
          <t-tooltip v-else :content="row.errorMsg || '处理失败'">
            <t-tag theme="danger" variant="light">处理失败</t-tag>
          </t-tooltip>
        </template>

        <template #op="{ row }">
          <t-popconfirm content="确定删除该文档吗？删除后不可恢复" @confirm="handleDelete(row)">
            <t-button variant="text" theme="danger">删除</t-button>
          </t-popconfirm>
        </template>
      </t-table>
    </t-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { UploadIcon } from 'tdesign-icons-vue-next'
import { knowledgeApi } from '@/api'
import type { KnowledgeDocument } from '@/types'

const MAX_SIZE = 20 * 1024 * 1024

const docs = ref<KnowledgeDocument[]>([])
const loading = ref(false)
const uploading = ref(false)
const fileInputRef = ref<HTMLInputElement>()

const page = ref(1)
const pageSize = ref(10)

const pagination = ref({
  total: 0,
  current: 1,
  pageSize: 10,
  showJumper: true,
})

const columns = [
  { colKey: 'fileName', title: '文件名', ellipsis: true },
  { colKey: 'fileType', title: '类型', width: 90 },
  { colKey: 'fileSize', title: '大小', width: 110 },
  { colKey: 'status', title: '状态', width: 120 },
  { colKey: 'createdAt', title: '上传时间', width: 190 },
  { colKey: 'op', title: '操作', width: 90 },
]

async function loadList() {
  loading.value = true
  try {
    const res = await knowledgeApi.list(page.value, pageSize.value)
    docs.value = res.data.records
    pagination.value.total = res.data.total
    pagination.value.current = page.value
  } finally {
    loading.value = false
  }
}

/** 选择文件后触发上传 */
function onFileSelect(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (input) input.value = '' // 允许重复选择同一文件
  if (file) handleUpload(file)
}

async function handleUpload(file: File) {
  if (!/\.(pdf|md)$/i.test(file.name)) {
    MessagePlugin.error('仅支持 PDF / Markdown 格式')
    return
  }
  if (file.size > MAX_SIZE) {
    MessagePlugin.error('文件大小不能超过 20MB')
    return
  }
  uploading.value = true
  try {
    const res = await knowledgeApi.upload(file)
    if (res.code === 200) {
      if (res.data.status === 'failed') {
        MessagePlugin.warning(`上传成功但处理失败：${res.data.errorMsg || '未知原因'}`)
      } else {
        MessagePlugin.success('上传成功，正在向量化')
      }
      await loadList()
    } else {
      MessagePlugin.error(res.message)
    }
  } catch {
    MessagePlugin.error('上传失败，请重试')
  } finally {
    uploading.value = false
  }
}

async function handleDelete(row: KnowledgeDocument) {
  try {
    await knowledgeApi.delete(row.id)
    MessagePlugin.success('删除成功')
    // 删除后若当前页为空则回退一页
    if (docs.value.length === 1 && page.value > 1) {
      page.value -= 1
    }
    await loadList()
  } catch {
    MessagePlugin.error('删除失败，请重试')
  }
}

function onPageChange(pageInfo: any) {
  page.value = pageInfo.current
  pageSize.value = pageInfo.pageSize
  loadList()
}

function formatSize(bytes: number): string {
  if (!bytes) return '-'
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}

onMounted(loadList)
</script>

<style scoped>
.knowledge-page {
  flex: 1;
  max-width: 1000px;
  width: 100%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  min-height: 0;
  min-width: 0;
}

.knowledge-card {
  flex: 1;
  min-height: 0;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.knowledge-card :deep(.t-card__body) {
  flex: 1;
  min-height: 0;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* TDesign t-card 内部有一层 loading 包裹，需让其参与 flex 撑满 */
.knowledge-card :deep(.t-loading__parent) {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

/* 表格撑满卡片内容区，主体滚动、分页贴底 */
.knowledge-card :deep(.t-table) {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.knowledge-card :deep(.t-table__content) {
  flex: 1;
  min-height: 0;
  overflow: auto;
}

.knowledge-card :deep(.t-table__pagination-wrap) {
  flex-shrink: 0;
}

/* 卡片头部（标题 + 上传按钮）适配 */
.knowledge-card :deep(.t-card__header) {
  flex-wrap: wrap;
  gap: 8px;
}

.knowledge-card :deep(.t-card__actions) {
  min-width: 0;
  max-width: 100%;
}

.upload-trigger {
  display: inline-flex;
  align-items: center;
}

.upload-input {
  display: none;
}

.upload-btn {
  white-space: nowrap;
}

/* 空态：在卡片内容区垂直水平居中 */
.kb-empty {
  flex: 1;
  min-height: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 窄屏适配 */
@media (max-width: 640px) {
  .knowledge-card :deep(.t-card__actions) {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
