<template>
  <div class="comment-section">
    <!-- 发表评论 -->
    <div v-if="userStore.isLoggedIn" class="comment-input">
      <el-avatar :size="40" :src="userStore.userInfo?.avatar">
        {{ userStore.userInfo?.nickname?.charAt(0) }}
      </el-avatar>
      <div class="input-area">
        <el-input
          v-model="commentContent"
          type="textarea"
          :rows="3"
          placeholder="写下你的评论..."
          maxlength="500"
          show-word-limit
        />
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          发表评论
        </el-button>
      </div>
    </div>
    <div v-else class="login-tip">
      <el-button type="primary" link @click="router.push('/login')">登录</el-button>
      后发表评论
    </div>

    <!-- 评论列表 -->
    <div v-loading="loading" class="comment-list">
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <el-avatar :size="40" :src="comment.userAvatar">
          {{ comment.username?.charAt(0) }}
        </el-avatar>
        <div class="comment-content">
          <div class="comment-header">
            <span class="username">{{ comment.username }}</span>
            <span class="time">{{ formatTime(comment.createdAt) }}</span>
          </div>
          <div class="comment-body">{{ comment.content }}</div>
          <div class="comment-actions">
            <el-button text size="small" @click="handleLike(comment)">
              <el-icon><Star /></el-icon> {{ comment.likeCount }}
            </el-button>
            <el-button text size="small" @click="handleReply(comment)">
              <el-icon><ChatDotRound /></el-icon> 回复
            </el-button>
          </div>

          <!-- 回复列表 -->
          <div v-if="comment.replies?.length" class="replies">
            <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
              <el-avatar :size="32" :src="reply.userAvatar">
                {{ reply.username?.charAt(0) }}
              </el-avatar>
              <div class="reply-content">
                <div class="reply-header">
                  <span class="username">{{ reply.username }}</span>
                  <span v-if="reply.replyToUsername" class="reply-to">
                    回复 <span class="target">@{{ reply.replyToUsername }}</span>
                  </span>
                  <span class="time">{{ formatTime(reply.createdAt) }}</span>
                </div>
                <div class="reply-body">{{ reply.content }}</div>
              </div>
            </div>
          </div>

          <!-- 回复输入框 -->
          <div v-if="replyingTo === comment.id" class="reply-input">
            <el-input
              v-model="replyContent"
              type="textarea"
              :rows="2"
              :placeholder="`回复 @${comment.username}`"
            />
            <div class="reply-actions">
              <el-button size="small" @click="replyingTo = null">取消</el-button>
              <el-button type="primary" size="small" @click="handleSubmitReply(comment)">
                回复
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && comments.length === 0" description="暂无评论" />

      <!-- 分页 -->
      <div v-if="total > pageSize" class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadComments"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import type { Comment } from '@/types'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import { Star, ChatDotRound } from '@element-plus/icons-vue'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const props = defineProps<{
  questionId: number
}>()

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const comments = ref<Comment[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const commentContent = ref('')
const replyContent = ref('')
const replyingTo = ref<number | null>(null)

function formatTime(time: string): string {
  return dayjs(time).fromNow()
}

async function loadComments() {
  loading.value = true
  // 模拟加载评论
  setTimeout(() => {
    comments.value = [
      {
        id: 1,
        questionId: props.questionId,
        userId: 1,
        username: '张三',
        content: '这道题讲得很清楚，学到了！',
        parentId: 0,
        rootId: 0,
        likeCount: 5,
        replyCount: 2,
        status: 1,
        createdAt: '2024-03-15 10:00:00',
        replies: [
          {
            id: 2,
            questionId: props.questionId,
            userId: 2,
            username: '李四',
            content: '同意，确实很有帮助',
            parentId: 1,
            rootId: 1,
            likeCount: 2,
            replyCount: 0,
            status: 1,
            createdAt: '2024-03-15 11:00:00',
          },
        ],
      },
    ]
    total.value = 1
    loading.value = false
  }, 500)
}

async function handleSubmit() {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  submitting.value = true
  // 模拟提交
  setTimeout(() => {
    ElMessage.success('评论发表成功')
    commentContent.value = ''
    submitting.value = false
    loadComments()
  }, 500)
}

function handleLike(comment: Comment) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  comment.likeCount++
}

function handleReply(comment: Comment) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  replyingTo.value = comment.id
  replyContent.value = ''
}

async function handleSubmitReply(comment: Comment) {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  // 模拟提交回复
  ElMessage.success('回复成功')
  replyingTo.value = null
  replyContent.value = ''
  loadComments()
}

onMounted(() => {
  loadComments()
})
</script>

<style lang="scss" scoped>
.comment-section {
  .comment-input {
    display: flex;
    gap: 16px;
    margin-bottom: 24px;

    .input-area {
      flex: 1;

      .el-button {
        margin-top: 12px;
        float: right;
      }
    }
  }

  .login-tip {
    text-align: center;
    padding: 20px;
    color: #666;
  }
}

.comment-list {
  .comment-item {
    display: flex;
    gap: 16px;
    padding: 16px 0;
    border-bottom: 1px solid #eee;

    &:last-child {
      border-bottom: none;
    }
  }

  .comment-content {
    flex: 1;
  }

  .comment-header {
    margin-bottom: 8px;

    .username {
      font-weight: 600;
      color: #333;
      margin-right: 12px;
    }

    .time {
      font-size: 12px;
      color: #999;
    }
  }

  .comment-body {
    color: #333;
    line-height: 1.6;
    margin-bottom: 8px;
  }

  .comment-actions {
    .el-button {
      color: #999;
    }
  }
}

.replies {
  background: #f9f9f9;
  border-radius: 8px;
  padding: 12px;
  margin-top: 12px;

  .reply-item {
    display: flex;
    gap: 12px;
    padding: 8px 0;

    &:not(:last-child) {
      border-bottom: 1px solid #eee;
    }
  }

  .reply-content {
    flex: 1;
  }

  .reply-header {
    margin-bottom: 4px;
    font-size: 13px;

    .username {
      font-weight: 600;
      color: #333;
    }

    .reply-to {
      color: #999;
      margin: 0 8px;

      .target {
        color: var(--el-color-primary);
      }
    }

    .time {
      font-size: 12px;
      color: #999;
    }
  }

  .reply-body {
    font-size: 14px;
    color: #333;
  }
}

.reply-input {
  margin-top: 12px;

  .reply-actions {
    margin-top: 8px;
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
