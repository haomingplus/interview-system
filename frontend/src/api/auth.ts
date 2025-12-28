import { request } from '@/utils/request'
import type { LoginRequest, LoginResponse, RegisterRequest, Result } from '@/types'

export const authApi = {
  // 登录
  login(data: LoginRequest): Promise<Result<LoginResponse>> {
    return request.post('/auth/login', data)
  },

  // 注册
  register(data: RegisterRequest): Promise<Result<void>> {
    return request.post('/auth/register', data)
  },

  // 刷新Token
  refreshToken(refreshToken: string): Promise<Result<LoginResponse>> {
    return request.post('/auth/refresh', null, { params: { refreshToken } })
  },

  // 登出
  logout(): Promise<Result<void>> {
    return request.post('/auth/logout')
  },

  // 发送密码重置邮件
  forgotPassword(email: string): Promise<Result<void>> {
    return request.post('/auth/forgot-password', null, { params: { email } })
  },

  // 重置密码
  resetPassword(token: string, newPassword: string): Promise<Result<void>> {
    return request.post('/auth/reset-password', null, { params: { token, newPassword } })
  },

  // 修改密码
  changePassword(oldPassword: string, newPassword: string): Promise<Result<void>> {
    return request.post('/user/change-password', { oldPassword, newPassword })
  },
}
