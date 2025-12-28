<template>
  <div class="category-questions-page">
    <el-page-header @back="router.back()">
      <template #content>
        <span class="page-title">{{ categoryName }}</span>
      </template>
    </el-page-header>

    <el-card class="mt-20">
      <div v-loading="loading" class="question-list">
        <div
          v-for="question in questions"
          :key="question.id"
          class="question-item"
          @click="router.push(`/questions/${question.id}`)"
        >
          <div class="question-tags">
            <el-tag :type="DifficultyMap[question.difficulty].type" size="small">
              {{ DifficultyMap[question.difficulty].label }}
            </el-tag>
          </div>
          <h3 class="question-title">{{ question.title }}</h3>
          <div class="question-meta">
            <span><el-icon><View /></el-icon> {{ question.viewCount }}</span>
            <span><el-icon><Star /></el-icon> {{ question.likeCount }}</span>
          </div>
        </div>

        <el-empty v-if="!loading && questions.length === 0" description="该分类下暂无题目" />
      </div>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadQuestions"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCategoryStore } from '@/stores/category'
import { questionApi } from '@/api/question'
import { DifficultyMap, type Question } from '@/types'
import { View, Star } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const categoryStore = useCategoryStore()

const loading = ref(false)
const questions = ref<Question[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const categoryName = ref('')

async function loadQuestions() {
  const categoryId = Number(route.params.id)
  if (!categoryId) return

  categoryName.value = categoryStore.getCategoryName(categoryId)

  loading.value = true
  try {
    const res = await questionApi.getPage({
      categoryId,
      page: page.value,
      size: size.value,
    })
    if (res.code === 200) {
      questions.value = res.data.records
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

watch(() => route.params.id, loadQuestions)

onMounted(() => {
  loadQuestions()
})
</script>

<style lang="scss" scoped>
.category-questions-page {
  max-width: 900px;
  margin: 0 auto;

  .page-title {
    font-size: 18px;
    font-weight: 600;
  }
}

.question-list {
  min-height: 300px;
}

.question-item {
  padding: 16px 0;
  border-bottom: 1px solid #eee;
  cursor: pointer;

  &:hover {
    background: #f9f9f9;
  }

  &:last-child {
    border-bottom: none;
  }

  .question-tags {
    margin-bottom: 8px;
  }

  .question-title {
    font-size: 16px;
    color: #333;
    margin-bottom: 8px;
  }

  .question-meta {
    display: flex;
    gap: 16px;
    font-size: 13px;
    color: #999;

    span {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
