<template>
  <div>
    <el-card>
      <el-form inline>
        <el-form-item label="知识点">
          <el-input v-model="query.knowledgePoint" placeholder="可选" style="width: 150px" clearable />
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="query.difficulty" placeholder="不限" clearable style="width: 120px">
            <el-option v-for="d in DIFFICULTIES" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="query.count" :min="1" :max="50" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="startPractice">开始刷题</el-button>
          <el-button type="success" :loading="loading" @click="smartRecommend">✨ 智能推荐</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="questions.length" style="margin-top: 16px" class="practice-card">
      <div class="progress">第 {{ currentIndex + 1 }} / {{ questions.length }} 题</div>
      <el-tag type="info" class="q-tag">{{ typeLabel(current.type) }}</el-tag>
      <el-tag type="warning" class="q-tag">{{ difficultyLabel(current.difficulty) }}</el-tag>
      <span class="kp">{{ current.knowledgePoint }}</span>

      <h3 class="content">{{ current.content }}</h3>

      <!-- 选择题/判断题 -->
      <div v-if="['single', 'judge'].includes(current.type)" class="opts">
        <el-radio-group v-model="currentAnswer" class="opt-group">
          <el-radio v-for="o in current.options" :key="o.key" :value="o.key === 'A' && current.type === 'judge' ? o.text : o.key" border class="opt-item">
            {{ o.key }}. {{ o.text }}
          </el-radio>
        </el-radio-group>
      </div>

      <div v-else-if="current.type === 'multiple'" class="opts">
        <el-checkbox-group v-model="currentAnswer" class="opt-group">
          <el-checkbox v-for="o in current.options" :key="o.key" :value="o.key" border class="opt-item">
            {{ o.key }}. {{ o.text }}
          </el-checkbox>
        </el-checkbox-group>
      </div>

      <div v-else-if="current.type === 'fill'" class="opts">
        <el-input v-model="currentAnswer" placeholder="请输入答案" style="max-width: 400px" />
      </div>

      <div v-else class="opts">
        <el-input v-model="currentAnswer" type="textarea" :rows="4" placeholder="请输入你的回答" style="max-width: 600px" />
      </div>

      <div class="actions">
        <el-button :disabled="currentIndex === 0" @click="prev">上一题</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">提交本题</el-button>
        <el-button :disabled="currentIndex === questions.length - 1" @click="next">下一题</el-button>
      </div>

      <!-- 答题结果 -->
      <el-alert v-if="currentResult" :type="currentResult.correct ? 'success' : 'error'" :closable="false" class="result">
        <template #title>
          {{ currentResult.correct ? '✅ 回答正确' : '❌ 回答错误' }}
        </template>
        <div>正确答案:{{ currentResult.correctAnswer }}</div>
        <div v-if="currentResult.analysis">解析:{{ currentResult.analysis }}</div>
      </el-alert>

      <div v-if="currentResult" class="ai-explain">
        <el-button text type="primary" :loading="explaining" @click="explain">🤖 AI 智能解析</el-button>
        <div v-if="aiText" class="ai-text">{{ aiText }}</div>
      </div>
    </el-card>

    <el-empty v-else description="点击「开始刷题」或「智能推荐」开始练习" />
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getPracticeQuestions, submitPractice, recommend, aiExplain } from '@/api/practice'
import { DIFFICULTIES, typeLabel, difficultyLabel } from '@/utils/constants'

const loading = ref(false)
const submitting = ref(false)
const explaining = ref(false)
const aiText = ref('')
const questions = ref([])
const currentIndex = ref(0)
const answers = reactive({})
const results = reactive({})
const query = reactive({ knowledgePoint: '', difficulty: '', count: 10 })

const current = computed(() => questions.value[currentIndex.value] || {})
const currentResult = computed(() => results[current.value.id])

const currentAnswer = computed({
  get() {
    if (answers[current.value.id] !== undefined) return answers[current.value.id]
    return current.value.type === 'multiple' ? [] : ''
  },
  set(v) {
    answers[current.value.id] = v
  }
})

async function startPractice() {
  loading.value = true
  try {
    const data = await getPracticeQuestions(query)
    setup(data)
  } finally {
    loading.value = false
  }
}

async function smartRecommend() {
  loading.value = true
  try {
    const data = await recommend({ count: query.count })
    setup(data)
    ElMessage.success('已根据你的薄弱知识点推荐题目')
  } finally {
    loading.value = false
  }
}

function setup(data) {
  questions.value = data
  currentIndex.value = 0
  Object.keys(answers).forEach((k) => delete answers[k])
  Object.keys(results).forEach((k) => delete results[k])
  aiText.value = ''
}

function prev() {
  if (currentIndex.value > 0) {
    currentIndex.value--
    aiText.value = ''
  }
}

function next() {
  if (currentIndex.value < questions.value.length - 1) {
    currentIndex.value++
    aiText.value = ''
  }
}

async function submit() {
  const q = current.value
  const ans = q.type === 'multiple' ? (currentAnswer.value || []).join(',') : currentAnswer.value
  submitting.value = true
  try {
    const data = await submitPractice({ questionId: q.id, userAnswer: ans })
    results[q.id] = data
    aiText.value = ''
  } finally {
    submitting.value = false
  }
}

async function explain() {
  explaining.value = true
  aiText.value = ''
  try {
    aiText.value = await aiExplain(current.value.id)
  } finally {
    explaining.value = false
  }
}
</script>

<style scoped>
.practice-card {
  max-width: 860px;
  margin: 0 auto;
}
.progress {
  color: #909399;
  font-size: 14px;
  margin-bottom: 8px;
}
.q-tag {
  margin-right: 8px;
}
.kp {
  color: #909399;
  font-size: 13px;
}
.content {
  margin: 16px 0;
  line-height: 1.6;
}
.opts {
  margin: 16px 0;
}
.opt-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: flex-start;
}
.opt-item {
  width: 100%;
  height: auto;
  white-space: normal;
}
.actions {
  margin: 20px 0;
}
.result {
  margin-top: 16px;
}
.result div {
  margin-top: 4px;
}
.ai-explain {
  margin-top: 16px;
}
.ai-text {
  margin-top: 8px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
  line-height: 1.7;
  white-space: pre-wrap;
}
</style>
