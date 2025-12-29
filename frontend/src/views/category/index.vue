<template>
  <div class="category-page">
    <div class="split-layout">
      <!-- 左侧分类树 -->
      <div class="split-left">
        <el-card class="category-tree-card">
          <template #header>
            <div class="card-header">
              <span>知识分类</span>
              <el-button
                v-if="userStore.isAdmin"
                type="primary"
                text
                :icon="Plus"
                @click="showAddCategoryDialog = true"
              >
                添加
              </el-button>
            </div>
          </template>

          <div v-loading="categoryStore.loading" class="tree-container">
            <el-tree
              ref="treeRef"
              :data="categoryStore.categoryTree"
              :props="{ label: 'name', children: 'children' }"
              node-key="id"
              highlight-current
              default-expand-all
              :expand-on-click-node="false"
              @node-click="handleCategoryClick"
            >
              <template #default="{ node, data }">
                <div class="tree-node">
                  <el-icon class="node-icon">
                    <Folder v-if="data.children?.length" />
                    <Document v-else />
                  </el-icon>
                  <span class="node-label">{{ data.name }}</span>
                  <el-tag size="small" type="info" class="node-count">{{ data.questionCount || 0 }}</el-tag>
                </div>
              </template>
            </el-tree>

            <el-empty v-if="!categoryStore.loading && categoryStore.categoryTree.length === 0" description="暂无分类" />
          </div>
        </el-card>
      </div>

      <!-- 右侧内容区 -->
      <div class="split-right">
        <!-- 题目详情视图 -->
        <el-card v-if="selectedQuestion" class="question-detail-card">
          <template #header>
            <div class="detail-header">
              <el-button :icon="ArrowLeft" text @click="selectedQuestion = null">返回列表</el-button>
              <div class="header-actions">
                <el-button :icon="Edit" text @click="handleEditQuestion">编辑</el-button>
              </div>
            </div>
          </template>

          <div v-loading="detailLoading" class="question-detail">
            <h1 class="question-title">{{ selectedQuestion.title }}</h1>

            <div class="question-meta">
              <el-tag :type="DifficultyMap[selectedQuestion.difficulty]?.type" size="small">
                {{ DifficultyMap[selectedQuestion.difficulty]?.label }}
              </el-tag>
              <span class="meta-item">
                <el-icon><Folder /></el-icon>
                {{ selectedQuestion.categoryName || selectedCategory?.name }}
              </span>
              <span class="meta-item">
                <el-icon><User /></el-icon>
                {{ selectedQuestion.creatorName || '匿名' }}
              </span>
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                {{ formatDate(selectedQuestion.createdAt) }}
              </span>
            </div>

            <div v-if="selectedQuestion.tags?.length" class="question-tags">
              <el-tag
                v-for="tag in selectedQuestion.tags"
                :key="tag.id"
                size="small"
                :color="tag.color"
                effect="plain"
              >
                {{ tag.name }}
              </el-tag>
            </div>

            <el-divider content-position="left">题目内容</el-divider>
            <div class="content-section">
              <MdPreview
                :model-value="selectedQuestion.content"
                :theme="themeStore.isDark ? 'dark' : 'light'"
                preview-theme="github"
                code-theme="github"
              />
            </div>

            <el-divider content-position="left">参考答案</el-divider>
            <div class="content-section answer-section">
              <MdPreview
                :model-value="selectedQuestion.answer || '暂无答案'"
                :theme="themeStore.isDark ? 'dark' : 'light'"
                preview-theme="github"
                code-theme="github"
              />
            </div>

            <div class="question-stats">
              <span><el-icon><View /></el-icon> {{ selectedQuestion.viewCount }} 浏览</span>
              <span><el-icon><Star /></el-icon> {{ selectedQuestion.likeCount }} 点赞</span>
              <span><el-icon><ChatDotRound /></el-icon> {{ selectedQuestion.commentCount }} 评论</span>
            </div>
          </div>
        </el-card>

        <!-- 题目列表视图 -->
        <el-card v-else class="question-list-card">
          <template #header>
            <div class="list-header">
              <div class="header-title">
                <el-breadcrumb separator="/">
                  <el-breadcrumb-item>
                    <span @click="selectedCategory = null; loadQuestions()" class="clickable">全部题目</span>
                  </el-breadcrumb-item>
                  <el-breadcrumb-item v-if="selectedCategory">
                    {{ selectedCategory.name }}
                  </el-breadcrumb-item>
                </el-breadcrumb>
                <span class="question-count">共 {{ total }} 道题目</span>
              </div>
              <div class="header-tools">
                <el-input
                  v-model="searchKeyword"
                  placeholder="搜索题目..."
                  clearable
                  :prefix-icon="Search"
                  style="width: 200px"
                  @input="handleSearch"
                />
              </div>
            </div>
          </template>

          <div v-loading="listLoading" class="question-list">
            <div
              v-for="question in questions"
              :key="question.id"
              class="question-item"
              :class="{ 'is-top': question.isTop }"
              @click="handleQuestionClick(question)"
            >
              <div class="item-main">
                <div class="item-title">
                  <el-tag v-if="question.isTop" type="danger" size="small" effect="dark">置顶</el-tag>
                  <el-tag v-if="question.isRecommend" type="warning" size="small" effect="dark">推荐</el-tag>
                  <span class="title-text">{{ question.title }}</span>
                </div>
                <div class="item-meta">
                  <el-tag :type="DifficultyMap[question.difficulty]?.type" size="small">
                    {{ DifficultyMap[question.difficulty]?.label }}
                  </el-tag>
                  <span class="meta-text">
                    <el-icon><User /></el-icon>
                    {{ question.creatorName || '匿名' }}
                  </span>
                  <span class="meta-text">
                    <el-icon><Clock /></el-icon>
                    {{ formatDate(question.createdAt) }}
                  </span>
                </div>
              </div>
              <div class="item-stats">
                <span><el-icon><View /></el-icon> {{ question.viewCount }}</span>
                <span><el-icon><Star /></el-icon> {{ question.likeCount }}</span>
              </div>
            </div>

            <el-empty v-if="!listLoading && questions.length === 0" description="暂无题目" />

            <!-- 分页 -->
            <div v-if="total > pageSize" class="pagination-wrapper">
              <el-pagination
                v-model:current-page="currentPage"
                :page-size="pageSize"
                :total="total"
                layout="prev, pager, next"
                @current-change="loadQuestions"
              />
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 添加分类对话框 -->
    <el-dialog
      v-model="showAddCategoryDialog"
      title="添加分类"
      width="500px"
      destroy-on-close
    >
      <el-form :model="categoryForm" label-width="80px">
        <el-form-item label="分类名称" required>
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="上级分类">
          <el-cascader
            v-model="categoryForm.parentId"
            :options="categoryStore.categoryTree"
            :props="{ value: 'id', label: 'name', checkStrictly: true, emitPath: false }"
            placeholder="选择上级分类（可选）"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="categoryForm.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="categoryForm.description" type="textarea" rows="3" placeholder="分类描述（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddCategoryDialog = false">取消</el-button>
        <el-button type="primary" :loading="categorySubmitting" @click="handleAddCategory">确定</el-button>
      </template>
    </el-dialog>

    <!-- 编辑题目对话框 -->
    <QuestionFormDialog
      v-model:visible="showEditQuestionDialog"
      :question="editingQuestion"
      @success="handleQuestionUpdated"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'
import { ElMessage } from 'element-plus'
import {
  Plus, Folder, Document, ArrowLeft, Edit, Search,
  View, Star, ChatDotRound, User, Clock
} from '@element-plus/icons-vue'
import { useCategoryStore } from '@/stores/category'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { questionApi } from '@/api/question'
import { categoryApi } from '@/api/category'
import type { Question, Category } from '@/types'
import { DifficultyMap } from '@/types'
import QuestionFormDialog from '@/components/question/QuestionFormDialog.vue'

const categoryStore = useCategoryStore()
const userStore = useUserStore()
const themeStore = useThemeStore()

const treeRef = ref()
const selectedCategory = ref<Category | null>(null)
const selectedQuestion = ref<Question | null>(null)
const editingQuestion = ref<Question | undefined>(undefined)

// 题目列表相关
const questions = ref<Question[]>([])
const listLoading = ref(false)
const detailLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const searchKeyword = ref('')

// 分类表单
const showAddCategoryDialog = ref(false)
const categorySubmitting = ref(false)
const categoryForm = reactive({
  name: '',
  parentId: null as number | null,
  sort: 0,
  description: ''
})

// 编辑题目
const showEditQuestionDialog = ref(false)

// 格式化日期
function formatDate(dateStr: string) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 处理分类点击
function handleCategoryClick(data: Category) {
  selectedCategory.value = data
  selectedQuestion.value = null
  currentPage.value = 1
  loadQuestions()
}

// 加载题目列表
async function loadQuestions() {
  listLoading.value = true
  try {
    const res = await questionApi.getPage({
      categoryId: selectedCategory.value?.id,
      keyword: searchKeyword.value || undefined,
      page: currentPage.value,
      size: pageSize.value,
      status: 1
    })
    if (res.code === 200) {
      questions.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('Failed to load questions:', error)
  } finally {
    listLoading.value = false
  }
}

// 处理题目点击
async function handleQuestionClick(question: Question) {
  detailLoading.value = true
  selectedQuestion.value = question

  try {
    const res = await questionApi.getDetail(question.id)
    if (res.code === 200) {
      selectedQuestion.value = res.data
    }
  } catch (error) {
    console.error('Failed to load question detail:', error)
  } finally {
    detailLoading.value = false
  }
}

// 搜索防抖
let searchTimer: ReturnType<typeof setTimeout>
function handleSearch() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    currentPage.value = 1
    loadQuestions()
  }, 300)
}

// 添加分类
async function handleAddCategory() {
  if (!categoryForm.name.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }

  categorySubmitting.value = true
  try {
    await categoryApi.create({
      name: categoryForm.name,
      parentId: categoryForm.parentId || 0,
      sort: categoryForm.sort,
      description: categoryForm.description
    })
    ElMessage.success('添加成功')
    showAddCategoryDialog.value = false
    categoryStore.loadCategoryTree()
    // 重置表单
    categoryForm.name = ''
    categoryForm.parentId = null
    categoryForm.sort = 0
    categoryForm.description = ''
  } catch (error) {
    console.error('Failed to add category:', error)
  } finally {
    categorySubmitting.value = false
  }
}

// 编辑题目
function handleEditQuestion() {
  if (selectedQuestion.value) {
    editingQuestion.value = selectedQuestion.value
    showEditQuestionDialog.value = true
  }
}

// 题目更新成功
function handleQuestionUpdated() {
  if (selectedQuestion.value) {
    handleQuestionClick(selectedQuestion.value)
  }
  loadQuestions()
}

onMounted(() => {
  categoryStore.loadCategoryTree()
  loadQuestions()
})
</script>

<style lang="scss" scoped>
.category-page {
  padding: 20px;
  min-height: calc(100vh - 60px);
}

.split-layout {
  display: flex;
  gap: 20px;
  height: calc(100vh - 140px);
  max-width: 1400px;
  margin: 0 auto;

  @media (max-width: 992px) {
    flex-direction: column;
    height: auto;
  }
}

.split-left {
  width: 300px;
  flex-shrink: 0;

  @media (max-width: 992px) {
    width: 100%;
  }

  .category-tree-card {
    height: 100%;
    display: flex;
    flex-direction: column;

    :deep(.el-card__body) {
      flex: 1;
      overflow-y: auto;
      padding: 0;
    }
  }
}

.split-right {
  flex: 1;
  min-width: 0;
  overflow-y: auto;

  @media (max-width: 992px) {
    overflow: visible;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.tree-container {
  padding: 12px;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  padding: 4px 0;

  .node-icon {
    color: var(--el-color-primary);
    font-size: 16px;
  }

  .node-label {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .node-count {
    margin-left: auto;
  }
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.question-detail {
  .question-title {
    font-size: 24px;
    font-weight: 600;
    margin-bottom: 16px;
    color: var(--text-color);
  }

  .question-meta {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 16px;
    margin-bottom: 12px;
    color: var(--text-secondary);
    font-size: 14px;

    .meta-item {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }

  .question-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 16px;
  }

  .content-section {
    background: var(--hover-bg);
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 16px;
  }

  .answer-section {
    background: rgba(103, 194, 58, 0.08);
  }

  .question-stats {
    display: flex;
    gap: 24px;
    color: var(--text-secondary);
    font-size: 14px;
    padding-top: 16px;
    border-top: 1px solid var(--border-color);

    span {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
}

.list-header {
  .header-title {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 12px;

    .clickable {
      cursor: pointer;
      &:hover {
        color: var(--el-color-primary);
      }
    }

    .question-count {
      color: var(--text-secondary);
      font-size: 14px;
    }
  }

  .header-tools {
    display: flex;
    gap: 12px;
  }
}

.question-list {
  .question-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    border-bottom: 1px solid var(--border-color);
    cursor: pointer;
    transition: background-color 0.2s;

    &:hover {
      background: var(--hover-bg);
    }

    &:last-child {
      border-bottom: none;
    }

    &.is-top {
      background: rgba(245, 108, 108, 0.05);
    }

    .item-main {
      flex: 1;
      min-width: 0;

      .item-title {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;

        .title-text {
          font-size: 16px;
          font-weight: 500;
          color: var(--text-color);
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .item-meta {
        display: flex;
        align-items: center;
        gap: 12px;
        font-size: 13px;
        color: var(--text-secondary);

        .meta-text {
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
    }

    .item-stats {
      display: flex;
      gap: 16px;
      color: var(--text-muted);
      font-size: 13px;

      span {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }

  .pagination-wrapper {
    display: flex;
    justify-content: center;
    padding: 20px 0;
  }
}
</style>
