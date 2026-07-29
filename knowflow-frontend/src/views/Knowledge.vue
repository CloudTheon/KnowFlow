<template>
  <div class="knowledge-page">
    <t-card title="知识库" :bordered="false">
      <template #actions>
        <t-button theme="primary">
          <template #icon><upload-icon /></template>
          上传文档
        </t-button>
      </template>
      <t-empty description="暂无文档" />
    </t-card>
  </div>
</template>

<script setup lang="ts">
import { UploadIcon } from 'tdesign-icons-vue-next'
</script>

<style scoped>
.knowledge-page {
  max-width: 1000px;
  margin: 0 auto;
}
</style>
