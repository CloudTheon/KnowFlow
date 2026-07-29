import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UserInfo } from '@/types'
import { userApi } from '@/api'
import { setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const user = ref<UserInfo | null>(null)
  const loggedIn = ref(false)

  async function login(username: string, password: string) {
    const res = await userApi.login(username, password)
    setToken(res.data.token)
    user.value = res.data.user
    loggedIn.value = true
  }

  async function fetchProfile() {
    const res = await userApi.profile()
    user.value = res.data
    loggedIn.value = true
  }

  function logout() {
    removeToken()
    user.value = null
    loggedIn.value = false
  }

  return { user, loggedIn, login, fetchProfile, logout }
})
