<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑题目' : '添加题目'"
    width="900px"
    :close-on-click-modal="false"
    destroy-on-close
    @update:model-value="$emit('update:visible', $event)"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="80px"
      class="question-form"
    >
      <el-form-item label="题目标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入题目标题" />
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="所属分类" prop="categoryId">
            <el-cascader
              v-model="categoryValue"
              :options="categoryStore.categoryTree"
              :props="{ value: 'id', label: 'name', checkStrictly: true, emitPath: false }"
              placeholder="选择分类"
              class="full-width"
              @change="form.categoryId = categoryValue"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="难度等级" prop="difficulty">
            <el-select v-model="form.difficulty" placeholder="选择难度" class="full-width">
              <el-option label="简单" :value="1" />
              <el-option label="中等" :value="2" />
              <el-option label="困难" :value="3" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="题目来源">
            <el-input v-model="form.source" placeholder="如：阿里面试、腾讯面试" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="标签">
            <el-select
              v-model="form.tagIds"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="选择或创建标签"
              class="full-width"
            >
              <el-option
                v-for="tag in tags"
                :key="tag.id"
                :label="tag.name"
                :value="tag.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="题目内容" prop="content">
        <div class="editor-wrapper">
          <MdEditor
            v-model="form.content"
            :theme="themeStore.isDark ? 'dark' : 'light'"
            preview-theme="github"
            code-theme="github"
            :toolbars="toolbars"
            style="height: 300px"
          />
        </div>
      </el-form-item>

      <el-form-item label="参考答案" prop="answer">
        <div class="editor-wrapper">
          <MdEditor
            v-model="form.answer"
            :theme="themeStore.isDark ? 'dark' : 'light'"
            preview-theme="github"
            code-theme="github"
            :toolbars="toolbars"
            style="height: 300px"
          />
        </div>
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="状态">
            <el-radio-group v-model="form.status">
              <el-radio :value="0">草稿</el-radio>
              <el-radio :value="1">发布</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="置顶">
            <el-switch v-model="isTop" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="推荐">
            <el-switch v-model="isRecommend" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        {{ isEdit ? '保存修改' : '添加题目' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import type { FormInstance, FormRules } from 'element-plus'
import { useCategoryStore } from '@/stores/category'
import { useThemeStore } from '@/stores/theme'
import { questionApi } from '@/api/question'
import type { QuestionForm, Tag, Question } from '@/types'

const props = defineProps<{
  visible: boolean
  question?: Question
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  success: []
}>()

const categoryStore = useCategoryStore()
const themeStore = useThemeStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const categoryValue = ref<number | null>(null)
const isTop = ref(false)
const isRecommend = ref(false)
const tags = ref<Tag[]>([])

const isEdit = computed(() => !!props.question)

const form = reactive<QuestionForm>({
  title: '',
  content: '',
  answer: '',
  categoryId: null,
  difficulty: 2,
  source: '',
  tagIds: [],
  status: 1,
  isTop: 0,
  isRecommend: 0,
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入题目标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
  content: [{ required: true, message: '请输入题目内容', trigger: 'blur' }],
}

const toolbars = [
  'bold', 'italic', 'strikeThrough', '-',
  'title', 'quote', 'unorderedList', 'orderedList', '-',
  'codeRow', 'code', 'link', 'image', 'table', '-',
  'revoke', 'next', '=',
  'preview', 'fullscreen',
]

watch(isTop, (val) => {
  form.isTop = val ? 1 : 0
})

watch(isRecommend, (val) => {
  form.isRecommend = val ? 1 : 0
})

watch(() => props.question, (question) => {
  if (question) {
    form.title = question.title
    form.content = question.content
    form.answer = question.answer
    form.categoryId = question.categoryId
    form.difficulty = question.difficulty
    form.source = question.source || ''
    form.tagIds = question.tags?.map(t => t.id) || []
    form.status = question.status
    form.isTop = question.isTop
    form.isRecommend = question.isRecommend
    categoryValue.value = question.categoryId
    isTop.value = question.isTop === 1
    isRecommend.value = question.isRecommend === 1
  }
}, { immediate: true })

async function handleSubmit() {
  const valid = await formRef.value?.validate()
  if (!valid) return

  loading.value = true
  try {
    if (isEdit.value && props.question) {
      await questionApi.update(props.question.id, form)
    } else {
      await questionApi.create(form)
    }
    emit('success')
    emit('update:visible', false)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.question-form {
  .full-width {
    width: 100%;
  }

  .editor-wrapper {
    width: 100%;
  }
}
</style>
