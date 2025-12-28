<template>
  <div class="user-center-page">
    <el-row :gutter="20">
      <el-col :xs="24" :lg="8">
        <el-card class="user-card">
          <div class="user-info">
            <el-avatar :size="80" :src="userStore.userInfo?.avatar">
              {{ userStore.userInfo?.nickname?.charAt(0) }}
            </el-avatar>
            <h2>{{ userStore.userInfo?.nickname }}</h2>
            <p class="username">@{{ userStore.userInfo?.username }}</p>
            <p v-if="userStore.userInfo?.bio" class="bio">{{ userStore.userInfo.bio }}</p>
          </div>

          <el-divider />

          <div class="user-stats">
            <div class="stat-item" @click="router.push('/user/favorites')">
              <div class="stat-value">{{ userStats.favoriteCount }}</div>
              <div class="stat-label">收藏</div>
            </div>
            <div class="stat-item" @click="router.push('/learning')">
              <div class="stat-value">{{ userStats.studiedCount }}</div>
              <div class="stat-label">已学</div>
            </div>
            <div class="stat-item" @click="router.push('/plan')">
              <div class="stat-value">{{ userStats.planCount }}</div>
              <div class="stat-label">计划</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="16">
        <el-card>
          <template #header>
            <span>个人信息</span>
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
              <el-button type="primary" @click="handleSave">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="mt-20">
          <template #header>
            <span>账号安全</span>
          </template>

          <div class="security-items">
            <div class="security-item">
              <div class="item-info">
                <h4>修改密码</h4>
                <p>定期修改密码可以保护账号安全</p>
              </div>
              <el-button @click="showPasswordDialog = true">修改</el-button>
            </div>
            <div class="security-item">
              <div class="item-info">
                <h4>邮箱验证</h4>
                <p>{{ userStore.userInfo?.email }}</p>
              </div>
              <el-tag type="success">已验证</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="showPasswordDialog" title="修改密码" width="400px">
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
  max-width: 1000px;
  margin: 0 auto;
}

.user-card {
  text-align: center;

  .user-info {
    padding: 20px 0;

    h2 {
      margin: 16px 0 8px;
      font-size: 20px;
      color: #333;
    }

    .username {
      color: #999;
      font-size: 14px;
    }

    .bio {
      margin-top: 12px;
      color: #666;
      font-size: 14px;
    }
  }

  .user-stats {
    display: flex;
    justify-content: space-around;

    .stat-item {
      cursor: pointer;
      padding: 10px 20px;
      transition: background-color 0.2s;

      &:hover {
        background: #f5f5f5;
        border-radius: 8px;
      }

      .stat-value {
        font-size: 24px;
        font-weight: 700;
        color: #333;
      }

      .stat-label {
        font-size: 12px;
        color: #999;
        margin-top: 4px;
      }
    }
  }
}

.security-items {
  .security-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 0;
    border-bottom: 1px solid #eee;

    &:last-child {
      border-bottom: none;
    }

    .item-info {
      h4 {
        font-size: 15px;
        color: #333;
        margin-bottom: 4px;
      }

      p {
        font-size: 13px;
        color: #999;
      }
    }
  }
}

@media (max-width: 992px) {
  .el-col:first-child {
    margin-bottom: 20px;
  }
}
</style>
