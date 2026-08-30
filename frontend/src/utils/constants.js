export const QUESTION_TYPES = [
  { value: 'single', label: '单选题' },
  { value: 'multiple', label: '多选题' },
  { value: 'judge', label: '判断题' },
  { value: 'fill', label: '填空题' },
  { value: 'short_answer', label: '简答题' }
]

export const DIFFICULTIES = [
  { value: 'easy', label: '简单' },
  { value: 'medium', label: '中等' },
  { value: 'hard', label: '困难' }
]

export const typeLabel = (v) => QUESTION_TYPES.find((t) => t.value === v)?.label || v
export const difficultyLabel = (v) => DIFFICULTIES.find((d) => d.value === v)?.label || v
