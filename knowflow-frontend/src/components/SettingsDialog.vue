<template>
  <t-dialog :visible="visible" header="系统设置" :footer="false" width="480px" @close="close">
    <t-tabs v-model="activeTab">
      <t-tab-panel value="profile" label="修改资料">
        <div class="profile-section">
          <t-avatar :size="72" :image="previewAvatar" @image-failed="avatarFailed = true">
            {{ avatarText }}
          </t-avatar>
          <t-input v-model="avatar" placeholder="头像图片 URL（留空则显示用户名首字母）" clearable />
          <p class="tip">头像支持任意图片地址，保存后立即生效。</p>
        </div>
      </t-tab-panel>

      <t-tab-panel value="password" label="修改密码">
        <div class="password-section">
          <t-input v-model="oldPassword" type="password" placeholder="原密码" clearable />
          <t-input v-model="newPassword" type="password" placeholder="新密码（至少 6 位）" clearable />
          <t-input v-model="confirmPassword" type="password" placeholder="确认新密码" clearable />
        </div>
      </t-tab-panel>
    </t-tabs>

    <template #footer>
      <div class="dialog-footer">
        <t-button variant="outline" @click="close">取消</t-button>
        <t-button theme="primary" :loading="saving" @click="handleSave">保存</t-button>
      </div>
    </template>
  </t-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { userApi } from '@/api'
import { useUserStore } from '@/stores/user'

const props = defineProps<{ visible: boolean }>()
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'saved'): void
}>()

const userStore = useUserStore()

const activeTab = ref('profile')
const avatar = ref('')
const avatarFailed = ref(false)
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const saving = ref(false)

const avatarText = computed(() => {
  const name = userStore.user?.username ?? ''
  return name ? name.charAt(0).toUpperCase() : '?'
})
const previewAvatar = computed(() => (avatarFailed.value ? '' : avatar.value))

// 打开时回填当前资料
watch(
  () => props.visible,
  (v) => {
    if (v) {
      avatar.value = userStore.user?.avatar ?? ''
      avatarFailed.value = false
      oldPassword.value = ''
      newPassword.value = ''
      confirmPassword.value = ''
    }
  },
)

function close() {
  emit('update:visible', false)
}

async function handleSave() {
  if (activeTab.value === 'profile') {
    saving.value = true
    try {
      const res = await userApi.updateProfile(avatar.value || '')
      if (res.code === 200) {
        MessagePlugin.success('资料已更新')
        emit('saved')
        close()
      } else {
        MessagePlugin.error(res.message)
      }
    } catch {
      MessagePlugin.error('更新失败，请重试')
    } finally {
      saving.value = false
    }
  } else {
    if (newPassword.value !== confirmPassword.value) {
      MessagePlugin.error('两次输入的新密码不一致')
      return
    }
    if (newPassword.value.length < 6) {
      MessagePlugin.error('新密码至少需要 6 位')
      return
    }
    saving.value = true
    try {
      const res = await userApi.updatePassword(oldPassword.value, newPassword.value)
      if (res.code === 200) {
        MessagePlugin.success('密码修改成功，下次登录请使用新密码')
        oldPassword.value = ''
        newPassword.value = ''
        confirmPassword.value = ''
        close()
      } else {
        MessagePlugin.error(res.message)
      }
    } catch {
      MessagePlugin.error('修改失败，请重试')
    } finally {
      saving.value = false
    }
  }
}
</script>

<style scoped>
.profile-section,
.password-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: flex-start;
}

.profile-section .t-avatar {
  align-self: center;
}

.password-section .t-input {
  width: 100%;
}

.profile-section .t-input {
  width: 100%;
}

.tip {
  margin: 0;
  font-size: 12px;
  color: var(--td-text-color-placeholder);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
