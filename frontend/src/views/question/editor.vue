<template>
  <div class="question-editor-page">
    <!-- 顶部工具栏 -->
    <header class="editor-header">
      <div class="header-left">
        <el-button :icon="ArrowLeft" text @click="handleBack">返回</el-button>
        <el-divider direction="vertical" />
        <span class="page-title">{{ isEdit ? '编辑题目' : '发布题目' }}</span>
        <el-tag v-if="autoSaved" type="success" size="small" class="auto-save-tag">
          <el-icon><Check /></el-icon> 已自动保存
        </el-tag>
      </div>
      <div class="header-center">
        <el-radio-group v-model="activeTab" size="default">
          <el-radio-button value="content">
            <el-icon><Document /></el-icon> 题目内容
          </el-radio-button>
          <el-radio-button value="answer">
            <el-icon><Checked /></el-icon> 参考答案
          </el-radio-button>
        </el-radio-group>
      </div>
      <div class="header-right">
        <el-button @click="handlePreview">
          <el-icon><View /></el-icon> 预览
        </el-button>
        <el-button @click="handleSaveDraft">保存草稿</el-button>
        <el-button type="primary" :loading="submitting" @click="handlePublish">
          {{ isEdit ? '保存修改' : '发布题目' }}
        </el-button>
      </div>
    </header>

    <!-- 编辑区域 -->
    <div class="editor-container">
      <!-- 左侧目录 -->
      <aside class="catalog-sidebar" :class="{ collapsed: catalogCollapsed }">
        <div class="catalog-header">
          <span>目录</span>
          <el-button :icon="catalogCollapsed ? Expand : Fold" text size="small" @click="catalogCollapsed = !catalogCollapsed" />
        </div>
        <div v-show="!catalogCollapsed" class="catalog-content">
          <MdCatalog
            :editor-id="activeTab === 'content' ? 'content-editor' : 'answer-editor'"
            :scroll-element="scrollElement"
            :theme="themeStore.isDark ? 'dark' : 'light'"
          />
          <el-empty v-if="!hasCatalog" description="暂无目录" :image-size="60">
            <template #description>
              <span class="catalog-tip">使用 # 标题语法<br />生成目录结构</span>
            </template>
          </el-empty>
        </div>
      </aside>

      <!-- 中间编辑区 -->
      <div class="editor-main">
        <!-- 标题输入 -->
        <div class="title-input-wrapper">
          <input
            v-model="form.title"
            type="text"
            class="title-input"
            placeholder="请输入题目标题..."
            maxlength="200"
          />
          <span class="title-count">{{ form.title.length }}/200</span>
        </div>

        <!-- 题目内容编辑器 -->
        <div v-show="activeTab === 'content'" class="editor-wrapper">
          <MdEditor
            v-model="form.content"
            editor-id="content-editor"
            :theme="themeStore.isDark ? 'dark' : 'light'"
            preview-theme="github"
            code-theme="github"
            :toolbars="toolbars"
            :footers="[]"
            show-code-row-number
            @on-get-catalog="handleContentCatalog"
            placeholder="请输入题目内容，支持 Markdown 格式..."
          />
        </div>

        <!-- 答案编辑器 -->
        <div v-show="activeTab === 'answer'" class="editor-wrapper">
          <MdEditor
            v-model="form.answer"
            editor-id="answer-editor"
            :theme="themeStore.isDark ? 'dark' : 'light'"
            preview-theme="github"
            code-theme="github"
            :toolbars="toolbars"
            :footers="[]"
            show-code-row-number
            @on-get-catalog="handleAnswerCatalog"
            placeholder="请输入参考答案，支持 Markdown 格式..."
          />
        </div>
      </div>

      <!-- 右侧设置面板 -->
      <aside class="editor-sidebar" :class="{ collapsed: settingsCollapsed }">
        <div class="sidebar-toggle" @click="settingsCollapsed = !settingsCollapsed">
          <el-icon><Setting /></el-icon>
        </div>

        <div v-show="!settingsCollapsed" class="sidebar-content">
          <el-card class="settings-card">
            <template #header>
              <div class="card-header">
                <el-icon><Setting /></el-icon>
                <span>题目设置</span>
              </div>
            </template>

            <el-form :model="form" label-position="top" size="default">
              <el-form-item label="所属分类" required>
                <el-cascader
                  v-model="categoryValue"
                  :options="categoryStore.categoryTree"
                  :props="{ value: 'id', label: 'name', checkStrictly: true, emitPath: false }"
                  placeholder="请选择分类"
                  clearable
                  style="width: 100%"
                  @change="handleCategoryChange"
                />
              </el-form-item>

              <el-form-item label="难度级别" required>
                <el-radio-group v-model="form.difficulty" class="difficulty-group">
                  <el-radio-button :value="1">
                    <el-tag type="success" effect="plain" size="small">简单</el-tag>
                  </el-radio-button>
                  <el-radio-button :value="2">
                    <el-tag type="warning" effect="plain" size="small">中等</el-tag>
                  </el-radio-button>
                  <el-radio-button :value="3">
                    <el-tag type="danger" effect="plain" size="small">困难</el-tag>
                  </el-radio-button>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="题目标签">
                <el-select
                  v-model="form.tagIds"
                  multiple
                  filterable
                  placeholder="选择标签"
                  style="width: 100%"
                >
                  <el-option
                    v-for="tag in availableTags"
                    :key="tag.id"
                    :label="tag.name"
                    :value="tag.id"
                  />
                </el-select>
              </el-form-item>

              <el-form-item label="题目来源">
                <el-input v-model="form.source" placeholder="如：阿里巴巴" />
              </el-form-item>
            </el-form>
          </el-card>

          <el-card class="settings-card">
            <template #header>
              <div class="card-header">
                <el-icon><Operation /></el-icon>
                <span>发布选项</span>
              </div>
            </template>

            <el-form :model="form" label-position="top" size="default">
              <el-form-item label="发布状态">
                <el-switch
                  v-model="form.status"
                  :active-value="1"
                  :inactive-value="0"
                  active-text="发布"
                  inactive-text="草稿"
                />
              </el-form-item>

              <el-form-item label="特殊标记">
                <div class="special-options">
                  <el-checkbox v-model="isTop" label="置顶" />
                  <el-checkbox v-model="isRecommend" label="推荐" />
                </div>
              </el-form-item>
            </el-form>
          </el-card>
        </div>
      </aside>
    </div>

    <!-- 预览对话框 -->
    <el-dialog
      v-model="showPreview"
      title="题目预览"
      width="80%"
      top="5vh"
      destroy-on-close
    >
      <div class="preview-content">
        <h1 class="preview-title">{{ form.title || '无标题' }}</h1>
        <div class="preview-meta">
          <el-tag :type="DifficultyMap[form.difficulty]?.type" size="small">
            {{ DifficultyMap[form.difficulty]?.label }}
          </el-tag>
        </div>
        <el-divider content-position="left">题目内容</el-divider>
        <MdPreview
          :model-value="form.content || '暂无内容'"
          :theme="themeStore.isDark ? 'dark' : 'light'"
          preview-theme="github"
        />
        <el-divider content-position="left">参考答案</el-divider>
        <MdPreview
          :model-value="form.answer || '暂无答案'"
          :theme="themeStore.isDark ? 'dark' : 'light'"
          preview-theme="github"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MdEditor, MdPreview, MdCatalog } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { useCategoryStore } from '@/stores/category'
import { useThemeStore } from '@/stores/theme'
import { questionApi } from '@/api/question'
import { tagApi } from '@/api/tag'
import { DifficultyMap, type Tag, type QuestionForm } from '@/types'
import {
  ArrowLeft, Document, Checked, Setting, Operation, View, Check, Expand, Fold
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const categoryStore = useCategoryStore()
const themeStore = useThemeStore()

const questionId = computed(() => route.params.id ? Number(route.params.id) : null)
const isEdit = computed(() => !!questionId.value)

const submitting = ref(false)
const autoSaved = ref(false)
const showPreview = ref(false)
const activeTab = ref<'content' | 'answer'>('content')
const catalogCollapsed = ref(false)
const settingsCollapsed = ref(false)
const availableTags = ref<Tag[]>([])
const categoryValue = ref<number | null>(null)
const scrollElement = ref<HTMLElement | null>(null)
const contentCatalog = ref<any[]>([])
const answerCatalog = ref<any[]>([])

const hasCatalog = computed(() => {
  return activeTab.value === 'content'
    ? contentCatalog.value.length > 0
    : answerCatalog.value.length > 0
})

const isTop = computed({
  get: () => form.isTop === 1,
  set: (val) => { form.isTop = val ? 1 : 0 }
})

const isRecommend = computed({
  get: () => form.isRecommend === 1,
  set: (val) => { form.isRecommend = val ? 1 : 0 }
})

const form = reactive<QuestionForm>({
  title: '',
  content: '',
  answer: '',
  categoryId: null,
  difficulty: 2,
  source: '',
  sourceUrl: '',
  tagIds: [],
  status: 1,
  isTop: 0,
  isRecommend: 0,
})

const toolbars = [
  'bold', 'underline', 'italic', 'strikeThrough', '-',
  'title', 'sub', 'sup', 'quote', '-',
  'unorderedList', 'orderedList', 'task', '-',
  'codeRow', 'code', 'link', 'image', 'table', '-',
  'revoke', 'next', '=',
  'prettier', 'preview', 'fullscreen'
]

function handleContentCatalog(list: any[]) {
  contentCatalog.value = list
}

function handleAnswerCatalog(list: any[]) {
  answerCatalog.value = list
}

function handleCategoryChange(value: number | null) {
  form.categoryId = value
}

function handlePreview() {
  showPreview.value = true
}

async function loadTags() {
  try {
    const res = await tagApi.getList()
    if (res.code === 200) {
      availableTags.value = res.data
    }
  } catch (error) {
    console.error('Failed to load tags:', error)
  }
}

async function loadQuestion() {
  if (!questionId.value) return

  try {
    const res = await questionApi.getDetail(questionId.value)
    if (res.code === 200) {
      const q = res.data
      form.title = q.title
      form.content = q.content
      form.answer = q.answer
      form.categoryId = q.categoryId
      form.difficulty = q.difficulty
      form.source = q.source || ''
      form.sourceUrl = q.sourceUrl || ''
      form.tagIds = q.tags?.map(t => t.id) || []
      form.status = q.status
      form.isTop = q.isTop
      form.isRecommend = q.isRecommend
      categoryValue.value = q.categoryId
    }
  } catch (error) {
    console.error('Failed to load question:', error)
    ElMessage.error('加载题目失败')
  }
}

function validateForm(): boolean {
  if (!form.title.trim()) {
    ElMessage.warning('请输入题目标题')
    return false
  }
  if (!form.content.trim()) {
    ElMessage.warning('请输入题目内容')
    activeTab.value = 'content'
    return false
  }
  if (!form.categoryId) {
    ElMessage.warning('请选择题目分类')
    settingsCollapsed.value = false
    return false
  }
  return true
}

async function handleSaveDraft() {
  if (!form.title.trim()) {
    ElMessage.warning('请先输入题目标题')
    return
  }

  form.status = 0
  await submitQuestion()
}

async function handlePublish() {
  if (!validateForm()) return

  form.status = 1
  await submitQuestion()
}

async function submitQuestion() {
  submitting.value = true
  try {
    if (isEdit.value && questionId.value) {
      await questionApi.update(questionId.value, form)
      ElMessage.success('题目更新成功')
    } else {
      await questionApi.create(form)
      ElMessage.success('题目发布成功')
    }
    router.push('/questions')
  } catch (error) {
    console.error('Failed to submit question:', error)
  } finally {
    submitting.value = false
  }
}

function handleBack() {
  if (form.title || form.content || form.answer) {
    ElMessageBox.confirm('确定要离开吗？未保存的内容将丢失。', '提示', {
      confirmButtonText: '确定离开',
      cancelButtonText: '继续编辑',
      type: 'warning',
    }).then(() => {
      router.back()
    }).catch(() => {})
  } else {
    router.back()
  }
}

// 自动保存
let autoSaveTimer: ReturnType<typeof setTimeout>
watch([() => form.title, () => form.content, () => form.answer], () => {
  clearTimeout(autoSaveTimer)
  autoSaved.value = false
  autoSaveTimer = setTimeout(() => {
    if (form.title || form.content || form.answer) {
      localStorage.setItem('question-draft', JSON.stringify(form))
      autoSaved.value = true
      setTimeout(() => { autoSaved.value = false }, 2000)
    }
  }, 3000)
})

onMounted(() => {
  categoryStore.loadCategoryTree()
  loadTags()
  scrollElement.value = document.documentElement

  if (isEdit.value) {
    loadQuestion()
  } else {
    // 恢复草稿
    const draft = localStorage.getItem('question-draft')
    if (draft) {
      try {
        const data = JSON.parse(draft)
        if (data.title || data.content) {
          ElMessageBox.confirm('检测到未保存的草稿，是否恢复？', '提示', {
            confirmButtonText: '恢复草稿',
            cancelButtonText: '新建题目',
            type: 'info',
          }).then(() => {
            Object.assign(form, data)
            if (data.categoryId) categoryValue.value = data.categoryId
          }).catch(() => {
            localStorage.removeItem('question-draft')
          })
        }
      } catch {}
    }
  }
})
</script>

<style lang="scss" scoped>
.question-editor-page {
  min-height: 100vh;
  background: var(--bg-color);
  display: flex;
  flex-direction: column;
}

.editor-header {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 20px;
  background: var(--card-bg);
  border-bottom: 1px solid var(--border-color);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  gap: 16px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-shrink: 0;

    .page-title {
      font-size: 16px;
      font-weight: 600;
      color: var(--text-color);
    }

    .auto-save-tag {
      margin-left: 8px;
    }
  }

  .header-center {
    flex: 1;
    display: flex;
    justify-content: center;
  }

  .header-right {
    display: flex;
    gap: 10px;
    flex-shrink: 0;
  }

  @media (max-width: 768px) {
    flex-wrap: wrap;
    padding: 10px 12px;

    .header-center {
      order: 3;
      width: 100%;
      justify-content: flex-start;
      margin-top: 8px;
    }

    .header-right {
      .el-button span {
        display: none;
      }
    }
  }
}

.editor-container {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.catalog-sidebar {
  width: 220px;
  flex-shrink: 0;
  background: var(--card-bg);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  transition: width 0.3s;

  &.collapsed {
    width: 0;
    overflow: hidden;
  }

  .catalog-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    font-weight: 600;
    color: var(--text-color);
    border-bottom: 1px solid var(--border-color);
    background: var(--hover-bg);
  }

  .catalog-content {
    flex: 1;
    overflow-y: auto;
    padding: 12px;

    :deep(.md-editor-catalog) {
      font-size: 13px;

      .md-editor-catalog-link {
        padding: 6px 8px;
        border-radius: 4px;
        color: var(--text-secondary);

        &:hover {
          background: var(--hover-bg);
          color: var(--el-color-primary);
        }

        &.md-editor-catalog-active {
          color: var(--el-color-primary);
          background: var(--el-color-primary-light-9);
        }
      }
    }

    .catalog-tip {
      font-size: 12px;
      color: var(--text-muted);
      line-height: 1.6;
    }
  }

  @media (max-width: 1200px) {
    display: none;
  }
}

.editor-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.title-input-wrapper {
  position: relative;
  background: var(--card-bg);
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-color);

  .title-input {
    width: 100%;
    border: none;
    outline: none;
    font-size: 24px;
    font-weight: 600;
    color: var(--text-color);
    background: transparent;
    padding-right: 60px;

    &::placeholder {
      color: var(--text-muted);
      font-weight: 400;
    }
  }

  .title-count {
    position: absolute;
    right: 20px;
    top: 50%;
    transform: translateY(-50%);
    font-size: 12px;
    color: var(--text-muted);
  }

  @media (max-width: 768px) {
    padding: 12px 16px;

    .title-input {
      font-size: 18px;
    }
  }
}

.editor-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  :deep(.md-editor) {
    flex: 1;
    border: none;
    border-radius: 0;
  }
}

.editor-sidebar {
  width: 300px;
  flex-shrink: 0;
  background: var(--card-bg);
  border-left: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  position: relative;
  transition: width 0.3s;

  &.collapsed {
    width: 40px;

    .sidebar-content {
      display: none;
    }
  }

  .sidebar-toggle {
    position: absolute;
    left: 0;
    top: 50%;
    transform: translate(-50%, -50%);
    width: 24px;
    height: 48px;
    background: var(--card-bg);
    border: 1px solid var(--border-color);
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    z-index: 10;

    &:hover {
      background: var(--hover-bg);
    }
  }

  .sidebar-content {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  @media (max-width: 992px) {
    display: none;
  }
}

.settings-card {
  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    font-size: 14px;
    color: var(--text-color);

    .el-icon {
      color: var(--el-color-primary);
    }
  }

  :deep(.el-card__header) {
    padding: 10px 14px;
    background: var(--hover-bg);
  }

  :deep(.el-card__body) {
    padding: 14px;
  }

  :deep(.el-form-item) {
    margin-bottom: 14px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  :deep(.el-form-item__label) {
    font-size: 13px;
    padding-bottom: 4px;
  }
}

.difficulty-group {
  width: 100%;

  :deep(.el-radio-button) {
    flex: 1;

    .el-radio-button__inner {
      width: 100%;
      display: flex;
      justify-content: center;
      padding: 8px;
    }
  }
}

.special-options {
  display: flex;
  gap: 16px;
}

.preview-content {
  .preview-title {
    font-size: 24px;
    font-weight: 600;
    color: var(--text-color);
    margin-bottom: 16px;
  }

  .preview-meta {
    margin-bottom: 16px;
  }
}
</style>
