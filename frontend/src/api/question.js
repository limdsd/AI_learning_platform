import request from '@/utils/request'

export const aiGenerate = (data) => request.post('/question/ai-generate', data)
export const saveQuestion = (data) => request.post('/question', data)
export const pageQuestions = (params) => request.get('/question/page', { params })
export const updateQuestion = (id, data) => request.put(`/question/${id}`, data)
export const deleteQuestion = (id) => request.delete(`/question/${id}`)
