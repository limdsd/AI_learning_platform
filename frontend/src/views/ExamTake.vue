<template>
  <div v-if="exam" class="exam-take">
    <el-card class="header-card">
      <div class="header">
        <div class="name">{{ exam.name }}</div>
        <div class="timer" :class="{ danger: remaining <= 60 }">⏱ {{ formatTime(remaining) }}</div>
        <el-button type="danger" @click="handleSubmit">交 卷</el-button>
      </div>
    </el-card>

    <div class="body">
      <el-card class="question-card">
        <div class="progress">第 {{ currentIndex + 1 }} / {{ exam.questions.length }} 题</div>
        <el-tag type="info">{{ typeLabel(current.type) }}</el-tag>
        <span class="score">本题 {{ current.score }} 分</span>

        <h3 class="content">{{ current.content }}</h3>

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
          <el-input v-model="currentAnswer" type="textarea" :rows="5" placeholder="请输入你的回答" style="max-width: 640px" />
        </div>

        <div class="actions">
          <el-button :disabled="currentIndex === 0" @click="currentIndex--">上一题</el-button>
          <el-button :disabled="currentIndex === exam.questions.length - 1" @click="currentIndex++">下一题</el-button>
        </div>
      </el-card>

      <el-card class="nav-card">
        <h4>答题卡</h4>
        <div class="grid">
          <div
            v-for="(q, i) in exam.questions"
            :key="q.id"
            class="cell"
            :class="{ active: i === currentIndex, answered: isAnswered(q) }"
            @click="currentIndex = i"
          >
            {{ i + 1 }}
          </div>
        </div>
        <div class="legend">
          <span class="dot answered-dot"></span> 已答
          <span class="dot"></span> 未答
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { examDetail, submitExam } from '@/api/exam'
import { typeLabel } from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const exam = ref(null)
const currentIndex = ref(0)
const answers = reactive({})
const submitting = ref(false)
const remaining = ref(0)
let timer = null

const current = computed(() => exam.value?.questions[currentIndex.value] || {})

const currentAnswer = computed({
  get() {
    if (answers[current.value.id] !== undefined) return answers[current.value.id]
    return current.value.type === 'multiple' ? [] : ''
  },
  set(v) {
    answers[current.value.id] = v
  }
})

function isAnswered(q) {
  const a = answers[q.id]
  if (q.type === 'multiple') return Array.isArray(a) && a.length > 0
  return a !== undefined && a !== ''
}

function formatTime(sec) {
  const m = String(Math.floor(sec / 60)).padStart(2, '0')
  const s = String(sec % 60).padStart(2, '0')
  return `${m}:${s}`
}

function buildAnswers() {
  return exam.value.questions.map((q) => {
    const a = answers[q.id]
    return {
      questionId: q.id,
      userAnswer: q.type === 'multiple' ? (a || []).join(',') : (a || '')
    }
  })
}

async function doSubmit() {
  if (submitting.value) return
  submitting.value = true
  try {
    const result = await submitExam(exam.value.id, { recordId: route.query.recordId, answers: buildAnswers() })
    ElMessage.success('交卷成功')
    router.replace(`/exam/record/${result.recordId}`)
  } finally {
    submitting.value = false
  }
}

async function handleSubmit() {
  await ElMessageBox.confirm('确定交卷吗?交卷后不可修改。', '提示', { type: 'warning' })
  doSubmit()
}

onMounted(async () => {
  exam.value = await examDetail(route.params.id)
  remaining.value = exam.value.durationMinutes * 60
  timer = setInterval(() => {
    remaining.value--
    if (remaining.value <= 0) {
      clearInterval(timer)
      doSubmit()
    }
  }, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.header-card {
  margin-bottom: 16px;
}
.header {
  display: flex;
  align-items: center;
  gap: 24px;
}
.name {
  font-size: 18px;
  font-weight: 600;
  flex: 1;
}
.timer {
  font-size: 20px;
  font-weight: 700;
  color: #409eff;
}
.timer.danger {
  color: #f56c6c;
}
.body {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}
.question-card {
  flex: 1;
}
.nav-card {
  width: 260px;
  flex-shrink: 0;
}
.progress {
  color: #909399;
  margin-bottom: 8px;
}
.score {
  margin-left: 8px;
  color: #909399;
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
  margin-top: 20px;
}
.grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
}
.cell {
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
}
.cell.active {
  border-color: #409eff;
  color: #409eff;
}
.cell.answered {
  background: #ecf5ff;
}
.legend {
  margin-top: 12px;
  font-size: 12px;
  color: #909399;
}
.dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  border: 1px solid #dcdfe6;
  border-radius: 2px;
  margin: 0 4px 0 12px;
}
.answered-dot {
  background: #ecf5ff;
}
</style>
