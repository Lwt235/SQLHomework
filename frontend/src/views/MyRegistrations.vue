<template>
  <div class="my-registrations-container">
    <el-card>
      <template #header>
        <span>我的报名</span>
      </template>

      <el-table :data="registrations" v-loading="loading" style="width: 100%">
        <el-table-column prop="registrationId" label="报名ID" width="100" />
        <el-table-column label="竞赛名称" min-width="200">
          <template #default="{ row }">
            {{ row.competition?.competitionTitle || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="registrationType" label="报名类型" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.teamId">团队报名</el-tag>
            <el-tag v-else type="success">个人报名</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="registrationStatus" label="状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.registrationStatus === 'pending'" type="warning">待审核</el-tag>
            <el-tag v-else-if="row.registrationStatus === 'approved'" type="success">已通过</el-tag>
            <el-tag v-else-if="row.registrationStatus === 'rejected'" type="danger">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="报名时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button 
              v-if="row.registrationStatus === 'approved'" 
              type="primary" 
              size="small" 
              @click="goToSubmission(row)"
            >
              提交作品
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { registrationAPI } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const registrations = ref([])
const loading = ref(false)

const loadRegistrations = async () => {
  loading.value = true
  try {
    const response = await registrationAPI.getRegistrationsByUser(authStore.user.userId)
    if (response.success) {
      registrations.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载报名列表失败')
  } finally {
    loading.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const goToSubmission = (registration) => {
  router.push('/my-submissions')
}

onMounted(() => {
  loadRegistrations()
})
</script>

<style scoped>
.my-registrations-container {
  max-width: 1400px;
  margin: 0 auto;
}
</style>
