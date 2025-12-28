<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <el-icon :size="48" color="var(--el-color-primary)"><Reading /></el-icon>
        <h1>面试知识库</h1>
        <p>专业的面试知识学习平台</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            size="large"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>

        <div class="form-options">
          <el-checkbox v-model="form.rememberMe">记住我</el-checkbox>
          <el-link type="primary" @click="handleForgotPassword">忘记密码?</el-link>
        </div>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            native-type="submit"
          >
            登录
          </el-button>
        </el-form-item>

        <div class="register-link">
          还没有账号?
          <el-link type="primary" @click="router.push('/register')">立即注册</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Reading, User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import type { LoginRequest } from '@/types'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<LoginRequest>({
  username: '',
  password: '',
  rememberMe: false,
})

const rules: FormRules<LoginRequest> = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' },
  ],
}

async function handleLogin() {
  const valid = await formRef.value?.validate()
  if (!valid) return

  loading.value = true
  try {
    const success = await userStore.login(form)
    if (success) {
      ElMessage.success('登录成功')
      const redirect = route.query.redirect as string
      router.push(redirect || '/')
    }
  } finally {
    loading.value = false
  }
}

function handleForgotPassword() {
  ElMessage.info('请联系管理员重置密码')
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-container {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;

  h1 {
    margin: 16px 0 8px;
    font-size: 24px;
    color: #333;
  }

  p {
    color: #666;
    font-size: 14px;
  }
}

.login-form {
  .form-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }

  .login-btn {
    width: 100%;
  }

  .register-link {
    text-align: center;
    color: #666;
    font-size: 14px;
  }
}
</style>
