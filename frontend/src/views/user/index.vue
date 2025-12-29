<template>
  <div class="user-center-page">
    <div class="page-content">
      <!-- 左侧主内容 -->
      <main class="main-content">
        <!-- 用户信息卡片 -->
        <el-card class="user-card">
          <div class="user-header">
            <el-avatar :size="90" :src="userStore.avatar">
              {{ userStore.userInfo?.nickname?.charAt(0) }}
            </el-avatar>
            <div class="user-info">
              <h2>{{ userStore.userInfo?.nickname }}</h2>
              <p class="username" v-if="userStore.userInfo?.username">@{{ userStore.userInfo?.username }}</p>
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

        <!-- 账号绑定 -->
        <el-card class="binding-card">
          <template #header>
            <div class="card-header">
              <el-icon><Link /></el-icon>
              <span>账号绑定</span>
            </div>
          </template>

          <div class="binding-items">
            <!-- 手机号绑定 -->
            <div class="binding-item">
              <div class="item-icon phone">
                <el-icon><Iphone /></el-icon>
              </div>
              <div class="item-info">
                <h4>手机号</h4>
                <p v-if="userStore.userInfo?.phone">
                  {{ maskPhone(userStore.userInfo.phone) }}
                  <el-tag v-if="userStore.userInfo?.phoneVerified" type="success" size="small">已验证</el-tag>
                </p>
                <p v-else class="not-bound">未绑定</p>
              </div>
              <el-button
                v-if="!userStore.userInfo?.phone"
                type="primary"
                plain
                @click="showBindPhoneDialog = true"
              >绑定</el-button>
              <el-button v-else @click="showBindPhoneDialog = true">更换</el-button>
            </div>

            <!-- 邮箱绑定 -->
            <div class="binding-item">
              <div class="item-icon email">
                <el-icon><Message /></el-icon>
              </div>
              <div class="item-info">
                <h4>邮箱</h4>
                <p v-if="userStore.userInfo?.email">
                  {{ maskEmail(userStore.userInfo.email) }}
                  <el-tag v-if="userStore.userInfo?.emailVerified" type="success" size="small">已验证</el-tag>
                </p>
                <p v-else class="not-bound">未绑定</p>
              </div>
              <el-button
                v-if="!userStore.userInfo?.email"
                type="primary"
                plain
                @click="showBindEmailDialog = true"
              >绑定</el-button>
              <el-button v-else @click="showBindEmailDialog = true">更换</el-button>
            </div>

            <!-- 微信绑定 -->
            <div class="binding-item">
              <div class="item-icon wechat">
                <el-icon><ChatDotRound /></el-icon>
              </div>
              <div class="item-info">
                <h4>微信</h4>
                <p v-if="userStore.userInfo?.wechatOpenid">
                  {{ userStore.userInfo?.wechatNickname || '已绑定' }}
                  <el-tag type="success" size="small">已绑定</el-tag>
                </p>
                <p v-else class="not-bound">未绑定</p>
              </div>
              <template v-if="userStore.userInfo?.wechatOpenid">
                <el-button type="danger" plain @click="handleUnbindWechat">解绑</el-button>
              </template>
              <template v-else>
                <el-button type="primary" plain @click="showBindWechatDialog = true">绑定</el-button>
              </template>
            </div>
          </div>
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
                <h4>登录密码</h4>
                <p v-if="userStore.userInfo?.username">定期修改密码可以保护账号安全</p>
                <p v-else class="not-set">未设置（使用手机或微信登录）</p>
              </div>
              <el-button v-if="userStore.userInfo?.username" @click="showPasswordDialog = true">修改</el-button>
              <el-button v-else type="primary" plain @click="showSetPasswordDialog = true">设置</el-button>
            </div>
          </div>
        </el-card>
      </main>

      <!-- 右侧栏 -->
      <RightSidebar />
    </div>

    <!-- 绑定手机号对话框 -->
    <el-dialog v-model="showBindPhoneDialog" title="绑定手机号" width="400px" destroy-on-close>
      <el-form ref="phoneFormRef" :model="phoneBindForm" :rules="phoneRules" label-width="80px">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="phoneBindForm.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="code-input-group">
            <el-input v-model="phoneBindForm.code" placeholder="请输入验证码" maxlength="6" />
            <el-button
              type="primary"
              :disabled="phoneCountdown > 0 || sendingPhoneCode"
              :loading="sendingPhoneCode"
              @click="sendPhoneBindCode"
            >
              {{ phoneCountdown > 0 ? `${phoneCountdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBindPhoneDialog = false">取消</el-button>
        <el-button type="primary" :loading="bindingPhone" @click="handleBindPhone">确认绑定</el-button>
      </template>
    </el-dialog>

    <!-- 绑定邮箱对话框 -->
    <el-dialog v-model="showBindEmailDialog" title="绑定邮箱" width="400px" destroy-on-close>
      <el-form ref="emailFormRef" :model="emailBindForm" :rules="emailRules" label-width="80px">
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="emailBindForm.email" placeholder="请输入邮箱地址" />
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="code-input-group">
            <el-input v-model="emailBindForm.code" placeholder="请输入验证码" maxlength="6" />
            <el-button
              type="primary"
              :disabled="emailCountdown > 0 || sendingEmailCode"
              :loading="sendingEmailCode"
              @click="sendEmailBindCode"
            >
              {{ emailCountdown > 0 ? `${emailCountdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBindEmailDialog = false">取消</el-button>
        <el-button type="primary" :loading="bindingEmail" @click="handleBindEmail">确认绑定</el-button>
      </template>
    </el-dialog>

    <!-- 绑定微信对话框 -->
    <el-dialog v-model="showBindWechatDialog" title="绑定微信" width="400px" destroy-on-close>
      <div class="wechat-bind-content">
        <div v-if="wechatBindLoading" class="qrcode-loading">
          <el-icon class="is-loading" :size="32"><Loading /></el-icon>
          <p>正在获取二维码...</p>
        </div>
        <div v-else class="qrcode-container">
          <div class="qrcode-wrapper">
            <img v-if="wechatBindQrcode" :src="wechatBindQrcode" alt="微信绑定二维码" />
          </div>
          <p class="scan-tip">请使用微信扫描二维码绑定</p>
        </div>
      </div>
    </el-dialog>

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

    <!-- 设置密码对话框 -->
    <el-dialog v-model="showSetPasswordDialog" title="设置登录密码" width="400px" destroy-on-close>
      <el-alert type="info" :closable="false" style="margin-bottom: 16px">
        设置密码后，您可以使用用户名和密码登录
      </el-alert>
      <el-form :model="setPasswordForm" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="setPasswordForm.username" placeholder="请设置用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="setPasswordForm.password" type="password" show-password placeholder="请设置密码" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="setPasswordForm.confirmPassword" type="password" show-password placeholder="请再次输入密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSetPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSetPassword">确认设置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'
import RightSidebar from '@/components/common/RightSidebar.vue'
import {
  Star, Reading, Calendar, User, Check, Lock, Key, Message,
  Link, Iphone, ChatDotRound, Loading
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 对话框控制
const showBindPhoneDialog = ref(false)
const showBindEmailDialog = ref(false)
const showBindWechatDialog = ref(false)
const showPasswordDialog = ref(false)
const showSetPasswordDialog = ref(false)

// 用户信息表单
const form = reactive({
  nickname: userStore.userInfo?.nickname || '',
  bio: userStore.userInfo?.bio || '',
})

// 绑定手机号
const phoneFormRef = ref<FormInstance>()
const phoneBindForm = reactive({ phone: '', code: '' })
const phoneCountdown = ref(0)
const sendingPhoneCode = ref(false)
const bindingPhone = ref(false)
let phoneCountdownTimer: ReturnType<typeof setInterval> | null = null

const phoneRules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' },
  ],
}

// 绑定邮箱
const emailFormRef = ref<FormInstance>()
const emailBindForm = reactive({ email: '', code: '' })
const emailCountdown = ref(0)
const sendingEmailCode = ref(false)
const bindingEmail = ref(false)
let emailCountdownTimer: ReturnType<typeof setInterval> | null = null

const emailRules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
  ],
}

// 绑定微信
const wechatBindLoading = ref(false)
const wechatBindQrcode = ref('')

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const setPasswordForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
})

const userStats = ref({
  favoriteCount: 0,
  studiedCount: 0,
  planCount: 0,
})

// 工具函数：脱敏显示
function maskPhone(phone: string) {
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

function maskEmail(email: string) {
  const [name, domain] = email.split('@')
  if (name.length <= 2) return email
  return name.substring(0, 2) + '***@' + domain
}

// 保存个人信息
function handleSave() {
  userStore.updateUserInfo({
    nickname: form.nickname,
    bio: form.bio,
  })
  ElMessage.success('保存成功')
}

// 发送手机绑定验证码
async function sendPhoneBindCode() {
  try {
    await phoneFormRef.value?.validateField('phone')
  } catch {
    return
  }

  sendingPhoneCode.value = true
  try {
    const res = await authApi.sendSmsCode({ phone: phoneBindForm.phone, purpose: 'bind' })
    if (res.code === 200) {
      ElMessage.success('验证码已发送')
      startPhoneCountdown()
    }
  } finally {
    sendingPhoneCode.value = false
  }
}

function startPhoneCountdown() {
  phoneCountdown.value = 60
  phoneCountdownTimer = setInterval(() => {
    phoneCountdown.value--
    if (phoneCountdown.value <= 0 && phoneCountdownTimer) {
      clearInterval(phoneCountdownTimer)
    }
  }, 1000)
}

// 绑定手机号
async function handleBindPhone() {
  const valid = await phoneFormRef.value?.validate()
  if (!valid) return

  bindingPhone.value = true
  try {
    const res = await authApi.bindPhone({
      phone: phoneBindForm.phone,
      code: phoneBindForm.code,
    })
    if (res.code === 200) {
      ElMessage.success('手机号绑定成功')
      userStore.updateUserInfo({
        phone: phoneBindForm.phone,
        phoneVerified: 1,
      })
      showBindPhoneDialog.value = false
      phoneBindForm.phone = ''
      phoneBindForm.code = ''
    }
  } finally {
    bindingPhone.value = false
  }
}

// 发送邮箱绑定验证码
async function sendEmailBindCode() {
  try {
    await emailFormRef.value?.validateField('email')
  } catch {
    return
  }

  sendingEmailCode.value = true
  try {
    // TODO: 实现发送邮箱验证码API
    ElMessage.success('验证码已发送到您的邮箱')
    startEmailCountdown()
  } finally {
    sendingEmailCode.value = false
  }
}

function startEmailCountdown() {
  emailCountdown.value = 60
  emailCountdownTimer = setInterval(() => {
    emailCountdown.value--
    if (emailCountdown.value <= 0 && emailCountdownTimer) {
      clearInterval(emailCountdownTimer)
    }
  }, 1000)
}

// 绑定邮箱
async function handleBindEmail() {
  const valid = await emailFormRef.value?.validate()
  if (!valid) return

  bindingEmail.value = true
  try {
    const res = await authApi.bindEmail({
      email: emailBindForm.email,
      code: emailBindForm.code,
    })
    if (res.code === 200) {
      ElMessage.success('邮箱绑定成功')
      userStore.updateUserInfo({
        email: emailBindForm.email,
        emailVerified: 1,
      })
      showBindEmailDialog.value = false
      emailBindForm.email = ''
      emailBindForm.code = ''
    }
  } finally {
    bindingEmail.value = false
  }
}

// 解绑微信
async function handleUnbindWechat() {
  try {
    await ElMessageBox.confirm(
      '解绑后将无法使用微信登录，确定要解绑吗？',
      '解绑微信',
      { type: 'warning' }
    )

    const res = await authApi.unbindWechat()
    if (res.code === 200) {
      ElMessage.success('解绑成功')
      userStore.updateUserInfo({
        wechatOpenid: undefined,
        wechatNickname: undefined,
      })
    }
  } catch {
    // 用户取消
  }
}

// 修改密码
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

// 设置密码
async function handleSetPassword() {
  if (!setPasswordForm.username) {
    ElMessage.error('请输入用户名')
    return
  }
  if (setPasswordForm.password !== setPasswordForm.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  if (setPasswordForm.password.length < 6) {
    ElMessage.error('密码长度至少6位')
    return
  }

  // TODO: 实现设置密码API
  ElMessage.success('密码设置成功')
  showSetPasswordDialog.value = false
}

onMounted(() => {
  // 加载用户统计数据
})

onUnmounted(() => {
  if (phoneCountdownTimer) clearInterval(phoneCountdownTimer)
  if (emailCountdownTimer) clearInterval(emailCountdownTimer)
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

.binding-items,
.security-items {
  .binding-item,
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
      font-size: 18px;

      &.phone {
        background: #e8f5e9;
        color: #4caf50;
      }

      &.email {
        background: #e3f2fd;
        color: #2196f3;
      }

      &.wechat {
        background: #e8f5e9;
        color: #07c160;
      }
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
        display: flex;
        align-items: center;
        gap: 8px;

        &.not-bound,
        &.not-set {
          color: #999;
        }
      }
    }
  }
}

.security-items .item-icon {
  background: var(--hover-bg);
  color: var(--text-secondary);
}

.code-input-group {
  display: flex;
  gap: 12px;
  width: 100%;

  .el-input {
    flex: 1;
  }

  .el-button {
    flex-shrink: 0;
  }
}

.wechat-bind-content {
  text-align: center;
  padding: 20px 0;

  .qrcode-loading {
    p {
      margin-top: 12px;
      color: #666;
    }
  }

  .qrcode-container {
    .qrcode-wrapper {
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
    }

    .scan-tip {
      margin-top: 16px;
      color: #666;
      font-size: 14px;
    }
  }
}

@media (max-width: 576px) {
  .user-center-page {
    padding: 12px;
  }

  .binding-items .binding-item,
  .security-items .security-item {
    flex-wrap: wrap;

    .item-info {
      flex: 1;
      min-width: 120px;
    }

    .el-button {
      width: 100%;
      margin-top: 8px;
    }
  }
}
</style>
