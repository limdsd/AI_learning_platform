import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/Layout.vue'),
    redirect: '/questions',
    children: [
      { path: 'questions', name: 'QuestionManage', component: () => import('@/views/QuestionManage.vue'), meta: { title: '题库管理' } },
      { path: 'questions/generate', name: 'QuestionGenerate', component: () => import('@/views/QuestionGenerate.vue'), meta: { title: '智能出题' } },
      { path: 'practice', name: 'Practice', component: () => import('@/views/Practice.vue'), meta: { title: '智能刷题' } },
      { path: 'practice/wrong', name: 'WrongBook', component: () => import('@/views/WrongBook.vue'), meta: { title: '错题本' } },
      { path: 'exams', name: 'ExamList', component: () => import('@/views/ExamList.vue'), meta: { title: '智能考试' } },
      { path: 'exam/:id', name: 'ExamTake', component: () => import('@/views/ExamTake.vue'), meta: { title: '考试作答' } },
      { path: 'exam/record/:id', name: 'ExamReport', component: () => import('@/views/ExamReport.vue'), meta: { title: '成绩报告' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = (to.meta.title ? to.meta.title + ' - ' : '') + 'AI 智能学习平台'
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
