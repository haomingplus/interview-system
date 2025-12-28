<template>
  <div class="home-page">
    <!-- Hero Section -->
    <section class="hero">
      <div class="hero-content">
        <h1>面试知识库</h1>
        <p>系统化学习面试知识，助你拿到心仪Offer</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="router.push('/questions')">
            开始学习
          </el-button>
          <el-button size="large" @click="router.push('/categories')">
            浏览分类
          </el-button>
        </div>
      </div>
    </section>

    <!-- 统计数据 -->
    <section class="stats-section">
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-value">{{ stats.questionCount }}+</div>
          <div class="stat-label">精选题目</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.categoryCount }}+</div>
          <div class="stat-label">知识分类</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.userCount }}+</div>
          <div class="stat-label">注册用户</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.studyCount }}+</div>
          <div class="stat-label">学习记录</div>
        </div>
      </div>
    </section>

    <!-- 分类导航 -->
    <section class="category-section">
      <h2 class="section-title">热门分类</h2>
      <div class="category-grid">
        <div
          v-for="category in topCategories"
          :key="category.id"
          class="category-card"
          @click="router.push(`/categories/${category.id}`)"
        >
          <el-icon :size="32"><Folder /></el-icon>
          <h3>{{ category.name }}</h3>
          <p>{{ category.questionCount }} 道题目</p>
        </div>
      </div>
    </section>

    <!-- 推荐题目 -->
    <section class="recommend-section">
      <h2 class="section-title">推荐题目</h2>
      <div class="question-list">
        <div
          v-for="question in recommendQuestions"
          :key="question.id"
          class="question-card"
          @click="router.push(`/questions/${question.id}`)"
        >
          <div class="question-header">
            <el-tag :type="DifficultyMap[question.difficulty].type" size="small">
              {{ DifficultyMap[question.difficulty].label }}
            </el-tag>
            <span class="category">{{ question.categoryName }}</span>
          </div>
          <h3 class="question-title">{{ question.title }}</h3>
          <div class="question-meta">
            <span><el-icon><View /></el-icon> {{ question.viewCount }}</span>
            <span><el-icon><Star /></el-icon> {{ question.likeCount }}</span>
            <span><el-icon><ChatDotRound /></el-icon> {{ question.commentCount }}</span>
          </div>
        </div>
      </div>
      <div class="view-more">
        <el-button text type="primary" @click="router.push('/questions')">
          查看更多 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
    </section>

    <!-- 功能特点 -->
    <section class="features-section">
      <h2 class="section-title">平台特点</h2>
      <div class="features-grid">
        <div class="feature-item">
          <el-icon :size="40" color="var(--el-color-primary)"><Collection /></el-icon>
          <h3>丰富题库</h3>
          <p>涵盖Java、前端、数据库等多个技术领域</p>
        </div>
        <div class="feature-item">
          <el-icon :size="40" color="var(--el-color-success)"><TrendCharts /></el-icon>
          <h3>进度追踪</h3>
          <p>记录学习进度，可视化数据统计</p>
        </div>
        <div class="feature-item">
          <el-icon :size="40" color="var(--el-color-warning)"><Calendar /></el-icon>
          <h3>学习计划</h3>
          <p>制定学习计划，每日目标提醒</p>
        </div>
        <div class="feature-item">
          <el-icon :size="40" color="var(--el-color-danger)"><Share /></el-icon>
          <h3>知识分享</h3>
          <p>生成分享海报，传播面试知识</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCategoryStore } from '@/stores/category'
import { questionApi } from '@/api/question'
import { DifficultyMap, type Question } from '@/types'
import {
  Folder,
  View,
  Star,
  ChatDotRound,
  ArrowRight,
  Collection,
  TrendCharts,
  Calendar,
  Share,
} from '@element-plus/icons-vue'

const router = useRouter()
const categoryStore = useCategoryStore()

const stats = ref({
  questionCount: 500,
  categoryCount: 20,
  userCount: 1000,
  studyCount: 5000,
})

const recommendQuestions = ref<Question[]>([])

const topCategories = computed(() => {
  return categoryStore.categoryTree.slice(0, 6)
})

async function loadRecommendQuestions() {
  try {
    const res = await questionApi.getPage({ page: 1, size: 6, sortField: 'viewCount' })
    if (res.code === 200) {
      recommendQuestions.value = res.data.records
    }
  } catch (error) {
    console.error('加载推荐题目失败', error)
  }
}

onMounted(() => {
  loadRecommendQuestions()
})
</script>

<style lang="scss" scoped>
.home-page {
  max-width: 1200px;
  margin: 0 auto;
}

.hero {
  text-align: center;
  padding: 60px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  color: #fff;
  margin-bottom: 40px;

  h1 {
    font-size: 42px;
    margin-bottom: 16px;
  }

  p {
    font-size: 18px;
    opacity: 0.9;
    margin-bottom: 32px;
  }

  .hero-actions {
    display: flex;
    justify-content: center;
    gap: 16px;
  }
}

.section-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 24px;
  color: #333;
}

.stats-section {
  margin-bottom: 40px;

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;

    @media (max-width: 768px) {
      grid-template-columns: repeat(2, 1fr);
    }
  }

  .stat-item {
    background: #fff;
    padding: 24px;
    border-radius: 12px;
    text-align: center;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

    .stat-value {
      font-size: 32px;
      font-weight: 700;
      color: var(--el-color-primary);
    }

    .stat-label {
      color: #666;
      margin-top: 8px;
    }
  }
}

.category-section {
  margin-bottom: 40px;

  .category-grid {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: 16px;

    @media (max-width: 1024px) {
      grid-template-columns: repeat(3, 1fr);
    }

    @media (max-width: 576px) {
      grid-template-columns: repeat(2, 1fr);
    }
  }

  .category-card {
    background: #fff;
    padding: 24px;
    border-radius: 12px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    }

    .el-icon {
      color: var(--el-color-primary);
    }

    h3 {
      margin: 12px 0 8px;
      font-size: 16px;
      color: #333;
    }

    p {
      font-size: 12px;
      color: #999;
    }
  }
}

.recommend-section {
  margin-bottom: 40px;

  .question-list {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;

    @media (max-width: 1024px) {
      grid-template-columns: repeat(2, 1fr);
    }

    @media (max-width: 576px) {
      grid-template-columns: 1fr;
    }
  }

  .question-card {
    background: #fff;
    padding: 20px;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.3s;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

    &:hover {
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    }

    .question-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 12px;

      .category {
        font-size: 12px;
        color: #999;
      }
    }

    .question-title {
      font-size: 16px;
      color: #333;
      line-height: 1.5;
      margin-bottom: 12px;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .question-meta {
      display: flex;
      gap: 16px;
      font-size: 12px;
      color: #999;

      span {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }

  .view-more {
    text-align: center;
    margin-top: 24px;
  }
}

.features-section {
  .features-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 24px;

    @media (max-width: 1024px) {
      grid-template-columns: repeat(2, 1fr);
    }

    @media (max-width: 576px) {
      grid-template-columns: 1fr;
    }
  }

  .feature-item {
    background: #fff;
    padding: 32px;
    border-radius: 12px;
    text-align: center;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

    h3 {
      margin: 16px 0 12px;
      font-size: 18px;
      color: #333;
    }

    p {
      font-size: 14px;
      color: #666;
      line-height: 1.6;
    }
  }
}
</style>
