import { request } from '@/utils/request'
import type { Tag, Result } from '@/types'

export const tagApi = {
  // 获取标签列表
  getList(): Promise<Result<Tag[]>> {
    return request.get('/tags')
  },

  // 创建标签
  create(data: Partial<Tag>): Promise<Result<void>> {
    return request.post('/tags', data)
  },

  // 更新标签
  update(id: number, data: Partial<Tag>): Promise<Result<void>> {
    return request.put(`/tags/${id}`, data)
  },

  // 删除标签
  delete(id: number): Promise<Result<void>> {
    return request.delete(`/tags/${id}`)
  },
}
