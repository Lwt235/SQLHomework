<template>
  <div class="competitions-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>竞赛列表</span>
          <el-button v-if="authStore.isAdmin" type="primary" @click="$router.push('/admin/competitions')">
            管理竞赛
          </el-button>
        </div>
      </template>
      
      <el-space wrap style="margin-bottom: 20px">
        <el-select v-model="statusFilter" placeholder="筛选状态" clearable @change="loadCompetitions">
          <el-option label="全部" value="" />
          <el-option label="草稿" value="draft" />
          <el-option label="已发布" value="published" />
          <el-option label="进行中" value="ongoing" />
          <el-option label="已结束" value="finished" />
        </el-select>
      </el-space>

      <el-table :data="competitions" v-loading="loading" style="width: 100%">
        <el-table-column prop="competitionId" label="ID" width="80" />
        <el-table-column prop="competitionTitle" label="竞赛名称" min-width="200" />
        <el-table-column prop="shortTitle" label="简称" width="120" />
        <el-table-column prop="level" label="级别" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.level === 'school'">校级</el-tag>
            <el-tag v-else-if="row.level === 'provincial'" type="success">省级</el-tag>
            <el-tag v-else-if="row.level === 'national'" type="warning">国家级</el-tag>
            <el-tag v-else-if="row.level === 'international'" type="danger">国际级</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="competitionStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.competitionStatus === 'draft'" type="info">草稿</el-tag>
            <el-tag v-else-if="row.competitionStatus === 'published'">已发布</el-tag>
            <el-tag v-else-if="row.competitionStatus === 'ongoing'" type="success">进行中</el-tag>
            <el-tag v-else-if="row.competitionStatus === 'finished'" type="warning">已结束</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="signupStart" label="报名开始" width="180">
          <template #default="{ row }">
            {{ formatDate(row.signupStart) }}
          </template>
        </el-table-column>
        <el-table-column prop="signupEnd" label="报名截止" width="180">
          <template #default="{ row }">
            {{ formatDate(row.signupEnd) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewDetail(row.competitionId)">
              查看详情
            </el-button>
            <el-button v-if="authStore.isStudent && canApply(row)" type="success" size="small" @click="navigateToApply(row.competitionId)">
              报名
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
import { competitionAPI, registrationAPI } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const competitions = ref([])
const loading = ref(false)
const statusFilter = ref('')

const loadCompetitions = async () => {
  loading.value = true
  try {
    const response = statusFilter.value 
      ? await competitionAPI.getCompetitionsByStatus(statusFilter.value)
      : await competitionAPI.getAllCompetitions()
    
    if (response.success) {
      competitions.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载竞赛列表失败')
  } finally {
    loading.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const canApply = (competition) => {
  const now = new Date()
  const signupStart = new Date(competition.signupStart)
  const signupEnd = new Date(competition.signupEnd)
  return now >= signupStart && now <= signupEnd && 
         (competition.competitionStatus === 'published' || competition.competitionStatus === 'ongoing')
}

const viewDetail = (id) => {
  router.push(`/competitions/${id}`)
}

const navigateToApply = (competitionId) => {
  router.push({
    name: 'ApplyCompetition',
    query: { competitionId }
  })
}

onMounted(() => {
  loadCompetitions()
})
</script>

<style scoped>
.competitions-container {
  max-width: 1400px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
