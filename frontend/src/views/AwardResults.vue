<template>
  <div class="award-results-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <div>
            <h2>{{ competition.competitionTitle }}</h2>
            <p style="color: #909399; margin: 5px 0 0 0;">获奖公示</p>
          </div>
          <el-button 
            v-if="isAdmin" 
            type="primary" 
            @click="handleAutoDistribute"
            :loading="distributing"
          >
            自动颁奖
          </el-button>
        </div>
      </template>

      <el-alert
        v-if="awardResults.length === 0 && !loading"
        title="暂无获奖结果"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      >
        <p>该竞赛尚未颁发奖项。</p>
        <p v-if="isAdmin">管理员可以点击"自动颁奖"按钮根据评审成绩自动分配奖项。</p>
      </el-alert>

      <div v-if="awardResults.length > 0">
        <!-- Group results by award -->
        <div v-for="group in awardGroups" :key="group.award.awardId" class="award-group">
          <div class="award-header">
            <h3>
              <el-tag 
                :type="getAwardTagType(group.award.awardLevel)" 
                size="large"
                style="margin-right: 10px"
              >
                {{ group.award.awardName }}
              </el-tag>
              <span style="color: #606266; font-size: 16px;">
                ({{ group.results.length }} 个获奖者)
              </span>
            </h3>
            <p v-if="group.award.awardPercentage" style="color: #909399; margin: 5px 0;">
              获奖比例：前 {{ (group.award.awardPercentage * 100).toFixed(1) }}%
              <span v-if="group.award.priority !== null">| 优先级：{{ group.award.priority }}</span>
            </p>
          </div>

          <el-table :data="group.results" border style="margin-bottom: 30px;">
            <el-table-column label="排名" type="index" width="80" :index="indexMethod" />
            <el-table-column label="参赛者" min-width="200">
              <template #default="{ row }">
                <div v-if="row.team">
                  <strong>{{ row.team.teamName }}</strong>
                  <el-tag size="small" style="margin-left: 10px">团队</el-tag>
                </div>
                <div v-else-if="row.user">
                  <strong>{{ row.user.username }}</strong>
                  <el-tag size="small" type="info" style="margin-left: 10px">个人</el-tag>
                </div>
                <div v-else>
                  <span style="color: #909399;">未知</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="获奖时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.awardResult?.awardTime) }}
              </template>
            </el-table-column>
            <el-table-column label="证书编号" width="150">
              <template #default="{ row }">
                {{ row.awardResult?.certificateNo || '-' }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { awardAPI, competitionAPI } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const authStore = useAuthStore()
const competition = ref({})
const awardResults = ref([])
const awards = ref([])
const loading = ref(false)
const distributing = ref(false)

const isAdmin = computed(() => {
  return authStore.user?.roles?.some(role => role.roleName === 'admin')
})

const loadCompetition = async () => {
  try {
    const response = await competitionAPI.getCompetitionById(route.params.id)
    if (response.success) {
      competition.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载竞赛信息失败')
  }
}

const loadAwards = async () => {
  try {
    const response = await awardAPI.getAwardsByCompetition(route.params.id)
    if (response.success) {
      awards.value = response.data
    }
  } catch (error) {
    console.error('Failed to load awards:', error)
  }
}

const loadAwardResults = async () => {
  loading.value = true
  try {
    const response = await awardAPI.getAwardResultsByCompetition(route.params.id)
    if (response.success) {
      awardResults.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载获奖结果失败')
  } finally {
    loading.value = false
  }
}

const awardGroups = computed(() => {
  const groups = []
  
  for (const award of awards.value) {
    const results = awardResults.value.filter(r => r.award?.awardId === award.awardId)
    if (results.length > 0) {
      groups.push({
        award,
        results
      })
    }
  }
  
  // Sort by priority
  groups.sort((a, b) => {
    const priorityA = a.award.priority !== null ? a.award.priority : 999
    const priorityB = b.award.priority !== null ? b.award.priority : 999
    return priorityA - priorityB
  })
  
  return groups
})

const handleAutoDistribute = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要自动分配奖项吗？这将根据评审成绩和奖项设置自动为符合条件的报名颁奖。已有的获奖结果将被清除并重新分配。',
      '自动颁奖确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    distributing.value = true
    const response = await awardAPI.autoDistributeAwards(route.params.id)
    
    if (response.success) {
      ElMessage.success(response.message || '自动颁奖成功')
      await loadAwardResults()
    } else {
      ElMessage.error(response.message || '自动颁奖失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '自动颁奖失败')
    }
  } finally {
    distributing.value = false
  }
}

const getAwardTagType = (level) => {
  const map = {
    'first': 'danger',
    'second': 'warning',
    'third': 'success',
    'other': 'info'
  }
  return map[level] || 'info'
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const indexMethod = (index) => {
  return index + 1
}

onMounted(async () => {
  await loadCompetition()
  await loadAwards()
  await loadAwardResults()
})
</script>

<style scoped>
.award-results-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.award-group {
  margin-bottom: 40px;
}

.award-header {
  margin-bottom: 15px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.award-header h3 {
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
}
</style>
