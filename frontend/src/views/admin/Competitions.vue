<template>
  <div class="admin-competitions">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>竞赛管理</span>
          <el-button type="primary" @click="showCreateDialog = true">创建竞赛</el-button>
        </div>
      </template>

      <el-table :data="competitions" v-loading="loading" style="width: 100%">
        <el-table-column prop="competitionId" label="ID" width="80" />
        <el-table-column prop="competitionTitle" label="竞赛名称" min-width="200" />
        <el-table-column prop="competitionStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.competitionStatus === 'draft'" type="info">草稿</el-tag>
            <el-tag v-else-if="row.competitionStatus === 'published'">已发布</el-tag>
            <el-tag v-else-if="row.competitionStatus === 'ongoing'" type="success">进行中</el-tag>
            <el-tag v-else-if="row.competitionStatus === 'finished'" type="warning">已结束</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="editCompetition(row)">编辑</el-button>
            <el-popconfirm
              title="确定删除该竞赛吗？"
              @confirm="deleteCompetition(row.competitionId)"
            >
              <template #reference>
                <el-button type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="showCreateDialog" :title="editingCompetitionId ? '编辑竞赛' : '创建竞赛'" width="800px">
      <el-form :model="competitionForm" :rules="competitionRules" ref="competitionFormRef" label-width="120px">
        <el-form-item label="竞赛名称" prop="competitionTitle">
          <el-input v-model="competitionForm.competitionTitle" placeholder="请输入竞赛名称" />
        </el-form-item>
        <el-form-item label="简称" prop="shortTitle">
          <el-input v-model="competitionForm.shortTitle" placeholder="请输入竞赛简称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="competitionForm.description" type="textarea" :rows="4" placeholder="请输入竞赛描述" />
        </el-form-item>
        <el-form-item label="类别" prop="category">
          <el-input v-model="competitionForm.category" placeholder="例如：程序设计、创新创业等" />
        </el-form-item>
        <el-form-item label="级别" prop="level">
          <el-select v-model="competitionForm.level" style="width: 100%">
            <el-option label="校级" value="school" />
            <el-option label="省级" value="provincial" />
            <el-option label="国家级" value="national" />
            <el-option label="国际级" value="international" />
          </el-select>
        </el-form-item>
        <el-form-item label="主办方" prop="organizer">
          <el-input v-model="competitionForm.organizer" placeholder="请输入主办单位" />
        </el-form-item>
        <el-form-item label="竞赛状态" prop="competitionStatus">
          <el-select v-model="competitionForm.competitionStatus" style="width: 100%">
            <el-option label="草稿" value="draft" />
            <el-option label="已发布" value="published" />
            <el-option label="进行中" value="ongoing" />
            <el-option label="已结束" value="finished" />
          </el-select>
        </el-form-item>
        <el-form-item label="竞赛开始时间" prop="startDate">
          <el-date-picker 
            v-model="competitionForm.startDate" 
            type="datetime" 
            placeholder="选择竞赛开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="竞赛结束时间" prop="endDate">
          <el-date-picker 
            v-model="competitionForm.endDate" 
            type="datetime" 
            placeholder="选择竞赛结束时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="报名开始时间" prop="signupStart">
          <el-date-picker 
            v-model="competitionForm.signupStart" 
            type="datetime" 
            placeholder="选择报名开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="报名截止时间" prop="signupEnd">
          <el-date-picker 
            v-model="competitionForm.signupEnd" 
            type="datetime" 
            placeholder="选择报名截止时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="作品提交开始" prop="submitStart">
          <el-date-picker 
            v-model="competitionForm.submitStart" 
            type="datetime" 
            placeholder="选择作品提交开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="作品提交截止" prop="submitEnd">
          <el-date-picker 
            v-model="competitionForm.submitEnd" 
            type="datetime" 
            placeholder="选择作品提交截止时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="最大团队人数" prop="maxTeamSize">
          <el-input-number v-model="competitionForm.maxTeamSize" :min="1" :max="20" />
          <span style="margin-left: 10px; color: #909399;">个人参赛设为1</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false; editingCompetitionId = null">取消</el-button>
        <el-button type="primary" @click="saveCompetition" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { competitionAPI } from '../../api'
import { ElMessage } from 'element-plus'

const competitions = ref([])
const loading = ref(false)
const saving = ref(false)
const showCreateDialog = ref(false)
const competitionFormRef = ref(null)

const getInitialCompetitionForm = () => ({
  competitionTitle: '',
  shortTitle: '',
  description: '',
  category: '',
  level: 'school',
  organizer: '',
  competitionStatus: 'draft',
  startDate: null,
  endDate: null,
  signupStart: null,
  signupEnd: null,
  submitStart: null,
  submitEnd: null,
  maxTeamSize: 5
})

const competitionForm = ref(getInitialCompetitionForm())
const editingCompetitionId = ref(null)

const competitionRules = {
  competitionTitle: [
    { required: true, message: '请输入竞赛名称', trigger: 'blur' }
  ],
  shortTitle: [
    { required: true, message: '请输入竞赛简称', trigger: 'blur' }
  ],
  level: [
    { required: true, message: '请选择竞赛级别', trigger: 'change' }
  ],
  organizer: [
    { required: true, message: '请输入主办方', trigger: 'blur' }
  ],
  signupStart: [
    { required: true, message: '请选择报名开始时间', trigger: 'change' }
  ],
  signupEnd: [
    { required: true, message: '请选择报名截止时间', trigger: 'change' }
  ],
  submitStart: [
    { required: true, message: '请选择作品提交开始时间', trigger: 'change' }
  ],
  submitEnd: [
    { required: true, message: '请选择作品提交截止时间', trigger: 'change' }
  ]
}

const loadCompetitions = async () => {
  loading.value = true
  try {
    const response = await competitionAPI.getAllCompetitions()
    if (response.success) {
      competitions.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载竞赛列表失败')
  } finally {
    loading.value = false
  }
}

const saveCompetition = async () => {
  if (!competitionFormRef.value) return
  
  await competitionFormRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        let response
        if (editingCompetitionId.value) {
          // Update existing competition
          response = await competitionAPI.updateCompetition(editingCompetitionId.value, competitionForm.value)
        } else {
          // Create new competition
          response = await competitionAPI.createCompetition(competitionForm.value)
        }
        
        if (response.success) {
          ElMessage.success(editingCompetitionId.value ? '竞赛更新成功' : '竞赛创建成功')
          showCreateDialog.value = false
          loadCompetitions()
          // Reset form
          competitionForm.value = getInitialCompetitionForm()
          editingCompetitionId.value = null
        } else {
          ElMessage.error(response.message || '保存竞赛失败')
        }
      } catch (error) {
        ElMessage.error(error.message || '保存竞赛失败')
      } finally {
        saving.value = false
      }
    }
  })
}

const editCompetition = (competition) => {
  // Populate form with competition data
  competitionForm.value = {
    competitionTitle: competition.competitionTitle,
    shortTitle: competition.shortTitle,
    description: competition.description,
    category: competition.category,
    level: competition.level,
    organizer: competition.organizer,
    competitionStatus: competition.competitionStatus,
    startDate: competition.startDate ? new Date(competition.startDate) : null,
    endDate: competition.endDate ? new Date(competition.endDate) : null,
    signupStart: competition.signupStart ? new Date(competition.signupStart) : null,
    signupEnd: competition.signupEnd ? new Date(competition.signupEnd) : null,
    submitStart: competition.submitStart ? new Date(competition.submitStart) : null,
    submitEnd: competition.submitEnd ? new Date(competition.submitEnd) : null,
    maxTeamSize: competition.maxTeamSize || 5
  }
  editingCompetitionId.value = competition.competitionId
  showCreateDialog.value = true
}

const deleteCompetition = async (id) => {
  try {
    const response = await competitionAPI.deleteCompetition(id)
    if (response.success) {
      ElMessage.success('竞赛已删除')
      loadCompetitions()
    }
  } catch (error) {
    ElMessage.error('删除竞赛失败')
  }
}

onMounted(() => {
  loadCompetitions()
})
</script>

<style scoped>
.admin-competitions {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
