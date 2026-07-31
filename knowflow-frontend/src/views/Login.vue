<template>
  <div class="login-page">
    <t-card :bordered="false" class="login-card">
      <h2 class="login-title">KnowFlow</h2>
      <p class="login-desc">智能学习助手平台</p>

      <t-form :data="formData" @submit="handleSubmit">
        <t-form-item name="username" label="用户名">
          <t-input v-model="formData.username" placeholder="请输入用户名" />
        </t-form-item>
        <t-form-item name="password" label="密码">
          <t-input v-model="formData.password" type="password" placeholder="请输入密码" />
        </t-form-item>
        <t-form-item v-if="isRegister" name="confirmPassword" label="确认密码">
          <t-input v-model="formData.confirmPassword" type="password" placeholder="请再次输入密码" />
        </t-form-item>
        <t-form-item>
          <t-button theme="primary" type="submit" block :loading="loading">
            {{ isRegister ? '注册' : '登录' }}
          </t-button>
        </t-form-item>
        <t-form-item>
          <t-link @click="toggleMode" theme="primary">
            {{ isRegister ? '已有账号？去登录' : '没有账号？去注册' }}
          </t-link>
        </t-form-item>
      </t-form>

      <t-alert v-if="errorMsg" theme="error" :message="errorMsg" style="margin-top: 16px" />
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
const isRegister = ref(false)
const errorMsg = ref('')

const formData = reactive({
  username: '',
  password: '',
  confirmPassword: '',
})

function toggleMode() {
  isRegister.value = !isRegister.value
  errorMsg.value = ''
}

async function handleSubmit() {
  errorMsg.value = ''

  if (!formData.username || !formData.password) {
    errorMsg.value = '请填写用户名和密码'
    return
  }

  if (isRegister.value) {
    if (formData.password.length < 6) {
      errorMsg.value = '密码长度不能少于 6 位'
      return
    }
    if (formData.password !== formData.confirmPassword) {
      errorMsg.value = '两次密码输入不一致'
      return
    }
  }

  loading.value = true
  try {
    if (isRegister.value) {
      await userStore.register(formData.username, formData.password)
    } else {
      await userStore.login(formData.username, formData.password)
    }
    router.push({ name: 'Chat' })
  } catch (e: any) {
    errorMsg.value = e?.response?.data?.message || e?.message || '操作失败，请重试'
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
