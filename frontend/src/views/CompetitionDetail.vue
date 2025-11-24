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

      <!-- Timeline Progress Display -->
      <div class="timeline-section" v-if="!loading && competition.competitionId">
        <h3 style="margin-bottom: 20px; color: #303133;">竞赛进度时间轴</h3>
        <el-timeline>
          <el-timeline-item 
            :timestamp="formatDate(competition.signupStart)"
            :type="getPhaseType('signup')"
            :icon="getPhaseIcon('signup')"
            :size="getPhaseSize('signup')"
          >
            <div class="timeline-content">
              <h4 :class="{ 'active-phase': isCurrentPhase('signup') }">
                报名阶段
                <el-tag v-if="isCurrentPhase('signup')" type="success" size="small" style="margin-left: 10px">进行中</el-tag>
                <el-tag v-else-if="isPhaseCompleted('signup')" type="info" size="small" style="margin-left: 10px">已完成</el-tag>
              </h4>
              <p>{{ formatDate(competition.signupStart) }} 至 {{ formatDate(competition.signupEnd) }}</p>
            </div>
          </el-timeline-item>

          <el-timeline-item 
            :timestamp="formatDate(competition.submitStart)"
            :type="getPhaseType('submit')"
            :icon="getPhaseIcon('submit')"
            :size="getPhaseSize('submit')"
          >
            <div class="timeline-content">
              <h4 :class="{ 'active-phase': isCurrentPhase('submit') }">
                作品提交阶段
                <el-tag v-if="isCurrentPhase('submit')" type="success" size="small" style="margin-left: 10px">进行中</el-tag>
                <el-tag v-else-if="isPhaseCompleted('submit')" type="info" size="small" style="margin-left: 10px">已完成</el-tag>
              </h4>
              <p>{{ formatDate(competition.submitStart) }} 至 {{ formatDate(competition.submitEnd) }}</p>
            </div>
          </el-timeline-item>

          <el-timeline-item 
            v-if="competition.reviewStart && competition.reviewEnd"
            :timestamp="formatDate(competition.reviewStart)"
            :type="getPhaseType('review')"
            :icon="getPhaseIcon('review')"
            :size="getPhaseSize('review')"
          >
            <div class="timeline-content">
              <h4 :class="{ 'active-phase': isCurrentPhase('review') }">
                评审阶段
                <el-tag v-if="isCurrentPhase('review')" type="success" size="small" style="margin-left: 10px">进行中</el-tag>
                <el-tag v-else-if="isPhaseCompleted('review')" type="info" size="small" style="margin-left: 10px">已完成</el-tag>
              </h4>
              <p>{{ formatDate(competition.reviewStart) }} 至 {{ formatDate(competition.reviewEnd) }}</p>
            </div>
          </el-timeline-item>

          <el-timeline-item 
            v-if="competition.awardPublishDate"
            :timestamp="formatDate(competition.awardPublishDate)"
            :type="getPhaseType('award')"
            :icon="getPhaseIcon('award')"
            :size="getPhaseSize('award')"
          >
            <div class="timeline-content">
              <h4 :class="{ 'active-phase': isCurrentPhase('award') }">
                获奖公示
                <el-tag v-if="isCurrentPhase('award')" type="success" size="small" style="margin-left: 10px">进行中</el-tag>
                <el-tag v-else-if="isPhaseCompleted('award')" type="info" size="small" style="margin-left: 10px">已完成</el-tag>
              </h4>
              <p>{{ formatDate(competition.awardPublishDate) }}</p>
            </div>
          </el-timeline-item>

          <el-timeline-item 
            :timestamp="formatDate(competition.endDate)"
            :type="getPhaseType('end')"
            :icon="getPhaseIcon('end')"
            :size="getPhaseSize('end')"
          >
            <div class="timeline-content">
              <h4 :class="{ 'active-phase': isCurrentPhase('end') }">
                竞赛结束
                <el-tag v-if="isPhaseCompleted('end')" type="info" size="small" style="margin-left: 10px">已完成</el-tag>
              </h4>
              <p>{{ formatDate(competition.endDate) }}</p>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>

      <el-divider />

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
import { ref, onMounted, computed } from 'vue'
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

// Get current time
const getCurrentTime = () => {
  return new Date()
}

// Check if a phase is the current active phase
const isCurrentPhase = (phase) => {
  const now = getCurrentTime()
  const comp = competition.value
  
  if (!comp.competitionId) return false
  
  switch (phase) {
    case 'signup':
      return comp.signupStart && comp.signupEnd &&
        new Date(comp.signupStart) <= now && now <= new Date(comp.signupEnd)
    case 'submit':
      return comp.submitStart && comp.submitEnd &&
        new Date(comp.submitStart) <= now && now <= new Date(comp.submitEnd)
    case 'review':
      return comp.reviewStart && comp.reviewEnd &&
        new Date(comp.reviewStart) <= now && now <= new Date(comp.reviewEnd)
    case 'award':
      return comp.awardPublishDate && comp.endDate &&
        new Date(comp.awardPublishDate) <= now && now <= new Date(comp.endDate)
    case 'end':
      return comp.endDate && now > new Date(comp.endDate)
    default:
      return false
  }
}

// Check if a phase is completed
const isPhaseCompleted = (phase) => {
  const now = getCurrentTime()
  const comp = competition.value
  
  if (!comp.competitionId) return false
  
  switch (phase) {
    case 'signup':
      return comp.signupEnd && now > new Date(comp.signupEnd)
    case 'submit':
      return comp.submitEnd && now > new Date(comp.submitEnd)
    case 'review':
      return comp.reviewEnd && now > new Date(comp.reviewEnd)
    case 'award':
      return comp.awardPublishDate && comp.endDate && now > new Date(comp.endDate)
    case 'end':
      return comp.endDate && now > new Date(comp.endDate)
    default:
      return false
  }
}

// Get phase type for timeline styling
const getPhaseType = (phase) => {
  if (isCurrentPhase(phase)) {
    return 'success'
  } else if (isPhaseCompleted(phase)) {
    return 'primary'
  } else {
    return 'info'
  }
}

// Get phase icon
const getPhaseIcon = (phase) => {
  if (isCurrentPhase(phase)) {
    return 'SuccessFilled'
  } else if (isPhaseCompleted(phase)) {
    return 'CircleCheckFilled'
  } else {
    return 'CircleFilled'
  }
}

// Get phase size
const getPhaseSize = (phase) => {
  return isCurrentPhase(phase) ? 'large' : 'normal'
}

onMounted(() => {
  loadCompetition()
})
</script>

<style scoped>
.competition-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.competition-title {
  font-size: 18px;
  font-weight: bold;
}

.timeline-section {
  margin-bottom: 30px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
}

.timeline-content h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #303133;
  transition: color 0.3s;
}

.timeline-content h4.active-phase {
  color: #67c23a;
  font-weight: bold;
}

.timeline-content p {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

:deep(.el-timeline-item__timestamp) {
  font-weight: 500;
  color: #606266;
}
</style>
