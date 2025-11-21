<template>
  <div class="teams-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的团队</span>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon>
            创建团队
          </el-button>
        </div>
      </template>

      <el-empty v-if="teams.length === 0" description="您还没有加入或创建任何团队">
        <el-button type="primary" @click="showCreateDialog">立即创建团队</el-button>
      </el-empty>

      <el-row :gutter="20" v-else>
        <el-col :span="8" v-for="team in teams" :key="team.teamId">
          <el-card class="team-card" shadow="hover">
            <template #header>
              <div class="team-card-header">
                <span class="team-name">{{ team.teamName }}</span>
                <el-tag v-if="isTeamLeader(team)" type="warning" size="small">队长</el-tag>
              </div>
            </template>
            
            <div class="team-info">
              <p><strong>创建时间：</strong>{{ formatDate(team.formedAt) }}</p>
              <p v-if="team.description"><strong>简介：</strong>{{ team.description }}</p>
              <p><strong>成员数：</strong>{{ getTeamMemberCount(team.teamId) }}</p>
            </div>

            <div class="team-actions">
              <el-button type="primary" size="small" @click="viewTeamDetail(team)">
                查看详情
              </el-button>
              <el-button 
                v-if="isTeamLeader(team)" 
                type="warning" 
                size="small" 
                @click="showEditDialog(team)"
              >
                编辑
              </el-button>
              <el-popconfirm
                v-if="isTeamLeader(team)"
                title="确定解散该团队吗？"
                @confirm="deleteTeam(team.teamId)"
              >
                <template #reference>
                  <el-button type="danger" size="small">解散</el-button>
                </template>
              </el-popconfirm>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- Create/Edit Team Dialog -->
    <el-dialog 
      v-model="teamDialogVisible" 
      :title="isEditing ? '编辑团队' : '创建团队'" 
      width="600px"
    >
      <el-form :model="teamForm" :rules="teamRules" ref="teamFormRef" label-width="100px">
        <el-form-item label="团队名称" prop="teamName">
          <el-input v-model="teamForm.teamName" placeholder="请输入团队名称" />
        </el-form-item>
        <el-form-item label="团队简介" prop="description">
          <el-input 
            v-model="teamForm.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入团队简介（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="teamDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitTeamForm" :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Team Detail Dialog -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="团队详情" 
      width="700px"
    >
      <div v-if="selectedTeam">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="团队名称">
            {{ selectedTeam.teamName }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDate(selectedTeam.formedAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="团队简介" v-if="selectedTeam.description">
            {{ selectedTeam.description }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <div class="members-section">
          <div class="section-header">
            <h3>团队成员</h3>
            <el-button 
              v-if="isTeamLeader(selectedTeam)" 
              type="primary" 
              size="small" 
              @click="showAddMemberDialog"
            >
              添加成员
            </el-button>
          </div>

          <el-table :data="currentTeamMembers" style="width: 100%; margin-top: 20px">
            <el-table-column label="用户ID" prop="userId" width="100" />
            <el-table-column label="角色" prop="roleInTeam" width="120">
              <template #default="{ row }">
                <el-tag v-if="row.roleInTeam === 'leader'" type="warning">队长</el-tag>
                <el-tag v-else>成员</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" v-if="isTeamLeader(selectedTeam)">
              <template #default="{ row }">
                <el-popconfirm
                  v-if="row.roleInTeam !== 'leader'"
                  title="确定移除该成员吗？"
                  @confirm="removeMember(selectedTeam.teamId, row.userId)"
                >
                  <template #reference>
                    <el-button type="danger" size="small">移除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>

    <!-- Add Member Dialog -->
    <el-dialog v-model="addMemberDialogVisible" title="添加成员" width="500px">
      <el-form :model="memberForm" :rules="memberRules" ref="memberFormRef" label-width="100px">
        <el-form-item label="用户ID" prop="userId">
          <el-input-number v-model="memberForm.userId" :min="1" placeholder="请输入要添加的用户ID" />
        </el-form-item>
        <el-form-item label="角色" prop="roleInTeam">
          <el-radio-group v-model="memberForm.roleInTeam">
            <el-radio label="member">成员</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addMemberDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAddMember" :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '../stores/auth'
import { teamAPI } from '../api'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const authStore = useAuthStore()
const teams = ref([])
const loading = ref(false)
const submitting = ref(false)
const teamDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const addMemberDialogVisible = ref(false)
const isEditing = ref(false)
const selectedTeam = ref(null)
const teamFormRef = ref(null)
const memberFormRef = ref(null)
const teamMembers = ref({}) // Map of teamId -> members array
const currentTeamMembers = ref([])

const teamForm = ref({
  teamName: '',
  description: ''
})

const memberForm = ref({
  userId: null,
  roleInTeam: 'member'
})

const teamRules = {
  teamName: [
    { required: true, message: '请输入团队名称', trigger: 'blur' }
  ]
}

const memberRules = {
  userId: [
    { required: true, message: '请输入用户ID', trigger: 'blur' }
  ]
}

const loadTeams = async () => {
  loading.value = true
  try {
    const response = await teamAPI.getTeamsByUser(authStore.user.userId)
    if (response.success) {
      teams.value = response.data
      // Load members for each team
      for (const team of teams.value) {
        await loadTeamMembers(team.teamId)
      }
    }
  } catch (error) {
    ElMessage.error('加载团队列表失败')
  } finally {
    loading.value = false
  }
}

const loadTeamMembers = async (teamId) => {
  try {
    const response = await teamAPI.getTeamMembers(teamId)
    if (response.success) {
      teamMembers.value[teamId] = response.data
    }
  } catch (error) {
    console.error('Failed to load team members:', error)
  }
}

const getTeamMemberCount = (teamId) => {
  return teamMembers.value[teamId]?.length || 0
}

const isTeamLeader = (team) => {
  const members = teamMembers.value[team.teamId]
  if (!members) return false
  const currentUserMembership = members.find(m => m.userId === authStore.user.userId)
  return currentUserMembership?.roleInTeam === 'leader'
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const showCreateDialog = () => {
  isEditing.value = false
  teamForm.value = {
    teamName: '',
    description: ''
  }
  teamDialogVisible.value = true
}

const showEditDialog = (team) => {
  isEditing.value = true
  selectedTeam.value = team
  teamForm.value = {
    teamName: team.teamName,
    description: team.description || ''
  }
  teamDialogVisible.value = true
}

const submitTeamForm = async () => {
  if (!teamFormRef.value) return
  
  await teamFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isEditing.value) {
        const response = await teamAPI.updateTeam(selectedTeam.value.teamId, teamForm.value)
        if (response.success) {
          ElMessage.success('团队更新成功')
          teamDialogVisible.value = false
          loadTeams()
        }
      } else {
        const response = await teamAPI.createTeam(teamForm.value)
        if (response.success) {
          ElMessage.success('团队创建成功')
          teamDialogVisible.value = false
          loadTeams()
        }
      }
    } catch (error) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

const deleteTeam = async (teamId) => {
  try {
    const response = await teamAPI.deleteTeam(teamId)
    if (response.success) {
      ElMessage.success('团队已解散')
      loadTeams()
    }
  } catch (error) {
    ElMessage.error(error.message || '解散团队失败')
  }
}

const viewTeamDetail = async (team) => {
  selectedTeam.value = team
  currentTeamMembers.value = teamMembers.value[team.teamId] || []
  detailDialogVisible.value = true
}

const showAddMemberDialog = () => {
  memberForm.value = {
    userId: null,
    roleInTeam: 'member'
  }
  addMemberDialogVisible.value = true
}

const submitAddMember = async () => {
  if (!memberFormRef.value) return
  
  await memberFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const response = await teamAPI.addTeamMember(selectedTeam.value.teamId, {
        userId: memberForm.value.userId,
        roleInTeam: memberForm.value.roleInTeam
      })
      
      if (response.success) {
        ElMessage.success('成员添加成功')
        addMemberDialogVisible.value = false
        await loadTeamMembers(selectedTeam.value.teamId)
        currentTeamMembers.value = teamMembers.value[selectedTeam.value.teamId] || []
      }
    } catch (error) {
      ElMessage.error(error.message || '添加成员失败')
    } finally {
      submitting.value = false
    }
  })
}

const removeMember = async (teamId, userId) => {
  try {
    const response = await teamAPI.removeTeamMember(teamId, userId)
    if (response.success) {
      ElMessage.success('成员已移除')
      await loadTeamMembers(teamId)
      currentTeamMembers.value = teamMembers.value[teamId] || []
    }
  } catch (error) {
    ElMessage.error(error.message || '移除成员失败')
  }
}

onMounted(() => {
  loadTeams()
})
</script>

<style scoped>
.teams-container {
  max-width: 1400px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.team-card {
  margin-bottom: 20px;
}

.team-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.team-name {
  font-size: 16px;
  font-weight: bold;
  color: #409eff;
}

.team-info {
  margin-bottom: 15px;
}

.team-info p {
  margin: 8px 0;
  color: #606266;
}

.team-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-start;
}

.members-section {
  margin-top: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-header h3 {
  margin: 0;
  color: #303133;
}
</style>
