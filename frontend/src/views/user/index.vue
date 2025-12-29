<template>
  <div class="user-center-page">
    <div class="page-content">
      <!-- 左侧主内容 -->
      <main class="main-content">
        <!-- 用户信息卡片 -->
        <el-card class="user-card">
          <div class="user-header">
            <el-avatar :size="90" :src="userStore.userInfo?.avatar">
              {{ userStore.userInfo?.nickname?.charAt(0) }}
            </el-avatar>
            <div class="user-info">
              <h2>{{ userStore.userInfo?.nickname }}</h2>
              <p class="username">@{{ userStore.userInfo?.username }}</p>
              <p v-if="userStore.userInfo?.bio" class="bio">{{ userStore.userInfo.bio }}</p>
            </div>
          </div>

          <div class="user-stats">
            <div class="stat-item" @click="router.push('/user/favorites')">
              <div class="stat-icon">
                <el-icon><Star /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ userStats.favoriteCount }}</div>
                <div class="stat-label">收藏</div>
              </div>
            </div>
            <div class="stat-item" @click="router.push('/learning')">
              <div class="stat-icon">
                <el-icon><Reading /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ userStats.studiedCount }}</div>
                <div class="stat-label">已学</div>
              </div>
            </div>
            <div class="stat-item" @click="router.push('/plan')">
              <div class="stat-icon">
                <el-icon><Calendar /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ userStats.planCount }}</div>
                <div class="stat-label">计划</div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 个人信息表单 -->
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <el-icon><User /></el-icon>
              <span>个人信息</span>
            </div>
          </template>

          <el-form :model="form" label-width="80px">
            <el-form-item label="昵称">
              <el-input v-model="form.nickname" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="form.email" disabled />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="个人简介">
              <el-input
                v-model="form.bio"
                type="textarea"
                :rows="3"
                placeholder="介绍一下自己"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSave">
                <el-icon><Check /></el-icon> 保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 账号安全 -->
        <el-card class="security-card">
          <template #header>
            <div class="card-header">
              <el-icon><Lock /></el-icon>
              <span>账号安全</span>
            </div>
          </template>

          <div class="security-items">
            <div class="security-item">
              <div class="item-icon">
                <el-icon><Key /></el-icon>
              </div>
              <div class="item-info">
                <h4>修改密码</h4>
                <p>定期修改密码可以保护账号安全</p>
              </div>
              <el-button @click="showPasswordDialog = true">修改</el-button>
            </div>
            <div class="security-item">
              <div class="item-icon">
                <el-icon><Message /></el-icon>
              </div>
              <div class="item-info">
                <h4>邮箱验证</h4>
                <p>{{ userStore.userInfo?.email }}</p>
              </div>
              <el-tag type="success">已验证</el-tag>
            </div>
          </div>
        </el-card>
      </main>

      <!-- 右侧栏 -->
      <RightSidebar />
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="showPasswordDialog" title="修改密码" width="400px" destroy-on-close>
      <el-form :model="passwordForm" label-width="100px">
        <el-form-item label="原密码">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'
import RightSidebar from '@/components/common/RightSidebar.vue'
import { Star, Reading, Calendar, User, Check, Lock, Key, Message } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const showPasswordDialog = ref(false)

const form = reactive({
  nickname: userStore.userInfo?.nickname || '',
  email: userStore.userInfo?.email || '',
  phone: userStore.userInfo?.phone || '',
  bio: userStore.userInfo?.bio || '',
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const userStats = ref({
  favoriteCount: 0,
  studiedCount: 0,
  planCount: 0,
})

function handleSave() {
  userStore.updateUserInfo({
    nickname: form.nickname,
    phone: form.phone,
    bio: form.bio,
  })
  ElMessage.success('保存成功')
}

async function handleChangePassword() {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }

  try {
    await authApi.changePassword(passwordForm.oldPassword, passwordForm.newPassword)
    ElMessage.success('密码修改成功')
    showPasswordDialog.value = false
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    console.error('修改密码失败', error)
  }
}

onMounted(() => {
  // 加载用户统计数据
})
</script>

<style lang="scss" scoped>
.user-center-page {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-content {
  display: flex;
  gap: 24px;

  @media (max-width: 992px) {
    flex-direction: column;
  }
}

.main-content {
  flex: 1;
  min-width: 0;

  .el-card {
    margin-bottom: 20px;
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: var(--text-color);

  .el-icon {
    color: var(--el-color-primary);
  }
}

.user-card {
  .user-header {
    display: flex;
    align-items: center;
    gap: 24px;
    padding-bottom: 24px;
    border-bottom: 1px solid var(--border-color);

    @media (max-width: 576px) {
      flex-direction: column;
      text-align: center;
    }

    .user-info {
      flex: 1;

      h2 {
        font-size: 24px;
        font-weight: 600;
        color: var(--text-color);
        margin-bottom: 4px;
      }

      .username {
        color: var(--text-secondary);
        font-size: 14px;
        margin-bottom: 8px;
      }

      .bio {
        color: var(--text-secondary);
        font-size: 14px;
        line-height: 1.5;
      }
    }
  }

  .user-stats {
    display: flex;
    justify-content: space-around;
    padding-top: 24px;

    .stat-item {
      display: flex;
      align-items: center;
      gap: 12px;
      cursor: pointer;
      padding: 12px 20px;
      border-radius: 10px;
      transition: all 0.2s;

      &:hover {
        background: var(--hover-bg);
      }

      .stat-icon {
        width: 40px;
        height: 40px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: var(--el-color-primary-light-9);
        color: var(--el-color-primary);
        font-size: 18px;
      }

      .stat-info {
        .stat-value {
          font-size: 22px;
          font-weight: 700;
          color: var(--text-color);
        }

        .stat-label {
          font-size: 12px;
          color: var(--text-secondary);
        }
      }
    }
  }
}

.security-items {
  .security-item {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 16px 0;
    border-bottom: 1px solid var(--border-color);

    &:last-child {
      border-bottom: none;
    }

    .item-icon {
      width: 40px;
      height: 40px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--hover-bg);
      color: var(--text-secondary);
      font-size: 18px;
    }

    .item-info {
      flex: 1;

      h4 {
        font-size: 15px;
        font-weight: 500;
        color: var(--text-color);
        margin-bottom: 4px;
      }

      p {
        font-size: 13px;
        color: var(--text-secondary);
      }
    }
  }
}
</style>
