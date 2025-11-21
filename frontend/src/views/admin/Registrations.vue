<template>
  <div class="admin-registrations">
    <el-card>
      <template #header>
        <span>报名审核</span>
      </template>

      <el-table :data="registrations" v-loading="loading" style="width: 100%">
        <el-table-column prop="registrationId" label="ID" width="80" />
        <el-table-column label="竞赛名称" min-width="200">
          <template #default="{ row }">
            {{ row.competition?.competitionTitle || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="用户" width="120">
          <template #default="{ row }">
            {{ row.user?.realName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="registrationStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.registrationStatus === 'pending'" type="warning">待审核</el-tag>
            <el-tag v-else-if="row.registrationStatus === 'approved'" type="success">已通过</el-tag>
            <el-tag v-else-if="row.registrationStatus === 'rejected'" type="danger">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button 
              v-if="row.registrationStatus === 'pending'" 
              type="success" 
              size="small" 
              @click="approveRegistration(row.registrationId)"
            >
              通过
            </el-button>
            <el-button 
              v-if="row.registrationStatus === 'pending'" 
              type="danger" 
              size="small" 
              @click="rejectRegistration(row.registrationId)"
            >
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { registrationAPI } from '../../api'
import { ElMessage } from 'element-plus'

const registrations = ref([])
const loading = ref(false)

const loadRegistrations = async () => {
  loading.value = true
  try {
    const response = await registrationAPI.getAllRegistrations()
    if (response.success) {
      registrations.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载报名列表失败')
  } finally {
    loading.value = false
  }
}

const approveRegistration = async (id) => {
  try {
    const response = await registrationAPI.updateRegistrationStatus(id, 'approved', 1)
    if (response.success) {
      ElMessage.success('报名已通过')
      loadRegistrations()
    }
  } catch (error) {
    ElMessage.error('审核失败')
  }
}

const rejectRegistration = async (id) => {
  try {
    const response = await registrationAPI.updateRegistrationStatus(id, 'rejected', 1)
    if (response.success) {
      ElMessage.success('报名已拒绝')
      loadRegistrations()
    }
  } catch (error) {
    ElMessage.error('审核失败')
  }
}

onMounted(() => {
  loadRegistrations()
})
</script>

<style scoped>
.admin-registrations {
  padding: 20px;
}
</style>
