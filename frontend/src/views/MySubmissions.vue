<template>
  <div class="my-submissions-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的作品</span>
          <el-button type="primary" @click="showCreateDialog = true">创建作品</el-button>
        </div>
      </template>

      <el-table :data="submissions" v-loading="loading" style="width: 100%">
        <el-table-column prop="submissionId" label="作品ID" width="100" />
        <el-table-column prop="submissionTitle" label="作品标题" min-width="200" />
        <el-table-column prop="submissionStatus" label="状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.submissionStatus === 'draft'" type="info">草稿</el-tag>
            <el-tag v-else-if="row.submissionStatus === 'submitted'" type="success">已提交</el-tag>
            <el-tag v-else-if="row.submissionStatus === 'locked'" type="warning">已锁定</el-tag>
            <el-tag v-else-if="row.submissionStatus === 'invalid'" type="danger">无效</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button 
              size="small" 
              @click="editSubmission(row)"
              :disabled="hasLockedSubmission(row.registrationId)"
            >
              编辑
            </el-button>
            <el-button 
              v-if="row.submissionStatus === 'draft'" 
              type="primary" 
              size="small" 
              @click="submitWork(row.submissionId)"
              :disabled="hasLockedSubmission(row.registrationId) || !isInSubmissionPeriod(row)"
              :title="hasLockedSubmission(row.registrationId) ? '该报名已有锁定的作品' : (!isInSubmissionPeriod(row) ? '当前不在作品提交时间范围内' : '')"
            >
              提交作品
            </el-button>
            <el-button 
              v-if="row.submissionStatus === 'submitted' && row.finalLockedAt == null" 
              type="warning" 
              size="small" 
              @click="lockWork(row.submissionId)"
              :disabled="hasLockedSubmission(row.registrationId)"
            >
              最终锁定
            </el-button>
            <el-tag v-if="row.finalLockedAt != null" type="danger" size="small">已锁定</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Create Submission Dialog -->
    <el-dialog v-model="showCreateDialog" title="创建作品" width="600px">
      <el-form :model="newSubmission" label-width="100px">
        <el-form-item label="选择报名">
          <el-select v-model="newSubmission.registrationId" placeholder="请选择已通过的报名">
            <el-option 
              v-for="reg in approvedRegistrations" 
              :key="reg.registrationId" 
              :label="reg.competition?.competitionTitle" 
              :value="reg.registrationId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="作品标题">
          <el-input v-model="newSubmission.submissionTitle" placeholder="请输入作品标题" />
        </el-form-item>
        <el-form-item label="作品摘要">
          <el-input 
            v-model="newSubmission.abstractText" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入作品摘要"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createSubmission">创建</el-button>
      </template>
    </el-dialog>

    <!-- Edit Submission Dialog -->
    <el-dialog v-model="showEditDialog" title="编辑作品" width="600px">
      <el-form :model="editSubmissionForm" label-width="100px">
        <el-form-item label="作品标题">
          <el-input v-model="editSubmissionForm.submissionTitle" placeholder="请输入作品标题" />
        </el-form-item>
        <el-form-item label="作品摘要">
          <el-input 
            v-model="editSubmissionForm.abstractText" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入作品摘要"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="updateSubmission">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import { submissionAPI, registrationAPI, systemTimeAPI } from '../api'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const submissions = ref([])
const approvedRegistrations = ref([])
const loading = ref(false)
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const editingSubmission = ref(null)
const systemTime = ref(null)
const newSubmission = ref({
  registrationId: null,
  submissionTitle: '',
  abstractText: ''
})
const editSubmissionForm = ref({
  submissionTitle: '',
  abstractText: ''
})

const loadSubmissions = async () => {
  loading.value = true
  try {
    const response = await submissionAPI.getAllSubmissions()
    if (response.success) {
      // Get all registrations for current user (not just the filtered approvedRegistrations)
      const userRegsResponse = await registrationAPI.getRegistrationsByUser(authStore.user.userId)
      const userRegistrationIds = userRegsResponse.success 
        ? userRegsResponse.data.filter(r => r.registrationStatus === 'approved').map(r => r.registrationId)
        : []
      
      // Filter submissions by user's registrations
      const userSubmissions = response.data.filter(s => 
        userRegistrationIds.includes(s.registrationId)
      )
      
      // Enrich with registration and competition data
      submissions.value = await Promise.all(
        userSubmissions.map(async (submission) => {
          const registration = userRegsResponse.data.find(r => r.registrationId === submission.registrationId)
          return {
            ...submission,
            registration: registration,
            competition: registration?.competition
          }
        })
      )
    }
  } catch (error) {
    ElMessage.error('加载作品列表失败')
  } finally {
    loading.value = false
  }
}

const loadSystemTime = async () => {
  try {
    const response = await systemTimeAPI.getCurrentTime()
    if (response.success) {
      systemTime.value = new Date(response.data.currentTime)
    }
  } catch (error) {
    // If system time API fails, fallback to browser time
    console.warn('Failed to fetch system time, using browser time:', error)
    systemTime.value = new Date()
  }
}

// Get current time (use system time if available, otherwise browser time)
const getCurrentTime = () => {
  return systemTime.value || new Date()
}

// Check if submission is within time period
const isInSubmissionPeriod = (submission) => {
  if (!submission.competition) return true // Allow if competition not loaded
  
  const now = getCurrentTime()
  const submitStart = submission.competition.submitStart ? new Date(submission.competition.submitStart) : null
  const awardPublishStart = submission.competition.awardPublishStart ? new Date(submission.competition.awardPublishStart) : null
  
  if (submitStart && now < submitStart) return false
  if (awardPublishStart && now > awardPublishStart) return false
  
  return true
}

// Check if any submission for the same registration is locked
// A submission is locked when finalLockedAt is set (which also sets status to 'locked')
const hasLockedSubmission = (registrationId) => {
  return submissions.value.some(s => 
    s.registrationId === registrationId && s.finalLockedAt != null
  )
}

const loadApprovedRegistrations = async () => {
  try {
    const response = await registrationAPI.getRegistrationsByUser(authStore.user.userId)
    if (response.success) {
      // Filter approved registrations
      const approvedRegs = response.data.filter(r => r.registrationStatus === 'approved')
      
      // For each registration, check if it has a locked submission - use Promise.all for concurrent requests
      const submissionChecks = await Promise.all(
        approvedRegs.map(async (reg) => {
          try {
            const submissionsRes = await submissionAPI.getSubmissionsByRegistration(reg.registrationId)
            if (submissionsRes.success) {
              const hasLocked = submissionsRes.data.some(s => s.finalLockedAt != null || s.submissionStatus === 'locked')
              return { reg, hasLocked }
            }
            return { reg, hasLocked: false }
          } catch (error) {
            return { reg, hasLocked: false }
          }
        })
      )
      
      // Filter out registrations with locked submissions
      approvedRegistrations.value = submissionChecks
        .filter(({ hasLocked }) => !hasLocked)
        .map(({ reg }) => reg)
    }
  } catch (error) {
    console.error('加载报名列表失败', error)
  }
}

const createSubmission = async () => {
  try {
    const response = await submissionAPI.createSubmission(newSubmission.value)
    if (response.success) {
      ElMessage.success('作品创建成功')
      showCreateDialog.value = false
      await loadApprovedRegistrations()
      await loadSubmissions()
      newSubmission.value = { registrationId: null, submissionTitle: '', abstractText: '' }
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '创建作品失败')
  }
}

const editSubmission = (submission) => {
  editingSubmission.value = submission
  editSubmissionForm.value = {
    submissionTitle: submission.submissionTitle,
    abstractText: submission.abstractText
  }
  showEditDialog.value = true
}

const updateSubmission = async () => {
  try {
    const response = await submissionAPI.updateSubmission(
      editingSubmission.value.submissionId, 
      editSubmissionForm.value
    )
    if (response.success) {
      ElMessage.success('作品更新成功')
      showEditDialog.value = false
      loadSubmissions()
      editingSubmission.value = null
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '更新作品失败')
  }
}

const submitWork = async (id) => {
  try {
    const response = await submissionAPI.submitSubmission(id)
    if (response.success) {
      ElMessage.success('作品提交成功')
      loadSubmissions()
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '提交作品失败')
  }
}

const lockWork = async (id) => {
  try {
    const response = await submissionAPI.lockSubmission(id)
    if (response.success) {
      ElMessage.success('作品已最终锁定')
      await loadApprovedRegistrations()
      await loadSubmissions()
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '锁定作品失败')
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(async () => {
  await loadSystemTime()
  await loadApprovedRegistrations()
  await loadSubmissions()
})
</script>

<style scoped>
.my-submissions-container {
  max-width: 1400px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
