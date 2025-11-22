<script setup>
import { ref, onMounted, watch } from 'vue'
import { useAuthStore } from './stores/auth'
import { useRouter } from 'vue-router'
import { notificationAPI } from './api'
import { Bell } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const router = useRouter()
const showNotifications = ref(false)
const notifications = ref([])
const unreadCount = ref(0)

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

const loadNotifications = async () => {
  if (!authStore.isAuthenticated || !authStore.user?.userId) return
  
  try {
    const response = await notificationAPI.getUnreadNotifications(authStore.user.userId)
    if (response.success) {
      notifications.value = response.data.slice(0, 10) // Show latest 10
    }
  } catch (error) {
    console.error('加载通知失败', error)
  }
}

const loadUnreadCount = async () => {
  if (!authStore.isAuthenticated || !authStore.user?.userId) return
  
  try {
    const response = await notificationAPI.getUnreadCount(authStore.user.userId)
    if (response.success) {
      unreadCount.value = response.data
    }
  } catch (error) {
    console.error('加载未读数失败', error)
  }
}

const markAllAsRead = async () => {
  if (!authStore.user?.userId) return
  
  try {
    const response = await notificationAPI.markAllAsRead(authStore.user.userId)
    if (response.success) {
      ElMessage.success('所有消息已标记为已读')
      await loadNotifications()
      await loadUnreadCount()
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleNotificationClick = async (notification) => {
  if (!notification.read) {
    try {
      await notificationAPI.markAsRead(notification.notificationId)
      await loadNotifications()
      await loadUnreadCount()
    } catch (error) {
      console.error('标记已读失败', error)
    }
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return date.toLocaleDateString('zh-CN')
}

// Load notifications when authenticated
watch(() => authStore.isAuthenticated, (newVal) => {
  if (newVal) {
    loadNotifications()
    loadUnreadCount()
  }
})

// Periodic refresh of unread count
let refreshInterval = null
onMounted(() => {
  if (authStore.isAuthenticated) {
    loadNotifications()
    loadUnreadCount()
  }
  
  // Refresh every 30 seconds
  refreshInterval = setInterval(() => {
    if (authStore.isAuthenticated) {
      loadUnreadCount()
    }
  }, 30000)
})

// Clean up interval on unmount
import { onUnmounted } from 'vue'
onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})
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
          <el-menu-item v-if="authStore.isAuthenticated" index="/profile">个人中心</el-menu-item>
          <el-menu-item v-if="authStore.isAuthenticated && (authStore.isStudent || (!authStore.isAdmin && !authStore.isTeacher))" index="/apply-competition">报名参赛</el-menu-item>
          <el-menu-item v-if="authStore.isAuthenticated && (authStore.isStudent || (!authStore.isAdmin && !authStore.isTeacher))" index="/teams">团队管理</el-menu-item>
          <el-menu-item v-if="authStore.isAuthenticated && !authStore.isAdmin && !authStore.isTeacher" index="/my-registrations">我的报名</el-menu-item>
          <el-menu-item v-if="authStore.isAuthenticated && !authStore.isAdmin && !authStore.isTeacher" index="/my-submissions">我的作品</el-menu-item>
          <el-menu-item v-if="authStore.isTeacher" index="/teacher-review">评审中心</el-menu-item>
          <el-menu-item v-if="authStore.isAdmin" index="/admin/users">管理后台</el-menu-item>
        </el-menu>
        <div class="user-section">
          <template v-if="authStore.isAuthenticated">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
              <el-button 
                circle 
                @click="showNotifications = true"
                :icon="Bell"
              />
            </el-badge>
            <div class="user-info">
              <span class="username">{{ authStore.user?.username }}</span>
              <el-tag v-if="authStore.isAdmin" type="danger" size="small">管理员</el-tag>
              <el-tag v-else-if="authStore.isTeacher" type="warning" size="small">教师</el-tag>
              <el-tag v-else-if="authStore.isStudent" type="success" size="small">学生</el-tag>
              <el-tag v-else type="info" size="small">{{ authStore.userRolesText }}</el-tag>
            </div>
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
    
    <!-- Notification Drawer -->
    <el-drawer
      v-model="showNotifications"
      title="消息通知"
      direction="rtl"
      size="400px"
    >
      <div class="notifications-container">
        <div class="notifications-header">
          <el-button 
            type="primary" 
            size="small" 
            @click="markAllAsRead"
            :disabled="unreadCount === 0"
          >
            全部已读
          </el-button>
          <el-button 
            size="small" 
            @click="$router.push('/notifications'); showNotifications = false"
          >
            查看全部
          </el-button>
        </div>
        
        <el-divider />
        
        <div v-if="notifications.length === 0" class="empty-notifications">
          <el-empty description="暂无消息" />
        </div>
        
        <div v-else class="notification-list">
          <div 
            v-for="notification in notifications" 
            :key="notification.notificationId"
            class="notification-item"
            :class="{ unread: !notification.read }"
            @click="handleNotificationClick(notification)"
          >
            <div class="notification-header">
              <span class="notification-title">{{ notification.title }}</span>
              <el-tag v-if="!notification.read" type="danger" size="small">新</el-tag>
            </div>
            <div class="notification-content">{{ notification.content }}</div>
            <div class="notification-time">{{ formatDate(notification.createdAt) }}</div>
          </div>
        </div>
      </div>
    </el-drawer>
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

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.username {
  margin-right: 4px;
  color: #666;
  font-weight: 500;
}

.app-main {
  background: #f5f5f5;
  min-height: calc(100vh - 60px);
  padding: 20px;
}

.notification-badge {
  margin-right: 10px;
}

.notifications-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.notifications-header {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.notification-list {
  flex: 1;
  overflow-y: auto;
}

.notification-item {
  padding: 15px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background-color 0.2s;
}

.notification-item:hover {
  background-color: #f5f7fa;
}

.notification-item.unread {
  background-color: #ecf5ff;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.notification-title {
  font-weight: bold;
  font-size: 14px;
}

.notification-content {
  color: #666;
  font-size: 13px;
  margin-bottom: 8px;
  line-height: 1.5;
}

.notification-time {
  color: #999;
  font-size: 12px;
}

.empty-notifications {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
}
</style>
