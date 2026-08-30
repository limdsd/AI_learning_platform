import { defineStore } from 'pinia'
import { login as loginApi, register as registerApi } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null')
  }),
  getters: {
    isLogin: (state) => !!state.token,
    nickname: (state) => state.user?.nickname || state.user?.username || ''
  },
  actions: {
    async login(payload) {
      const data = await loginApi(payload)
      this.setSession(data)
    },
    async register(payload) {
      const data = await registerApi(payload)
      this.setSession(data)
    },
    setSession(data) {
      this.token = data.token
      this.user = { userId: data.userId, username: data.username, nickname: data.nickname }
      localStorage.setItem('token', data.token)
      localStorage.setItem('user', JSON.stringify(this.user))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
