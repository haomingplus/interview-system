import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, LoginRequest, RegisterRequest, PhoneLoginRequest, WechatLoginRequest, LoginResponse } from '@/types'
import { authApi } from '@/api/auth'
import router from '@/router'
import { DEFAULT_AVATAR } from '@/constants'

export const useUserStore = defineStore(
  'user',
  () => {
    const token = ref<string>('')
    const refreshToken = ref<string>('')
    const userInfo = ref<User | null>(null)

    const isLoggedIn = computed(() => !!token.value && !!userInfo.value)
    const isAdmin = computed(() => userInfo.value?.roles?.includes('ROLE_ADMIN') ?? false)
    const avatar = computed(() => userInfo.value?.avatar || DEFAULT_AVATAR)

    function handleLoginResponse(data: LoginResponse) {
      token.value = data.accessToken
      refreshToken.value = data.refreshToken
      userInfo.value = data.userInfo
    }

    async function login(data: LoginRequest) {
      const res = await authApi.login(data)
      if (res.code === 200) {
        handleLoginResponse(res.data)
        return true
      }
      return false
    }

    async function loginByPhone(data: PhoneLoginRequest) {
      const res = await authApi.loginByPhone(data)
      if (res.code === 200) {
        handleLoginResponse(res.data)
        return true
      }
      return false
    }

    async function loginByWechat(data: WechatLoginRequest) {
      const res = await authApi.loginByWechat(data)
      if (res.code === 200) {
        handleLoginResponse(res.data)
        return true
      }
      return false
    }

    async function checkWechatLoginStatus(sceneStr: string) {
      const res = await authApi.checkWechatLoginStatus(sceneStr)
      if (res.code === 200 && res.data) {
        handleLoginResponse(res.data)
        return true
      }
      return false
    }

    async function register(data: RegisterRequest) {
      const res = await authApi.register(data)
      return res.code === 200
    }

    async function refreshAccessToken() {
      if (!refreshToken.value) {
        logout()
        return false
      }
      try {
        const res = await authApi.refreshToken(refreshToken.value)
        if (res.code === 200) {
          token.value = res.data.accessToken
          refreshToken.value = res.data.refreshToken
          userInfo.value = res.data.userInfo
          return true
        }
      } catch (error) {
        console.error('刷新Token失败:', error)
      }
      logout()
      return false
    }

    function logout() {
      token.value = ''
      refreshToken.value = ''
      userInfo.value = null
      router.push('/login')
    }

    function updateUserInfo(info: Partial<User>) {
      if (userInfo.value) {
        userInfo.value = { ...userInfo.value, ...info }
      }
    }

    return {
      token,
      refreshToken,
      userInfo,
      isLoggedIn,
      isAdmin,
      avatar,
      login,
      loginByPhone,
      loginByWechat,
      checkWechatLoginStatus,
      register,
      refreshAccessToken,
      logout,
      updateUserInfo,
    }
  },
  {
    persist: {
      key: 'interview-kb-user',
      storage: localStorage,
      pick: ['token', 'refreshToken', 'userInfo'],
    },
  }
)
