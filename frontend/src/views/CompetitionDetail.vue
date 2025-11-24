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
              {{ formatDate(competition.reviewStart || competition.awardPublishStart) }}
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
              <span v-if="competition.reviewStart">
                {{ formatDate(competition.reviewStart) }}
                <br/>至<br/>
                {{ formatDate(competition.awardPublishStart) }}
              </span>
              <span v-else>
                （内部评审，在奖项公示前完成）
              </span>
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
          <el-tag v-else-if="competition.competitionStatus === 'registering'" type="primary">报名中</el-tag>
          <el-tag v-else-if="competition.competitionStatus === 'submitting'" type="success">进行中-提交作品</el-tag>
          <el-tag v-else-if="competition.competitionStatus === 'reviewing'" type="warning">评审中</el-tag>
          <el-tag v-else-if="competition.competitionStatus === 'publicizing'">公示中</el-tag>
          <el-tag v-else-if="competition.competitionStatus === 'finished'" type="info">已结束</el-tag>
          <!-- Backward compatibility with old statuses -->
          <el-tag v-else-if="competition.competitionStatus === 'published'">已发布</el-tag>
          <el-tag v-else-if="competition.competitionStatus === 'ongoing'" type="success">进行中</el-tag>
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
          {{ formatDate(competition.submitStart) }} 至 {{ formatDate(competition.reviewStart || competition.awardPublishStart) }}
        </el-descriptions-item>
        <el-descriptions-item label="评审时间" :span="2" v-if="competition.reviewStart">
          {{ formatDate(competition.reviewStart) }} 至 {{ formatDate(competition.awardPublishStart) }}
        </el-descriptions-item>
        <el-descriptions-item label="评审时间" :span="2" v-else>
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
import { competitionAPI, systemTimeAPI } from '../api'
import { ElMessage } from 'element-plus'
import { SuccessFilled, CircleCheckFilled, CircleCheck } from '@element-plus/icons-vue'

const route = useRoute()
const competition = ref({})
const loading = ref(false)
const systemTime = ref(null)

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

const loadSystemTime = async () => {
  try {
    const response = await systemTimeAPI.getCurrentTime()
    if (response.success) {
      systemTime.value = new Date(response.data.currentTime)
    }
  } catch (error) {
    // If system time API fails, fallback to browser time
    console.warn('Failed to fetch system time, using browser time:', error)
    systemTime.value = new Date()
    ElMessage.warning('无法获取系统时间，使用浏览器时间')
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

// Get current time (use system time if available, otherwise browser time)
const getCurrentTime = () => {
  return systemTime.value || new Date()
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
      // If reviewStart exists, use it; otherwise use awardPublishStart
      const submitEnd = comp.reviewStart || comp.awardPublishStart
      return comp.submitStart && submitEnd &&
        new Date(comp.submitStart) <= now && now < new Date(submitEnd)
    case 'review':
      // Review phase is visible if reviewStart exists
      if (!comp.reviewStart) return false
      return comp.reviewStart && comp.awardPublishStart &&
        new Date(comp.reviewStart) <= now && now < new Date(comp.awardPublishStart)
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
      const submitEnd = comp.reviewStart || comp.awardPublishStart
      return submitEnd && now >= new Date(submitEnd)
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

onMounted(async () => {
  await loadSystemTime()
  await loadCompetition()
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
