<template>
  <div class="apply-competition-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-icon size="24"><DocumentAdd /></el-icon>
          <span>报名参赛</span>
        </div>
      </template>

      <el-steps :active="activeStep" finish-status="success" align-center style="margin-bottom: 30px">
        <el-step title="选择竞赛" />
        <el-step title="填写信息" />
        <el-step title="确认提交" />
      </el-steps>

      <!-- Step 1: Select Competition -->
      <div v-show="activeStep === 0" class="step-content">
        <h3>请选择要报名的竞赛</h3>
        <el-table 
          :data="availableCompetitions" 
          v-loading="loading" 
          @row-click="selectCompetition"
          highlight-current-row
          style="width: 100%; margin-top: 20px"
        >
          <el-table-column prop="competitionTitle" label="竞赛名称" min-width="200" />
          <el-table-column prop="level" label="级别" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.level === 'school'">校级</el-tag>
              <el-tag v-else-if="row.level === 'provincial'" type="success">省级</el-tag>
              <el-tag v-else-if="row.level === 'national'" type="warning">国家级</el-tag>
              <el-tag v-else-if="row.level === 'international'" type="danger">国际级</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="signupEnd" label="报名截止" width="180">
            <template #default="{ row }">
              {{ formatDate(row.signupEnd) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click.stop="selectCompetition(row)">
                选择
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Step 2: Fill in Registration Form -->
      <div v-show="activeStep === 1" class="step-content">
        <h3>填写报名信息</h3>
        <div v-if="selectedCompetition" class="competition-info">
          <el-alert title="竞赛信息" type="info" :closable="false" style="margin-bottom: 20px">
            <p><strong>竞赛名称：</strong>{{ selectedCompetition.competitionTitle }}</p>
            <p><strong>竞赛级别：</strong>{{ getLevelText(selectedCompetition.level) }}</p>
            <p><strong>最大团队人数：</strong>{{ selectedCompetition.maxTeamSize || '不限' }}</p>
          </el-alert>
        </div>
        
        <el-form :model="registrationForm" :rules="rules" ref="registrationFormRef" label-width="120px">
          <el-form-item label="参赛形式" prop="participationType">
            <el-radio-group v-model="registrationForm.participationType">
              <el-radio label="individual">个人参赛</el-radio>
              <el-radio label="team">团队参赛</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item v-if="registrationForm.participationType === 'team'" label="团队名称" prop="teamName">
            <el-input v-model="registrationForm.teamName" placeholder="请输入团队名称" />
          </el-form-item>
          
          <el-form-item label="备注信息" prop="remark">
            <el-input 
              v-model="registrationForm.remark" 
              type="textarea" 
              :rows="4" 
              placeholder="请填写其他需要说明的信息"
            />
          </el-form-item>
        </el-form>

        <div class="step-buttons">
          <el-button @click="activeStep = 0">上一步</el-button>
          <el-button type="primary" @click="goToConfirm">下一步</el-button>
        </div>
      </div>

      <!-- Step 3: Confirm and Submit -->
      <div v-show="activeStep === 2" class="step-content">
        <h3>确认报名信息</h3>
        <el-descriptions :column="1" border style="margin-top: 20px">
          <el-descriptions-item label="竞赛名称">
            {{ selectedCompetition?.competitionTitle }}
          </el-descriptions-item>
          <el-descriptions-item label="参赛形式">
            {{ registrationForm.participationType === 'individual' ? '个人参赛' : '团队参赛' }}
          </el-descriptions-item>
          <el-descriptions-item v-if="registrationForm.participationType === 'team'" label="团队名称">
            {{ registrationForm.teamName }}
          </el-descriptions-item>
          <el-descriptions-item label="备注信息">
            {{ registrationForm.remark || '无' }}
          </el-descriptions-item>
        </el-descriptions>

        <el-alert 
          title="提示" 
          type="warning" 
          :closable="false" 
          style="margin-top: 20px"
        >
          <p>报名提交后需要管理员审核，审核通过后方可参赛。请确认信息无误后提交。</p>
        </el-alert>

        <div class="step-buttons">
          <el-button @click="activeStep = 1">上一步</el-button>
          <el-button type="primary" @click="submitRegistration" :loading="submitting">
            提交报名
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { competitionAPI, registrationAPI } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const activeStep = ref(0)
const loading = ref(false)
const submitting = ref(false)
const availableCompetitions = ref([])
const selectedCompetition = ref(null)
const registrationFormRef = ref(null)

const registrationForm = ref({
  participationType: 'individual',
  teamName: '',
  remark: ''
})

const rules = {
  participationType: [
    { required: true, message: '请选择参赛形式', trigger: 'change' }
  ],
  teamName: [
    { required: true, message: '请输入团队名称', trigger: 'blur' }
  ]
}

const loadAvailableCompetitions = async () => {
  loading.value = true
  try {
    const response = await competitionAPI.getAllCompetitions()
    if (response.success) {
      // Filter competitions that are in signup period
      const now = new Date()
      availableCompetitions.value = response.data.filter(comp => {
        if (!comp.signupStart || !comp.signupEnd) return false
        const signupStart = new Date(comp.signupStart)
        const signupEnd = new Date(comp.signupEnd)
        return !isNaN(signupStart.getTime()) && !isNaN(signupEnd.getTime()) &&
               now >= signupStart && now <= signupEnd && 
               (comp.competitionStatus === 'published' || comp.competitionStatus === 'ongoing')
      })
    }
  } catch (error) {
    ElMessage.error('加载竞赛列表失败')
  } finally {
    loading.value = false
  }
}

const selectCompetition = (competition) => {
  selectedCompetition.value = competition
  activeStep.value = 1
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const getLevelText = (level) => {
  const levelMap = {
    'school': '校级',
    'provincial': '省级',
    'national': '国家级',
    'international': '国际级'
  }
  return levelMap[level] || level
}

const goToConfirm = async () => {
  if (!registrationFormRef.value) {
    activeStep.value = 2
    return
  }
  
  await registrationFormRef.value.validate((valid) => {
    if (valid) {
      activeStep.value = 2
    }
  })
}

const submitRegistration = async () => {
  submitting.value = true
  try {
    const registrationData = {
      competitionId: selectedCompetition.value.competitionId,
      userId: authStore.user.userId,
      remark: registrationForm.value.remark
    }
    
    // Add teamName if it's team registration
    if (registrationForm.value.participationType === 'team') {
      registrationData.teamName = registrationForm.value.teamName
    }
    
    const response = await registrationAPI.createRegistration(registrationData)
    
    if (response.success) {
      ElMessage.success('报名成功！等待管理员审核')
      router.push('/my-registrations')
    } else {
      ElMessage.error(response.message || '报名失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '报名失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadAvailableCompetitions()
})
</script>

<style scoped>
.apply-competition-container {
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: bold;
}

.step-content {
  min-height: 400px;
  padding: 20px 0;
}

.step-content h3 {
  margin-bottom: 20px;
  color: #409eff;
}

.competition-info {
  margin-bottom: 20px;
}

.competition-info p {
  margin: 8px 0;
}

.step-buttons {
  margin-top: 30px;
  text-align: right;
}

.step-buttons .el-button {
  margin-left: 10px;
}

:deep(.el-table__row) {
  cursor: pointer;
}
</style>
