<template>
  <div class="teacher-review-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-icon size="24"><Edit /></el-icon>
          <span>评审中心</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" type="border-card">
        <!-- My Assignments Tab -->
        <el-tab-pane label="我的评审任务" name="myAssignments">
          <el-alert 
            title="评审说明" 
            type="info" 
            :closable="false"
            style="margin-bottom: 20px"
          >
            <p>以下是分配给您的评审任务，请对每个作品进行评分和评语。</p>
          </el-alert>

          <el-table :data="myAssignments" v-loading="loading" style="width: 100%">
            <el-table-column prop="assignmentId" label="任务ID" width="80" />
            <el-table-column label="作品标题" min-width="200">
              <template #default="{ row }">
                {{ row.submission?.submissionTitle || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="竞赛名称" min-width="180">
              <template #default="{ row }">
                {{ row.submission?.competition?.competitionTitle || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="score" label="评分" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.score !== null && row.score !== undefined" type="success">
                  {{ row.score }}
                </el-tag>
                <el-tag v-else type="warning">未评分</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.score !== null && row.score !== undefined" type="success">
                  已完成
                </el-tag>
                <el-tag v-else type="info">待评审</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="openReviewDialog(row)">
                  {{ row.score !== null && row.score !== undefined ? '查看/修改' : '开始评审' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- Review History Tab -->
        <el-tab-pane label="评审历史" name="history">
          <el-table 
            :data="myAssignments.filter(a => a.score !== null && a.score !== undefined)" 
            v-loading="loading" 
            style="width: 100%"
          >
            <el-table-column prop="assignmentId" label="任务ID" width="80" />
            <el-table-column label="作品标题" min-width="200">
              <template #default="{ row }">
                {{ row.submission?.submissionTitle || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="score" label="评分" width="100">
              <template #default="{ row }">
                <el-tag type="success">{{ row.score }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="comment" label="评语" min-width="200" />
            <el-table-column label="评审时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.reviewDate) }}
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- Review Dialog -->
    <el-dialog 
      v-model="reviewDialogVisible" 
      title="评审作品" 
      width="700px"
      :close-on-click-modal="false"
    >
      <div v-if="currentAssignment">
        <el-descriptions :column="1" border style="margin-bottom: 20px">
          <el-descriptions-item label="作品标题">
            {{ currentAssignment.submission?.submissionTitle || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="竞赛名称">
            {{ currentAssignment.submission?.competition?.competitionTitle || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="作品描述">
            {{ currentAssignment.submission?.description || '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <el-form :model="reviewForm" :rules="reviewRules" ref="reviewFormRef" label-width="100px">
          <el-form-item label="评分" prop="score">
            <el-input-number 
              v-model="reviewForm.score" 
              :min="0" 
              :max="100" 
              :precision="2"
              placeholder="请输入0-100分"
              style="width: 200px"
            />
            <span style="margin-left: 10px; color: #909399;">满分100分</span>
          </el-form-item>
          <el-form-item label="评语" prop="comment">
            <el-input 
              v-model="reviewForm.comment" 
              type="textarea" 
              :rows="6"
              placeholder="请输入对作品的评价和建议"
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview" :loading="submitting">
          提交评审
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import { judgeAPI } from '../api'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const activeTab = ref('myAssignments')
const loading = ref(false)
const submitting = ref(false)
const myAssignments = ref([])
const reviewDialogVisible = ref(false)
const currentAssignment = ref(null)
const reviewFormRef = ref(null)

const reviewForm = ref({
  score: null,
  comment: ''
})

const reviewRules = {
  score: [
    { required: true, message: '请输入评分', trigger: 'blur' },
    { type: 'number', min: 0, max: 100, message: '评分必须在0-100之间', trigger: 'blur' }
  ],
  comment: [
    { required: true, message: '请输入评语', trigger: 'blur' },
    { min: 10, message: '评语至少需要10个字符', trigger: 'blur' }
  ]
}

const loadMyAssignments = async () => {
  loading.value = true
  try {
    const response = await judgeAPI.getAssignmentsByJudge(authStore.user?.userId)
    if (response.success) {
      myAssignments.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载评审任务失败')
  } finally {
    loading.value = false
  }
}

const openReviewDialog = (assignment) => {
  currentAssignment.value = assignment
  reviewForm.value = {
    score: assignment.score !== null ? assignment.score : null,
    comment: assignment.comment || ''
  }
  reviewDialogVisible.value = true
}

const submitReview = async () => {
  if (!reviewFormRef.value) return
  
  await reviewFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const response = await judgeAPI.updateAssignment({
          userId: currentAssignment.value.userId,
          submissionId: currentAssignment.value.submissionId,
          score: reviewForm.value.score,
          comment: reviewForm.value.comment,
          reviewDate: new Date().toISOString()
        })
        
        if (response.success) {
          ElMessage.success('评审提交成功')
          reviewDialogVisible.value = false
          loadMyAssignments()
        } else {
          ElMessage.error(response.message || '评审提交失败')
        }
      } catch (error) {
        ElMessage.error(error.message || '评审提交失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(() => {
  loadMyAssignments()
})
</script>

<style scoped>
.teacher-review-container {
  max-width: 1400px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: bold;
}
</style>
