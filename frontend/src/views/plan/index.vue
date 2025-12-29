<template>
  <div class="plan-page">
    <div class="page-content">
      <!-- 左侧主内容 -->
      <main class="main-content">
        <el-card class="plan-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon><Calendar /></el-icon>
                <span>学习计划</span>
              </div>
              <el-button type="primary" :icon="Plus" @click="showCreateDialog = true">
                新建计划
              </el-button>
            </div>
          </template>

          <div v-loading="loading" class="plan-list">
            <div
              v-for="plan in plans"
              :key="plan.id"
              class="plan-item"
              @click="router.push(`/plan/${plan.id}`)"
            >
              <div class="plan-info">
                <div class="plan-status">
                  <el-tag :type="getStatusType(plan.status)" size="small">
                    {{ getStatusText(plan.status) }}
                  </el-tag>
                </div>
                <h3 class="plan-name">{{ plan.name }}</h3>
                <p v-if="plan.description" class="plan-desc">{{ plan.description }}</p>
                <div class="plan-meta">
                  <span><el-icon><Clock /></el-icon> {{ formatDate(plan.startDate) }} - {{ formatDate(plan.endDate) }}</span>
                  <span><el-icon><Aim /></el-icon> 每日 {{ plan.dailyTarget }} 题</span>
                </div>
              </div>
              <div class="plan-progress">
                <el-progress
                  type="circle"
                  :percentage="plan.progress || 0"
                  :width="60"
                  :stroke-width="6"
                />
              </div>
            </div>

            <el-empty v-if="!loading && plans.length === 0" description="暂无学习计划">
              <el-button type="primary" @click="showCreateDialog = true">
                创建第一个计划
              </el-button>
            </el-empty>
          </div>
        </el-card>

        <!-- 学习建议卡片 -->
        <el-card class="tips-card">
          <template #header>
            <div class="card-header">
              <el-icon><MagicStick /></el-icon>
              <span>学习建议</span>
            </div>
          </template>
          <div class="tips-content">
            <div class="tip-item">
              <div class="tip-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="tip-info">
                <h4>制定合理目标</h4>
                <p>每日学习 5-10 题为宜，保持稳定的学习节奏</p>
              </div>
            </div>
            <div class="tip-item">
              <div class="tip-icon" style="background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);">
                <el-icon><TrendCharts /></el-icon>
              </div>
              <div class="tip-info">
                <h4>循序渐进</h4>
                <p>从简单题目开始，逐步提升难度</p>
              </div>
            </div>
            <div class="tip-item">
              <div class="tip-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
                <el-icon><Refresh /></el-icon>
              </div>
              <div class="tip-info">
                <h4>定期复习</h4>
                <p>温故知新，巩固已学知识点</p>
              </div>
            </div>
          </div>
        </el-card>
      </main>

      <!-- 右侧栏 -->
      <RightSidebar />
    </div>

    <!-- 创建计划对话框 -->
    <el-dialog v-model="showCreateDialog" title="新建学习计划" width="500px" destroy-on-close>
      <el-form :model="planForm" label-width="100px">
        <el-form-item label="计划名称" required>
          <el-input v-model="planForm.name" placeholder="请输入计划名称" />
        </el-form-item>
        <el-form-item label="时间范围" required>
          <el-date-picker
            v-model="planForm.dateRange"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="每日目标">
          <el-input-number v-model="planForm.dailyTarget" :min="1" :max="50" />
          <span class="ml-2">道题目</span>
        </el-form-item>
        <el-form-item label="选择分类">
          <el-cascader
            v-model="planForm.categoryIds"
            :options="categoryStore.categoryTree"
            :props="{ value: 'id', label: 'name', multiple: true, checkStrictly: true }"
            placeholder="选择学习分类（可多选）"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="计划描述">
          <el-input v-model="planForm.description" type="textarea" :rows="3" placeholder="描述一下你的学习目标" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useCategoryStore } from '@/stores/category'
import RightSidebar from '@/components/common/RightSidebar.vue'
import dayjs from 'dayjs'
import {
  Plus, Calendar, Clock, Aim, MagicStick, TrendCharts, Refresh
} from '@element-plus/icons-vue'

interface StudyPlan {
  id: number
  name: string
  description?: string
  startDate: string
  endDate: string
  dailyTarget: number
  status: number
  progress?: number
}

const router = useRouter()
const categoryStore = useCategoryStore()

const loading = ref(false)
const creating = ref(false)
const showCreateDialog = ref(false)
const plans = ref<StudyPlan[]>([])

const planForm = reactive({
  name: '',
  dateRange: [] as Date[],
  dailyTarget: 5,
  categoryIds: [] as number[],
  description: '',
})

function formatDate(date: string): string {
  return dayjs(date).format('YYYY-MM-DD')
}

function getStatusType(status: number): string {
  switch (status) {
    case 0: return 'info'
    case 1: return 'primary'
    case 2: return 'success'
    default: return 'info'
  }
}

function getStatusText(status: number): string {
  switch (status) {
    case 0: return '未开始'
    case 1: return '进行中'
    case 2: return '已完成'
    default: return '未知'
  }
}

async function loadPlans() {
  loading.value = true
  // TODO: 调用API加载计划列表
  setTimeout(() => {
    loading.value = false
  }, 500)
}

function handleCreate() {
  if (!planForm.name.trim()) {
    ElMessage.warning('请输入计划名称')
    return
  }
  if (!planForm.dateRange || planForm.dateRange.length !== 2) {
    ElMessage.warning('请选择时间范围')
    return
  }

  creating.value = true
  // TODO: 调用API创建计划
  setTimeout(() => {
    ElMessage.success('计划创建成功')
    showCreateDialog.value = false
    creating.value = false
    // 重置表单
    planForm.name = ''
    planForm.dateRange = []
    planForm.dailyTarget = 5
    planForm.categoryIds = []
    planForm.description = ''
  }, 500)
}

onMounted(() => {
  categoryStore.loadCategoryTree()
  loadPlans()
})
</script>

<style lang="scss" scoped>
.plan-page {
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

.plan-card {
  margin-bottom: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 600;
      color: var(--text-color);

      .el-icon {
        color: var(--el-color-primary);
      }
    }
  }
}

.plan-list {
  min-height: 200px;
}

.plan-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  margin: 0 -20px;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: var(--hover-bg);
  }

  &:last-child {
    border-bottom: none;
  }

  .plan-info {
    flex: 1;

    .plan-status {
      margin-bottom: 8px;
    }

    .plan-name {
      font-size: 16px;
      font-weight: 600;
      color: var(--text-color);
      margin-bottom: 8px;
    }

    .plan-desc {
      font-size: 13px;
      color: var(--text-secondary);
      margin-bottom: 12px;
      line-height: 1.5;
    }

    .plan-meta {
      display: flex;
      gap: 20px;
      font-size: 13px;
      color: var(--text-secondary);

      span {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }

  .plan-progress {
    flex-shrink: 0;
    margin-left: 20px;
  }
}

.tips-card {
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

  .tips-content {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .tip-item {
    display: flex;
    align-items: flex-start;
    gap: 16px;

    .tip-icon {
      width: 40px;
      height: 40px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      flex-shrink: 0;
    }

    .tip-info {
      flex: 1;

      h4 {
        font-size: 14px;
        font-weight: 600;
        color: var(--text-color);
        margin-bottom: 4px;
      }

      p {
        font-size: 13px;
        color: var(--text-secondary);
        line-height: 1.5;
      }
    }
  }
}

.ml-2 {
  margin-left: 8px;
  color: var(--text-secondary);
}
</style>
