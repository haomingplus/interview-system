<template>
  <div class="home-page">
    <!-- 搜索区域 -->
    <section class="search-hero">
      <div class="hero-content">
        <h1>面试知识库</h1>
        <p>系统化学习面试知识，助你拿到心仪 Offer</p>
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索题目、知识点..."
            size="large"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
            </template>
          </el-input>
          <div class="hot-keywords">
            <span class="label">热门搜索：</span>
            <el-tag
              v-for="keyword in hotKeywords"
              :key="keyword"
              size="small"
              effect="plain"
              class="keyword-tag"
              @click="quickSearch(keyword)"
            >
              {{ keyword }}
            </el-tag>
          </div>
        </div>
      </div>
    </section>

    <!-- 主内容区 + 右侧栏 -->
    <div class="main-content-wrapper">
      <!-- 左侧主内容 -->
      <main class="main-content">
        <!-- 统计卡片 -->
        <section class="stats-section">
          <div class="stat-card" @click="router.push('/questions')">
            <div class="stat-icon questions">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.questionCount }}+</div>
              <div class="stat-label">精选题目</div>
            </div>
          </div>
          <div class="stat-card" @click="router.push('/categories')">
            <div class="stat-icon categories">
              <el-icon><Folder /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.categoryCount }}+</div>
              <div class="stat-label">知识分类</div>
            </div>
          </div>
          <div class="stat-card" @click="router.push('/learning')">
            <div class="stat-icon users">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.userCount }}+</div>
              <div class="stat-label">注册用户</div>
            </div>
          </div>
          <div class="stat-card" @click="router.push('/learning/statistics')">
            <div class="stat-icon records">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.studyCount }}+</div>
              <div class="stat-label">学习记录</div>
            </div>
          </div>
        </section>

        <!-- 快捷入口 -->
        <section class="quick-entry">
          <el-card>
            <div class="entry-grid">
              <div
                v-for="entry in quickEntries"
                :key="entry.path"
                class="entry-item"
                @click="router.push(entry.path)"
              >
                <div class="entry-icon" :style="{ background: entry.color }">
                  <el-icon :size="24"><component :is="entry.icon" /></el-icon>
                </div>
                <div class="entry-info">
                  <div class="entry-title">{{ entry.title }}</div>
                  <div class="entry-desc">{{ entry.desc }}</div>
                </div>
                <el-icon class="entry-arrow"><ArrowRight /></el-icon>
              </div>
            </div>
          </el-card>
        </section>

        <!-- 分类导航 -->
        <section class="category-section">
          <div class="section-header">
            <h2>
              <el-icon><Grid /></el-icon>
              知识分类
            </h2>
            <el-button text type="primary" @click="router.push('/categories')">
              查看全部 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
          <div class="category-grid">
            <div
              v-for="category in topCategories"
              :key="category.id"
              class="category-card"
              @click="router.push(`/categories?id=${category.id}`)"
            >
              <div class="category-icon">
                <el-icon :size="28"><Folder /></el-icon>
              </div>
              <div class="category-info">
                <h3>{{ category.name }}</h3>
                <span>{{ category.questionCount || 0 }} 道题目</span>
              </div>
            </div>
          </div>
        </section>

        <!-- 推荐题目 -->
        <section class="recommend-section">
          <div class="section-header">
            <h2>
              <el-icon><Star /></el-icon>
              推荐题目
            </h2>
            <el-button text type="primary" @click="router.push('/questions')">
              查看全部 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
          <div v-loading="loading" class="question-list">
            <div
              v-for="(question, index) in recommendQuestions"
              :key="question.id"
              class="question-item"
              @click="router.push(`/questions/${question.id}`)"
            >
              <div class="question-rank" :class="{ top: index < 3 }">{{ index + 1 }}</div>
              <div class="question-content">
                <div class="question-title">
                  <el-tag v-if="question.isTop" type="danger" size="small" effect="dark">置顶</el-tag>
                  <el-tag v-if="question.isRecommend" type="warning" size="small" effect="dark">推荐</el-tag>
                  <span>{{ question.title }}</span>
                </div>
                <div class="question-meta">
                  <el-tag :type="DifficultyMap[question.difficulty]?.type" size="small">
                    {{ DifficultyMap[question.difficulty]?.label }}
                  </el-tag>
                  <span class="meta-item">{{ question.categoryName }}</span>
                  <span class="meta-item">
                    <el-icon><View /></el-icon> {{ question.viewCount }}
                  </span>
                  <span class="meta-item">
                    <el-icon><ChatDotRound /></el-icon> {{ question.commentCount }}
                  </span>
                </div>
              </div>
              <el-icon class="question-arrow"><ArrowRight /></el-icon>
            </div>
            <el-empty v-if="!loading && recommendQuestions.length === 0" description="暂无推荐题目" />
          </div>
        </section>

        <!-- 最新题目 -->
        <section class="latest-section">
          <div class="section-header">
            <h2>
              <el-icon><Clock /></el-icon>
              最新题目
            </h2>
            <el-button text type="primary" @click="router.push('/questions?sort=newest')">
              查看全部 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
          <div v-loading="latestLoading" class="question-grid">
            <el-card
              v-for="question in latestQuestions"
              :key="question.id"
              class="question-card"
              shadow="hover"
              @click="router.push(`/questions/${question.id}`)"
            >
              <div class="card-header">
                <el-tag :type="DifficultyMap[question.difficulty]?.type" size="small">
                  {{ DifficultyMap[question.difficulty]?.label }}
                </el-tag>
                <span class="category">{{ question.categoryName }}</span>
              </div>
              <h3 class="card-title">{{ question.title }}</h3>
              <div class="card-footer">
                <span><el-icon><View /></el-icon> {{ question.viewCount }}</span>
                <span><el-icon><Star /></el-icon> {{ question.likeCount }}</span>
                <span class="time">{{ formatTime(question.createdAt) }}</span>
              </div>
            </el-card>
          </div>
        </section>

        <!-- 平台特点 -->
        <section class="features-section">
          <div class="section-header">
            <h2>
              <el-icon><MagicStick /></el-icon>
              平台特点
            </h2>
          </div>
          <div class="features-grid">
            <div class="feature-item">
              <div class="feature-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
                <el-icon :size="28"><Collection /></el-icon>
              </div>
              <h3>丰富题库</h3>
              <p>涵盖 Java、前端、数据库等多个技术领域，持续更新</p>
            </div>
            <div class="feature-item">
              <div class="feature-icon" style="background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);">
                <el-icon :size="28"><TrendCharts /></el-icon>
              </div>
              <h3>进度追踪</h3>
              <p>记录学习进度，可视化数据统计，清晰掌握学习状态</p>
            </div>
            <div class="feature-item">
              <div class="feature-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
                <el-icon :size="28"><Calendar /></el-icon>
              </div>
              <h3>学习计划</h3>
              <p>制定个性化学习计划，每日目标提醒，坚持养成习惯</p>
            </div>
            <div class="feature-item">
              <div class="feature-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
                <el-icon :size="28"><Share /></el-icon>
              </div>
              <h3>知识分享</h3>
              <p>一键生成分享海报，与好友共同学习进步</p>
            </div>
          </div>
        </section>
      </main>

      <!-- 右侧栏 -->
      <RightSidebar />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, markRaw } from 'vue'
import { useRouter } from 'vue-router'
import { useCategoryStore } from '@/stores/category'
import { questionApi } from '@/api/question'
import { DifficultyMap, type Question } from '@/types'
import RightSidebar from '@/components/common/RightSidebar.vue'
import {
  Search, Document, Folder, User, TrendCharts, ArrowRight, Grid, Star,
  View, ChatDotRound, Clock, MagicStick, Collection, Calendar, Share,
  Reading, Edit, DataLine
} from '@element-plus/icons-vue'

const router = useRouter()
const categoryStore = useCategoryStore()

const searchKeyword = ref('')
const loading = ref(false)
const latestLoading = ref(false)

const stats = ref({
  questionCount: 500,
  categoryCount: 20,
  userCount: 1000,
  studyCount: 5000,
})

const hotKeywords = ['Java基础', 'Spring Boot', 'MySQL', 'Redis', 'Vue', '算法']

const quickEntries = [
  {
    title: '刷题模式',
    desc: '随机抽题，检验知识掌握',
    icon: markRaw(Edit),
    path: '/questions',
    color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    title: '分类学习',
    desc: '按知识体系系统学习',
    icon: markRaw(Reading),
    path: '/categories',
    color: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)'
  },
  {
    title: '学习进度',
    desc: '查看个人学习记录',
    icon: markRaw(TrendCharts),
    path: '/learning',
    color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  {
    title: '数据统计',
    desc: '可视化学习数据分析',
    icon: markRaw(DataLine),
    path: '/learning/statistics',
    color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  }
]

const recommendQuestions = ref<Question[]>([])
const latestQuestions = ref<Question[]>([])

const topCategories = computed(() => {
  return categoryStore.categoryTree.slice(0, 8)
})

function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push(`/questions?keyword=${encodeURIComponent(searchKeyword.value)}`)
  }
}

function quickSearch(keyword: string) {
  searchKeyword.value = keyword
  handleSearch()
}

function formatTime(dateStr: string) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  if (days < 30) return `${Math.floor(days / 7)}周前`
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

async function loadRecommendQuestions() {
  loading.value = true
  try {
    const res = await questionApi.getPage({ page: 1, size: 10, sortField: 'viewCount', status: 1 })
    if (res.code === 200) {
      recommendQuestions.value = res.data.records
    }
  } catch (error) {
    console.error('加载推荐题目失败', error)
  } finally {
    loading.value = false
  }
}

async function loadLatestQuestions() {
  latestLoading.value = true
  try {
    const res = await questionApi.getPage({ page: 1, size: 6, sortField: 'createdAt', sortOrder: 'desc', status: 1 })
    if (res.code === 200) {
      latestQuestions.value = res.data.records
    }
  } catch (error) {
    console.error('加载最新题目失败', error)
  } finally {
    latestLoading.value = false
  }
}

onMounted(() => {
  categoryStore.loadCategoryTree()
  loadRecommendQuestions()
  loadLatestQuestions()
})
</script>

<style lang="scss" scoped>
.home-page {
  min-height: calc(100vh - 60px);
}

// 搜索区域
.search-hero {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 48px 24px;
  text-align: center;
  color: #fff;

  .hero-content {
    max-width: 700px;
    margin: 0 auto;

    h1 {
      font-size: 36px;
      font-weight: 700;
      margin-bottom: 12px;
    }

    p {
      font-size: 16px;
      opacity: 0.9;
      margin-bottom: 28px;
    }
  }

  .search-box {
    :deep(.el-input) {
      .el-input__wrapper {
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
        border-radius: 8px 0 0 8px;
      }
    }

    :deep(.el-input-group__append) {
      border-radius: 0 8px 8px 0;

      .el-button {
        border-radius: 0 8px 8px 0;
      }
    }

    .hot-keywords {
      display: flex;
      align-items: center;
      justify-content: center;
      flex-wrap: wrap;
      gap: 8px;
      margin-top: 16px;

      .label {
        font-size: 13px;
        opacity: 0.8;
      }

      .keyword-tag {
        cursor: pointer;
        background: rgba(255, 255, 255, 0.2);
        border-color: rgba(255, 255, 255, 0.3);
        color: #fff;

        &:hover {
          background: rgba(255, 255, 255, 0.3);
        }
      }
    }
  }
}

// 主内容区
.main-content-wrapper {
  display: flex;
  gap: 24px;
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;

  @media (max-width: 992px) {
    flex-direction: column;
  }
}

.main-content {
  flex: 1;
  min-width: 0;
}

// 统计卡片
.stats-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;

  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
  }

  .stat-card {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px;
    background: var(--card-bg);
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.3s;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    }

    .stat-icon {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 22px;

      &.questions { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
      &.categories { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
      &.users { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
      &.records { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
    }

    .stat-info {
      .stat-value {
        font-size: 24px;
        font-weight: 700;
        color: var(--text-color);
      }

      .stat-label {
        font-size: 13px;
        color: var(--text-secondary);
        margin-top: 2px;
      }
    }
  }
}

// 快捷入口
.quick-entry {
  margin-bottom: 24px;

  .entry-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;

    @media (max-width: 576px) {
      grid-template-columns: 1fr;
    }
  }

  .entry-item {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 16px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: var(--hover-bg);

      .entry-arrow {
        transform: translateX(4px);
      }
    }

    .entry-icon {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      flex-shrink: 0;
    }

    .entry-info {
      flex: 1;

      .entry-title {
        font-size: 15px;
        font-weight: 600;
        color: var(--text-color);
        margin-bottom: 4px;
      }

      .entry-desc {
        font-size: 12px;
        color: var(--text-secondary);
      }
    }

    .entry-arrow {
      color: var(--text-muted);
      transition: transform 0.2s;
    }
  }
}

// 通用区块头部
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h2 {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 18px;
    font-weight: 600;
    color: var(--text-color);

    .el-icon {
      color: var(--el-color-primary);
    }
  }
}

// 分类导航
.category-section {
  margin-bottom: 24px;

  .category-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;

    @media (max-width: 768px) {
      grid-template-columns: repeat(2, 1fr);
    }
  }

  .category-card {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    background: var(--card-bg);
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.2s;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

    &:hover {
      background: var(--el-color-primary-light-9);

      .category-icon {
        background: var(--el-color-primary);
        color: #fff;
      }
    }

    .category-icon {
      width: 44px;
      height: 44px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--el-color-primary-light-9);
      color: var(--el-color-primary);
      transition: all 0.2s;
    }

    .category-info {
      h3 {
        font-size: 14px;
        font-weight: 600;
        color: var(--text-color);
        margin-bottom: 2px;
      }

      span {
        font-size: 12px;
        color: var(--text-secondary);
      }
    }
  }
}

// 推荐题目
.recommend-section {
  margin-bottom: 24px;

  .question-list {
    background: var(--card-bg);
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  }

  .question-item {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 16px 20px;
    border-bottom: 1px solid var(--border-color);
    cursor: pointer;
    transition: background 0.2s;

    &:hover {
      background: var(--hover-bg);

      .question-arrow {
        opacity: 1;
        transform: translateX(0);
      }
    }

    &:last-child {
      border-bottom: none;
    }

    .question-rank {
      width: 28px;
      height: 28px;
      border-radius: 6px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 14px;
      font-weight: 600;
      background: var(--hover-bg);
      color: var(--text-secondary);
      flex-shrink: 0;

      &.top {
        background: linear-gradient(135deg, #f5af19 0%, #f12711 100%);
        color: #fff;
      }
    }

    .question-content {
      flex: 1;
      min-width: 0;

      .question-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 14px;
        color: var(--text-color);
        margin-bottom: 8px;

        span {
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .question-meta {
        display: flex;
        align-items: center;
        gap: 12px;
        font-size: 12px;
        color: var(--text-secondary);

        .meta-item {
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
    }

    .question-arrow {
      color: var(--text-muted);
      opacity: 0;
      transform: translateX(-8px);
      transition: all 0.2s;
    }
  }
}

// 最新题目
.latest-section {
  margin-bottom: 24px;

  .question-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;

    @media (max-width: 768px) {
      grid-template-columns: repeat(2, 1fr);
    }

    @media (max-width: 576px) {
      grid-template-columns: 1fr;
    }
  }

  .question-card {
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      transform: translateY(-2px);
    }

    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 12px;

      .category {
        font-size: 12px;
        color: var(--text-secondary);
      }
    }

    .card-title {
      font-size: 14px;
      font-weight: 500;
      color: var(--text-color);
      line-height: 1.5;
      margin-bottom: 12px;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .card-footer {
      display: flex;
      align-items: center;
      gap: 12px;
      font-size: 12px;
      color: var(--text-muted);

      span {
        display: flex;
        align-items: center;
        gap: 4px;
      }

      .time {
        margin-left: auto;
      }
    }
  }
}

// 平台特点
.features-section {
  .features-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;

    @media (max-width: 768px) {
      grid-template-columns: repeat(2, 1fr);
    }

    @media (max-width: 576px) {
      grid-template-columns: 1fr;
    }
  }

  .feature-item {
    background: var(--card-bg);
    padding: 24px;
    border-radius: 12px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

    .feature-icon {
      width: 56px;
      height: 56px;
      border-radius: 14px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      margin-bottom: 16px;
    }

    h3 {
      font-size: 16px;
      font-weight: 600;
      color: var(--text-color);
      margin-bottom: 8px;
    }

    p {
      font-size: 13px;
      color: var(--text-secondary);
      line-height: 1.6;
    }
  }
}
</style>
