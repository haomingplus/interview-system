import { request } from '@/utils/request'
import type { Question, QuestionForm, QuestionQuery, PageResult, Result } from '@/types'

export const questionApi = {
  // 获取题目详情
  getDetail(id: number): Promise<Result<Question>> {
    return request.get(`/questions/${id}`)
  },

  // 分页查询题目
  getPage(query: QuestionQuery): Promise<Result<PageResult<Question>>> {
    return request.get('/questions', { params: query })
  },

  // 搜索题目
  search(keyword: string): Promise<Result<Question[]>> {
    return request.get('/questions/search', { params: { keyword } })
  },

  // 创建题目
  create(data: QuestionForm): Promise<Result<number>> {
    return request.post('/questions', data)
  },

  // 更新题目
  update(id: number, data: QuestionForm): Promise<Result<void>> {
    return request.put(`/questions/${id}`, data)
  },

  // 删除题目
  delete(id: number): Promise<Result<void>> {
    return request.delete(`/questions/${id}`)
  },

  // 点赞题目
  like(id: number): Promise<Result<void>> {
    return request.post(`/questions/${id}/like`)
  },

  // 取消点赞
  unlike(id: number): Promise<Result<void>> {
    return request.delete(`/questions/${id}/like`)
  },

  // 检查是否已点赞
  isLiked(id: number): Promise<Result<boolean>> {
    return request.get(`/questions/${id}/like/status`)
  },
}
