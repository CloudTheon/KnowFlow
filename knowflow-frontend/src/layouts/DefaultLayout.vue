<template>
  <t-layout class="root-layout">
    <!-- 顶部导航栏 -->
    <t-header>
      <div class="header-inner">
        <span class="logo-text">KnowFlow</span>

        <!-- 平行导航按钮 -->
        <div class="nav-buttons">
          <t-button
            variant="text"
            class="nav-btn"
            :class="{ active: route.name === 'Chat' }"
            @click="router.push({ name: 'Chat' })"
          >
            <template #icon><chat-icon /></template>
            智能对话
          </t-button>
          <t-button
            variant="text"
            class="nav-btn"
            :class="{ active: route.name === 'Knowledge' }"
            @click="router.push({ name: 'Knowledge' })"
          >
            <template #icon><book-icon /></template>
            知识库
          </t-button>
        </div>

        <!-- 用户菜单 -->
        <t-dropdown trigger="click" placement="bottom-right" :options="dropdownOptions" @click="handleDropdownClick">
          <div class="user-trigger">
            <t-avatar
              class="user-avatar"
              size="32px"
              :image="avatarUrl"
              @image-failed="handleAvatarFailed"
            >
              {{ avatarText }}
            </t-avatar>
            <span class="user-name">{{ userStore.user?.username }}</span>
            <t-icon name="more" size="18px" />
          </div>
        </t-dropdown>
      </div>
    </t-header>

    <!-- 主内容区 -->
    <t-content>
      <router-view />
    </t-content>

    <!-- 系统设置 / 帮助与反馈弹窗 -->
    <SettingsDialog v-model:visible="settingsVisible" @saved="handleSettingsSaved" />
    <FeedbackDialog v-model:visible="helpVisible" />
  </t-layout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ChatIcon, BookIcon } from 'tdesign-icons-vue-next'
import type { DropdownOption } from 'tdesign-vue-next'
import { useUserStore } from '@/stores/user'
import SettingsDialog from '@/components/SettingsDialog.vue'
import FeedbackDialog from '@/components/FeedbackDialog.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

/** 用户下拉菜单选项 */
const dropdownOptions: DropdownOption[] = [
  { content: '系统设置', value: 'settings' },
  { content: '帮助与反馈', value: 'help' },
  { content: '退出登录', value: 'logout' },
]

/** 弹窗显隐 */
const settingsVisible = ref(false)
const helpVisible = ref(false)

/** 头像加载失败时回退为用户名首字母 */
const avatarFailed = ref(false)

const avatarText = computed(() => {
  const name = userStore.user?.username ?? ''
  return name ? name.charAt(0).toUpperCase() : '?'
})

const avatarUrl = computed(() =>
  avatarFailed.value ? '' : (userStore.user?.avatar ?? ''),
)

function handleAvatarFailed() {
  avatarFailed.value = true
}

function handleDropdownClick(option: DropdownOption) {
  if (option.value === 'settings') {
    settingsVisible.value = true
  } else if (option.value === 'help') {
    helpVisible.value = true
  } else if (option.value === 'logout') {
    userStore.logout()
    router.push({ name: 'Login' })
  }
}

/** 设置保存后刷新用户信息 */
async function handleSettingsSaved() {
  try {
    await userStore.fetchProfile()
  } catch {
    // 忽略刷新失败
  }
}
</script>

<style scoped>
.root-layout {
  height: 100vh;
  overflow: hidden;
}

/* 顶部导航栏 */
:deep(.t-layout__header) {
  display: flex;
  align-items: center;
  border-bottom: 1px solid var(--td-border-level-1-color);
  background: var(--td-bg-color-container);
}

.header-inner {
  flex: 1;
  min-width: 0;
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  gap: 32px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: var(--td-brand-color);
  white-space: nowrap;
}

.nav-buttons {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-btn {
  height: 36px;
  padding: 0 16px;
  border-radius: var(--td-radius-default);
  color: var(--td-text-color-primary);
  white-space: nowrap;
}

.nav-btn.active {
  color: var(--td-brand-color);
  background: var(--td-brand-color-light);
  font-weight: 600;
}

/* 用户菜单 */
.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  border-radius: var(--td-radius-default);
  cursor: pointer;
  transition: background-color 0.2s;
}

.user-trigger:hover {
  background: var(--td-bg-color-container-hover);
}

.user-avatar {
  flex-shrink: 0;
  background: var(--td-brand-color);
  color: #fff;
  font-weight: 600;
}

.user-name {
  font-size: 14px;
  color: var(--td-text-color-primary);
  white-space: nowrap;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 内容区（TDesign t-content 根元素 class 是 .t-layout__content） */
:deep(.t-layout__content) {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: 24px;
  background: var(--td-bg-color-page);
  overflow: hidden;
  box-sizing: border-box;
}
</style>
