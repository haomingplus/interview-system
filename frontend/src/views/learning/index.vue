<template>
  <div class="learning-page">
    <el-row :gutter="20">
      <!-- 左侧统计卡片 -->
      <el-col :xs="24" :lg="8">
        <el-card class="stats-card">
          <template #header>
            <span>学习概览</span>
          </template>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-value">{{ stats.totalQuestions }}</div>
              <div class="stat-label">总题目</div>
            </div>
            <div class="stat-item">
              <div class="stat-value text-success">{{ stats.masteredQuestions }}</div>
              <div class="stat-label">已掌握</div>
            </div>
            <div class="stat-item">
              <div class="stat-value text-primary">{{ stats.studyingQuestions }}</div>
              <div class="stat-label">学习中</div>
            </div>
            <div class="stat-item">
              <div class="stat-value text-warning">{{ stats.needReviewQuestions }}</div>
              <div class="stat-label">需复习</div>
            </div>
          </div>

          <el-divider />

          <div class="streak-info">
            <el-icon :size="32" color="var(--el-color-warning)"><Aim /></el-icon>
            <div class="streak-text">
              <div class="streak-value">{{ stats.consecutiveDays }} 天</div>
              <div class="streak-label">连续学习</div>
            </div>
          </div>

          <el-button type="primary" class="full-btn" @click="router.push('/learning/statistics')">
            查看详细统计
          </el-button>
        </el-card>
      </el-col>

      <!-- 右侧进度列表 -->
      <el-col :xs="24" :lg="16">
        <el-card>
          <template #header>
            <div class="list-header">
              <span>学习进度</span>
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
              <div class="item-content">
                <el-tag :type="StudyStatusMap[item.status].type" size="small">
                  {{ StudyStatusMap[item.status].label }}
                </el-tag>
                <h4 class="item-title">{{ item.question?.title }}</h4>
              </div>
              <div class="item-meta">
                <span>学习 {{ item.studyCount }} 次</span>
                <span v-if="item.lastStudyTime">
                  最近: {{ formatDate(item.lastStudyTime) }}
                </span>
              </div>
            </div>

            <el-empty v-if="!loading && progressList.length === 0" description="暂无学习记录" />
          </div>

          <div class="pagination">
            <el-pagination
              v-model:current-page="page"
              :page-size="size"
              :total="total"
              layout="prev, pager, next"
              @current-change="loadProgress"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { learningApi } from '@/api/learning'
import { StudyStatusMap, type LearningProgress, type Statistics } from '@/types'
import dayjs from 'dayjs'
import { Aim } from '@element-plus/icons-vue'

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
  max-width: 1200px;
  margin: 0 auto;
}

.stats-card {
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
    text-align: center;
  }

  .stat-item {
    padding: 12px;
    background: #f9f9f9;
    border-radius: 8px;

    .stat-value {
      font-size: 24px;
      font-weight: 700;

      &.text-success {
        color: var(--el-color-success);
      }

      &.text-primary {
        color: var(--el-color-primary);
      }

      &.text-warning {
        color: var(--el-color-warning);
      }
    }

    .stat-label {
      font-size: 12px;
      color: #999;
      margin-top: 4px;
    }
  }

  .streak-info {
    display: flex;
    align-items: center;
    gap: 16px;
    justify-content: center;

    .streak-text {
      text-align: left;

      .streak-value {
        font-size: 20px;
        font-weight: 700;
        color: #333;
      }

      .streak-label {
        font-size: 12px;
        color: #999;
      }
    }
  }

  .full-btn {
    width: 100%;
    margin-top: 16px;
  }
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.progress-list {
  min-height: 300px;
}

.progress-item {
  padding: 16px 0;
  border-bottom: 1px solid #eee;
  cursor: pointer;

  &:hover {
    background: #f9f9f9;
  }

  &:last-child {
    border-bottom: none;
  }

  .item-content {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 8px;
  }

  .item-title {
    font-size: 15px;
    color: #333;
    flex: 1;
  }

  .item-meta {
    display: flex;
    gap: 16px;
    font-size: 12px;
    color: #999;
  }
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

@media (max-width: 992px) {
  .el-col:first-child {
    margin-bottom: 20px;
  }
}
</style>
