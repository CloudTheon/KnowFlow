<template>
  <t-config-provider :global-config="{ classPrefix: 't' }">
    <router-view />
  </t-config-provider>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { isAuthenticated } from '@/utils/auth'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 应用启动时恢复登录用户信息（刷新页面后 token 仍在，但 user 为空）
onMounted(async () => {
  if (isAuthenticated() && !userStore.user) {
    try {
      await userStore.fetchProfile()
    } catch {
      // token 失效等情况由请求拦截器统一处理
    }
  }
})
</script>

<style>
@import './styles/index.css';
</style>
