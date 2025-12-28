<template>
  <div class="category-page">
    <el-card>
      <template #header>
        <h2>知识分类</h2>
      </template>

      <div v-loading="categoryStore.loading" class="category-tree">
        <div v-for="category in categoryStore.categoryTree" :key="category.id" class="category-group">
          <div class="category-header" @click="toggleExpand(category.id)">
            <el-icon class="expand-icon" :class="{ expanded: expandedIds.includes(category.id) }">
              <ArrowRight />
            </el-icon>
            <el-icon v-if="category.icon" class="category-icon"><Folder /></el-icon>
            <span class="category-name">{{ category.name }}</span>
            <el-tag size="small" type="info">{{ category.questionCount }}</el-tag>
          </div>

          <el-collapse-transition>
            <div v-show="expandedIds.includes(category.id)" class="category-children">
              <div
                v-for="child in category.children"
                :key="child.id"
                class="category-item level-2"
              >
                <div class="item-content" @click="goToCategory(child.id)">
                  <span class="category-name">{{ child.name }}</span>
                  <el-tag size="small" type="info">{{ child.questionCount }}</el-tag>
                </div>

                <!-- 三级分类 -->
                <div v-if="child.children?.length" class="category-children level-3">
                  <div
                    v-for="grandChild in child.children"
                    :key="grandChild.id"
                    class="category-item"
                    @click="goToCategory(grandChild.id)"
                  >
                    <span class="category-name">{{ grandChild.name }}</span>
                    <el-tag size="small" type="info">{{ grandChild.questionCount }}</el-tag>
                  </div>
                </div>
              </div>
            </div>
          </el-collapse-transition>
        </div>

        <el-empty v-if="!categoryStore.loading && categoryStore.categoryTree.length === 0" description="暂无分类" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCategoryStore } from '@/stores/category'
import { ArrowRight, Folder } from '@element-plus/icons-vue'

const router = useRouter()
const categoryStore = useCategoryStore()

const expandedIds = ref<number[]>([])

function toggleExpand(id: number) {
  const index = expandedIds.value.indexOf(id)
  if (index === -1) {
    expandedIds.value.push(id)
  } else {
    expandedIds.value.splice(index, 1)
  }
}

function goToCategory(id: number) {
  router.push(`/categories/${id}`)
}

onMounted(() => {
  categoryStore.loadCategoryTree()
  // 默认展开所有一级分类
  expandedIds.value = categoryStore.categoryTree.map(c => c.id)
})
</script>

<style lang="scss" scoped>
.category-page {
  max-width: 900px;
  margin: 0 auto;
}

.category-tree {
  min-height: 300px;
}

.category-group {
  margin-bottom: 8px;
}

.category-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f9f9f9;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;

  &:hover {
    background: #f0f0f0;
  }

  .expand-icon {
    transition: transform 0.2s;

    &.expanded {
      transform: rotate(90deg);
    }
  }

  .category-icon {
    color: var(--el-color-primary);
  }

  .category-name {
    flex: 1;
    font-size: 16px;
    font-weight: 600;
    color: #333;
  }
}

.category-children {
  padding-left: 44px;

  &.level-3 {
    padding-left: 20px;
  }
}

.category-item {
  border-bottom: 1px solid #eee;

  &:last-child {
    border-bottom: none;
  }

  &.level-2 {
    .item-content {
      padding: 14px 16px;
    }
  }

  .item-content,
  & > .category-name {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    cursor: pointer;
    transition: background-color 0.2s;

    &:hover {
      background: #f5f5f5;
    }
  }

  .category-name {
    color: #333;
  }
}
</style>
