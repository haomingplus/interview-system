<template>
  <el-container class="main-layout">
    <!-- 顶部导航 -->
    <el-header class="header">
      <div class="header-content">
        <div class="logo" @click="router.push('/')">
          <el-icon size="24"><Reading /></el-icon>
          <span class="logo-text">面试知识库</span>
        </div>

        <!-- PC端导航菜单 -->
        <el-menu
          v-if="!isMobile"
          mode="horizontal"
          :default-active="activeMenu"
          :ellipsis="false"
          class="nav-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/questions">题库</el-menu-item>
          <el-menu-item index="/categories">分类</el-menu-item>
          <el-menu-item index="/learning">学习中心</el-menu-item>
          <el-menu-item index="/plan">学习计划</el-menu-item>
        </el-menu>

        <!-- 搜索框 -->
        <el-input
          v-if="!isMobile"
          v-model="searchKeyword"
          placeholder="搜索题目..."
          class="search-input"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <!-- 用户操作 -->
        <div class="header-actions">
          <template v-if="userStore.isLoggedIn">
            <el-dropdown @command="handleUserCommand">
              <div class="user-info">
                <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                  {{ userStore.userInfo?.nickname?.charAt(0) }}
                </el-avatar>
                <span v-if="!isMobile" class="username">{{ userStore.userInfo?.nickname }}</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="user">
                    <el-icon><User /></el-icon>个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="favorites">
                    <el-icon><Star /></el-icon>我的收藏
                  </el-dropdown-item>
                  <el-dropdown-item command="statistics">
                    <el-icon><DataAnalysis /></el-icon>学习统计
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" @click="router.push('/login')">登录</el-button>
            <el-button v-if="!isMobile" @click="router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </el-header>

    <!-- 主内容区 -->
    <el-main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </el-main>

    <!-- 底部 -->
    <el-footer v-if="!isMobile" class="footer">
      <div class="footer-content">
        <p>面试知识库 &copy; {{ new Date().getFullYear() }}</p>
        <p>专业的面试知识学习平台</p>
      </div>
    </el-footer>

    <!-- 移动端底部导航 -->
    <div v-if="isMobile" class="mobile-nav">
      <div
        v-for="item in mobileNavItems"
        :key="item.path"
        class="nav-item"
        :class="{ active: isActiveMobileNav(item.path) }"
        @click="router.push(item.path)"
      >
        <el-icon :size="20"><component :is="item.icon" /></el-icon>
        <span>{{ item.label }}</span>
      </div>
    </div>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCategoryStore } from '@/stores/category'
import {
  Reading,
  Search,
  User,
  Star,
  DataAnalysis,
  SwitchButton,
  House,
  Collection,
  Grid,
  TrendCharts,
  UserFilled,
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const categoryStore = useCategoryStore()

const searchKeyword = ref('')
const isMobile = ref(false)

const activeMenu = computed(() => route.path)

const mobileNavItems = [
  { path: '/', label: '首页', icon: House },
  { path: '/questions', label: '题库', icon: Collection },
  { path: '/categories', label: '分类', icon: Grid },
  { path: '/learning', label: '学习', icon: TrendCharts },
  { path: '/user', label: '我的', icon: UserFilled },
]

function checkMobile() {
  isMobile.value = window.innerWidth < 768
}

function isActiveMobileNav(path: string): boolean {
  if (path === '/') {
    return route.path === '/'
  }
  return route.path.startsWith(path)
}

function handleMenuSelect(index: string) {
  router.push(index)
}

function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/questions', query: { keyword: searchKeyword.value } })
  }
}

function handleUserCommand(command: string) {
  switch (command) {
    case 'user':
      router.push('/user')
      break
    case 'favorites':
      router.push('/user/favorites')
      break
    case 'statistics':
      router.push('/learning/statistics')
      break
    case 'logout':
      userStore.logout()
      break
  }
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  categoryStore.loadCategoryTree()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style lang="scss" scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0;
  height: 60px;
  position: sticky;
  top: 0;
  z-index: 100;

  .header-content {
    max-width: 1400px;
    margin: 0 auto;
    height: 100%;
    display: flex;
    align-items: center;
    padding: 0 20px;
    gap: 20px;
  }
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: var(--el-color-primary);
  font-weight: bold;
  white-space: nowrap;

  .logo-text {
    font-size: 18px;
  }
}

.nav-menu {
  flex: 1;
  border-bottom: none;

  :deep(.el-menu-item) {
    height: 60px;
    line-height: 60px;
  }
}

.search-input {
  width: 240px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;

  .username {
    font-size: 14px;
    color: #333;
  }
}

.main-content {
  flex: 1;
  background: #f5f7fa;
  padding: 20px;

  @media (max-width: 768px) {
    padding: 10px;
    padding-bottom: 70px;
  }
}

.footer {
  background: #fff;
  text-align: center;
  padding: 20px;
  color: #666;
  font-size: 14px;

  .footer-content p {
    margin: 5px 0;
  }
}

.mobile-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  display: flex;
  justify-content: space-around;
  padding: 8px 0;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
  z-index: 100;

  .nav-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2px;
    color: #666;
    font-size: 12px;
    cursor: pointer;
    padding: 4px 12px;

    &.active {
      color: var(--el-color-primary);
    }
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
