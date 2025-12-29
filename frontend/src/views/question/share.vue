<template>
  <div class="share-page">
    <el-card v-loading="loading">
      <div v-if="question" class="share-content">
        <h1 class="question-title">{{ question.title }}</h1>
        <el-divider />
        <div class="question-body" v-html="question.content"></div>
        <el-divider />
        <div class="share-footer">
          <p>来自 <strong>面试知识库</strong></p>
          <el-button type="primary" @click="router.push(`/questions/${question.id}`)">
            查看完整内容
          </el-button>
        </div>
      </div>
      <el-empty v-else-if="!loading" description="分享链接无效或已过期" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { Question } from '@/types'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const question = ref<Question | null>(null)

async function loadShare() {
  const code = route.params.code as string
  if (!code) return

  loading.value = true
  // 模拟加载分享内容
  setTimeout(() => {
    loading.value = false
  }, 500)
}

onMounted(() => {
  loadShare()
})
</script>

<style lang="scss" scoped>
.share-page {
  max-width: 800px;
  margin: 40px auto;
  padding: 0 20px;

  .share-content {
    .question-title {
      font-size: 24px;
      font-weight: 600;
      color: var(--text-color);
      text-align: center;
    }

    .question-body {
      line-height: 1.8;
      color: var(--text-color);
    }

    .share-footer {
      text-align: center;

      p {
        margin-bottom: 16px;
        color: var(--text-secondary);
      }
    }
  }
}
</style>
