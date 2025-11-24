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

      <!-- Timeline Progress Display - Horizontal Layout -->
      <div class="timeline-section" v-if="!loading && competition.competitionId">
        <h3 style="margin-bottom: 20px; color: #303133;">竞赛进度时间轴</h3>
        <div class="horizontal-timeline">
          <div 
            class="timeline-item"
            :class="{ 
              'timeline-item-active': isCurrentPhase('signup'),
              'timeline-item-completed': isPhaseCompleted('signup')
            }"
          >
            <div class="timeline-icon">
              <el-icon v-if="isCurrentPhase('signup')" color="#67c23a"><SuccessFilled /></el-icon>
              <el-icon v-else-if="isPhaseCompleted('signup')" color="#409eff"><CircleCheckFilled /></el-icon>
              <el-icon v-else color="#dcdfe6"><CircleCheck /></el-icon>
            </div>
            <div class="timeline-title">
              报名阶段
              <el-tag v-if="isCurrentPhase('signup')" type="success" size="small">进行中</el-tag>
              <el-tag v-else-if="isPhaseCompleted('signup')" type="info" size="small">已完成</el-tag>
            </div>
            <div class="timeline-date">
              {{ formatDate(competition.signupStart) }}
              <br/>至<br/>
              {{ formatDate(competition.submitStart) }}
            </div>
          </div>
          
          <div class="timeline-connector" :class="{ 'timeline-connector-active': isPhaseCompleted('signup') }"></div>
          
          <div 
            class="timeline-item"
            :class="{ 
              'timeline-item-active': isCurrentPhase('submit'),
              'timeline-item-completed': isPhaseCompleted('submit')
            }"
          >
            <div class="timeline-icon">
              <el-icon v-if="isCurrentPhase('submit')" color="#67c23a"><SuccessFilled /></el-icon>
              <el-icon v-else-if="isPhaseCompleted('submit')" color="#409eff"><CircleCheckFilled /></el-icon>
              <el-icon v-else color="#dcdfe6"><CircleCheck /></el-icon>
            </div>
            <div class="timeline-title">
              作品提交阶段
              <el-tag v-if="isCurrentPhase('submit')" type="success" size="small">进行中</el-tag>
              <el-tag v-else-if="isPhaseCompleted('submit')" type="info" size="small">已完成</el-tag>
            </div>
            <div class="timeline-date">
              {{ formatDate(competition.submitStart) }}
              <br/>至<br/>
              {{ formatDate(competition.awardPublishStart) }}
            </div>
          </div>
          
          <div class="timeline-connector" :class="{ 'timeline-connector-active': isPhaseCompleted('submit') }"></div>
          
          <div 
            class="timeline-item"
            :class="{ 
              'timeline-item-active': isCurrentPhase('review'),
              'timeline-item-completed': isPhaseCompleted('review')
            }"
          >
            <div class="timeline-icon">
              <el-icon v-if="isCurrentPhase('review')" color="#67c23a"><SuccessFilled /></el-icon>
              <el-icon v-else-if="isPhaseCompleted('review')" color="#409eff"><CircleCheckFilled /></el-icon>
              <el-icon v-else color="#dcdfe6"><CircleCheck /></el-icon>
            </div>
            <div class="timeline-title">
              评审阶段
              <el-tag v-if="isCurrentPhase('review')" type="success" size="small">进行中</el-tag>
              <el-tag v-else-if="isPhaseCompleted('review')" type="info" size="small">已完成</el-tag>
            </div>
            <div class="timeline-date">
              （内部评审，在奖项公示前完成）
            </div>
          </div>
          
          <div class="timeline-connector" :class="{ 'timeline-connector-active': isPhaseCompleted('review') }"></div>
          
          <div 
            class="timeline-item"
            :class="{ 
              'timeline-item-active': isCurrentPhase('award'),
              'timeline-item-completed': isPhaseCompleted('award')
            }"
          >
            <div class="timeline-icon">
              <el-icon v-if="isCurrentPhase('award')" color="#67c23a"><SuccessFilled /></el-icon>
              <el-icon v-else-if="isPhaseCompleted('award')" color="#409eff"><CircleCheckFilled /></el-icon>
              <el-icon v-else color="#dcdfe6"><CircleCheck /></el-icon>
            </div>
            <div class="timeline-title">
              获奖公示 (1天)
              <el-tag v-if="isCurrentPhase('award')" type="success" size="small">进行中</el-tag>
              <el-tag v-else-if="isPhaseCompleted('award')" type="info" size="small">已完成</el-tag>
            </div>
            <div class="timeline-date">
              {{ formatDate(competition.awardPublishStart) }}
              <br/>至<br/>
              {{ formatEndDate(competition.awardPublishStart) }}
            </div>
          </div>
        </div>
        
        <!-- Link to Award Results -->
        <div style="text-align: center; margin-top: 30px;">
          <el-button type="primary" @click="$router.push(`/competitions/${competition.competitionId}/awards`)">
            查看获奖公示
          </el-button>
        </div>
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
          {{ formatDate(competition.signupStart) }} 至 {{ formatDate(competition.submitStart) }}
        </el-descriptions-item>
        <el-descriptions-item label="竞赛时间（作品提交）" :span="2">
          {{ formatDate(competition.submitStart) }} 至 {{ formatDate(competition.awardPublishStart) }}
        </el-descriptions-item>
        <el-descriptions-item label="评审时间" :span="2">
          评审将在作品提交截止后进行，公示开始于 {{ formatDate(competition.awardPublishStart) }}
        </el-descriptions-item>
        <el-descriptions-item label="获奖公示时间" :span="2">
          {{ formatDate(competition.awardPublishStart) }} 至 {{ formatEndDate(competition.awardPublishStart) }} (1天)
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
import { SuccessFilled, CircleCheckFilled, CircleCheck } from '@element-plus/icons-vue'

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

const formatEndDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  date.setDate(date.getDate() + 1)
  return date.toLocaleString('zh-CN')
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
      return comp.signupStart && comp.submitStart &&
        new Date(comp.signupStart) <= now && now < new Date(comp.submitStart)
    case 'submit':
      return comp.submitStart && comp.awardPublishStart &&
        new Date(comp.submitStart) <= now && now < new Date(comp.awardPublishStart)
    case 'review':
      // Review happens internally by judges, not a visible user-facing phase
      // UI still shows the review phase card for informational purposes
      // This case exists to handle the isCurrentPhase('review') calls in template
      return false // Never show as "active" - it's an internal process
    case 'award':
      if (!comp.awardPublishStart) return false
      const endDate = new Date(comp.awardPublishStart)
      endDate.setDate(endDate.getDate() + 1)
      return new Date(comp.awardPublishStart) <= now && now < endDate
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
      return comp.submitStart && now >= new Date(comp.submitStart)
    case 'submit':
      return comp.awardPublishStart && now >= new Date(comp.awardPublishStart)
    case 'review':
      return comp.awardPublishStart && now >= new Date(comp.awardPublishStart)
    case 'award':
      if (!comp.awardPublishStart) return false
      const endDate = new Date(comp.awardPublishStart)
      endDate.setDate(endDate.getDate() + 1)
      return now >= endDate
    default:
      return false
  }
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
  padding: 30px;
  background: #f9f9f9;
  border-radius: 8px;
}

.horizontal-timeline {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 20px 0;
}

.timeline-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  position: relative;
}

.timeline-icon {
  font-size: 32px;
  margin-bottom: 12px;
  z-index: 2;
}

.timeline-title {
  font-size: 16px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 8px;
  min-height: 50px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

.timeline-item-active .timeline-title {
  color: #67c23a;
  font-weight: bold;
}

.timeline-item-completed .timeline-title {
  color: #409eff;
}

.timeline-date {
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
}

.timeline-connector {
  flex: 0 0 60px;
  height: 4px;
  background: #dcdfe6;
  margin-top: 16px;
  position: relative;
}

.timeline-connector-active {
  background: #409eff;
}

@media (max-width: 768px) {
  .horizontal-timeline {
    flex-direction: column;
    gap: 20px;
  }
  
  .timeline-connector {
    width: 4px;
    height: 40px;
    margin: 10px auto;
  }
  
  .timeline-item {
    width: 100%;
  }
}
</style>
