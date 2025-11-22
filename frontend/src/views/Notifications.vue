<template>
  <div class="notifications-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>消息通知</span>
          <div>
            <el-button 
              v-if="selectedNotifications.length > 0"
              type="danger" 
              size="small" 
              @click="batchDelete"
            >
              批量删除 ({{ selectedNotifications.length }})
            </el-button>
            <el-button 
              type="primary" 
              size="small" 
              @click="markAllAsRead"
              :disabled="unreadCount === 0"
            >
              全部已读
            </el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="未读消息" name="unread">
          <div v-if="unreadNotifications.length === 0" class="empty-state">
            <el-empty description="暂无未读消息" />
          </div>
          <div v-else class="notification-list">
            <div class="select-all-bar">
              <el-checkbox 
                v-model="selectAllUnread" 
                @change="handleSelectAllUnread"
              >
                全选
              </el-checkbox>
            </div>
            <div 
              v-for="notification in unreadNotifications" 
              :key="notification.notificationId"
              class="notification-item unread"
            >
              <div class="notification-checkbox">
                <el-checkbox 
                  :model-value="isSelected(notification.notificationId)"
                  @change="toggleSelection(notification.notificationId)"
                />
              </div>
              <div class="notification-content-wrapper">
                <div class="notification-header">
                  <div class="notification-type-badge">
                    <el-tag :type="getTypeColor(notification.notificationType)" size="small">
                      {{ getTypeName(notification.notificationType) }}
                    </el-tag>
                  </div>
                  <div class="notification-actions">
                    <el-button 
                      type="primary" 
                      size="small" 
                      text
                      @click="markAsRead(notification.notificationId)"
                    >
                      标记已读
                    </el-button>
                    <el-button 
                      type="danger" 
                      size="small" 
                      text
                      @click="deleteNotification(notification.notificationId)"
                    >
                      删除
                    </el-button>
                  </div>
                </div>
                <div class="notification-title">{{ notification.title }}</div>
                <div class="notification-content">{{ notification.content }}</div>
                <div class="notification-time">{{ formatDate(notification.createdAt) }}</div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="所有消息" name="all">
          <div v-if="allNotifications.length === 0" class="empty-state">
            <el-empty description="暂无消息" />
          </div>
          <div v-else class="notification-list">
            <div class="select-all-bar">
              <el-checkbox 
                v-model="selectAllMessages" 
                @change="handleSelectAllMessages"
              >
                全选
              </el-checkbox>
            </div>
            <div 
              v-for="notification in allNotifications" 
              :key="notification.notificationId"
              class="notification-item"
              :class="{ unread: !notification.read }"
            >
              <div class="notification-checkbox">
                <el-checkbox 
                  :model-value="isSelected(notification.notificationId)"
                  @change="toggleSelection(notification.notificationId)"
                />
              </div>
              <div class="notification-content-wrapper">
                <div class="notification-header">
                  <div class="notification-type-badge">
                    <el-tag :type="getTypeColor(notification.notificationType)" size="small">
                      {{ getTypeName(notification.notificationType) }}
                    </el-tag>
                    <el-tag v-if="!notification.read" type="danger" size="small" style="margin-left: 5px">
                      未读
                    </el-tag>
                  </div>
                  <div class="notification-actions">
                    <el-button 
                      v-if="!notification.read"
                      type="primary" 
                      size="small" 
                      text
                      @click="markAsRead(notification.notificationId)"
                    >
                      标记已读
                    </el-button>
                    <el-button 
                      type="danger" 
                      size="small" 
                      text
                      @click="deleteNotification(notification.notificationId)"
                    >
                      删除
                    </el-button>
                  </div>
                </div>
                <div class="notification-title">{{ notification.title }}</div>
                <div class="notification-content">{{ notification.content }}</div>
                <div class="notification-time">{{ formatDate(notification.createdAt) }}</div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '../stores/auth'
import { notificationAPI } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const authStore = useAuthStore()
const activeTab = ref('unread')
const allNotifications = ref([])
const unreadCount = ref(0)
const selectedNotifications = ref([])
const selectAllUnread = ref(false)
const selectAllMessages = ref(false)

const unreadNotifications = computed(() => {
  return allNotifications.value.filter(n => !n.read)
})

const loadAllNotifications = async () => {
  try {
    const response = await notificationAPI.getUserNotifications(authStore.user.userId)
    if (response.success) {
      allNotifications.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载消息失败')
  }
}

const loadUnreadCount = async () => {
  try {
    const response = await notificationAPI.getUnreadCount(authStore.user.userId)
    if (response.success) {
      unreadCount.value = response.data
    }
  } catch (error) {
    console.error('加载未读数失败', error)
  }
}

const isSelected = (notificationId) => {
  return selectedNotifications.value.includes(notificationId)
}

const toggleSelection = (notificationId) => {
  const index = selectedNotifications.value.indexOf(notificationId)
  if (index > -1) {
    selectedNotifications.value.splice(index, 1)
  } else {
    selectedNotifications.value.push(notificationId)
  }
  updateSelectAllState()
}

const handleSelectAllUnread = (value) => {
  if (value) {
    const unreadIds = unreadNotifications.value.map(n => n.notificationId)
    selectedNotifications.value = [...new Set([...selectedNotifications.value, ...unreadIds])]
  } else {
    const unreadIds = unreadNotifications.value.map(n => n.notificationId)
    selectedNotifications.value = selectedNotifications.value.filter(id => !unreadIds.includes(id))
  }
}

const handleSelectAllMessages = (value) => {
  if (value) {
    selectedNotifications.value = allNotifications.value.map(n => n.notificationId)
  } else {
    selectedNotifications.value = []
  }
}

const updateSelectAllState = () => {
  if (activeTab.value === 'unread') {
    const unreadIds = unreadNotifications.value.map(n => n.notificationId)
    selectAllUnread.value = unreadIds.length > 0 && unreadIds.every(id => selectedNotifications.value.includes(id))
  } else {
    const allIds = allNotifications.value.map(n => n.notificationId)
    selectAllMessages.value = allIds.length > 0 && allIds.every(id => selectedNotifications.value.includes(id))
  }
}

const markAsRead = async (notificationId) => {
  try {
    const response = await notificationAPI.markAsRead(notificationId)
    if (response.success) {
      ElMessage.success('已标记为已读')
      await loadAllNotifications()
      await loadUnreadCount()
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const markAllAsRead = async () => {
  try {
    const response = await notificationAPI.markAllAsRead(authStore.user.userId)
    if (response.success) {
      ElMessage.success('所有消息已标记为已读')
      await loadAllNotifications()
      await loadUnreadCount()
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const deleteNotification = async (notificationId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条消息吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await notificationAPI.deleteNotification(notificationId)
    if (response.success) {
      ElMessage.success('删除成功')
      // Remove from selected if it was selected
      const index = selectedNotifications.value.indexOf(notificationId)
      if (index > -1) {
        selectedNotifications.value.splice(index, 1)
      }
      await loadAllNotifications()
      await loadUnreadCount()
      updateSelectAllState()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedNotifications.value.length} 条消息吗？`, 
      '批量删除', 
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await notificationAPI.batchDeleteNotifications(selectedNotifications.value)
    if (response.success) {
      ElMessage.success('批量删除成功')
      selectedNotifications.value = []
      selectAllUnread.value = false
      selectAllMessages.value = false
      await loadAllNotifications()
      await loadUnreadCount()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const getTypeName = (type) => {
  const typeMap = {
    'registration': '报名通知',
    'competition': '比赛通知',
    'review': '评审通知',
    'submission': '作品通知',
    'award': '获奖通知',
    'team': '团队通知',
    'system': '系统通知'
  }
  return typeMap[type] || '通知'
}

const getTypeColor = (type) => {
  const colorMap = {
    'registration': 'primary',
    'competition': 'success',
    'review': 'warning',
    'submission': 'info',
    'award': 'danger',
    'team': 'primary',
    'system': 'info'
  }
  return colorMap[type] || 'info'
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(() => {
  loadAllNotifications()
  loadUnreadCount()
})
</script>

<style scoped>
.notifications-page {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notification-list {
  margin-top: 20px;
}

.select-all-bar {
  padding: 10px 20px;
  border: 1px solid #eee;
  border-radius: 4px;
  margin-bottom: 10px;
  background: #fafafa;
}

.notification-item {
  display: flex;
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 4px;
  margin-bottom: 15px;
  background: white;
  transition: all 0.3s;
}

.notification-item:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.notification-item.unread {
  background-color: #ecf5ff;
  border-color: #b3d8ff;
}

.notification-checkbox {
  display: flex;
  align-items: flex-start;
  padding-top: 5px;
  margin-right: 15px;
}

.notification-content-wrapper {
  flex: 1;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.notification-type-badge {
  display: flex;
  align-items: center;
}

.notification-actions {
  display: flex;
  gap: 5px;
}

.notification-title {
  font-weight: bold;
  font-size: 16px;
  margin-bottom: 10px;
  color: #303133;
}

.notification-content {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 10px;
}

.notification-time {
  color: #909399;
  font-size: 12px;
}

.empty-state {
  padding: 40px 0;
}
</style>
