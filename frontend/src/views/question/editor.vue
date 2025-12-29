<template>
  <div class="question-editor-page">
    <!-- 顶部工具栏 -->
    <header class="editor-header">
      <div class="header-left">
        <el-button :icon="ArrowLeft" text @click="handleBack">返回</el-button>
        <el-divider direction="vertical" />
        <span class="page-title">{{ isEdit ? '编辑题目' : '发布题目' }}</span>
      </div>
      <div class="header-right">
        <el-button @click="handleSaveDraft">保存草稿</el-button>
        <el-button type="primary" :loading="submitting" @click="handlePublish">
          {{ isEdit ? '保存修改' : '发布题目' }}
        </el-button>
      </div>
    </header>

    <!-- 编辑区域 -->
    <div class="editor-container">
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
        <div class="editor-section">
          <div class="section-label">
            <el-icon><Document /></el-icon>
            <span>题目内容</span>
          </div>
          <MdEditor
            v-model="form.content"
            :theme="themeStore.isDark ? 'dark' : 'light'"
            preview-theme="github"
            code-theme="github"
            :toolbars="toolbars"
            style="height: 350px"
            placeholder="请输入题目内容，支持 Markdown 格式..."
          />
        </div>

        <!-- 答案编辑器 -->
        <div class="editor-section">
          <div class="section-label">
            <el-icon><Checked /></el-icon>
            <span>参考答案</span>
          </div>
          <MdEditor
            v-model="form.answer"
            :theme="themeStore.isDark ? 'dark' : 'light'"
            preview-theme="github"
            code-theme="github"
            :toolbars="toolbars"
            style="height: 350px"
            placeholder="请输入参考答案，支持 Markdown 格式..."
          />
        </div>
      </div>

      <!-- 右侧设置面板 -->
      <aside class="editor-sidebar">
        <el-card class="settings-card">
          <template #header>
            <div class="card-header">
              <el-icon><Setting /></el-icon>
              <span>题目设置</span>
            </div>
          </template>

          <el-form :model="form" label-position="top" size="default">
            <!-- 分类选择 -->
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

            <!-- 难度选择 -->
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

            <!-- 标签选择 -->
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

            <!-- 题目来源 -->
            <el-form-item label="题目来源">
              <el-input v-model="form.source" placeholder="如：阿里巴巴、字节跳动" />
            </el-form-item>

            <!-- 来源链接 -->
            <el-form-item label="来源链接">
              <el-input v-model="form.sourceUrl" placeholder="原文链接（可选）" />
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
            <!-- 状态 -->
            <el-form-item label="发布状态">
              <el-switch
                v-model="form.status"
                :active-value="1"
                :inactive-value="0"
                active-text="发布"
                inactive-text="草稿"
              />
            </el-form-item>

            <!-- 置顶 -->
            <el-form-item label="置顶题目">
              <el-switch
                v-model="form.isTop"
                :active-value="1"
                :inactive-value="0"
              />
            </el-form-item>

            <!-- 推荐 -->
            <el-form-item label="推荐题目">
              <el-switch
                v-model="form.isRecommend"
                :active-value="1"
                :inactive-value="0"
              />
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 操作提示 -->
        <el-card class="tips-card">
          <template #header>
            <div class="card-header">
              <el-icon><InfoFilled /></el-icon>
              <span>温馨提示</span>
            </div>
          </template>
          <ul class="tips-list">
            <li>标题建议简洁明了，概括题目核心</li>
            <li>内容支持 Markdown 语法</li>
            <li>可以插入代码块、表格、图片等</li>
            <li>答案可以包含详细的解题思路</li>
            <li>选择合适的分类便于他人查找</li>
          </ul>
        </el-card>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { useCategoryStore } from '@/stores/category'
import { useThemeStore } from '@/stores/theme'
import { questionApi } from '@/api/question'
import { tagApi } from '@/api/tag'
import type { Tag, QuestionForm } from '@/types'
import {
  ArrowLeft, Document, Checked, Setting, Operation, InfoFilled
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const categoryStore = useCategoryStore()
const themeStore = useThemeStore()

const questionId = computed(() => route.params.id ? Number(route.params.id) : null)
const isEdit = computed(() => !!questionId.value)

const submitting = ref(false)
const availableTags = ref<Tag[]>([])
const categoryValue = ref<number | null>(null)

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
  'preview', 'htmlPreview', 'catalog'
]

function handleCategoryChange(value: number | null) {
  form.categoryId = value
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
    return false
  }
  if (!form.categoryId) {
    ElMessage.warning('请选择题目分类')
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

onMounted(() => {
  categoryStore.loadCategoryTree()
  loadTags()
  if (isEdit.value) {
    loadQuestion()
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
  padding: 12px 24px;
  background: var(--card-bg);
  border-bottom: 1px solid var(--border-color);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;

    .page-title {
      font-size: 16px;
      font-weight: 600;
      color: var(--text-color);
    }
  }

  .header-right {
    display: flex;
    gap: 12px;
  }
}

.editor-container {
  flex: 1;
  display: flex;
  gap: 24px;
  max-width: 1600px;
  margin: 0 auto;
  padding: 24px;
  width: 100%;
  box-sizing: border-box;

  @media (max-width: 1200px) {
    flex-direction: column;
  }
}

.editor-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.title-input-wrapper {
  position: relative;
  background: var(--card-bg);
  border-radius: 8px;
  padding: 16px 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

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
}

.editor-section {
  background: var(--card-bg);
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  .section-label {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    background: var(--hover-bg);
    font-weight: 500;
    color: var(--text-color);
    border-bottom: 1px solid var(--border-color);

    .el-icon {
      color: var(--el-color-primary);
    }
  }
}

.editor-sidebar {
  width: 320px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;

  @media (max-width: 1200px) {
    width: 100%;
  }
}

.settings-card, .tips-card {
  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    color: var(--text-color);

    .el-icon {
      color: var(--el-color-primary);
    }
  }

  :deep(.el-card__header) {
    padding: 12px 16px;
    background: var(--hover-bg);
  }

  :deep(.el-card__body) {
    padding: 16px;
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
    }
  }
}

.tips-card {
  .tips-list {
    list-style: none;
    padding: 0;
    margin: 0;

    li {
      position: relative;
      padding: 8px 0 8px 16px;
      font-size: 13px;
      color: var(--text-secondary);
      border-bottom: 1px dashed var(--border-color);

      &::before {
        content: '•';
        position: absolute;
        left: 0;
        color: var(--el-color-primary);
      }

      &:last-child {
        border-bottom: none;
        padding-bottom: 0;
      }

      &:first-child {
        padding-top: 0;
      }
    }
  }
}
</style>
