import request from '@/utils/request'

export const getPracticeQuestions = (params) => request.get('/practice/questions', { params })
export const submitPractice = (data) => request.post('/practice/submit', data)
export const getWrongList = () => request.get('/practice/wrong')
export const masterWrong = (id) => request.post(`/practice/wrong/${id}/master`)
export const aiExplain = (questionId) => request.post('/practice/ai-explain', null, { params: { questionId } })
export const recommend = (params) => request.get('/practice/recommend', { params })
