import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页' },
      },
      {
        path: 'questions',
        name: 'QuestionList',
        component: () => import('@/views/question/list.vue'),
        meta: { title: '题库' },
      },
      {
        path: 'questions/:id',
        name: 'QuestionDetail',
        component: () => import('@/views/question/detail.vue'),
        meta: { title: '题目详情' },
      },
      {
        path: 'categories',
        name: 'CategoryList',
        component: () => import('@/views/category/index.vue'),
        meta: { title: '分类' },
      },
      {
        path: 'categories/:id',
        name: 'CategoryQuestions',
        component: () => import('@/views/category/questions.vue'),
        meta: { title: '分类题目' },
      },
      {
        path: 'learning',
        name: 'Learning',
        component: () => import('@/views/learning/index.vue'),
        meta: { title: '学习中心', requiresAuth: true },
      },
      {
        path: 'learning/statistics',
        name: 'Statistics',
        component: () => import('@/views/learning/statistics.vue'),
        meta: { title: '学习统计', requiresAuth: true },
      },
      {
        path: 'plan',
        name: 'PlanList',
        component: () => import('@/views/plan/index.vue'),
        meta: { title: '学习计划', requiresAuth: true },
      },
      {
        path: 'plan/:id',
        name: 'PlanDetail',
        component: () => import('@/views/plan/detail.vue'),
        meta: { title: '计划详情', requiresAuth: true },
      },
      {
        path: 'user',
        name: 'UserCenter',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '个人中心', requiresAuth: true },
      },
      {
        path: 'user/favorites',
        name: 'Favorites',
        component: () => import('@/views/user/favorites.vue'),
        meta: { title: '我的收藏', requiresAuth: true },
      },
    ],
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/login.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/register.vue'),
    meta: { title: '注册' },
  },
  {
    path: '/share/:code',
    name: 'Share',
    component: () => import('@/views/question/share.vue'),
    meta: { title: '分享' },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '404' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  },
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title || ''} - 面试知识库`

  const userStore = useUserStore()

  // 需要登录的页面
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({
      path: '/login',
      query: { redirect: to.fullPath },
    })
    return
  }

  // 已登录用户访问登录/注册页
  if ((to.name === 'Login' || to.name === 'Register') && userStore.isLoggedIn) {
    next('/')
    return
  }

  next()
})

export default router
