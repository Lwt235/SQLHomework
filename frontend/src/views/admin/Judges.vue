<template>
  <div class="admin-judges">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>评审管理</span>
          <div>
            <el-button type="primary" @click="showManualAssignDialog = true">手动分配评审</el-button>
            <el-button type="success" @click="showRandomAssignDialog = true">一键随机分配</el-button>
          </div>
        </div>
      </template>

      <el-table :data="assignments" v-loading="loading" style="width: 100%">
        <el-table-column label="评委" width="120">
          <template #default="{ row }">
            {{ row.user?.realName || row.user?.username || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="作品" min-width="200">
          <template #default="{ row }">
            {{ row.submission?.submissionTitle || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="weight" label="权重" width="100" />
        <el-table-column prop="score" label="评分" width="100" />
        <el-table-column prop="comment" label="评语" min-width="200" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Manual Assignment Dialog -->
    <el-dialog v-model="showManualAssignDialog" title="手动分配评审" width="600px">
      <el-form :model="manualAssignForm" label-width="120px">
        <el-form-item label="选择作品">
          <el-select 
            v-model="manualAssignForm.submissionId" 
            placeholder="请选择已锁定的作品"
            filterable
            style="width: 100%"
          >
            <el-option 
              v-for="submission in lockedSubmissions" 
              :key="submission.submissionId" 
              :label="submission.submissionTitle" 
              :value="submission.submissionId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择评审教师">
          <el-select 
            v-model="manualAssignForm.judgeUserId" 
            placeholder="请选择教师"
            filterable
            style="width: 100%"
          >
            <el-option 
              v-for="teacher in teachers" 
              :key="teacher.userId" 
              :label="`${teacher.realName || teacher.username} (ID: ${teacher.userId})`" 
              :value="teacher.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="权重">
          <el-input-number 
            v-model="manualAssignForm.weight" 
            :min="0" 
            :max="1" 
            :step="0.1" 
            :precision="2"
            placeholder="评审权重"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showManualAssignDialog = false">取消</el-button>
        <el-button type="primary" @click="submitManualAssign" :loading="assigning">确定分配</el-button>
      </template>
    </el-dialog>

    <!-- Random Assignment Dialog -->
    <el-dialog v-model="showRandomAssignDialog" title="一键随机分配" width="700px">
      <el-alert 
        title="随机分配说明" 
        type="info" 
        :closable="false"
        style="margin-bottom: 20px"
      >
        <p>系统将为指定作品随机分配指定数量的评审教师</p>
        <p>如果作品已有部分评审，将自动补齐至指定数量</p>
        <p>可以选择特定作品和筛选教师范围</p>
      </el-alert>
      <el-form label-width="150px">
        <el-form-item label="选择作品">
          <el-select 
            v-model="randomAssignForm.submissionId" 
            placeholder="留空则为所有锁定作品分配"
            clearable
            filterable
            style="width: 100%"
          >
            <el-option label="所有锁定作品" :value="null" />
            <el-option 
              v-for="submission in lockedSubmissions" 
              :key="submission.submissionId" 
              :label="submission.submissionTitle" 
              :value="submission.submissionId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="每个作品评审数量">
          <el-input-number 
            v-model="randomAssignForm.judgesPerSubmission" 
            :min="1" 
            :max="10"
            placeholder="评审数量"
          />
        </el-form-item>
        <el-form-item label="教师筛选模式">
          <el-radio-group v-model="randomAssignForm.filterMode">
            <el-radio label="all">所有教师</el-radio>
            <el-radio label="include">仅包含</el-radio>
            <el-radio label="exclude">排除</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item 
          v-if="randomAssignForm.filterMode === 'include'" 
          label="选择包含的教师"
        >
          <el-select 
            v-model="randomAssignForm.includeTeacherIds" 
            multiple
            filterable
            placeholder="选择要包含的教师"
            style="width: 100%"
          >
            <el-option 
              v-for="teacher in teachers" 
              :key="teacher.userId" 
              :label="`${teacher.realName || teacher.username} (ID: ${teacher.userId})`" 
              :value="teacher.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item 
          v-if="randomAssignForm.filterMode === 'exclude'" 
          label="选择排除的教师"
        >
          <el-select 
            v-model="randomAssignForm.excludeTeacherIds" 
            multiple
            filterable
            placeholder="选择要排除的教师"
            style="width: 100%"
          >
            <el-option 
              v-for="teacher in teachers" 
              :key="teacher.userId" 
              :label="`${teacher.realName || teacher.username} (ID: ${teacher.userId})`" 
              :value="teacher.userId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRandomAssignDialog = false">取消</el-button>
        <el-button type="success" @click="submitRandomAssign" :loading="assigning">开始分配</el-button>
      </template>
    </el-dialog>
    
    <!-- View Detail Dialog -->
    <el-dialog v-model="showDetailDialog" title="评审详情" width="700px">
      <div v-if="selectedAssignment">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="评委">
            {{ selectedAssignment.user?.realName || selectedAssignment.user?.username || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="作品标题">
            {{ selectedAssignment.submission?.submissionTitle || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="竞赛名称">
            {{ selectedAssignment.submission?.competition?.competitionTitle || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="作品描述">
            {{ selectedAssignment.submission?.description || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="权重">
            {{ selectedAssignment.weight || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="评分">
            <el-tag v-if="selectedAssignment.score != null" type="success">
              {{ selectedAssignment.score }}
            </el-tag>
            <span v-else>未评分</span>
          </el-descriptions-item>
          <el-descriptions-item label="评语">
            {{ selectedAssignment.comment || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="评审状态">
            <el-tag v-if="selectedAssignment.judgeStatus === 'completed'" type="success">
              已确认
            </el-tag>
            <el-tag v-else-if="selectedAssignment.judgeStatus === 'reviewed'" type="warning">
              已评分
            </el-tag>
            <el-tag v-else type="info">待评审</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDate(selectedAssignment.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDate(selectedAssignment.updatedAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { judgeAPI, submissionAPI, userAPI } from '../../api'
import { ElMessage } from 'element-plus'

const assignments = ref([])
const loading = ref(false)
const showManualAssignDialog = ref(false)
const showRandomAssignDialog = ref(false)
const showDetailDialog = ref(false)
const assigning = ref(false)
const lockedSubmissions = ref([])
const teachers = ref([])
const selectedAssignment = ref(null)

const manualAssignForm = ref({
  submissionId: null,
  judgeUserId: null,
  weight: 1.0
})

const randomAssignForm = ref({
  submissionId: null,
  judgesPerSubmission: 3,
  filterMode: 'all',
  includeTeacherIds: [],
  excludeTeacherIds: []
})

const loadAssignments = async () => {
  loading.value = true
  try {
    const response = await judgeAPI.getAllAssignmentsWithDetails()
    if (response.success) {
      assignments.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载评审列表失败')
  } finally {
    loading.value = false
  }
}

const loadLockedSubmissions = async () => {
  try {
    const response = await submissionAPI.getAllSubmissions()
    if (response.success) {
      lockedSubmissions.value = response.data.filter(s => s.submissionStatus === 'locked')
    }
  } catch (error) {
    console.error('加载作品列表失败', error)
  }
}

const loadTeachers = async () => {
  try {
    const response = await userAPI.getAllUsers()
    if (response.success) {
      // Filter to only include teachers
      // Note: This makes N+1 API calls. In production, consider creating a 
      // dedicated backend endpoint that returns users with their roles in a single call
      const allRolesResponse = await userAPI.getAllRoles()
      const teacherRole = allRolesResponse.data.find(r => r.roleCode === 'TEACHER')
      
      if (teacherRole) {
        const teacherUserIds = []
        for (const user of response.data) {
          const rolesResponse = await userAPI.getUserRoles(user.userId)
          if (rolesResponse.success && rolesResponse.data.some(r => r.roleCode === 'TEACHER')) {
            teacherUserIds.push(user.userId)
          }
        }
        teachers.value = response.data.filter(u => teacherUserIds.includes(u.userId))
      }
    }
  } catch (error) {
    console.error('加载教师列表失败', error)
  }
}

const submitManualAssign = async () => {
  if (!manualAssignForm.value.submissionId || !manualAssignForm.value.judgeUserId) {
    ElMessage.error('请选择作品和评审教师')
    return
  }
  
  assigning.value = true
  try {
    const response = await judgeAPI.manualAssignJudge({
      submissionId: manualAssignForm.value.submissionId,
      judgeUserId: manualAssignForm.value.judgeUserId,
      weight: manualAssignForm.value.weight
    })
    
    if (response.success) {
      ElMessage.success('评审分配成功')
      showManualAssignDialog.value = false
      manualAssignForm.value = { submissionId: null, judgeUserId: null, weight: 1.0 }
      await loadAssignments()
    } else {
      ElMessage.error(response.message || '评审分配失败')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '评审分配失败')
  } finally {
    assigning.value = false
  }
}

const submitRandomAssign = async () => {
  if (!randomAssignForm.value.judgesPerSubmission || randomAssignForm.value.judgesPerSubmission <= 0) {
    ElMessage.error('请输入有效的评审数量')
    return
  }
  
  assigning.value = true
  try {
    // Build payload based on filter mode
    const payload = {
      judgesPerSubmission: randomAssignForm.value.judgesPerSubmission,
      submissionId: randomAssignForm.value.submissionId
    }
    
    if (randomAssignForm.value.filterMode === 'include' && randomAssignForm.value.includeTeacherIds.length > 0) {
      payload.includeTeacherIds = randomAssignForm.value.includeTeacherIds
    } else if (randomAssignForm.value.filterMode === 'exclude' && randomAssignForm.value.excludeTeacherIds.length > 0) {
      payload.excludeTeacherIds = randomAssignForm.value.excludeTeacherIds
    }
    
    const response = await judgeAPI.randomAssignJudges(payload)
    
    if (response.success) {
      ElMessage.success(response.message || '随机分配成功')
      showRandomAssignDialog.value = false
      await loadAssignments()
    } else {
      ElMessage.error(response.message || '随机分配失败')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '随机分配失败')
  } finally {
    assigning.value = false
  }
}

const viewDetail = (assignment) => {
  selectedAssignment.value = assignment
  showDetailDialog.value = true
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(async () => {
  await loadAssignments()
  await loadLockedSubmissions()
  await loadTeachers()
})
</script>

<style scoped>
.admin-judges {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
