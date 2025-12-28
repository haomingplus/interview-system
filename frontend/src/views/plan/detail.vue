<template>
  <div class="plan-detail-page">
    <el-page-header @back="router.back()">
      <template #content>
        <span class="page-title">计划详情</span>
      </template>
    </el-page-header>

    <el-card class="mt-20">
      <el-empty description="计划不存在" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style lang="scss" scoped>
.plan-detail-page {
  max-width: 900px;
  margin: 0 auto;

  .page-title {
    font-size: 18px;
    font-weight: 600;
  }
}
</style>
