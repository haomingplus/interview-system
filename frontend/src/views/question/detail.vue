<template>
  <div v-loading="loading" class="question-detail-page">
    <template v-if="question">
      <!-- 题目信息 -->
      <el-card class="question-card">
        <template #header>
          <div class="question-header">
            <div class="question-tags">
              <el-tag :type="DifficultyMap[question.difficulty].type">
                {{ DifficultyMap[question.difficulty].label }}
              </el-tag>
              <el-tag v-if="question.source" type="info">{{ question.source }}</el-tag>
              <el-tag
                v-for="tag in question.tags"
                :key="tag.id"
                :color="tag.color"
                effect="plain"
              >
                {{ tag.name }}
              </el-tag>
            </div>
            <div class="question-actions">
              <el-button :icon="isLiked ? StarFilled : Star" :type="isLiked ? 'warning' : 'default'" @click="handleLike">
                {{ question.likeCount }}
              </el-button>
              <el-button :icon="isCollected ? FolderChecked : FolderAdd" @click="handleCollect">
                收藏
              </el-button>
              <el-button :icon="Share" @click="handleShare">
                分享
              </el-button>
            </div>
          </div>
        </template>

        <h1 class="question-title">{{ question.title }}</h1>

        <div class="question-meta">
          <span>
            <el-icon><Folder /></el-icon>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item
                v-for="cat in categoryPath"
                :key="cat.id"
                :to="`/categories/${cat.id}`"
              >
                {{ cat.name }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </span>
          <span><el-icon><View /></el-icon> {{ question.viewCount }} 浏览</span>
          <span><el-icon><Clock /></el-icon> {{ formatDate(question.createdAt) }}</span>
        </div>

        <!-- 学习状态 -->
        <div v-if="userStore.isLoggedIn" class="study-status">
          <span>学习状态：</span>
          <el-radio-group v-model="studyStatus" size="small" @change="handleStatusChange">
            <el-radio-button :value="0">未学</el-radio-button>
            <el-radio-button :value="1">学习中</el-radio-button>
            <el-radio-button :value="2">已掌握</el-radio-button>
            <el-radio-button :value="3">需复习</el-radio-button>
          </el-radio-group>
        </div>
      </el-card>

      <!-- 题目内容 -->
      <el-card class="content-card">
        <template #header>
          <span class="card-title">题目内容</span>
        </template>
        <MdPreview
          :model-value="question.content || '暂无内容'"
          :theme="'light'"
          preview-theme="github"
          code-theme="github"
        />
      </el-card>

      <!-- 参考答案 -->
      <el-card class="answer-card">
        <template #header>
          <div class="answer-header">
            <span class="card-title">参考答案</span>
            <el-button v-if="!showAnswer" type="primary" size="small" @click="showAnswer = true">
              显示答案
            </el-button>
          </div>
        </template>
        <div v-if="showAnswer">
          <MdPreview
            :model-value="question.answer || '暂无答案'"
            :theme="'light'"
            preview-theme="github"
            code-theme="github"
          />
        </div>
        <div v-else class="answer-hidden">
          <el-icon :size="48"><Lock /></el-icon>
          <p>点击上方按钮查看答案</p>
        </div>
      </el-card>

      <!-- 个人笔记 -->
      <el-card v-if="userStore.isLoggedIn" class="note-card">
        <template #header>
          <div class="note-header">
            <span class="card-title">个人笔记</span>
            <el-button v-if="!editingNote" type="primary" text @click="editingNote = true">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
          </div>
        </template>
        <div v-if="editingNote">
          <MdEditor
            v-model="noteContent"
            :theme="'light'"
            preview-theme="github"
            code-theme="github"
            style="height: 300px"
          />
          <div class="note-actions">
            <el-button type="primary" @click="handleSaveNote">保存</el-button>
            <el-button @click="editingNote = false">取消</el-button>
          </div>
        </div>
        <div v-else-if="noteContent">
          <MdPreview
            :model-value="noteContent"
            :theme="'light'"
            preview-theme="github"
            code-theme="github"
          />
        </div>
        <el-empty v-else description="暂无笔记，点击编辑添加" />
      </el-card>

      <!-- 评论区 -->
      <el-card class="comment-card">
        <template #header>
          <span class="card-title">评论 ({{ question.commentCount }})</span>
        </template>
        <CommentSection :question-id="question.id" />
      </el-card>
    </template>

    <el-empty v-else-if="!loading" description="题目不存在" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MdPreview, MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { useUserStore } from '@/stores/user'
import { useCategoryStore } from '@/stores/category'
import { questionApi } from '@/api/question'
import { learningApi } from '@/api/learning'
import { DifficultyMap, type Question } from '@/types'
import CommentSection from '@/components/comment/CommentSection.vue'
import dayjs from 'dayjs'
import {
  Star,
  StarFilled,
  FolderAdd,
  FolderChecked,
  Share,
  Folder,
  View,
  Clock,
  Lock,
  Edit,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const categoryStore = useCategoryStore()

const loading = ref(false)
const question = ref<Question | null>(null)
const showAnswer = ref(false)
const isLiked = ref(false)
const isCollected = ref(false)
const studyStatus = ref(0)
const noteContent = ref('')
const editingNote = ref(false)

const categoryPath = computed(() => {
  if (!question.value) return []
  return categoryStore.getCategoryPath(question.value.categoryId)
})

function formatDate(date: string): string {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

async function loadQuestion() {
  const id = Number(route.params.id)
  if (!id) return

  loading.value = true
  try {
    const res = await questionApi.getDetail(id)
    if (res.code === 200) {
      question.value = res.data

      if (userStore.isLoggedIn) {
        // 加载点赞状态
        const likeRes = await questionApi.isLiked(id)
        if (likeRes.code === 200) {
          isLiked.value = likeRes.data
        }

        // 加载学习进度
        const progressRes = await learningApi.getProgress(id)
        if (progressRes.code === 200 && progressRes.data) {
          studyStatus.value = progressRes.data.status
          noteContent.value = progressRes.data.note || ''
        }

        // 记录学习行为
        learningApi.recordStudy(id, 'VIEW')
      }
    }
  } finally {
    loading.value = false
  }
}

async function handleLike() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  const id = question.value?.id
  if (!id) return

  try {
    if (isLiked.value) {
      await questionApi.unlike(id)
      isLiked.value = false
      question.value!.likeCount--
    } else {
      await questionApi.like(id)
      isLiked.value = true
      question.value!.likeCount++
    }
  } catch (error) {
    console.error('操作失败', error)
  }
}

function handleCollect() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  ElMessage.info('收藏功能开发中')
}

function handleShare() {
  ElMessage.info('分享功能开发中')
}

async function handleStatusChange(status: number) {
  const id = question.value?.id
  if (!id) return

  try {
    await learningApi.updateProgress(id, status)
    ElMessage.success('状态更新成功')
  } catch (error) {
    console.error('更新状态失败', error)
  }
}

async function handleSaveNote() {
  const id = question.value?.id
  if (!id) return

  try {
    await learningApi.updateProgress(id, studyStatus.value, noteContent.value)
    ElMessage.success('笔记保存成功')
    editingNote.value = false
  } catch (error) {
    console.error('保存笔记失败', error)
  }
}

watch(() => route.params.id, loadQuestion, { immediate: true })

onMounted(() => {
  if (!question.value) {
    loadQuestion()
  }
})
</script>

<style lang="scss" scoped>
.question-detail-page {
  max-width: 900px;
  margin: 0 auto;

  .el-card {
    margin-bottom: 20px;
  }
}

.question-card {
  .question-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 16px;
  }

  .question-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .question-title {
    font-size: 24px;
    font-weight: 600;
    color: #333;
    margin-bottom: 16px;
    line-height: 1.4;
  }

  .question-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 20px;
    font-size: 14px;
    color: #666;
    margin-bottom: 16px;

    span {
      display: flex;
      align-items: center;
      gap: 4px;
    }

    .el-breadcrumb {
      display: inline-flex;
    }
  }

  .study-status {
    display: flex;
    align-items: center;
    gap: 12px;
    padding-top: 16px;
    border-top: 1px solid #eee;
  }
}

.card-title {
  font-size: 16px;
  font-weight: 600;
}

.answer-card {
  .answer-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .answer-hidden {
    text-align: center;
    padding: 40px;
    color: #999;

    p {
      margin-top: 12px;
    }
  }
}

.note-card {
  .note-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .note-actions {
    margin-top: 16px;
    display: flex;
    gap: 12px;
  }
}

@media (max-width: 768px) {
  .question-header {
    flex-direction: column;
    align-items: flex-start !important;
  }

  .question-actions {
    width: 100%;
    display: flex;
    justify-content: flex-start;
  }
}
</style>
