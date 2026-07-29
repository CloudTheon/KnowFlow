<template>
  <div class="login-page">
    <t-card :bordered="false" class="login-card">
      <h2 class="login-title">KnowFlow</h2>
      <p class="login-desc">智能学习助手平台</p>

      <t-form :data="formData" @submit="handleLogin">
        <t-form-item name="username" label="用户名">
          <t-input v-model="formData.username" placeholder="请输入用户名" />
        </t-form-item>
        <t-form-item name="password" label="密码">
          <t-input v-model="formData.password" type="password" placeholder="请输入密码" />
        </t-form-item>
        <t-form-item>
          <t-button theme="primary" type="submit" block :loading="loading">
            登录
          </t-button>
        </t-form-item>
      </t-form>
    </t-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const formData = reactive({
  username: '',
  password: '',
})

async function handleLogin() {
  loading.value = true
  try {
    await userStore.login(formData.username, formData.password)
    router.push({ name: 'Chat' })
  } catch {
    // 错误由请求拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--td-bg-color-page);
}

.login-card {
  width: 400px;
}

.login-title {
  text-align: center;
  color: var(--td-brand-color);
  margin-bottom: 4px;
}

.login-desc {
  text-align: center;
  color: var(--td-text-color-secondary);
  margin-bottom: 24px;
}
</style>
