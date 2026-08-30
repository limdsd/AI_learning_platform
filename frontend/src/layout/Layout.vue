<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">🧠 AI 学习平台</div>
      <el-menu
        :default-active="route.path"
        router
        background-color="#1f2d3d"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/questions">
          <el-icon><Collection /></el-icon><span>题库管理</span>
        </el-menu-item>
        <el-menu-item index="/questions/generate">
          <el-icon><MagicStick /></el-icon><span>智能出题</span>
        </el-menu-item>
        <el-menu-item index="/practice">
          <el-icon><EditPen /></el-icon><span>智能刷题</span>
        </el-menu-item>
        <el-menu-item index="/practice/wrong">
          <el-icon><Notebook /></el-icon><span>错题本</span>
        </el-menu-item>
        <el-menu-item index="/exams">
          <el-icon><Tickets /></el-icon><span>智能考试</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="title">{{ route.meta.title }}</div>
        <el-dropdown @command="handleCommand">
          <span class="user">
            {{ userStore.nickname }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

function handleCommand(command) {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout {
  height: 100vh;
}
.aside {
  background-color: #1f2d3d;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
}
.aside :deep(.el-menu) {
  border-right: none;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e6e6e6;
  background: #fff;
}
.title {
  font-size: 16px;
  font-weight: 600;
}
.user {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  color: #333;
}
.main {
  background: #f5f7fa;
}
</style>
