<template>
  <div class="statistics-page">
    <el-page-header @back="router.back()">
      <template #content>
        <span class="page-title">学习统计</span>
      </template>
    </el-page-header>

    <div v-loading="loading" class="stats-content mt-20">
      <!-- 概览卡片 -->
      <el-row :gutter="20" class="overview-cards">
        <el-col :xs="12" :sm="6">
          <el-card class="stat-card">
            <div class="stat-icon" style="background: #e6f7ff">
              <el-icon :size="24" color="#1890ff"><Reading /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalQuestions }}</div>
              <div class="stat-label">总学习题目</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card class="stat-card">
            <div class="stat-icon" style="background: #f6ffed">
              <el-icon :size="24" color="#52c41a"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.masteredQuestions }}</div>
              <div class="stat-label">已掌握</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card class="stat-card">
            <div class="stat-icon" style="background: #fff7e6">
              <el-icon :size="24" color="#fa8c16"><Aim /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.consecutiveDays }}</div>
              <div class="stat-label">连续学习天数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card class="stat-card">
            <div class="stat-icon" style="background: #f9f0ff">
              <el-icon :size="24" color="#722ed1"><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalStudyDays }}</div>
              <div class="stat-label">总学习天数</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区域 -->
      <el-row :gutter="20" class="chart-row">
        <el-col :xs="24" :lg="16">
          <el-card>
            <template #header>
              <span>每日学习趋势</span>
            </template>
            <div ref="dailyChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="8">
          <el-card>
            <template #header>
              <span>分类掌握情况</span>
            </template>
            <div ref="categoryChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 学习热力图 -->
      <el-card class="heatmap-card">
        <template #header>
          <span>学习热力图</span>
        </template>
        <div ref="heatmapRef" class="heatmap-container"></div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { learningApi } from '@/api/learning'
import type { Statistics } from '@/types'
import { Reading, CircleCheck, Aim, Calendar } from '@element-plus/icons-vue'

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

const dailyChartRef = ref<HTMLDivElement>()
const categoryChartRef = ref<HTMLDivElement>()
const heatmapRef = ref<HTMLDivElement>()

let dailyChart: echarts.ECharts | null = null
let categoryChart: echarts.ECharts | null = null
let heatmapChart: echarts.ECharts | null = null

async function loadStats() {
  loading.value = true
  try {
    const res = await learningApi.getStatistics()
    if (res.code === 200) {
      stats.value = res.data
      await nextTick()
      initCharts()
    }
  } finally {
    loading.value = false
  }
}

function initCharts() {
  initDailyChart()
  initCategoryChart()
  initHeatmapChart()
}

function initDailyChart() {
  if (!dailyChartRef.value) return

  dailyChart = echarts.init(dailyChartRef.value)
  const dates = stats.value.dailyStats.map(d => d.date)
  const counts = stats.value.dailyStats.map(d => d.count)

  dailyChart.setOption({
    tooltip: {
      trigger: 'axis',
    },
    xAxis: {
      type: 'category',
      data: dates,
    },
    yAxis: {
      type: 'value',
      name: '题目数',
    },
    series: [
      {
        name: '学习题目数',
        type: 'line',
        data: counts,
        smooth: true,
        areaStyle: {
          opacity: 0.3,
        },
        itemStyle: {
          color: '#409eff',
        },
      },
    ],
  })
}

function initCategoryChart() {
  if (!categoryChartRef.value) return

  categoryChart = echarts.init(categoryChartRef.value)
  const data = stats.value.categoryStats.map(c => ({
    name: c.name,
    value: c.mastered,
  }))

  categoryChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        data,
        label: {
          show: true,
          formatter: '{b}',
        },
      },
    ],
  })
}

function initHeatmapChart() {
  if (!heatmapRef.value) return

  heatmapChart = echarts.init(heatmapRef.value)

  // 生成模拟热力图数据
  const data: [string, number][] = []
  const now = new Date()
  for (let i = 365; i >= 0; i--) {
    const date = new Date(now.getTime() - i * 24 * 60 * 60 * 1000)
    const dateStr = date.toISOString().split('T')[0]
    const value = stats.value.heatMapData[dateStr] || 0
    data.push([dateStr, value])
  }

  heatmapChart.setOption({
    tooltip: {
      formatter: (params: any) => `${params.value[0]}: ${params.value[1]} 道题`,
    },
    visualMap: {
      min: 0,
      max: 10,
      type: 'piecewise',
      orient: 'horizontal',
      left: 'center',
      top: 0,
      pieces: [
        { value: 0, color: '#ebedf0' },
        { gte: 1, lte: 3, color: '#9be9a8' },
        { gte: 4, lte: 6, color: '#40c463' },
        { gte: 7, lte: 9, color: '#30a14e' },
        { gte: 10, color: '#216e39' },
      ],
    },
    calendar: {
      top: 60,
      left: 30,
      right: 30,
      cellSize: ['auto', 12],
      range: [
        new Date(now.getTime() - 364 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
        now.toISOString().split('T')[0],
      ],
      itemStyle: {
        borderWidth: 2,
        borderColor: '#fff',
      },
      yearLabel: { show: false },
    },
    series: [
      {
        type: 'heatmap',
        coordinateSystem: 'calendar',
        data,
      },
    ],
  })
}

function handleResize() {
  dailyChart?.resize()
  categoryChart?.resize()
  heatmapChart?.resize()
}

onMounted(() => {
  loadStats()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  dailyChart?.dispose()
  categoryChart?.dispose()
  heatmapChart?.dispose()
})
</script>

<style lang="scss" scoped>
.statistics-page {
  max-width: 1200px;
  margin: 0 auto;

  .page-title {
    font-size: 18px;
    font-weight: 600;
  }
}

.overview-cards {
  margin-bottom: 20px;

  .el-col {
    margin-bottom: 20px;
  }
}

.stat-card {
  :deep(.el-card__body) {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .stat-info {
    .stat-value {
      font-size: 24px;
      font-weight: 700;
      color: #333;
    }

    .stat-label {
      font-size: 12px;
      color: #999;
    }
  }
}

.chart-row {
  margin-bottom: 20px;

  .el-col {
    margin-bottom: 20px;
  }
}

.chart-container {
  height: 300px;
}

.heatmap-container {
  height: 180px;
}
</style>
