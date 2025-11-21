<template>
  <div class="user-profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <el-icon size="24"><User /></el-icon>
          <span>个人中心</span>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名" label-class-name="label">
          <el-text>{{ authStore.user?.username }}</el-text>
        </el-descriptions-item>
        <el-descriptions-item label="用户ID" label-class-name="label">
          <el-text>{{ authStore.user?.userId }}</el-text>
        </el-descriptions-item>
        <el-descriptions-item label="角色" label-class-name="label" :span="2">
          <el-space>
            <el-tag v-if="authStore.isAdmin" type="danger">管理员</el-tag>
            <el-tag v-if="authStore.isTeacher" type="warning">教师</el-tag>
            <el-tag v-if="authStore.isStudent" type="success">学生</el-tag>
            <el-tag v-if="!authStore.user?.roles || authStore.user.roles.length === 0" type="info">
              普通用户
            </el-tag>
          </el-space>
        </el-descriptions-item>
        <el-descriptions-item label="真实姓名" label-class-name="label" v-if="userDetails">
          <el-text>{{ userDetails.realName || '-' }}</el-text>
        </el-descriptions-item>
        <el-descriptions-item label="邮箱" label-class-name="label" v-if="userDetails">
          <el-text>{{ userDetails.email || '-' }}</el-text>
        </el-descriptions-item>
        <el-descriptions-item label="手机号" label-class-name="label" v-if="userDetails">
          <el-text>{{ userDetails.phone || '-' }}</el-text>
        </el-descriptions-item>
        <el-descriptions-item label="学号" label-class-name="label" v-if="userDetails">
          <el-text>{{ userDetails.studentNo || '-' }}</el-text>
        </el-descriptions-item>
        <el-descriptions-item label="学校" label-class-name="label" :span="2" v-if="userDetails">
          <el-text>{{ userDetails.school || '-' }}</el-text>
        </el-descriptions-item>
        <el-descriptions-item label="院系" label-class-name="label" :span="2" v-if="userDetails">
          <el-text>{{ userDetails.department || '-' }}</el-text>
        </el-descriptions-item>
        <el-descriptions-item label="账号状态" label-class-name="label" v-if="userDetails">
          <el-tag v-if="userDetails.userStatus === 'active'" type="success">已激活</el-tag>
          <el-tag v-else-if="userDetails.userStatus === 'inactive'" type="warning">未激活</el-tag>
          <el-tag v-else-if="userDetails.userStatus === 'suspended'" type="danger">已暂停</el-tag>
          <el-tag v-else type="info">{{ userDetails.userStatus }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间" label-class-name="label" v-if="userDetails">
          <el-text>{{ formatDate(userDetails.createdAt) }}</el-text>
        </el-descriptions-item>
      </el-descriptions>

      <div class="account-actions" v-if="!authStore.isAdmin">
        <el-divider />
        <el-alert 
          title="账号注销"
          type="warning"
          description="注销账号后将无法恢复，请谨慎操作！"
          :closable="false"
          style="margin-bottom: 15px"
        />
        <el-popconfirm
          title="确定要注销账号吗？此操作不可恢复！"
          confirm-button-text="确定注销"
          cancel-button-text="取消"
          @confirm="deactivateAccount"
        >
          <template #reference>
            <el-button type="danger" plain>注销账号</el-button>
          </template>
        </el-popconfirm>
      </div>
    </el-card>

    <el-card class="permissions-card" v-if="authStore.isAuthenticated">
      <template #header>
        <div class="card-header">
          <el-icon size="24"><Lock /></el-icon>
          <span>权限说明</span>
        </div>
      </template>

      <div class="permissions-list">
        <div v-if="authStore.isAdmin" class="permission-section">
          <h4><el-icon><Avatar /></el-icon> 管理员权限</h4>
          <ul>
            <li>审核用户注册申请</li>
            <li>管理所有用户账号（激活、暂停、删除）</li>
            <li>创建和管理竞赛信息</li>
            <li>审核学生报名申请</li>
            <li>分配和管理评审任务</li>
            <li>管理奖项设置和获奖结果</li>
          </ul>
        </div>

        <div v-if="authStore.isTeacher" class="permission-section">
          <h4><el-icon><Edit /></el-icon> 教师权限</h4>
          <ul>
            <li>查看分配给自己的评审任务</li>
            <li>对学生作品进行评分和评语</li>
            <li>查看自己的评审历史</li>
          </ul>
        </div>

        <div v-if="authStore.isStudent" class="permission-section">
          <h4><el-icon><User /></el-icon> 学生权限</h4>
          <ul>
            <li>浏览和查看竞赛信息</li>
            <li>报名参加竞赛（需审核）</li>
            <li>提交竞赛作品</li>
            <li>查看自己的报名和作品状态</li>
            <li>查看获奖结果</li>
          </ul>
        </div>

        <div v-if="!authStore.user?.roles || authStore.user.roles.length === 0" class="permission-section">
          <h4><el-icon><Warning /></el-icon> 普通用户</h4>
          <ul>
            <li>您当前没有分配任何角色</li>
            <li>请联系管理员为您分配相应的角色权限</li>
          </ul>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { userAPI, authAPI } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const userDetails = ref(null)

const loadUserDetails = async () => {
  try {
    const response = await userAPI.getUserById(authStore.user?.userId)
    if (response.success) {
      userDetails.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载用户详情失败')
  }
}

const deactivateAccount = async () => {
  try {
    const response = await authAPI.deactivateAccount()
    if (response.success) {
      ElMessage.success('账号注销成功')
      // Logout and redirect to login page
      authStore.logout()
      router.push('/login')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '账号注销失败')
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(() => {
  if (authStore.user?.userId) {
    loadUserDetails()
  }
})
</script>

<style scoped>
.user-profile-container {
  max-width: 1000px;
  margin: 0 auto;
}

.profile-card, .permissions-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: bold;
}

.label {
  font-weight: 600;
}

.permissions-list {
  padding: 10px 0;
}

.permission-section {
  margin-bottom: 20px;
}

.permission-section h4 {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #409eff;
  margin-bottom: 10px;
}

.permission-section ul {
  list-style: none;
  padding-left: 0;
}

.permission-section ul li {
  padding: 8px 0;
  padding-left: 28px;
  position: relative;
}

.permission-section ul li:before {
  content: "✓";
  position: absolute;
  left: 8px;
  color: #67c23a;
  font-weight: bold;
}

.account-actions {
  margin-top: 20px;
  padding: 20px;
  text-align: center;
}

.account-actions :deep(.el-alert__description) {
  text-align: center;
}
</style>
