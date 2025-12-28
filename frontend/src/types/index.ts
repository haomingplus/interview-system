// 通用响应类型
export interface Result<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 分页结果类型
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
  hasNext: boolean
  hasPrevious: boolean
}

// 用户类型
export interface User {
  id: number
  username: string
  email: string
  nickname: string
  avatar: string
  phone?: string
  gender?: number
  bio?: string
  roles?: string[]
}

// 登录请求
export interface LoginRequest {
  username: string
  password: string
  rememberMe?: boolean
}

// 注册请求
export interface RegisterRequest {
  username: string
  email: string
  password: string
  confirmPassword: string
  nickname?: string
}

// 登录响应
export interface LoginResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  userInfo: User
}

// 分类类型
export interface Category {
  id: number
  name: string
  parentId: number
  level: number
  sort: number
  icon?: string
  description?: string
  questionCount: number
  status: number
  children?: Category[]
}

// 标签类型
export interface Tag {
  id: number
  name: string
  color: string
  description?: string
  questionCount: number
}

// 题目类型
export interface Question {
  id: number
  title: string
  content: string
  answer: string
  categoryId: number
  categoryName?: string
  difficulty: number
  source?: string
  sourceUrl?: string
  viewCount: number
  likeCount: number
  collectCount: number
  commentCount: number
  status: number
  isTop: number
  isRecommend: number
  createdBy: number
  creatorName?: string
  createdAt: string
  updatedAt: string
  tags?: Tag[]
  isLiked?: boolean
  isCollected?: boolean
  studyStatus?: number
}

// 题目查询请求
export interface QuestionQuery {
  keyword?: string
  categoryId?: number
  difficulty?: number
  source?: string
  tagIds?: number[]
  status?: number
  page?: number
  size?: number
  sortField?: string
  sortOrder?: string
}

// 题目表单
export interface QuestionForm {
  title: string
  content: string
  answer: string
  categoryId: number | null
  difficulty: number
  source?: string
  sourceUrl?: string
  tagIds?: number[]
  status: number
  isTop: number
  isRecommend: number
}

// 评论类型
export interface Comment {
  id: number
  questionId: number
  userId: number
  username: string
  userAvatar?: string
  content: string
  parentId: number
  replyToUserId?: number
  replyToUsername?: string
  rootId: number
  likeCount: number
  replyCount: number
  status: number
  createdAt: string
  replies?: Comment[]
  isLiked?: boolean
}

// 收藏夹类型
export interface FavoriteFolder {
  id: number
  userId: number
  name: string
  description?: string
  cover?: string
  isPublic: number
  isDefault: number
  itemCount: number
  sort: number
  createdAt: string
}

// 学习进度类型
export interface LearningProgress {
  id: number
  userId: number
  questionId: number
  status: number  // 0-未学 1-学习中 2-已掌握 3-需复习
  studyCount: number
  lastStudyTime?: string
  totalStudyDuration: number
  note?: string
  masteryLevel: number
  question?: Question
}

// 学习统计类型
export interface Statistics {
  totalQuestions: number
  masteredQuestions: number
  studyingQuestions: number
  needReviewQuestions: number
  consecutiveDays: number
  todayStudied: number
  totalStudyDays: number
  dailyStats: DailyStats[]
  categoryStats: CategoryStats[]
  heatMapData: Record<string, number>
}

export interface DailyStats {
  date: string
  count: number
  duration: number
}

export interface CategoryStats {
  name: string
  total: number
  mastered: number
  rate: number
}

// 学习计划类型
export interface StudyPlan {
  id: number
  userId: number
  name: string
  description?: string
  startDate: string
  endDate: string
  dailyTarget: number
  totalQuestions: number
  completedQuestions: number
  progress: number
  status: number  // 0-暂停 1-进行中 2-已完成 3-已放弃
  remindTime?: string
  remindEnabled: number
  createdAt: string
  questions?: Question[]
}

// 难度级别映射
export const DifficultyMap: Record<number, { label: string; type: 'success' | 'warning' | 'danger' }> = {
  1: { label: '简单', type: 'success' },
  2: { label: '中等', type: 'warning' },
  3: { label: '困难', type: 'danger' },
}

// 学习状态映射
export const StudyStatusMap: Record<number, { label: string; type: 'info' | 'primary' | 'success' | 'warning' }> = {
  0: { label: '未学', type: 'info' },
  1: { label: '学习中', type: 'primary' },
  2: { label: '已掌握', type: 'success' },
  3: { label: '需复习', type: 'warning' },
}
