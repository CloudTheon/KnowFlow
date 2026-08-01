import { createRouter, createWebHistory } from 'vue-router'
import { isAuthenticated } from '@/utils/auth'
import { useUserStore } from '@/stores/user'
import DefaultLayout from '@/layouts/DefaultLayout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: DefaultLayout,
      redirect: '/chat',
      children: [
        {
          path: 'chat',
          name: 'Chat',
          component: () => import('@/views/Chat.vue'),
          meta: { title: '智能对话' },
        },
        {
          path: 'knowledge',
          name: 'Knowledge',
          component: () => import('@/views/Knowledge.vue'),
          meta: { title: '知识库' },
        },
        {
          path: 'agent',
          name: 'Agent',
          component: () => import('@/views/Agent.vue'),
          meta: { title: '智能体' },
        },
        {
          path: 'admin',
          name: 'Admin',
          component: () => import('@/views/Admin.vue'),
          meta: { title: '系统管理', requiresAdmin: true },
        },
      ],
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { title: '登录' },
    },
  ],
})

// 路由守卫：未登录跳转登录页；非管理员禁止访问管理页
router.beforeEach(async (to) => {
  if (to.name !== 'Login' && !isAuthenticated()) {
    return { name: 'Login' }
  }
  if (to.meta.requiresAdmin) {
    const userStore = useUserStore()
    // 刷新页面后 store 中无用户信息，先拉取一次
    if (!userStore.user) {
      try {
        await userStore.fetchProfile()
      } catch {
        // token 失效等情况由请求层统一处理
      }
    }
    if (userStore.user?.role !== 'admin') {
      return { name: 'Chat' }
    }
  }
})

export default router
