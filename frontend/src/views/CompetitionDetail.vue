<template>
  <div class="competition-detail-container">
    <el-card v-loading="loading">
      <template #header>
        <el-page-header @back="$router.back()" :title="competition.shortTitle">
          <template #content>
            <span class="competition-title">{{ competition.competitionTitle }}</span>
          </template>
        </el-page-header>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="竞赛ID">{{ competition.competitionId }}</el-descriptions-item>
        <el-descriptions-item label="简称">{{ competition.shortTitle }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="competition.competitionStatus === 'draft'" type="info">草稿</el-tag>
          <el-tag v-else-if="competition.competitionStatus === 'published'">已发布</el-tag>
          <el-tag v-else-if="competition.competitionStatus === 'ongoing'" type="success">进行中</el-tag>
          <el-tag v-else-if="competition.competitionStatus === 'finished'" type="warning">已结束</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="级别">
          <el-tag v-if="competition.level === 'school'">校级</el-tag>
          <el-tag v-else-if="competition.level === 'provincial'" type="success">省级</el-tag>
          <el-tag v-else-if="competition.level === 'national'" type="warning">国家级</el-tag>
          <el-tag v-else-if="competition.level === 'international'" type="danger">国际级</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="类别">{{ competition.category }}</el-descriptions-item>
        <el-descriptions-item label="主办方">{{ competition.organizer }}</el-descriptions-item>
        <el-descriptions-item label="报名时间" :span="2">
          {{ formatDate(competition.signupStart) }} 至 {{ formatDate(competition.signupEnd) }}
        </el-descriptions-item>
        <el-descriptions-item label="竞赛时间" :span="2">
          {{ formatDate(competition.startDate) }} 至 {{ formatDate(competition.endDate) }}
        </el-descriptions-item>
        <el-descriptions-item label="作品提交时间" :span="2">
          {{ formatDate(competition.submitStart) }} 至 {{ formatDate(competition.submitEnd) }}
        </el-descriptions-item>
        <el-descriptions-item label="最大团队人数">{{ competition.maxTeamSize }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">
          <div v-html="competition.description"></div>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { competitionAPI } from '../api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const competition = ref({})
const loading = ref(false)

const loadCompetition = async () => {
  loading.value = true
  try {
    const response = await competitionAPI.getCompetitionById(route.params.id)
    if (response.success) {
      competition.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载竞赛详情失败')
  } finally {
    loading.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(() => {
  loadCompetition()
})
</script>

<style scoped>
.competition-detail-container {
  max-width: 1200px;
  margin: 0 auto;
}

.competition-title {
  font-size: 18px;
  font-weight: bold;
}
</style>
