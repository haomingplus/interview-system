import { request } from '@/utils/request'
import type {
  LoginRequest, LoginResponse, RegisterRequest, Result,
  PhoneLoginRequest, SmsCodeRequest, WechatLoginRequest,
  WechatQrcodeResponse, BindPhoneRequest, BindEmailRequest
} from '@/types'

export const authApi = {
  // 账号密码登录
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

  // ====================== 手机验证码登录 ======================

  // 发送短信验证码
  sendSmsCode(data: SmsCodeRequest): Promise<Result<void>> {
    return request.post('/auth/sms/send', data)
  },

  // 手机验证码登录
  loginByPhone(data: PhoneLoginRequest): Promise<Result<LoginResponse>> {
    return request.post('/auth/login/phone', data)
  },

  // ====================== 微信登录 ======================

  // 获取微信登录二维码
  getWechatQrcode(): Promise<Result<WechatQrcodeResponse>> {
    return request.get('/auth/wechat/qrcode')
  },

  // 微信授权登录
  loginByWechat(data: WechatLoginRequest): Promise<Result<LoginResponse>> {
    return request.post('/auth/login/wechat', data)
  },

  // 检查微信扫码登录状态
  checkWechatLoginStatus(sceneStr: string): Promise<Result<LoginResponse | null>> {
    return request.get('/auth/wechat/status', { params: { sceneStr } })
  },

  // ====================== 绑定功能 ======================

  // 绑定手机号
  bindPhone(data: BindPhoneRequest): Promise<Result<void>> {
    return request.post('/auth/bind/phone', data)
  },

  // 绑定邮箱
  bindEmail(data: BindEmailRequest): Promise<Result<void>> {
    return request.post('/auth/bind/email', data)
  },

  // 绑定微信
  bindWechat(code: string): Promise<Result<void>> {
    return request.post('/auth/bind/wechat', null, { params: { code } })
  },

  // 解绑微信
  unbindWechat(): Promise<Result<void>> {
    return request.post('/auth/unbind/wechat')
  },
}
