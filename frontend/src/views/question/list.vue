<template>
  <div class="question-list-page">
    <div class="page-content">
      <!-- 左侧主内容 -->
      <main class="main-content">
        <!-- 筛选条件 -->
        <div class="filter-section">
          <el-card>
            <el-form :inline="true" :model="query" class="filter-form">
              <el-form-item label="关键词">
                <el-input
                  v-model="query.keyword"
                  placeholder="搜索题目"
                  clearable
                  :prefix-icon="Search"
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
                <el-select v-model="query.difficulty" placeholder="选择难度" clearable style="width: 120px">
                  <el-option label="简单" :value="1" />
                  <el-option label="中等" :value="2" />
                  <el-option label="困难" :value="3" />
                </el-select>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
                <el-button :icon="Refresh" @click="handleReset">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </div>

        <!-- 题目列表 -->
        <div class="list-section">
          <el-card>
            <template #header>
              <div class="list-header">
                <span class="total-count">共 <strong>{{ total }}</strong> 道题目</span>
                <div class="sort-options">
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
                  <div class="question-header">
                    <el-tag :type="DifficultyMap[question.difficulty]?.type" size="small">
                      {{ DifficultyMap[question.difficulty]?.label }}
                    </el-tag>
                    <el-tag v-if="question.isTop" type="danger" size="small" effect="dark">置顶</el-tag>
                    <el-tag v-if="question.isRecommend" type="warning" size="small" effect="dark">推荐</el-tag>
                    <span class="category-name">{{ question.categoryName }}</span>
                  </div>
                  <h3 class="question-title">{{ question.title }}</h3>
                  <p class="question-summary">{{ getContentSummary(question.content) }}</p>
                  <div class="question-footer">
                    <div class="question-tags">
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
                    <div class="question-meta">
                      <span><el-icon><View /></el-icon> {{ question.viewCount }}</span>
                      <span><el-icon><Star /></el-icon> {{ question.likeCount }}</span>
                      <span><el-icon><ChatDotRound /></el-icon> {{ question.commentCount }}</span>
                      <span class="time">{{ formatDate(question.createdAt) }}</span>
                    </div>
                  </div>
                </div>
                <el-icon class="arrow-icon"><ArrowRight /></el-icon>
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
                layout="total, sizes, prev, pager, next"
                @size-change="loadQuestions"
                @current-change="loadQuestions"
              />
            </div>
          </el-card>
        </div>
      </main>

      <!-- 右侧栏 -->
      <RightSidebar :category-id="query.categoryId" />
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
import { Search, Refresh, View, Star, ChatDotRound, ArrowRight } from '@element-plus/icons-vue'
import RightSidebar from '@/components/common/RightSidebar.vue'

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
  status: 1
})

function getContentSummary(content: string): string {
  if (!content) return ''
  const text = content.replace(/[#*`\[\]()!]/g, '').replace(/\n/g, ' ')
  return text.length > 120 ? text.substring(0, 120) + '...' : text
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
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-content {
  display: flex;
  gap: 24px;

  @media (max-width: 992px) {
    flex-direction: column;
  }
}

.main-content {
  flex: 1;
  min-width: 0;
}

.filter-section {
  margin-bottom: 20px;

  .filter-form {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;

    .el-form-item {
      margin-bottom: 0;
      margin-right: 0;
    }
  }
}

.list-section {
  .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .total-count {
      color: var(--text-secondary);

      strong {
        color: var(--el-color-primary);
      }
    }
  }
}

.question-list {
  min-height: 400px;
}

.question-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  margin: 0 -20px;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: var(--hover-bg);

    .question-title {
      color: var(--el-color-primary);
    }

    .arrow-icon {
      opacity: 1;
      transform: translateX(0);
    }
  }

  &:last-child {
    border-bottom: none;
  }

  .question-content {
    flex: 1;
    min-width: 0;
  }

  .question-header {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 10px;

    .category-name {
      font-size: 12px;
      color: var(--text-secondary);
    }
  }

  .question-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-color);
    margin-bottom: 8px;
    line-height: 1.5;
    transition: color 0.2s;
  }

  .question-summary {
    font-size: 13px;
    color: var(--text-secondary);
    line-height: 1.6;
    margin-bottom: 12px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .question-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 12px;
  }

  .question-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
  }

  .question-meta {
    display: flex;
    gap: 16px;
    font-size: 12px;
    color: var(--text-muted);

    span {
      display: flex;
      align-items: center;
      gap: 4px;
    }

    .time {
      color: var(--text-secondary);
    }
  }

  .arrow-icon {
    color: var(--text-muted);
    opacity: 0;
    transform: translateX(-8px);
    transition: all 0.2s;
    flex-shrink: 0;
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

  .question-footer {
    flex-direction: column;
    align-items: flex-start !important;
  }
}
</style>
