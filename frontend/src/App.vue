<script setup>
import { useAuthStore } from './stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <el-container class="app-container">
    <el-header class="app-header">
      <div class="header-content">
        <div class="logo">
          <el-icon><Trophy /></el-icon>
          <span>竞赛管理系统</span>
        </div>
        <el-menu
          mode="horizontal"
          :router="true"
          class="nav-menu"
        >
          <el-menu-item index="/competitions">竞赛列表</el-menu-item>
          <el-menu-item v-if="authStore.isAuthenticated" index="/my-registrations">我的报名</el-menu-item>
          <el-menu-item v-if="authStore.isAuthenticated" index="/my-submissions">我的作品</el-menu-item>
          <el-menu-item v-if="authStore.isAdmin" index="/admin/users">管理后台</el-menu-item>
        </el-menu>
        <div class="user-section">
          <template v-if="authStore.isAuthenticated">
            <span class="username">{{ authStore.user?.username }}</span>
            <el-button @click="handleLogout" type="danger" plain>退出登录</el-button>
          </template>
          <template v-else>
            <el-button @click="$router.push('/login')" type="primary" plain>登录</el-button>
            <el-button @click="$router.push('/register')" type="success" plain>注册</el-button>
          </template>
        </div>
      </div>
    </el-header>
    <el-main class="app-main">
      <router-view />
    </el-main>
  </el-container>
</template>

<style scoped>
.app-container {
  min-height: 100vh;
}

.app-header {
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  padding: 0 20px;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  max-width: 1400px;
  margin: 0 auto;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
}

.nav-menu {
  flex: 1;
  border: none;
  margin: 0 20px;
}

.user-section {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username {
  margin-right: 10px;
  color: #666;
}

.app-main {
  background: #f5f5f5;
  min-height: calc(100vh - 60px);
  padding: 20px;
}
</style>
