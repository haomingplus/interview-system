import { request } from '@/utils/request'
import type { Category, Result } from '@/types'

export const categoryApi = {
  // 获取分类树
  getTree(): Promise<Result<Category[]>> {
    return request.get('/categories/tree')
  },

  // 获取子分类
  getByParentId(parentId: number = 0): Promise<Result<Category[]>> {
    return request.get('/categories', { params: { parentId } })
  },

  // 获取分类详情
  getById(id: number): Promise<Result<Category>> {
    return request.get(`/categories/${id}`)
  },

  // 创建分类
  create(data: Partial<Category>): Promise<Result<void>> {
    return request.post('/categories', data)
  },

  // 更新分类
  update(id: number, data: Partial<Category>): Promise<Result<void>> {
    return request.put(`/categories/${id}`, data)
  },

  // 删除分类
  delete(id: number): Promise<Result<void>> {
    return request.delete(`/categories/${id}`)
  },
}
