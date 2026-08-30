<template>
  <div>
    <el-card>
      <h3 class="card-title">🤖 智能生成题目</h3>
      <el-form inline>
        <el-form-item label="知识点">
          <el-input v-model="form.knowledgePoint" placeholder="如:二次函数" style="width: 180px" />
        </el-form-item>
        <el-form-item label="题型">
          <el-select v-model="form.type" style="width: 140px">
            <el-option v-for="t in QUESTION_TYPES" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="form.difficulty" style="width: 120px">
            <el-option v-for="d in DIFFICULTIES" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="form.count" :min="1" :max="20" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="generate">开始生成</el-button>
        </el-form-item>
      </el-form>
      <div class="tip">AI 将根据知识点、题型和难度自动生成题目并存入题库,可到「题库管理」查看。</div>
    </el-card>

    <el-card v-if="list.length" style="margin-top: 16px">
      <h3 class="card-title">生成结果({{ list.length }} 道)</h3>
      <el-collapse>
        <el-collapse-item v-for="(q, i) in list" :key="q.id || i" :title="`${i + 1}. ${q.content}`">
          <div v-if="q.options && q.options.length" class="block">
            <div v-for="o in q.options" :key="o.key" class="opt">{{ o.key }}. {{ o.text }}</div>
          </div>
          <div class="block"><b>答案:</b>{{ q.answer }}</div>
          <div class="block"><b>解析:</b>{{ q.analysis || '无' }}</div>
          <div class="meta">{{ typeLabel(q.type) }} · {{ difficultyLabel(q.difficulty) }} · {{ q.knowledgePoint }}</div>
        </el-collapse-item>
      </el-collapse>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { aiGenerate } from '@/api/question'
import { QUESTION_TYPES, DIFFICULTIES, typeLabel, difficultyLabel } from '@/utils/constants'

const loading = ref(false)
const list = ref([])
const form = reactive({ knowledgePoint: '', type: 'single', difficulty: 'medium', count: 5 })

async function generate() {
  if (!form.knowledgePoint) {
    ElMessage.warning('请输入知识点')
    return
  }
  loading.value = true
  try {
    list.value = await aiGenerate(form)
    ElMessage.success(`成功生成 ${list.value.length} 道题目`)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.card-title {
  margin: 0 0 16px;
}
.tip {
  color: #909399;
  font-size: 13px;
  margin-top: 8px;
}
.block {
  margin: 6px 0;
  line-height: 1.6;
}
.opt {
  margin-left: 16px;
  line-height: 1.6;
}
.meta {
  color: #909399;
  font-size: 13px;
  margin-top: 6px;
}
</style>
