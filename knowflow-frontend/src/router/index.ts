import { createRouter, createWebHistory } from 'vue-router'
import { isAuthenticated } from '@/utils/auth'
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

// 路由守卫：未登录跳转登录页
router.beforeEach((to) => {
  if (to.name !== 'Login' && !isAuthenticated()) {
    return { name: 'Login' }
  }
})

export default router
