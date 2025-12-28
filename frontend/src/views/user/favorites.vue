<template>
  <div class="favorites-page">
    <el-page-header @back="router.back()">
      <template #content>
        <span class="page-title">我的收藏</span>
      </template>
    </el-page-header>

    <el-card class="mt-20">
      <el-empty description="暂无收藏" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style lang="scss" scoped>
.favorites-page {
  max-width: 900px;
  margin: 0 auto;

  .page-title {
    font-size: 18px;
    font-weight: 600;
  }
}
</style>
