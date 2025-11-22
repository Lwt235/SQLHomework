<template>
  <div class="system-time-manager">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统时间管理（测试模式）</span>
          <el-tag v-if="timeInfo.testMode" type="danger">测试模式已启用</el-tag>
          <el-tag v-else type="success">正常模式</el-tag>
        </div>
      </template>

      <el-alert 
        title="测试模式说明" 
        type="warning" 
        :closable="false"
        style="margin-bottom: 20px"
      >
        <p>测试模式允许手动调整系统时间，用于测试竞赛状态的自动切换功能</p>
        <p>启用测试模式后，所有时间相关的判断都将使用调整后的时间</p>
        <p><strong>注意：</strong>此功能仅用于测试，生产环境请勿使用</p>
      </el-alert>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="当前系统时间">
          {{ formatDateTime(timeInfo.currentTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="真实时间">
          {{ formatDateTime(timeInfo.realTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="测试模式状态">
          {{ timeInfo.testMode ? '已启用' : '已禁用' }}
        </el-descriptions-item>
        <el-descriptions-item label="时间偏移">
          {{ timeInfo.offsetSeconds }} 秒 ({{ formatDuration(timeInfo.offsetSeconds) }})
        </el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <div class="control-section">
        <h3>测试模式控制</h3>
        
        <el-space direction="vertical" style="width: 100%">
          <el-button 
            v-if="!timeInfo.testMode" 
            type="primary" 
            @click="enableTestMode"
          >
            启用测试模式
          </el-button>
          <el-button 
            v-else 
            type="danger" 
            @click="disableTestMode"
          >
            禁用测试模式并重置
          </el-button>

          <el-form v-if="timeInfo.testMode" :inline="false" label-width="120px">
            <el-form-item label="设置测试时间">
              <el-date-picker 
                v-model="testTime" 
                type="datetime" 
                placeholder="选择测试时间"
                style="width: 300px"
              />
              <el-button 
                type="primary" 
                @click="setTestTime" 
                :loading="loading"
                style="margin-left: 10px"
              >
                设置时间
              </el-button>
            </el-form-item>

            <el-form-item label="快速调整">
              <el-space>
                <el-button @click="adjustTime(-86400)">-1天</el-button>
                <el-button @click="adjustTime(-3600)">-1小时</el-button>
                <el-button @click="adjustTime(-60)">-1分钟</el-button>
                <el-button @click="adjustTime(60)">+1分钟</el-button>
                <el-button @click="adjustTime(3600)">+1小时</el-button>
                <el-button @click="adjustTime(86400)">+1天</el-button>
              </el-space>
            </el-form-item>

            <el-form-item label="自定义偏移">
              <el-input-number 
                v-model="customOffset" 
                :step="60" 
                placeholder="秒数"
                style="width: 150px"
              />
              <span style="margin: 0 10px">秒</span>
              <el-button 
                type="primary" 
                @click="setCustomOffset" 
                :loading="loading"
              >
                应用偏移
              </el-button>
            </el-form-item>
          </el-form>
        </el-space>
      </div>

      <el-divider />

      <div class="refresh-section">
        <el-button @click="loadTimeInfo" :loading="loading">刷新时间信息</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { systemTimeAPI } from '../../api'
import { ElMessage } from 'element-plus'

const timeInfo = ref({
  currentTime: '',
  realTime: '',
  testMode: false,
  offsetSeconds: 0
})
const loading = ref(false)
const testTime = ref(null)
const customOffset = ref(0)

const loadTimeInfo = async () => {
  loading.value = true
  try {
    const response = await systemTimeAPI.getCurrentTime()
    if (response.success) {
      timeInfo.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载时间信息失败')
  } finally {
    loading.value = false
  }
}

const enableTestMode = async () => {
  try {
    const response = await systemTimeAPI.enableTestMode()
    if (response.success) {
      ElMessage.success('测试模式已启用')
      await loadTimeInfo()
    }
  } catch (error) {
    ElMessage.error('启用测试模式失败')
  }
}

const disableTestMode = async () => {
  try {
    const response = await systemTimeAPI.disableTestMode()
    if (response.success) {
      ElMessage.success('测试模式已禁用')
      await loadTimeInfo()
    }
  } catch (error) {
    ElMessage.error('禁用测试模式失败')
  }
}

const setTestTime = async () => {
  if (!testTime.value) {
    ElMessage.error('请选择测试时间')
    return
  }
  
  loading.value = true
  try {
    const timeStr = testTime.value.toISOString().slice(0, 19)
    const response = await systemTimeAPI.setTestTime(timeStr)
    if (response.success) {
      ElMessage.success('测试时间已设置')
      await loadTimeInfo()
    }
  } catch (error) {
    ElMessage.error('设置测试时间失败')
  } finally {
    loading.value = false
  }
}

const adjustTime = async (seconds) => {
  loading.value = true
  try {
    const newOffset = timeInfo.value.offsetSeconds + seconds
    const response = await systemTimeAPI.setTimeOffset(newOffset)
    if (response.success) {
      ElMessage.success(`时间已调整 ${seconds > 0 ? '+' : ''}${seconds} 秒`)
      await loadTimeInfo()
    }
  } catch (error) {
    ElMessage.error('调整时间失败')
  } finally {
    loading.value = false
  }
}

const setCustomOffset = async () => {
  loading.value = true
  try {
    const response = await systemTimeAPI.setTimeOffset(customOffset.value)
    if (response.success) {
      ElMessage.success('自定义偏移已应用')
      await loadTimeInfo()
    }
  } catch (error) {
    ElMessage.error('设置偏移失败')
  } finally {
    loading.value = false
  }
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const formatDuration = (seconds) => {
  if (seconds === 0) return '无偏移'
  
  const absSeconds = Math.abs(seconds)
  const days = Math.floor(absSeconds / 86400)
  const hours = Math.floor((absSeconds % 86400) / 3600)
  const minutes = Math.floor((absSeconds % 3600) / 60)
  const secs = absSeconds % 60
  
  let parts = []
  if (days > 0) parts.push(`${days}天`)
  if (hours > 0) parts.push(`${hours}小时`)
  if (minutes > 0) parts.push(`${minutes}分钟`)
  if (secs > 0) parts.push(`${secs}秒`)
  
  const result = parts.join(' ')
  return seconds < 0 ? `-${result}` : `+${result}`
}

onMounted(() => {
  loadTimeInfo()
})
</script>

<style scoped>
.system-time-manager {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.control-section {
  margin: 20px 0;
}

.refresh-section {
  margin-top: 20px;
}
</style>
