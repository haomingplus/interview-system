import { request } from '@/utils/request'
import type { LearningProgress, Statistics, PageResult, Result } from '@/types'

export const learningApi = {
  // 获取题目学习进度
  getProgress(questionId: number): Promise<Result<LearningProgress>> {
    return request.get(`/learning/progress/${questionId}`)
  },

  // 更新学习进度
  updateProgress(questionId: number, status: number, note?: string): Promise<Result<void>> {
    return request.post(`/learning/progress/${questionId}`, null, {
      params: { status, note },
    })
  },

  // 记录学习行为
  recordStudy(questionId: number, action: string, duration?: number): Promise<Result<void>> {
    return request.post('/learning/record', null, {
      params: { questionId, action, duration },
    })
  },

  // 获取学习进度列表
  getProgressList(
    status?: number,
    page: number = 1,
    size: number = 10
  ): Promise<Result<PageResult<LearningProgress>>> {
    return request.get('/learning/progress', {
      params: { status, page, size },
    })
  },

  // 获取学习统计
  getStatistics(): Promise<Result<Statistics>> {
    return request.get('/learning/statistics')
  },
}
