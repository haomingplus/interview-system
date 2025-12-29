<template>
  <div class="learning-page">
    <div class="page-content">
      <!-- 左侧主内容 -->
      <main class="main-content">
        <!-- 学习概览卡片 -->
        <el-card class="overview-card">
          <div class="overview-grid">
            <div class="stat-item">
              <div class="stat-icon total">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.totalQuestions }}</div>
                <div class="stat-label">总题目</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon mastered">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.masteredQuestions }}</div>
                <div class="stat-label">已掌握</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon studying">
                <el-icon><Reading /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.studyingQuestions }}</div>
                <div class="stat-label">学习中</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon review">
                <el-icon><Refresh /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.needReviewQuestions }}</div>
                <div class="stat-label">需复习</div>
              </div>
            </div>
            <div class="stat-item streak">
              <div class="stat-icon fire">
                <el-icon><Aim /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.consecutiveDays }} <small>天</small></div>
                <div class="stat-label">连续学习</div>
              </div>
            </div>
            <div class="stat-item today">
              <div class="stat-icon today-icon">
                <el-icon><Calendar /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.todayStudied }}</div>
                <div class="stat-label">今日学习</div>
              </div>
            </div>
          </div>
          <div class="overview-actions">
            <el-button type="primary" @click="router.push('/learning/statistics')">
              <el-icon><DataLine /></el-icon> 查看详细统计
            </el-button>
            <el-button @click="router.push('/questions')">
              <el-icon><Reading /></el-icon> 继续学习
            </el-button>
          </div>
        </el-card>

        <!-- 学习进度列表 -->
        <el-card class="progress-card">
          <template #header>
            <div class="list-header">
              <h3>
                <el-icon><Clock /></el-icon>
                学习进度
              </h3>
              <el-radio-group v-model="statusFilter" size="small" @change="loadProgress">
                <el-radio-button :value="undefined">全部</el-radio-button>
                <el-radio-button :value="1">学习中</el-radio-button>
                <el-radio-button :value="2">已掌握</el-radio-button>
                <el-radio-button :value="3">需复习</el-radio-button>
              </el-radio-group>
            </div>
          </template>

          <div v-loading="loading" class="progress-list">
            <div
              v-for="item in progressList"
              :key="item.id"
              class="progress-item"
              @click="router.push(`/questions/${item.questionId}`)"
            >
              <div class="item-status">
                <el-tag :type="StudyStatusMap[item.status]?.type" size="small">
                  {{ StudyStatusMap[item.status]?.label }}
                </el-tag>
              </div>
              <div class="item-content">
                <h4 class="item-title">{{ item.question?.title }}</h4>
                <div class="item-meta">
                  <span><el-icon><Reading /></el-icon> 学习 {{ item.studyCount }} 次</span>
                  <span v-if="item.lastStudyTime">
                    <el-icon><Clock /></el-icon> {{ formatDate(item.lastStudyTime) }}
                  </span>
                </div>
              </div>
              <el-icon class="item-arrow"><ArrowRight /></el-icon>
            </div>

            <el-empty v-if="!loading && progressList.length === 0" description="暂无学习记录，快去学习吧！">
              <el-button type="primary" @click="router.push('/questions')">开始学习</el-button>
            </el-empty>
          </div>

          <div v-if="total > size" class="pagination">
            <el-pagination
              v-model:current-page="page"
              :page-size="size"
              :total="total"
              layout="prev, pager, next"
              @current-change="loadProgress"
            />
          </div>
        </el-card>
      </main>

      <!-- 右侧栏 -->
      <RightSidebar />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { learningApi } from '@/api/learning'
import { StudyStatusMap, type LearningProgress, type Statistics } from '@/types'
import RightSidebar from '@/components/common/RightSidebar.vue'
import dayjs from 'dayjs'
import {
  Document, CircleCheck, Reading, Refresh, Aim, Calendar,
  DataLine, Clock, ArrowRight
} from '@element-plus/icons-vue'

const router = useRouter()

const loading = ref(false)
const stats = ref<Statistics>({
  totalQuestions: 0,
  masteredQuestions: 0,
  studyingQuestions: 0,
  needReviewQuestions: 0,
  consecutiveDays: 0,
  todayStudied: 0,
  totalStudyDays: 0,
  dailyStats: [],
  categoryStats: [],
  heatMapData: {},
})

const progressList = ref<LearningProgress[]>([])
const statusFilter = ref<number | undefined>(undefined)
const page = ref(1)
const size = ref(10)
const total = ref(0)

function formatDate(date: string): string {
  return dayjs(date).format('MM-DD HH:mm')
}

async function loadStats() {
  try {
    const res = await learningApi.getStatistics()
    if (res.code === 200) {
      stats.value = res.data
    }
  } catch (error) {
    console.error('加载统计失败', error)
  }
}

async function loadProgress() {
  loading.value = true
  try {
    const res = await learningApi.getProgressList(statusFilter.value, page.value, size.value)
    if (res.code === 200) {
      progressList.value = res.data.records
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStats()
  loadProgress()
})
</script>

<style lang="scss" scoped>
.learning-page {
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

.overview-card {
  margin-bottom: 20px;

  .overview-grid {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: 16px;

    @media (max-width: 1200px) {
      grid-template-columns: repeat(3, 1fr);
    }

    @media (max-width: 576px) {
      grid-template-columns: repeat(2, 1fr);
    }
  }

  .stat-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    background: var(--hover-bg);
    border-radius: 10px;

    .stat-icon {
      width: 44px;
      height: 44px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 20px;

      &.total { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
      &.mastered { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
      &.studying { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
      &.review { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
      &.fire { background: linear-gradient(135deg, #f5af19 0%, #f12711 100%); }
      &.today-icon { background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%); color: #666; }
    }

    .stat-info {
      .stat-value {
        font-size: 22px;
        font-weight: 700;
        color: var(--text-color);

        small {
          font-size: 14px;
          font-weight: 400;
        }
      }

      .stat-label {
        font-size: 12px;
        color: var(--text-secondary);
        margin-top: 2px;
      }
    }
  }

  .overview-actions {
    display: flex;
    gap: 12px;
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid var(--border-color);
  }
}

.progress-card {
  .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 12px;

    h3 {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 600;
      color: var(--text-color);

      .el-icon {
        color: var(--el-color-primary);
      }
    }
  }
}

.progress-list {
  min-height: 300px;
}

.progress-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  margin: 0 -20px;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: var(--hover-bg);

    .item-arrow {
      opacity: 1;
      transform: translateX(0);
    }
  }

  &:last-child {
    border-bottom: none;
  }

  .item-status {
    flex-shrink: 0;
  }

  .item-content {
    flex: 1;
    min-width: 0;

    .item-title {
      font-size: 15px;
      font-weight: 500;
      color: var(--text-color);
      margin-bottom: 6px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .item-meta {
      display: flex;
      gap: 16px;
      font-size: 12px;
      color: var(--text-secondary);

      span {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }

  .item-arrow {
    color: var(--text-muted);
    opacity: 0;
    transform: translateX(-8px);
    transition: all 0.2s;
  }
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
</style>
