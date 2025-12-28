<template>
  <div class="plan-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学习计划</span>
          <el-button type="primary" @click="showCreateDialog = true">
            <el-icon><Plus /></el-icon> 新建计划
          </el-button>
        </div>
      </template>

      <el-empty description="暂无学习计划，创建一个开始学习吧" />
    </el-card>

    <!-- 创建计划对话框 -->
    <el-dialog v-model="showCreateDialog" title="新建学习计划" width="500px">
      <el-form :model="planForm" label-width="100px">
        <el-form-item label="计划名称">
          <el-input v-model="planForm.name" placeholder="请输入计划名称" />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="planForm.dateRange"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item label="每日目标">
          <el-input-number v-model="planForm.dailyTarget" :min="1" :max="50" />
          <span class="ml-2">道题目</span>
        </el-form-item>
        <el-form-item label="计划描述">
          <el-input v-model="planForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const showCreateDialog = ref(false)

const planForm = reactive({
  name: '',
  dateRange: [],
  dailyTarget: 5,
  description: '',
})

function handleCreate() {
  ElMessage.success('功能开发中')
  showCreateDialog.value = false
}
</script>

<style lang="scss" scoped>
.plan-page {
  max-width: 900px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ml-2 {
  margin-left: 8px;
}
</style>
