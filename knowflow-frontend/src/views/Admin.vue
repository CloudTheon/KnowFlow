<template>
  <div class="admin-page">
    <t-card title="系统管理" :bordered="false" class="admin-card">
      <t-tabs v-model="tab" @change="onTabChange">
        <!-- 数据概览 -->
        <t-tab-panel value="overview" label="数据概览">
          <div class="stat-grid">
            <div v-for="s in stats" :key="s.key" class="stat-card">
              <div class="stat-value">{{ s.value }}</div>
              <div class="stat-label">{{ s.label }}</div>
            </div>
          </div>
        </t-tab-panel>

        <!-- 用户管理 -->
        <t-tab-panel value="users" label="用户管理">
          <div class="user-toolbar">
            <t-input
              v-model="keyword"
              placeholder="按用户名搜索"
              clearable
              @enter="loadUsers(1)"
              @clear="loadUsers(1)"
            />
            <t-button theme="primary" @click="loadUsers(1)">搜索</t-button>
          </div>
          <t-table
            :data="users"
            :columns="userColumns"
            :loading="usersLoading"
            :pagination="userPagination"
            row-key="id"
            @change="onUserPage"
          >
            <template #role="{ row }">
              <t-tag :theme="row.role === 'admin' ? 'warning' : 'default'" variant="light">
                {{ row.role === 'admin' ? '管理员' : '用户' }}
              </t-tag>
            </template>
            <template #status="{ row }">
              <t-tag :theme="row.status === 'enabled' ? 'success' : 'danger'" variant="light">
                {{ row.status === 'enabled' ? '正常' : '禁用' }}
              </t-tag>
            </template>
            <template #op="{ row }">
              <t-button
                v-if="row.status === 'enabled'"
                variant="text"
                theme="danger"
                @click="toggleUser(row)"
              >
                禁用
              </t-button>
              <t-button v-else variant="text" theme="success" @click="toggleUser(row)">启用</t-button>
            </template>
          </t-table>
        </t-tab-panel>

        <!-- 系统配置 -->
        <t-tab-panel value="config" label="系统配置">
          <div class="config-form">
            <t-form :data="config" label-align="top" class="config-form-inner">
              <t-form-item label="站点名称">
                <t-input v-model="config.siteName" placeholder="如：KnowFlow" />
              </t-form-item>
              <t-form-item label="站点公告">
                <t-textarea v-model="config.notice" :rows="3" placeholder="展示在登录页或首页的公告" />
              </t-form-item>
              <t-form-item>
                <t-button theme="primary" :loading="configSaving" @click="saveConfig">保存配置</t-button>
              </t-form-item>
            </t-form>
          </div>
        </t-tab-panel>
      </t-tabs>
    </t-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { adminApi } from '@/api'
import type { AdminOverview, AdminUser } from '@/types'

const tab = ref('overview')

// ---- 数据概览 ----
const stats = reactive([
  { key: 'userCount', label: '用户数', value: 0 },
  { key: 'conversationCount', label: '对话数', value: 0 },
  { key: 'documentCount', label: '文档数', value: 0 },
  { key: 'feedbackCount', label: '反馈数', value: 0 },
])

async function loadOverview() {
  try {
    const res = await adminApi.overview()
    if (res.code === 200) {
      const d = res.data as AdminOverview
      stats.forEach((s) => {
        s.value = (d as any)[s.key] ?? 0
      })
    }
  } catch {
    MessagePlugin.error('加载概览失败')
  }
}

// ---- 用户管理 ----
const users = ref<AdminUser[]>([])
const usersLoading = ref(false)
const keyword = ref('')
const userPage = ref(1)
const userPagination = reactive({ total: 0, current: 1, pageSize: 10, showJumper: true })

const userColumns = [
  { colKey: 'id', title: 'ID', width: 70 },
  { colKey: 'username', title: '用户名' },
  { colKey: 'role', title: '角色', width: 100 },
  { colKey: 'status', title: '状态', width: 100 },
  { colKey: 'createdAt', title: '注册时间', width: 190 },
  { colKey: 'op', title: '操作', width: 100 },
]

async function loadUsers(page = 1) {
  usersLoading.value = true
  userPage.value = page
  try {
    const res = await adminApi.users(page, userPagination.pageSize, keyword.value || undefined)
    users.value = res.data.records
    userPagination.total = res.data.total
    userPagination.current = page
  } catch {
    MessagePlugin.error('加载用户列表失败')
  } finally {
    usersLoading.value = false
  }
}

async function toggleUser(row: AdminUser) {
  const next = row.status === 'enabled' ? 'disabled' : 'enabled'
  try {
    const res = await adminApi.updateUserStatus(row.id, next)
    if (res.code === 200) {
      MessagePlugin.success(next === 'disabled' ? `已禁用 ${row.username}` : `已启用 ${row.username}`)
      loadUsers(userPage.value)
    } else {
      MessagePlugin.error(res.message)
    }
  } catch {
    MessagePlugin.error('操作失败')
  }
}

function onUserPage(pageInfo: any) {
  loadUsers(pageInfo.current)
}

// ---- 系统配置 ----
const config = reactive<Record<string, string>>({ siteName: '', notice: '' })
const configSaving = ref(false)

async function loadConfig() {
  try {
    const res = await adminApi.getConfig()
    if (res.code === 200) {
      config.siteName = res.data.siteName || ''
      config.notice = res.data.notice || ''
    }
  } catch {
    // 忽略
  }
}

async function saveConfig() {
  configSaving.value = true
  try {
    const res = await adminApi.updateConfig({ siteName: config.siteName, notice: config.notice })
    if (res.code === 200) {
      MessagePlugin.success('配置已保存')
    } else {
      MessagePlugin.error(res.message)
    }
  } catch {
    MessagePlugin.error('保存失败')
  } finally {
    configSaving.value = false
  }
}

function onTabChange(value: string | number) {
  if (value === 'overview') loadOverview()
  else if (value === 'users') loadUsers(1)
  else if (value === 'config') loadConfig()
}

onMounted(loadOverview)
</script>

<style scoped>
.admin-page {
  max-width: 1000px;
  width: 100%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  min-height: 0;
  min-width: 0;
}

.admin-card {
  flex: 1;
  min-height: 0;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.stat-card {
  background: var(--td-bg-color-container-hover);
  border-radius: var(--td-radius-default);
  padding: 24px;
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--td-brand-color);
}

.stat-label {
  margin-top: 8px;
  font-size: 14px;
  color: var(--td-text-color-secondary);
}

.user-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  max-width: 360px;
}

.config-form {
  max-width: 480px;
}

.config-form-inner {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>
