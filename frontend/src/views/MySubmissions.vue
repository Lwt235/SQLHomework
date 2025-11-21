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
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button size="small" @click="editSubmission(row)">编辑</el-button>
            <el-button 
              v-if="row.submissionStatus === 'draft'" 
              type="primary" 
              size="small" 
              @click="submitWork(row.submissionId)"
            >
              提交作品
            </el-button>
            <el-button 
              v-if="row.submissionStatus === 'submitted'" 
              type="warning" 
              size="small" 
              @click="lockWork(row.submissionId)"
            >
              最终锁定
            </el-button>
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
import { submissionAPI, registrationAPI } from '../api'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const submissions = ref([])
const approvedRegistrations = ref([])
const loading = ref(false)
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const editingSubmission = ref(null)
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
      submissions.value = response.data.filter(s => 
        approvedRegistrations.value.some(r => r.registrationId === s.registrationId)
      )
    }
  } catch (error) {
    ElMessage.error('加载作品列表失败')
  } finally {
    loading.value = false
  }
}

const loadApprovedRegistrations = async () => {
  try {
    const response = await registrationAPI.getRegistrationsByUser(authStore.user.userId)
    if (response.success) {
      approvedRegistrations.value = response.data.filter(r => r.registrationStatus === 'approved')
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
      loadSubmissions()
      newSubmission.value = { registrationId: null, submissionTitle: '', abstractText: '' }
    }
  } catch (error) {
    ElMessage.error('创建作品失败')
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
    ElMessage.error('更新作品失败')
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
    ElMessage.error('提交作品失败')
  }
}

const lockWork = async (id) => {
  try {
    const response = await submissionAPI.lockSubmission(id)
    if (response.success) {
      ElMessage.success('作品已最终锁定')
      loadSubmissions()
    }
  } catch (error) {
    ElMessage.error('锁定作品失败')
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(async () => {
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
