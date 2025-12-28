import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Category } from '@/types'
import { categoryApi } from '@/api/category'

export const useCategoryStore = defineStore('category', () => {
  const categoryTree = ref<Category[]>([])
  const loading = ref(false)

  // 扁平化分类列表
  const flatCategories = computed(() => {
    const result: Category[] = []
    const flatten = (list: Category[]) => {
      list.forEach((item) => {
        result.push(item)
        if (item.children && item.children.length > 0) {
          flatten(item.children)
        }
      })
    }
    flatten(categoryTree.value)
    return result
  })

  // 获取分类名称
  function getCategoryName(id: number): string {
    const category = flatCategories.value.find((c) => c.id === id)
    return category?.name || ''
  }

  // 获取分类路径
  function getCategoryPath(id: number): Category[] {
    const path: Category[] = []
    const findPath = (list: Category[], targetId: number): boolean => {
      for (const item of list) {
        if (item.id === targetId) {
          path.push(item)
          return true
        }
        if (item.children && item.children.length > 0) {
          path.push(item)
          if (findPath(item.children, targetId)) {
            return true
          }
          path.pop()
        }
      }
      return false
    }
    findPath(categoryTree.value, id)
    return path
  }

  // 加载分类树
  async function loadCategoryTree() {
    if (categoryTree.value.length > 0) {
      return
    }
    loading.value = true
    try {
      const res = await categoryApi.getTree()
      if (res.code === 200) {
        categoryTree.value = res.data
      }
    } finally {
      loading.value = false
    }
  }

  // 刷新分类树
  async function refreshCategoryTree() {
    loading.value = true
    try {
      const res = await categoryApi.getTree()
      if (res.code === 200) {
        categoryTree.value = res.data
      }
    } finally {
      loading.value = false
    }
  }

  return {
    categoryTree,
    flatCategories,
    loading,
    getCategoryName,
    getCategoryPath,
    loadCategoryTree,
    refreshCategoryTree,
  }
})
