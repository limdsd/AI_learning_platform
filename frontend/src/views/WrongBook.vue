<template>
  <div>
    <el-card>
      <h3 class="card-title">📒 我的错题本</h3>
      <el-button type="primary" @click="load" :loading="loading">刷新</el-button>
    </el-card>

    <el-card style="margin-top: 16px" v-loading="loading">
      <el-empty v-if="!list.length && !loading" description="暂无错题,继续保持!" />
      <div v-for="item in list" :key="item.wrongId" class="wrong-item">
        <div class="head">
          <el-tag type="info">{{ typeLabel(item.type) }}</el-tag>
          <el-tag type="warning" class="ml">{{ difficultyLabel(item.difficulty) }}</el-tag>
          <el-tag type="danger" class="ml">错 {{ item.wrongCount }} 次</el-tag>
          <span class="kp">{{ item.knowledgePoint }}</span>
        </div>
        <div class="content">{{ item.content }}</div>
        <div v-if="item.options && item.options.length" class="opts">
          <div v-for="o in item.options" :key="o.key">{{ o.key }}. {{ o.text }}</div>
        </div>
        <el-collapse class="detail">
          <el-collapse-item title="查看答案与解析">
            <div><b>答案:</b>{{ item.answer }}</div>
            <div v-if="item.analysis"><b>解析:</b>{{ item.analysis }}</div>
          </el-collapse-item>
        </el-collapse>
        <div class="actions">
          <el-button size="small" type="primary" text @click="explain(item)" :loading="explainingId === item.wrongId">
            🤖 AI 解析
          </el-button>
          <el-button size="small" type="success" @click="master(item)">已掌握</el-button>
        </div>
        <div v-if="explainingId === item.wrongId && aiText" class="ai-text">{{ aiText }}</div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getWrongList, masterWrong, aiExplain } from '@/api/practice'
import { typeLabel, difficultyLabel } from '@/utils/constants'

const list = ref([])
const loading = ref(false)
const explainingId = ref(null)
const aiText = ref('')

async function load() {
  loading.value = true
  try {
    list.value = await getWrongList()
  } finally {
    loading.value = false
  }
}

async function master(item) {
  await masterWrong(item.wrongId)
  ElMessage.success('已标记为掌握')
  load()
}

async function explain(item) {
  explainingId.value = item.wrongId
  aiText.value = ''
  try {
    aiText.value = await aiExplain(item.questionId)
  } finally {
    explainingId.value = null
  }
}

onMounted(load)
</script>

<style scoped>
.card-title {
  margin: 0 0 16px;
}
.wrong-item {
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
}
.wrong-item:last-child {
  border-bottom: none;
}
.head {
  margin-bottom: 8px;
}
.ml {
  margin-left: 8px;
}
.kp {
  margin-left: 8px;
  color: #909399;
  font-size: 13px;
}
.content {
  font-weight: 500;
  line-height: 1.6;
  margin-bottom: 8px;
}
.opts {
  color: #606266;
  margin-left: 16px;
  line-height: 1.6;
}
.detail {
  margin-top: 8px;
}
.actions {
  margin-top: 8px;
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
