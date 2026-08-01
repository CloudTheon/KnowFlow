<template>
  <div class="agent-page">
    <t-card title="Agent 智能体" :bordered="false" class="agent-card">
      <template #description>
        多步骤任务自主规划（ReAct 模式），可调用代码执行 / Web 搜索工具获取信息
      </template>

      <div class="agent-form">
        <t-radio-group v-model="mode">
          <t-radio value="general">通用任务</t-radio>
          <t-radio value="learning-path">学习路径规划</t-radio>
        </t-radio-group>

        <t-textarea
          v-model="content"
          :rows="4"
          placeholder="描述你的任务，例如：帮我制定一个 3 个月的 Java 进阶学习路线，或：分析这段代码的时间复杂度"
        />

        <div class="actions">
          <t-button theme="primary" :loading="running" @click="runTask">
            <template #icon><robot-icon /></template>
            开始执行
          </t-button>
          <t-button variant="outline" v-if="result" @click="clear">清空</t-button>
        </div>
      </div>

      <div v-if="result" class="agent-result">
        <t-divider align="left">执行结果</t-divider>
        <pre class="result-content">{{ result }}</pre>
      </div>
    </t-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { RobotIcon } from 'tdesign-icons-vue-next'
import { agentApi } from '@/api'

const mode = ref('general')
const content = ref('')
const running = ref(false)
const result = ref('')

async function runTask() {
  if (!content.value.trim()) {
    MessagePlugin.error('请先描述你的任务')
    return
  }
  running.value = true
  result.value = ''
  try {
    const res = await agentApi.task(content.value.trim(), mode.value)
    if (res.code === 200) {
      result.value = res.data.result
    } else {
      MessagePlugin.error(res.message)
    }
  } catch {
    MessagePlugin.error('任务执行失败，请稍后重试')
  } finally {
    running.value = false
  }
}

function clear() {
  result.value = ''
  content.value = ''
}
</script>

<style scoped>
.agent-page {
  max-width: 900px;
  width: 100%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  min-height: 0;
  min-width: 0;
}

.agent-card {
  flex: 1;
  min-height: 0;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.agent-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.actions {
  display: flex;
  gap: 8px;
}

.agent-result {
  margin-top: 8px;
  min-height: 0;
  overflow: auto;
}

.result-content {
  margin: 0;
  padding: 16px;
  background: var(--td-bg-color-container-hover);
  border-radius: var(--td-radius-default);
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
  color: var(--td-text-color-primary);
}
</style>
