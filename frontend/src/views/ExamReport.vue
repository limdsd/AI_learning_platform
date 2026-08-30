<template>
  <div v-if="report" class="report">
    <el-card class="score-card">
      <div class="score-title">{{ report.examName }}</div>
      <div class="score-main">
        <div class="total">
          <span class="num">{{ report.totalScore }}</span>
          <span class="den">/ {{ report.fullScore }} 分</span>
        </div>
        <div class="detail">
          <div>客观题:{{ report.objectiveScore }} 分</div>
          <div>主观题:{{ report.subjectiveScore }} 分</div>
          <div class="time">交卷时间:{{ report.submitTime }}</div>
        </div>
      </div>
    </el-card>

    <el-card style="margin-top: 16px">
      <h3>答题详情</h3>
      <div v-for="(item, i) in report.items" :key="item.questionId" class="item">
        <div class="item-head">
          <span class="index">{{ i + 1 }}.</span>
          <el-tag type="info" size="small">{{ typeLabel(item.type) }}</el-tag>
          <span class="got" :class="{ ok: item.gotScore === item.score, bad: item.gotScore < item.score }">
            {{ item.gotScore }} / {{ item.score }} 分
          </span>
        </div>
        <div class="content">{{ item.content }}</div>
        <div v-if="item.options && item.options.length" class="opts">
          <span v-for="o in item.options" :key="o.key" class="opt">{{ o.key }}. {{ o.text }}</span>
        </div>
        <div class="answer-row">
          <div>你的答案:<span class="user">{{ item.userAnswer || '未作答' }}</span></div>
          <div>正确答案:<span class="correct">{{ item.correctAnswer }}</span></div>
        </div>
        <div v-if="item.aiComment" class="comment">💬 {{ item.aiComment }}</div>
        <div v-if="item.analysis" class="analysis">📖 {{ item.analysis }}</div>
      </div>
    </el-card>

    <div class="back">
      <el-button type="primary" @click="$router.push('/exams')">返回考试列表</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { examReport } from '@/api/exam'
import { typeLabel } from '@/utils/constants'

const route = useRoute()
const report = ref(null)

onMounted(async () => {
  report.value = await examReport(route.params.id)
})
</script>

<style scoped>
.score-card {
  text-align: center;
}
.score-title {
  font-size: 18px;
  font-weight: 600;
}
.score-main {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 48px;
  margin-top: 16px;
}
.total .num {
  font-size: 48px;
  font-weight: 700;
  color: #409eff;
}
.total .den {
  color: #909399;
}
.detail {
  text-align: left;
  color: #606266;
  line-height: 1.8;
}
.time {
  color: #909399;
  font-size: 13px;
}
.item {
  padding: 16px 0;
  border-bottom: 1px solid #ebeef5;
}
.item:last-child {
  border-bottom: none;
}
.item-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.index {
  font-weight: 600;
}
.got {
  margin-left: auto;
  font-weight: 600;
}
.got.ok {
  color: #67c23a;
}
.got.bad {
  color: #f56c6c;
}
.content {
  line-height: 1.6;
  margin-bottom: 8px;
}
.opts {
  margin-bottom: 8px;
}
.opt {
  margin-right: 16px;
  color: #606266;
}
.answer-row {
  display: flex;
  gap: 32px;
  font-size: 14px;
  margin-bottom: 8px;
}
.user {
  color: #f56c6c;
}
.correct {
  color: #67c23a;
  font-weight: 500;
}
.comment {
  background: #f0f9eb;
  padding: 8px 12px;
  border-radius: 6px;
  margin-bottom: 8px;
  line-height: 1.6;
}
.analysis {
  background: #f5f7fa;
  padding: 8px 12px;
  border-radius: 6px;
  line-height: 1.6;
  color: #606266;
}
.back {
  margin-top: 16px;
  text-align: center;
}
</style>
