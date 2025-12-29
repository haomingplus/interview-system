<template>
  <aside class="right-sidebar">
    <!-- 热门题目 / 相关题目 -->
    <el-card class="sidebar-card">
      <template #header>
        <div class="card-header">
          <el-icon><TrendCharts /></el-icon>
          <span>{{ relatedTitle }}</span>
        </div>
      </template>
      <div v-loading="loading" class="related-questions">
        <div
          v-for="question in displayQuestions"
          :key="question.id"
          class="question-item"
          @click="handleQuestionClick(question)"
        >
          <div class="question-title">{{ question.title }}</div>
          <div class="question-meta">
            <el-tag :type="DifficultyMap[question.difficulty]?.type" size="small">
              {{ DifficultyMap[question.difficulty]?.label }}
            </el-tag>
            <span class="view-count">
              <el-icon><View /></el-icon>
              {{ question.viewCount }}
            </span>
          </div>
        </div>
        <el-empty v-if="!loading && displayQuestions.length === 0" description="暂无题目" :image-size="60" />
      </div>
    </el-card>

    <!-- 友情提示 -->
    <el-card class="sidebar-card tips-card">
      <template #header>
        <div class="card-header">
          <el-icon><InfoFilled /></el-icon>
          <span>友情提示</span>
        </div>
      </template>
      <ul class="tips-list">
        <li>
          <el-icon><Clock /></el-icon>
          <span>每天坚持学习，效果更好</span>
        </li>
        <li>
          <el-icon><Edit /></el-icon>
          <span>动手实践比死记硬背更有效</span>
        </li>
        <li>
          <el-icon><Reading /></el-icon>
          <span>理解原理，举一反三</span>
        </li>
        <li>
          <el-icon><Refresh /></el-icon>
          <span>定期复习，巩固记忆</span>
        </li>
        <li>
          <el-icon><ChatDotSquare /></el-icon>
          <span>多与他人讨论交流</span>
        </li>
      </ul>
    </el-card>

    <!-- 联系我们 -->
    <el-card class="sidebar-card contact-card">
      <template #header>
        <div class="card-header">
          <el-icon><Service /></el-icon>
          <span>联系我们</span>
        </div>
      </template>
      <div class="contact-content">
        <div class="contact-item">
          <el-icon><Message /></el-icon>
          <span>邮箱：contact@interviewkb.com</span>
        </div>
        <div class="contact-item">
          <el-icon><ChatDotRound /></el-icon>
          <span>微信：InterviewKB</span>
        </div>
        <div class="contact-item">
          <el-icon><Link /></el-icon>
          <span>GitHub：interview-kb</span>
        </div>
        <div class="qrcode-wrapper">
          <div class="qrcode-placeholder">
            <el-icon :size="40"><Iphone /></el-icon>
            <span>扫码关注公众号</span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 快捷入口 -->
    <el-card class="sidebar-card quick-links">
      <template #header>
        <div class="card-header">
          <el-icon><Menu /></el-icon>
          <span>快捷入口</span>
        </div>
      </template>
      <div class="links-grid">
        <div class="link-item" @click="router.push('/questions')">
          <el-icon><Document /></el-icon>
          <span>题目列表</span>
        </div>
        <div class="link-item" @click="router.push('/categories')">
          <el-icon><Folder /></el-icon>
          <span>知识分类</span>
        </div>
        <div class="link-item" @click="router.push('/learning')">
          <el-icon><Reading /></el-icon>
          <span>我的学习</span>
        </div>
        <div class="link-item" @click="router.push('/learning/statistics')">
          <el-icon><DataLine /></el-icon>
          <span>学习统计</span>
        </div>
      </div>
    </el-card>
  </aside>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { questionApi } from '@/api/question'
import { DifficultyMap, type Question } from '@/types'
import {
  TrendCharts, View, InfoFilled, Clock, Edit, Reading, Refresh,
  ChatDotSquare, Service, Message, ChatDotRound, Link, Iphone,
  Menu, Document, Folder, DataLine
} from '@element-plus/icons-vue'

const props = withDefaults(defineProps<{
  categoryId?: number
  excludeId?: number
  relatedTitle?: string
}>(), {
  relatedTitle: '热门题目'
})

const router = useRouter()
const loading = ref(false)
const hotQuestions = ref<Question[]>([])
const relatedQuestions = ref<Question[]>([])

const displayQuestions = computed(() => {
  if (props.categoryId) {
    return relatedQuestions.value
  }
  return hotQuestions.value
})

async function loadHotQuestions() {
  loading.value = true
  try {
    const res = await questionApi.getPage({
      page: 1,
      size: 8,
      sortField: 'viewCount',
      sortOrder: 'desc',
      status: 1
    })
    if (res.code === 200) {
      hotQuestions.value = res.data.records.filter(q => q.id !== props.excludeId)
    }
  } catch (error) {
    console.error('Failed to load hot questions:', error)
  } finally {
    loading.value = false
  }
}

async function loadRelatedQuestions() {
  if (!props.categoryId) return

  loading.value = true
  try {
    const res = await questionApi.getPage({
      categoryId: props.categoryId,
      page: 1,
      size: 8,
      status: 1
    })
    if (res.code === 200) {
      relatedQuestions.value = res.data.records.filter(q => q.id !== props.excludeId)
    }
  } catch (error) {
    console.error('Failed to load related questions:', error)
  } finally {
    loading.value = false
  }
}

function handleQuestionClick(question: Question) {
  router.push(`/questions/${question.id}`)
}

watch(() => props.categoryId, (newVal) => {
  if (newVal) {
    loadRelatedQuestions()
  } else {
    loadHotQuestions()
  }
})

onMounted(() => {
  if (props.categoryId) {
    loadRelatedQuestions()
  } else {
    loadHotQuestions()
  }
})
</script>

<style lang="scss" scoped>
.right-sidebar {
  width: 300px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;

  @media (max-width: 1200px) {
    width: 260px;
  }

  @media (max-width: 992px) {
    width: 100%;
  }
}

.sidebar-card {
  :deep(.el-card__header) {
    padding: 12px 16px;
    background: var(--hover-bg);
  }

  :deep(.el-card__body) {
    padding: 12px 16px;
  }

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
}

.related-questions {
  .question-item {
    padding: 10px 0;
    border-bottom: 1px solid var(--border-color);
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      .question-title {
        color: var(--el-color-primary);
      }
    }

    &:last-child {
      border-bottom: none;
      padding-bottom: 0;
    }

    &:first-child {
      padding-top: 0;
    }

    .question-title {
      font-size: 13px;
      color: var(--text-color);
      line-height: 1.5;
      margin-bottom: 6px;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .question-meta {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 12px;

      .view-count {
        display: flex;
        align-items: center;
        gap: 2px;
        color: var(--text-muted);
      }
    }
  }
}

.tips-card {
  .tips-list {
    list-style: none;
    padding: 0;
    margin: 0;

    li {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 0;
      font-size: 13px;
      color: var(--text-secondary);
      border-bottom: 1px dashed var(--border-color);

      &:last-child {
        border-bottom: none;
        padding-bottom: 0;
      }

      &:first-child {
        padding-top: 0;
      }

      .el-icon {
        color: var(--el-color-warning);
        flex-shrink: 0;
      }
    }
  }
}

.contact-card {
  .contact-content {
    .contact-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 0;
      font-size: 13px;
      color: var(--text-secondary);

      .el-icon {
        color: var(--el-color-primary);
        flex-shrink: 0;
      }
    }

    .qrcode-wrapper {
      margin-top: 12px;
      padding-top: 12px;
      border-top: 1px solid var(--border-color);

      .qrcode-placeholder {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 8px;
        padding: 20px;
        background: var(--hover-bg);
        border-radius: 8px;
        color: var(--text-muted);
        font-size: 12px;
      }
    }
  }
}

.quick-links {
  .links-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;

    .link-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 6px;
      padding: 16px 8px;
      background: var(--hover-bg);
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s;
      font-size: 12px;
      color: var(--text-secondary);

      &:hover {
        background: var(--el-color-primary-light-9);
        color: var(--el-color-primary);

        .el-icon {
          color: var(--el-color-primary);
        }
      }

      .el-icon {
        font-size: 20px;
        color: var(--text-muted);
        transition: color 0.2s;
      }
    }
  }
}
</style>
