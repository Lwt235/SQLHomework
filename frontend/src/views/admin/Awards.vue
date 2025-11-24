<template>
  <div class="admin-awards">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>奖项管理</span>
          <el-button type="primary" @click="showCreateDialog = true">创建奖项</el-button>
        </div>
      </template>

      <!-- Competition selection for viewing awards by competition -->
      <div style="margin-bottom: 20px;">
        <el-alert 
          title="奖项优先级管理说明" 
          type="info" 
          :closable="false"
          style="margin-bottom: 15px"
        >
          <p>• 选择竞赛后，可通过拖动奖项调整优先级（从上到下依次为优先级0, 1, 2...）</p>
          <p>• 同一人获得多个奖项时，系统将保留优先级最小（数值最小）的奖项</p>
          <p>• 拖动排序后优先级将自动保存</p>
        </el-alert>
        
        <el-select 
          v-model="selectedCompetitionId" 
          placeholder="选择竞赛查看其奖项" 
          style="width: 300px"
          @change="loadAwardsByCompetition"
          clearable
        >
          <el-option
            v-for="comp in competitions"
            :key="comp.competitionId"
            :label="comp.competitionTitle"
            :value="comp.competitionId"
          />
        </el-select>
      </div>

      <!-- Competition-specific awards table with drag-and-drop -->
      <div v-if="selectedCompetitionId && competitionAwards.length > 0">
        <h3>{{ selectedCompetitionTitle }} - 奖项列表</h3>
        <el-table 
          :data="competitionAwards" 
          v-loading="loading" 
          style="width: 100%"
          row-key="awardId"
          ref="awardTableRef"
        >
          <el-table-column label="拖动" width="60">
            <template #default>
              <el-icon class="drag-handle" style="cursor: move;">
                <Rank />
              </el-icon>
            </template>
          </el-table-column>
          <el-table-column prop="priority" label="优先级" width="100">
            <template #default="{ row }">
              <el-tag :type="row.priority === 0 ? 'danger' : (row.priority === 1 ? 'warning' : 'info')">
                {{ row.priority }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="awardName" label="奖项名称" width="150" />
          <el-table-column prop="awardLevel" label="级别" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.awardLevel === 'first'" type="danger">一等奖</el-tag>
              <el-tag v-else-if="row.awardLevel === 'second'" type="warning">二等奖</el-tag>
              <el-tag v-else-if="row.awardLevel === 'third'" type="success">三等奖</el-tag>
              <el-tag v-else>其他</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="awardPercentage" label="获奖比例" width="150">
            <template #default="{ row }">
              {{ row.awardPercentage ? (row.awardPercentage * 100).toFixed(1) + '%' : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="评选标准" min-width="200">
            <template #default="{ row }">
              {{ row.criteriaDescription || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button size="small" @click="editAward(row)">编辑</el-button>
              <el-button size="small" @click="grantAward(row)">授予奖项</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div v-else-if="selectedCompetitionId && competitionAwards.length === 0" style="text-align: center; padding: 40px; color: #909399;">
        该竞赛暂无奖项
      </div>

      <!-- All awards table -->
      <div v-if="!selectedCompetitionId">
        <h3>所有奖项</h3>
        <el-table :data="awards" v-loading="loading" style="width: 100%">
          <el-table-column prop="awardId" label="ID" width="80" />
          <el-table-column prop="awardName" label="奖项名称" width="150" />
          <el-table-column prop="awardLevel" label="级别" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.awardLevel === 'first'" type="danger">一等奖</el-tag>
              <el-tag v-else-if="row.awardLevel === 'second'" type="warning">二等奖</el-tag>
              <el-tag v-else-if="row.awardLevel === 'third'" type="success">三等奖</el-tag>
              <el-tag v-else>其他</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="awardPercentage" label="获奖比例" width="150">
            <template #default="{ row }">
              {{ row.awardPercentage ? (row.awardPercentage * 100).toFixed(1) + '%' : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="priority" label="优先级" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.priority !== null && row.priority !== undefined" :type="row.priority === 0 ? 'danger' : (row.priority === 1 ? 'warning' : 'info')">
                {{ row.priority }}
              </el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="竞赛" min-width="150">
            <template #default="{ row }">
              {{ row.competition?.competitionTitle || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button size="small" @click="editAward(row)">编辑</el-button>
              <el-button size="small" @click="grantAward(row)">授予奖项</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- Create Award Dialog -->
    <el-dialog v-model="showCreateDialog" title="创建奖项" width="600px">
      <el-form :model="awardForm" label-width="120px">
        <el-form-item label="竞赛">
          <el-select v-model="awardForm.competitionId" placeholder="选择竞赛" style="width: 100%">
            <el-option
              v-for="comp in competitions"
              :key="comp.competitionId"
              :label="comp.competitionTitle"
              :value="comp.competitionId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="奖项名称">
          <el-input v-model="awardForm.awardName" />
        </el-form-item>
        <el-form-item label="奖项级别">
          <el-select v-model="awardForm.awardLevel">
            <el-option label="一等奖" value="first" />
            <el-option label="二等奖" value="second" />
            <el-option label="三等奖" value="third" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="获奖比例">
          <el-input-number 
            v-model="awardForm.awardPercentage" 
            :min="0" 
            :max="1" 
            :precision="4" 
            :step="0.01"
            placeholder="如0.3表示前30%获奖"
            style="width: 250px"
          />
          <span style="margin-left: 10px; color: #909399;">
            {{ awardForm.awardPercentage ? `前${(awardForm.awardPercentage * 100).toFixed(1)}%获奖` : '请设置获奖比例' }}
          </span>
        </el-form-item>
        <el-form-item label="评选标准">
          <el-input v-model="awardForm.criteriaDescription" type="textarea" :rows="3" />
        </el-form-item>
        <el-alert 
          title="优先级说明" 
          type="info" 
          :closable="false"
          style="margin-bottom: 10px"
        >
          <p>创建后可在竞赛视图中通过拖动调整优先级，无需手动设置</p>
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createAward">创建</el-button>
      </template>
    </el-dialog>

    <!-- Edit Award Dialog -->
    <el-dialog v-model="showEditDialog" title="编辑奖项" width="600px">
      <el-form :model="editForm" label-width="120px">
        <el-form-item label="奖项名称">
          <el-input v-model="editForm.awardName" />
        </el-form-item>
        <el-form-item label="奖项级别">
          <el-select v-model="editForm.awardLevel">
            <el-option label="一等奖" value="first" />
            <el-option label="二等奖" value="second" />
            <el-option label="三等奖" value="third" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="获奖比例">
          <el-input-number 
            v-model="editForm.awardPercentage" 
            :min="0" 
            :max="1" 
            :precision="4" 
            :step="0.01"
            placeholder="如0.3表示前30%获奖"
            style="width: 250px"
          />
          <span style="margin-left: 10px; color: #909399;">
            {{ editForm.awardPercentage ? `前${(editForm.awardPercentage * 100).toFixed(1)}%获奖` : '请设置获奖比例' }}
          </span>
        </el-form-item>
        <el-form-item label="评选标准">
          <el-input v-model="editForm.criteriaDescription" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="updateAward">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { awardAPI, competitionAPI } from '../../api'
import { ElMessage } from 'element-plus'
import { Rank } from '@element-plus/icons-vue'
import Sortable from 'sortablejs'

const awards = ref([])
const competitions = ref([])
const competitionAwards = ref([])
const selectedCompetitionId = ref(null)
const loading = ref(false)
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const awardTableRef = ref(null)
let sortableInstance = null

const awardForm = ref({
  competitionId: null,
  awardName: '',
  awardLevel: 'first',
  awardPercentage: null,
  criteriaDescription: ''
})

const editForm = ref({
  awardId: null,
  awardName: '',
  awardLevel: 'first',
  awardPercentage: null,
  criteriaDescription: ''
})

const selectedCompetitionTitle = computed(() => {
  const comp = competitions.value.find(c => c.competitionId === selectedCompetitionId.value)
  return comp ? comp.competitionTitle : ''
})

const loadAwards = async () => {
  loading.value = true
  try {
    const response = await awardAPI.getAllAwards()
    if (response.success) {
      awards.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载奖项列表失败')
  } finally {
    loading.value = false
  }
}

const loadCompetitions = async () => {
  try {
    const response = await competitionAPI.getAllCompetitions()
    if (response.success) {
      competitions.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载竞赛列表失败')
  }
}

const loadAwardsByCompetition = async () => {
  if (!selectedCompetitionId.value) {
    competitionAwards.value = []
    return
  }
  
  loading.value = true
  try {
    const response = await awardAPI.getAwardsByCompetition(selectedCompetitionId.value)
    if (response.success) {
      // Sort by priority ascending
      competitionAwards.value = response.data.sort((a, b) => {
        const priorityA = a.priority !== null ? a.priority : 999
        const priorityB = b.priority !== null ? b.priority : 999
        return priorityA - priorityB
      })
      
      // Initialize sortable after data is loaded
      await nextTick()
      initSortable()
    }
  } catch (error) {
    ElMessage.error('加载竞赛奖项失败')
  } finally {
    loading.value = false
  }
}

const initSortable = () => {
  if (sortableInstance) {
    sortableInstance.destroy()
  }
  
  const tableEl = awardTableRef.value?.$el.querySelector('.el-table__body-wrapper tbody')
  if (!tableEl) {
    console.warn('Unable to initialize drag-and-drop: table element not found')
    return
  }
  
  try {
    sortableInstance = Sortable.create(tableEl, {
      handle: '.drag-handle',
      animation: 150,
      onEnd: async (evt) => {
        const { oldIndex, newIndex } = evt
        if (oldIndex !== newIndex) {
          // Reorder the array
          const movedItem = competitionAwards.value.splice(oldIndex, 1)[0]
          competitionAwards.value.splice(newIndex, 0, movedItem)
          
          // Update priorities based on new order
          await updatePriorities()
        }
      }
    })
  } catch (error) {
    console.error('Failed to initialize sortable:', error)
    ElMessage.warning('拖动排序功能初始化失败，请刷新页面重试')
  }
}

const updatePriorities = async () => {
  try {
    // Update priority for each award based on its position
    const updatedAwards = competitionAwards.value.map((award, index) => ({
      ...award,
      priority: index
    }))
    
    // Send batch update request
    await awardAPI.batchUpdatePriorities(updatedAwards)
    
    // Update local priorities
    competitionAwards.value.forEach((award, index) => {
      award.priority = index
    })
    
    ElMessage.success('奖项优先级已更新')
  } catch (error) {
    ElMessage.error('更新优先级失败: ' + (error.message || '未知错误'))
    // Reload to ensure consistency
    await loadAwardsByCompetition()
  }
}

const createAward = async () => {
  if (!awardForm.value.competitionId) {
    ElMessage.error('请选择竞赛')
    return
  }
  
  try {
    // Get existing awards count for this competition to set priority
    const response = await awardAPI.getAwardsByCompetition(awardForm.value.competitionId)
    const existingCount = response.success ? response.data.length : 0
    
    // Set priority as the next number
    // Note: There's a potential race condition if two admins create awards simultaneously
    // The priority can be adjusted later through drag-and-drop in the competition view
    const newAward = {
      ...awardForm.value,
      priority: existingCount
    }
    
    const createResponse = await awardAPI.createAward(newAward)
    if (createResponse.success) {
      ElMessage.success('奖项创建成功，可在竞赛视图中拖动调整优先级')
      showCreateDialog.value = false
      
      // Reset form
      awardForm.value = {
        competitionId: null,
        awardName: '',
        awardLevel: 'first',
        awardPercentage: null,
        criteriaDescription: ''
      }
      
      await loadAwards()
      if (selectedCompetitionId.value === newAward.competitionId) {
        await loadAwardsByCompetition()
      }
    }
  } catch (error) {
    ElMessage.error('创建奖项失败: ' + (error.message || '未知错误'))
  }
}

const editAward = (award) => {
  editForm.value = {
    awardId: award.awardId,
    awardName: award.awardName,
    awardLevel: award.awardLevel,
    awardPercentage: award.awardPercentage,
    criteriaDescription: award.criteriaDescription
  }
  showEditDialog.value = true
}

const updateAward = async () => {
  try {
    const response = await awardAPI.updateAward(editForm.value.awardId, editForm.value)
    if (response.success) {
      ElMessage.success('奖项更新成功')
      showEditDialog.value = false
      await loadAwards()
      if (selectedCompetitionId.value) {
        await loadAwardsByCompetition()
      }
    }
  } catch (error) {
    ElMessage.error('更新奖项失败')
  }
}

const grantAward = (award) => {
  ElMessage.info('授予奖项功能开发中')
}

onMounted(() => {
  loadAwards()
  loadCompetitions()
})
</script>

<style scoped>
.admin-awards {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.drag-handle {
  cursor: move;
  font-size: 18px;
  color: #909399;
}

.drag-handle:hover {
  color: #409eff;
}

:deep(.sortable-ghost) {
  opacity: 0.5;
  background: #f5f7fa;
}
</style>
