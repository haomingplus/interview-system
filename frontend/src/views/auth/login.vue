<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <el-icon :size="48" color="var(--el-color-primary)"><Reading /></el-icon>
        <h1>面试知识库</h1>
        <p>专业的面试知识学习平台</p>
      </div>

      <!-- 登录方式选项卡 -->
      <div class="login-tabs">
        <div
          class="tab-item"
          :class="{ active: activeTab === 'account' }"
          @click="activeTab = 'account'"
        >
          <el-icon><User /></el-icon>
          <span>账号登录</span>
        </div>
        <div
          class="tab-item"
          :class="{ active: activeTab === 'phone' }"
          @click="activeTab = 'phone'"
        >
          <el-icon><Iphone /></el-icon>
          <span>手机登录</span>
        </div>
        <div
          class="tab-item"
          :class="{ active: activeTab === 'wechat' }"
          @click="switchToWechat"
        >
          <el-icon><ChatDotRound /></el-icon>
          <span>微信登录</span>
        </div>
      </div>

      <!-- 账号密码登录 -->
      <el-form
        v-show="activeTab === 'account'"
        ref="accountFormRef"
        :model="accountForm"
        :rules="accountRules"
        class="login-form"
        @submit.prevent="handleAccountLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="accountForm.username"
            placeholder="用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="accountForm.password"
            type="password"
            placeholder="密码"
            size="large"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>

        <div class="form-options">
          <el-checkbox v-model="accountForm.rememberMe">记住我</el-checkbox>
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
      </el-form>

      <!-- 手机验证码登录 -->
      <el-form
        v-show="activeTab === 'phone'"
        ref="phoneFormRef"
        :model="phoneForm"
        :rules="phoneRules"
        class="login-form"
        @submit.prevent="handlePhoneLogin"
      >
        <el-form-item prop="phone">
          <el-input
            v-model="phoneForm.phone"
            placeholder="手机号"
            size="large"
            :prefix-icon="Iphone"
            maxlength="11"
          />
        </el-form-item>

        <el-form-item prop="code">
          <div class="code-input-group">
            <el-input
              v-model="phoneForm.code"
              placeholder="验证码"
              size="large"
              :prefix-icon="Message"
              maxlength="6"
            />
            <el-button
              type="primary"
              size="large"
              :disabled="countdown > 0 || sendingCode"
              :loading="sendingCode"
              @click="sendSmsCode"
            >
              {{ countdown > 0 ? `${countdown}s后重发` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            native-type="submit"
          >
            登录 / 注册
          </el-button>
        </el-form-item>

        <p class="login-tip">未注册的手机号将自动创建账号</p>
      </el-form>

      <!-- 微信扫码登录 -->
      <div v-show="activeTab === 'wechat'" class="wechat-login">
        <div v-if="wechatLoading" class="qrcode-loading">
          <el-icon class="is-loading" :size="32"><Loading /></el-icon>
          <p>正在获取二维码...</p>
        </div>
        <div v-else-if="wechatError" class="qrcode-error">
          <el-icon :size="48" color="#f56c6c"><CircleCloseFilled /></el-icon>
          <p>{{ wechatError }}</p>
          <el-button type="primary" @click="getWechatQrcode">刷新二维码</el-button>
        </div>
        <div v-else class="qrcode-container">
          <div class="qrcode-wrapper">
            <img v-if="qrcodeUrl" :src="qrcodeUrl" alt="微信登录二维码" />
            <div v-if="qrcodeExpired" class="qrcode-mask">
              <p>二维码已过期</p>
              <el-button type="primary" size="small" @click="getWechatQrcode">刷新</el-button>
            </div>
          </div>
          <p class="scan-tip">请使用微信扫描二维码登录</p>
          <p class="auto-tip">扫码后将自动登录</p>
        </div>
      </div>

      <!-- 第三方登录 -->
      <div v-if="activeTab !== 'wechat'" class="third-party-login">
        <el-divider>其他登录方式</el-divider>
        <div class="third-party-icons">
          <el-tooltip content="微信登录" placement="top">
            <div class="icon-btn wechat" @click="switchToWechat">
              <el-icon :size="24"><ChatDotRound /></el-icon>
            </div>
          </el-tooltip>
        </div>
      </div>

      <!-- 用户协议 -->
      <div class="agreement">
        登录即表示同意
        <el-link type="primary">《用户协议》</el-link>
        和
        <el-link type="primary">《隐私政策》</el-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Reading, User, Lock, Iphone, Message, ChatDotRound, Loading, CircleCloseFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'
import type { LoginRequest, PhoneLoginRequest } from '@/types'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 当前登录方式
const activeTab = ref<'account' | 'phone' | 'wechat'>('phone')
const loading = ref(false)

// 账号登录表单
const accountFormRef = ref<FormInstance>()
const accountForm = reactive<LoginRequest>({
  username: '',
  password: '',
  rememberMe: false,
})

const accountRules: FormRules<LoginRequest> = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' },
  ],
}

// 手机登录表单
const phoneFormRef = ref<FormInstance>()
const phoneForm = reactive<PhoneLoginRequest>({
  phone: '',
  code: '',
})

const phoneRules: FormRules<PhoneLoginRequest> = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' },
  ],
}

// 验证码倒计时
const countdown = ref(0)
const sendingCode = ref(false)
let countdownTimer: ReturnType<typeof setInterval> | null = null

// 微信登录相关
const wechatLoading = ref(false)
const wechatError = ref('')
const qrcodeUrl = ref('')
const sceneStr = ref('')
const qrcodeExpired = ref(false)
let pollTimer: ReturnType<typeof setInterval> | null = null
let expireTimer: ReturnType<typeof setTimeout> | null = null

// 账号密码登录
async function handleAccountLogin() {
  const valid = await accountFormRef.value?.validate()
  if (!valid) return

  loading.value = true
  try {
    const success = await userStore.login(accountForm)
    if (success) {
      handleLoginSuccess()
    }
  } finally {
    loading.value = false
  }
}

// 手机验证码登录
async function handlePhoneLogin() {
  const valid = await phoneFormRef.value?.validate()
  if (!valid) return

  loading.value = true
  try {
    const success = await userStore.loginByPhone(phoneForm)
    if (success) {
      handleLoginSuccess()
    }
  } finally {
    loading.value = false
  }
}

// 发送验证码
async function sendSmsCode() {
  // 先验证手机号
  try {
    await phoneFormRef.value?.validateField('phone')
  } catch {
    return
  }

  sendingCode.value = true
  try {
    const res = await authApi.sendSmsCode({ phone: phoneForm.phone, purpose: 'login' })
    if (res.code === 200) {
      ElMessage.success('验证码已发送')
      startCountdown()
    }
  } finally {
    sendingCode.value = false
  }
}

function startCountdown() {
  countdown.value = 60
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer!)
      countdownTimer = null
    }
  }, 1000)
}

// 切换到微信登录
function switchToWechat() {
  activeTab.value = 'wechat'
  if (!qrcodeUrl.value || qrcodeExpired.value) {
    getWechatQrcode()
  }
}

// 获取微信二维码
async function getWechatQrcode() {
  wechatLoading.value = true
  wechatError.value = ''
  qrcodeExpired.value = false

  // 清除之前的轮询
  stopPolling()

  try {
    const res = await authApi.getWechatQrcode()
    if (res.code === 200) {
      qrcodeUrl.value = res.data.qrcodeUrl
      sceneStr.value = res.data.sceneStr

      // 开始轮询检查登录状态
      startPolling()

      // 设置过期定时器
      expireTimer = setTimeout(() => {
        qrcodeExpired.value = true
        stopPolling()
      }, (res.data.expireSeconds || 300) * 1000)
    } else {
      wechatError.value = '获取二维码失败'
    }
  } catch {
    wechatError.value = '网络错误，请重试'
  } finally {
    wechatLoading.value = false
  }
}

// 轮询检查微信扫码状态
function startPolling() {
  pollTimer = setInterval(async () => {
    if (!sceneStr.value || qrcodeExpired.value) {
      stopPolling()
      return
    }

    try {
      const success = await userStore.checkWechatLoginStatus(sceneStr.value)
      if (success) {
        stopPolling()
        handleLoginSuccess()
      }
    } catch (error: any) {
      if (error?.response?.data?.message?.includes('过期')) {
        qrcodeExpired.value = true
        stopPolling()
      }
    }
  }, 2000)
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
  if (expireTimer) {
    clearTimeout(expireTimer)
    expireTimer = null
  }
}

// 登录成功处理
function handleLoginSuccess() {
  ElMessage.success('登录成功')
  const redirect = route.query.redirect as string
  router.push(redirect || '/')
}

function handleForgotPassword() {
  ElMessage.info('请联系管理员重置密码')
}

// 清理定时器
onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
  stopPolling()
})
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
  max-width: 420px;
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;

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

.login-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  background: #f5f7fa;
  padding: 4px;
  border-radius: 8px;

  .tab-item {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
    padding: 10px 12px;
    border-radius: 6px;
    cursor: pointer;
    font-size: 14px;
    color: #666;
    transition: all 0.3s;

    &:hover {
      color: var(--el-color-primary);
    }

    &.active {
      background: #fff;
      color: var(--el-color-primary);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    }

    .el-icon {
      font-size: 16px;
    }
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

  .code-input-group {
    display: flex;
    gap: 12px;
    width: 100%;

    .el-input {
      flex: 1;
    }

    .el-button {
      width: 120px;
      flex-shrink: 0;
    }
  }
}

.login-tip {
  text-align: center;
  color: #999;
  font-size: 12px;
  margin-top: 12px;
}

.wechat-login {
  min-height: 280px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  .qrcode-loading,
  .qrcode-error {
    text-align: center;

    p {
      margin-top: 12px;
      color: #666;
    }

    .el-button {
      margin-top: 16px;
    }
  }

  .qrcode-container {
    text-align: center;

    .qrcode-wrapper {
      position: relative;
      width: 180px;
      height: 180px;
      margin: 0 auto;
      border: 1px solid #eee;
      border-radius: 8px;
      overflow: hidden;

      img {
        width: 100%;
        height: 100%;
        object-fit: contain;
      }

      .qrcode-mask {
        position: absolute;
        inset: 0;
        background: rgba(255, 255, 255, 0.95);
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;

        p {
          margin-bottom: 12px;
          color: #666;
        }
      }
    }

    .scan-tip {
      margin-top: 16px;
      color: #333;
      font-size: 14px;
    }

    .auto-tip {
      margin-top: 8px;
      color: #999;
      font-size: 12px;
    }
  }
}

.third-party-login {
  margin-top: 24px;

  :deep(.el-divider__text) {
    color: #999;
    font-size: 12px;
  }

  .third-party-icons {
    display: flex;
    justify-content: center;
    gap: 20px;

    .icon-btn {
      width: 44px;
      height: 44px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.3s;

      &.wechat {
        background: #07c160;
        color: #fff;

        &:hover {
          transform: scale(1.1);
          box-shadow: 0 4px 12px rgba(7, 193, 96, 0.4);
        }
      }
    }
  }
}

.agreement {
  margin-top: 24px;
  text-align: center;
  font-size: 12px;
  color: #999;

  .el-link {
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .login-container {
    padding: 24px;
  }

  .login-tabs {
    .tab-item {
      padding: 8px;
      font-size: 13px;

      span {
        display: none;
      }

      .el-icon {
        font-size: 20px;
      }
    }
  }

  .login-form {
    .code-input-group {
      flex-direction: column;

      .el-button {
        width: 100%;
      }
    }
  }
}
</style>
