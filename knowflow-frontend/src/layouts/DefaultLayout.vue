<template>
  <t-layout>
    <!-- 侧边栏 -->
    <t-aside>
      <div class="logo-area">
        <span class="logo-text">KnowFlow</span>
      </div>
      <t-menu :value="activeMenu" :collapsed="false" @change="handleMenuChange">
        <t-menu-item value="chat">
          <template #icon><chat-icon /></template>
          智能对话
        </t-menu-item>
        <t-menu-item value="knowledge">
          <template #icon><book-icon /></template>
          知识库
        </t-menu-item>
      </t-menu>

      <div class="user-area">
        <t-button variant="text" shape="square" @click="handleLogout">
          <template #icon><logout-icon /></template>
        </t-button>
      </div>
    </t-aside>

    <!-- 主内容区 -->
    <t-layout>
      <t-content>
        <router-view />
      </t-content>
    </t-layout>
  </t-layout>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ChatIcon, BookIcon, LogoutIcon } from 'tdesign-icons-vue-next'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.name as string)

function handleMenuChange(value: string) {
  router.push({ name: value })
}

function handleLogout() {
  userStore.logout()
  router.push({ name: 'Login' })
}
</script>

<style scoped>
.t-layout {
  height: 100vh;
}

.t-aside {
  display: flex;
  flex-direction: column;
  background: var(--td-bg-color-component);
  border-right: 1px solid var(--td-border-level-1-color);
}

.logo-area {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid var(--td-border-level-1-color);
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: var(--td-brand-color);
}

.t-menu {
  flex: 1;
  border-right: none;
}

.user-area {
  padding: 12px;
  border-top: 1px solid var(--td-border-level-1-color);
  display: flex;
  justify-content: center;
}

.t-content {
  padding: 24px;
  background: var(--td-bg-color-page);
  min-height: 100vh;
}
</style>
