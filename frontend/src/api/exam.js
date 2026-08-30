import request from '@/utils/request'

export const generateExam = (data) => request.post('/exam/generate', data)
export const listExams = () => request.get('/exam/list')
export const examDetail = (id) => request.get(`/exam/${id}/detail`)
export const startExam = (id) => request.post(`/exam/${id}/start`)
export const submitExam = (id, data) => request.post(`/exam/${id}/submit`, data)
export const examReport = (recordId) => request.get(`/exam/record/${recordId}`)
