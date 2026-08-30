<template>
  <div>
    <el-card>
      <el-button type="primary" @click="dialogVisible = true">🤖 AI 智能组卷</el-button>
      <el-button @click="load">刷新</el-button>
    </el-card>

    <el-card style="margin-top: 16px" v-loading="loading">
      <el-empty v-if="!list.length && !loading" description="暂无试卷,点击「AI 智能组卷」创建" />
      <el-table :data="list">
        <el-table-column prop="name" label="试卷名称" min-width="220" />
        <el-table-column prop="totalScore" label="总分" width="90" />
        <el-table-column label="时长" width="100">
          <template #default="{ row }">{{ row.durationMinutes }} 分钟</template>
        </el-table-column>
        <el-table-column prop="createType" label="组卷方式" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="start(row)">开始考试</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="AI 智能组卷" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="试卷名称">
          <el-input v-model="form.name" placeholder="如:期中数学测试" />
        </el-form-item>
        <el-form-item label="考试时长">
          <el-input-number v-model="form.durationMinutes" :min="5" :max="180" />
          <span class="unit">分钟</span>
        </el-form-item>
        <el-form-item label="知识点">
          <el-input v-model="form.knowledgePoint" placeholder="如:三角函数(可选)" />
        </el-form-item>
        <el-form-item label="题型">
          <el-select v-model="form.type" style="width: 200px">
            <el-option v-for="t in QUESTION_TYPES" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="form.difficulty" style="width: 200px">
            <el-option v-for="d in DIFFICULTIES" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="题量">
          <el-input-number v-model="form.count" :min="1" :max="30" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="generating" @click="generate">生成试卷</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listExams, generateExam, startExam } from '@/api/exam'
import { QUESTION_TYPES, DIFFICULTIES } from '@/utils/constants'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const generating = ref(false)
const dialogVisible = ref(false)
const form = reactive({
  name: '',
  durationMinutes: 60,
  knowledgePoint: '',
  type: 'single',
  difficulty: 'medium',
  count: 10
})

async function load() {
  loading.value = true
  try {
    list.value = await listExams()
  } finally {
    loading.value = false
  }
}

async function generate() {
  if (!form.name) {
    ElMessage.warning('请输入试卷名称')
    return
  }
  generating.value = true
  try {
    await generateExam(form)
    ElMessage.success('试卷生成成功')
    dialogVisible.value = false
    load()
  } finally {
    generating.value = false
  }
}

async function start(row) {
  const record = await startExam(row.id)
  router.push({ path: `/exam/${row.id}`, query: { recordId: record.id } })
}

onMounted(load)
</script>

<style scoped>
.unit {
  margin-left: 8px;
  color: #909399;
}
</style>
