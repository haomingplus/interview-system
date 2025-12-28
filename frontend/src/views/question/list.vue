<template>
  <div class="question-list-page">
    <!-- 筛选条件 -->
    <div class="filter-section">
      <el-card>
        <el-form :inline="true" :model="query" class="filter-form">
          <el-form-item label="关键词">
            <el-input
              v-model="query.keyword"
              placeholder="搜索题目"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>

          <el-form-item label="分类">
            <el-cascader
              v-model="categoryValue"
              :options="categoryStore.categoryTree"
              :props="{ value: 'id', label: 'name', checkStrictly: true, emitPath: false }"
              placeholder="选择分类"
              clearable
              @change="handleCategoryChange"
            />
          </el-form-item>

          <el-form-item label="难度">
            <el-select v-model="query.difficulty" placeholder="选择难度" clearable>
              <el-option label="简单" :value="1" />
              <el-option label="中等" :value="2" />
              <el-option label="困难" :value="3" />
            </el-select>
          </el-form-item>

          <el-form-item label="来源">
            <el-input v-model="query.source" placeholder="题目来源" clearable />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 题目列表 -->
    <div class="list-section">
      <el-card>
        <template #header>
          <div class="list-header">
            <span>共 {{ total }} 道题目</span>
            <div class="sort-options">
              <span>排序：</span>
              <el-radio-group v-model="query.sortField" size="small" @change="loadQuestions">
                <el-radio-button value="createdAt">最新</el-radio-button>
                <el-radio-button value="viewCount">热门</el-radio-button>
                <el-radio-button value="likeCount">点赞</el-radio-button>
              </el-radio-group>
            </div>
          </div>
        </template>

        <div v-loading="loading" class="question-list">
          <div
            v-for="question in questions"
            :key="question.id"
            class="question-item"
            @click="router.push(`/questions/${question.id}`)"
          >
            <div class="question-content">
              <div class="question-tags">
                <el-tag :type="DifficultyMap[question.difficulty].type" size="small">
                  {{ DifficultyMap[question.difficulty].label }}
                </el-tag>
                <el-tag v-if="question.source" type="info" size="small">
                  {{ question.source }}
                </el-tag>
                <el-tag
                  v-for="tag in question.tags?.slice(0, 3)"
                  :key="tag.id"
                  :color="tag.color"
                  size="small"
                  effect="plain"
                >
                  {{ tag.name }}
                </el-tag>
              </div>
              <h3 class="question-title">{{ question.title }}</h3>
              <p class="question-summary">{{ getContentSummary(question.content) }}</p>
              <div class="question-meta">
                <span><el-icon><Folder /></el-icon> {{ question.categoryName }}</span>
                <span><el-icon><View /></el-icon> {{ question.viewCount }}</span>
                <span><el-icon><Star /></el-icon> {{ question.likeCount }}</span>
                <span><el-icon><ChatDotRound /></el-icon> {{ question.commentCount }}</span>
                <span><el-icon><Clock /></el-icon> {{ formatDate(question.createdAt) }}</span>
              </div>
            </div>
          </div>

          <el-empty v-if="!loading && questions.length === 0" description="暂无题目" />
        </div>

        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="query.page"
            v-model:page-size="query.size"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadQuestions"
            @current-change="loadQuestions"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useCategoryStore } from '@/stores/category'
import { questionApi } from '@/api/question'
import { DifficultyMap, type Question, type QuestionQuery } from '@/types'
import dayjs from 'dayjs'
import { Folder, View, Star, ChatDotRound, Clock } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const categoryStore = useCategoryStore()

const loading = ref(false)
const questions = ref<Question[]>([])
const total = ref(0)
const categoryValue = ref<number | null>(null)

const query = reactive<QuestionQuery>({
  keyword: '',
  categoryId: undefined,
  difficulty: undefined,
  source: '',
  page: 1,
  size: 10,
  sortField: 'createdAt',
  sortOrder: 'desc',
})

function getContentSummary(content: string): string {
  if (!content) return ''
  // 移除Markdown标记，获取纯文本摘要
  const text = content.replace(/[#*`\[\]()!]/g, '').replace(/\n/g, ' ')
  return text.length > 100 ? text.substring(0, 100) + '...' : text
}

function formatDate(date: string): string {
  return dayjs(date).format('YYYY-MM-DD')
}

function handleCategoryChange(value: number | null) {
  query.categoryId = value || undefined
}

async function loadQuestions() {
  loading.value = true
  try {
    const res = await questionApi.getPage(query)
    if (res.code === 200) {
      questions.value = res.data.records
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  loadQuestions()
}

function handleReset() {
  query.keyword = ''
  query.categoryId = undefined
  query.difficulty = undefined
  query.source = ''
  query.page = 1
  categoryValue.value = null
  loadQuestions()
}

// 从URL参数初始化查询条件
watch(
  () => route.query,
  (newQuery) => {
    if (newQuery.keyword) {
      query.keyword = newQuery.keyword as string
    }
    if (newQuery.categoryId) {
      query.categoryId = Number(newQuery.categoryId)
      categoryValue.value = query.categoryId
    }
  },
  { immediate: true }
)

onMounted(() => {
  loadQuestions()
})
</script>

<style lang="scss" scoped>
.question-list-page {
  max-width: 1200px;
  margin: 0 auto;
}

.filter-section {
  margin-bottom: 20px;

  .filter-form {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;

    .el-form-item {
      margin-bottom: 0;
    }
  }
}

.list-section {
  .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .sort-options {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }
}

.question-list {
  min-height: 400px;
}

.question-item {
  padding: 20px 0;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background-color 0.2s;

  &:hover {
    background-color: #f9f9f9;
  }

  &:last-child {
    border-bottom: none;
  }

  .question-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 12px;
  }

  .question-title {
    font-size: 18px;
    font-weight: 600;
    color: #333;
    margin-bottom: 8px;
    line-height: 1.4;

    &:hover {
      color: var(--el-color-primary);
    }
  }

  .question-summary {
    font-size: 14px;
    color: #666;
    line-height: 1.6;
    margin-bottom: 12px;
  }

  .question-meta {
    display: flex;
    flex-wrap: wrap;
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

@media (max-width: 768px) {
  .filter-form {
    flex-direction: column;

    .el-form-item {
      width: 100%;
    }
  }

  .list-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start !important;
  }
}
</style>
