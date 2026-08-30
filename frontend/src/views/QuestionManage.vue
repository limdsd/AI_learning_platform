<template>
  <div>
    <el-card>
      <el-form inline>
        <el-form-item>
          <el-input v-model="query.knowledgePoint" placeholder="知识点" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="query.type" placeholder="题型" clearable style="width: 130px">
            <el-option v-for="t in QUESTION_TYPES" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="query.difficulty" placeholder="难度" clearable style="width: 130px">
            <el-option v-for="d in DIFFICULTIES" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button type="success" @click="openAdd">新增题目</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 16px">
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="content" label="题干" min-width="280" show-overflow-tooltip />
        <el-table-column label="题型" width="90">
          <template #default="{ row }">{{ typeLabel(row.type) }}</template>
        </el-table-column>
        <el-table-column prop="knowledgePoint" label="知识点" width="130" show-overflow-tooltip />
        <el-table-column label="难度" width="80">
          <template #default="{ row }">{{ difficultyLabel(row.difficulty) }}</template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="80" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pager"
        layout="total, prev, pager, next"
        :total="total"
        :page-size="size"
        :current-page="page"
        @current-change="onPageChange"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑题目' : '新增题目'" width="640px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="题型">
          <el-select v-model="form.type" style="width: 200px" @change="onTypeChange">
            <el-option v-for="t in QUESTION_TYPES" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="题干">
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="请输入题干" />
        </el-form-item>
        <el-form-item v-if="['single', 'multiple', 'judge'].includes(form.type)" label="选项">
          <div class="options">
            <div v-for="opt in optionList" :key="opt.key" class="option-row">
              <el-tag class="opt-key">{{ opt.key }}</el-tag>
              <el-input v-model="opt.text" :placeholder="`选项 ${opt.key}`" />
            </div>
          </div>
        </el-form-item>
        <el-form-item label="答案">
          <el-input v-model="form.answer" placeholder="如 A / A,C / 对 / 参考答案" />
        </el-form-item>
        <el-form-item label="解析">
          <el-input v-model="form.analysis" type="textarea" :rows="2" placeholder="解析(可选)" />
        </el-form-item>
        <el-form-item label="知识点">
          <el-input v-model="form.knowledgePoint" placeholder="知识点" />
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="form.difficulty" style="width: 200px">
            <el-option v-for="d in DIFFICULTIES" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitDialog">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageQuestions, saveQuestion, updateQuestion, deleteQuestion } from '@/api/question'
import { QUESTION_TYPES, DIFFICULTIES, typeLabel, difficultyLabel } from '@/utils/constants'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const loading = ref(false)
const query = reactive({ knowledgePoint: '', type: '', difficulty: '' })

const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)
const optionList = ref([])
const form = reactive({ type: 'single', content: '', answer: '', analysis: '', knowledgePoint: '', difficulty: 'medium' })

async function load() {
  loading.value = true
  try {
    const data = await pageQuestions({ page: page.value, size, ...query })
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function onPageChange(p) {
  page.value = p
  load()
}

function resetForm() {
  editingId.value = null
  Object.assign(form, { type: 'single', content: '', answer: '', analysis: '', knowledgePoint: '', difficulty: 'medium' })
  initOptions('single')
}

function initOptions(type) {
  if (type === 'judge') {
    optionList.value = [{ key: 'A', text: '对' }, { key: 'B', text: '错' }]
  } else if (type === 'single' || type === 'multiple') {
    optionList.value = [
      { key: 'A', text: '' }, { key: 'B', text: '' }, { key: 'C', text: '' }, { key: 'D', text: '' }
    ]
  } else {
    optionList.value = []
  }
}

function onTypeChange(type) {
  initOptions(type)
}

function openAdd() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, {
    type: row.type,
    content: row.content,
    answer: row.answer,
    analysis: row.analysis,
    knowledgePoint: row.knowledgePoint,
    difficulty: row.difficulty
  })
  if (row.options && row.options.length) {
    optionList.value = row.options.map((o) => ({ key: o.key, text: o.text }))
  } else {
    initOptions(row.type)
  }
  dialogVisible.value = true
}

async function submitDialog() {
  if (!form.content || !form.answer) {
    ElMessage.warning('请填写题干和答案')
    return
  }
  const payload = { ...form }
  if (['single', 'multiple', 'judge'].includes(form.type)) {
    payload.options = JSON.stringify(optionList.value)
  } else {
    payload.options = '[]'
  }
  saving.value = true
  try {
    if (editingId.value) {
      await updateQuestion(editingId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await saveQuestion(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  await ElMessageBox.confirm('确定删除该题目吗?', '提示', { type: 'warning' })
  await deleteQuestion(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
.options {
  width: 100%;
}
.option-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.opt-key {
  width: 40px;
  text-align: center;
}
</style>
