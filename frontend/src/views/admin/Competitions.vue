<template>
  <div class="admin-competitions">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>竞赛管理</span>
          <el-button type="primary" @click="showCreateDialog = true">创建竞赛</el-button>
        </div>
      </template>

      <el-table :data="competitions" v-loading="loading" style="width: 100%">
        <el-table-column prop="competitionId" label="ID" width="80" />
        <el-table-column prop="competitionTitle" label="竞赛名称" min-width="200" />
        <el-table-column prop="competitionStatus" label="状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.competitionStatus === 'draft'" type="info">草稿</el-tag>
            <el-tag v-else-if="row.competitionStatus === 'registering'" type="primary">报名中</el-tag>
            <el-tag v-else-if="row.competitionStatus === 'submitting'" type="success">进行中</el-tag>
            <el-tag v-else-if="row.competitionStatus === 'reviewing'" type="warning">评审中</el-tag>
            <el-tag v-else-if="row.competitionStatus === 'publicizing'">公示中</el-tag>
            <el-tag v-else-if="row.competitionStatus === 'finished'" type="info">已结束</el-tag>
            <!-- Backward compatibility with old statuses -->
            <el-tag v-else-if="row.competitionStatus === 'published'">已发布</el-tag>
            <el-tag v-else-if="row.competitionStatus === 'ongoing'" type="success">进行中</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="editCompetition(row)">编辑</el-button>
            <el-popconfirm
              title="确定删除该竞赛吗？"
              @confirm="deleteCompetition(row.competitionId)"
            >
              <template #reference>
                <el-button type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="showCreateDialog" :title="editingCompetitionId ? '编辑竞赛' : '创建竞赛'" width="800px">
      <el-form :model="competitionForm" :rules="competitionRules" ref="competitionFormRef" label-width="120px">
        <el-form-item label="竞赛名称" prop="competitionTitle">
          <el-input v-model="competitionForm.competitionTitle" placeholder="请输入竞赛名称" />
        </el-form-item>
        <el-form-item label="简称" prop="shortTitle">
          <el-input v-model="competitionForm.shortTitle" placeholder="请输入竞赛简称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="competitionForm.description" type="textarea" :rows="4" placeholder="请输入竞赛描述" />
        </el-form-item>
        <el-form-item label="类别" prop="category">
          <el-input v-model="competitionForm.category" placeholder="例如：程序设计、创新创业等" />
        </el-form-item>
        <el-form-item label="级别" prop="level">
          <el-select v-model="competitionForm.level" style="width: 100%">
            <el-option label="校级" value="school" />
            <el-option label="省级" value="provincial" />
            <el-option label="国家级" value="national" />
            <el-option label="国际级" value="international" />
          </el-select>
        </el-form-item>
        <el-form-item label="主办方" prop="organizer">
          <el-input v-model="competitionForm.organizer" placeholder="请输入主办单位" />
        </el-form-item>
        <el-form-item label="竞赛状态" prop="competitionStatus">
          <el-select v-model="competitionForm.competitionStatus" style="width: 100%">
            <el-option label="草稿" value="draft" />
            <el-option label="报名中" value="registering" />
            <el-option label="进行中-提交作品" value="submitting" />
            <el-option label="评审中" value="reviewing" />
            <el-option label="公示中" value="publicizing" />
            <el-option label="已结束" value="finished" />
          </el-select>
        </el-form-item>
        <el-form-item label="报名开始时间" prop="signupStart">
          <el-date-picker 
            v-model="competitionForm.signupStart" 
            type="datetime" 
            placeholder="选择报名开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="提交开始时间" prop="submitStart">
          <span style="color: #909399; font-size: 12px;">（同时也是报名截止时间和竞赛开始时间）</span>
          <el-date-picker 
            v-model="competitionForm.submitStart" 
            type="datetime" 
            placeholder="选择作品提交开始时间"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="评审开始时间" prop="reviewStart">
          <span style="color: #909399; font-size: 12px;">（作品提交截止后开始评审）</span>
          <el-date-picker 
            v-model="competitionForm.reviewStart" 
            type="datetime" 
            placeholder="选择评审开始时间"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="奖项公示开始" prop="awardPublishStart">
          <span style="color: #909399; font-size: 12px;">（同时也是评审结束时间，公示期为1天后结束）</span>
          <el-date-picker 
            v-model="competitionForm.awardPublishStart" 
            type="datetime" 
            placeholder="选择奖项公示开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="最大团队人数" prop="maxTeamSize">
          <el-input-number v-model="competitionForm.maxTeamSize" :min="1" :max="20" />
          <span style="margin-left: 10px; color: #909399;">个人参赛设为1</span>
        </el-form-item>
        
        <!-- Awards Configuration -->
        <el-divider v-if="!editingCompetitionId">奖项设置（可选）</el-divider>
        <div v-if="!editingCompetitionId">
          <el-alert 
            title="奖项优先级说明" 
            type="info" 
            :closable="false"
            style="margin-bottom: 15px"
          >
            <p>• 通过拖动奖项卡片可以调整优先级（从上到下依次为优先级0, 1, 2...）</p>
            <p>• 同一人获得多个奖项时，系统将保留优先级最小（数值最小）的奖项</p>
          </el-alert>
          
          <el-button 
            type="primary" 
            size="small" 
            @click="addAward" 
            style="margin-bottom: 10px"
          >
            添加奖项
          </el-button>
          
          <div ref="awardsContainerRef" style="min-height: 50px;">
            <div 
              v-for="(award, index) in competitionForm.awards" 
              :key="award.tempId || index" 
              class="award-card"
              style="margin-bottom: 15px; padding: 15px; border: 1px solid #dcdfe6; border-radius: 4px; background: #fff;"
            >
              <div style="display: flex; align-items: center; margin-bottom: 10px;">
                <el-icon class="drag-handle" style="cursor: grab; margin-right: 10px; color: #909399; font-size: 18px;">
                  <Rank />
                </el-icon>
                <el-tag :type="index === 0 ? 'danger' : (index === 1 ? 'warning' : 'info')" size="small">
                  优先级 {{ index }}
                </el-tag>
                <span style="margin-left: 10px; color: #606266; font-weight: 500;">奖项 {{ index + 1 }}</span>
              </div>
              <el-row :gutter="10">
                <el-col :span="8">
                  <el-form-item label="奖项名称" label-width="80px">
                    <el-input v-model="award.awardName" placeholder="如：一等奖" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="奖项级别" label-width="80px">
                    <el-select v-model="award.awardLevel" style="width: 100%">
                      <el-option label="一等奖" value="first" />
                      <el-option label="二等奖" value="second" />
                      <el-option label="三等奖" value="third" />
                      <el-option label="其他" value="other" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="获奖比例" label-width="80px">
                    <el-input-number 
                      v-model="award.awardPercentage" 
                      :min="0" 
                      :max="1" 
                      :precision="4" 
                      :step="0.01"
                      placeholder="0.3"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="2">
                  <el-button 
                    type="danger" 
                    size="small" 
                    @click="removeAward(index)"
                    style="margin-top: 30px"
                  >
                    删除
                  </el-button>
                </el-col>
              </el-row>
              <el-row :gutter="10">
                <el-col :span="24">
                  <el-form-item label="评选标准" label-width="80px">
                    <el-input v-model="award.criteriaDescription" placeholder="描述获奖条件" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </div>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false; editingCompetitionId = null">取消</el-button>
        <el-button type="primary" @click="saveCompetition" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { competitionAPI } from '../../api'
import { ElMessage } from 'element-plus'
import { Rank } from '@element-plus/icons-vue'
import Sortable from 'sortablejs'

const competitions = ref([])
const loading = ref(false)
const saving = ref(false)
const showCreateDialog = ref(false)
const competitionFormRef = ref(null)
const awardsContainerRef = ref(null)
let awardsSortableInstance = null
let nextAwardTempId = 0

const getInitialCompetitionForm = () => ({
  competitionTitle: '',
  shortTitle: '',
  description: '',
  category: '',
  level: 'school',
  organizer: '',
  competitionStatus: 'draft',
  signupStart: null,
  submitStart: null,
  reviewStart: null,
  awardPublishStart: null,
  maxTeamSize: 5,
  awards: []
})

const competitionForm = ref(getInitialCompetitionForm())
const editingCompetitionId = ref(null)

const competitionRules = {
  competitionTitle: [
    { required: true, message: '请输入竞赛名称', trigger: 'blur' }
  ],
  shortTitle: [
    { required: true, message: '请输入竞赛简称', trigger: 'blur' }
  ],
  level: [
    { required: true, message: '请选择竞赛级别', trigger: 'change' }
  ],
  organizer: [
    { required: true, message: '请输入主办方', trigger: 'blur' }
  ],
  signupStart: [
    { required: true, message: '请选择报名开始时间', trigger: 'change' }
  ],
  submitStart: [
    { required: true, message: '请选择作品提交开始时间（报名截止时间）', trigger: 'change' }
  ],
  reviewStart: [
    { required: false, trigger: 'change' }
  ],
  awardPublishStart: [
    { required: true, message: '请选择奖项公示开始时间（评审结束时间）', trigger: 'change' }
  ]
}

const loadCompetitions = async () => {
  loading.value = true
  try {
    const response = await competitionAPI.getAllCompetitions()
    if (response.success) {
      competitions.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载竞赛列表失败')
  } finally {
    loading.value = false
  }
}

const saveCompetition = async () => {
  if (!competitionFormRef.value) return
  
  await competitionFormRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        let response
        if (editingCompetitionId.value) {
          // Update existing competition
          response = await competitionAPI.updateCompetition(editingCompetitionId.value, competitionForm.value)
        } else {
          // Create new competition with or without awards
          if (competitionForm.value.awards && competitionForm.value.awards.length > 0) {
            // Assign priority based on array order, exclude tempId
            const awardsWithPriority = competitionForm.value.awards.map((award, index) => {
              const { tempId, ...awardData } = award
              return {
                ...awardData,
                priority: index
              }
            })
            
            response = await competitionAPI.createCompetitionWithAwards({
              competition: competitionForm.value,
              awards: awardsWithPriority
            })
          } else {
            response = await competitionAPI.createCompetition(competitionForm.value)
          }
        }
        
        if (response.success) {
          ElMessage.success(editingCompetitionId.value ? '竞赛更新成功' : '竞赛创建成功')
          showCreateDialog.value = false
          loadCompetitions()
          // Reset form and cleanup
          competitionForm.value = getInitialCompetitionForm()
          editingCompetitionId.value = null
          nextAwardTempId = 0  // Reset temp ID counter
          if (awardsSortableInstance) {
            awardsSortableInstance.destroy()
            awardsSortableInstance = null
          }
        } else {
          ElMessage.error(response.message || '保存竞赛失败')
        }
      } catch (error) {
        ElMessage.error(error.message || '保存竞赛失败')
      } finally {
        saving.value = false
      }
    }
  })
}

const addAward = async () => {
  competitionForm.value.awards.push({
    tempId: nextAwardTempId++, // Use temp ID for better key tracking during drag
    awardName: '',
    awardLevel: 'first',
    awardPercentage: null,
    criteriaDescription: ''
  })
  
  // Initialize sortable after adding award
  await nextTick()
  if (!awardsSortableInstance && awardsContainerRef.value) {
    initAwardsSortable()
  }
}

const removeAward = (index) => {
  competitionForm.value.awards.splice(index, 1)
}

const initAwardsSortable = () => {
  if (awardsSortableInstance) {
    awardsSortableInstance.destroy()
  }
  
  if (!awardsContainerRef.value) {
    return
  }
  
  try {
    awardsSortableInstance = Sortable.create(awardsContainerRef.value, {
      animation: 150,
      handle: '.drag-handle',
      ghostClass: 'sortable-ghost',
      onEnd: (evt) => {
        const { oldIndex, newIndex } = evt
        if (oldIndex !== newIndex) {
          // Reorder the awards array
          const movedItem = competitionForm.value.awards.splice(oldIndex, 1)[0]
          competitionForm.value.awards.splice(newIndex, 0, movedItem)
        }
      }
    })
  } catch (error) {
    console.error('Failed to initialize awards sortable:', error)
    ElMessage.warning('拖动排序功能初始化失败，请刷新页面重试')
  }
}

const editCompetition = (competition) => {
  // Populate form with competition data
  competitionForm.value = {
    competitionTitle: competition.competitionTitle,
    shortTitle: competition.shortTitle,
    description: competition.description,
    category: competition.category,
    level: competition.level,
    organizer: competition.organizer,
    competitionStatus: competition.competitionStatus,
    signupStart: competition.signupStart ? new Date(competition.signupStart) : null,
    submitStart: competition.submitStart ? new Date(competition.submitStart) : null,
    reviewStart: competition.reviewStart ? new Date(competition.reviewStart) : null,
    awardPublishStart: competition.awardPublishStart ? new Date(competition.awardPublishStart) : null,
    maxTeamSize: competition.maxTeamSize || 5
  }
  editingCompetitionId.value = competition.competitionId
  showCreateDialog.value = true
}

const deleteCompetition = async (id) => {
  try {
    const response = await competitionAPI.deleteCompetition(id)
    if (response.success) {
      ElMessage.success('竞赛已删除')
      loadCompetitions()
    }
  } catch (error) {
    ElMessage.error('删除竞赛失败')
  }
}

// Watch dialog state to initialize sortable when it opens
watch(showCreateDialog, async (newVal) => {
  if (newVal && !editingCompetitionId.value) {
    await nextTick()
    // Clean up previous instance if exists
    if (awardsSortableInstance) {
      awardsSortableInstance.destroy()
      awardsSortableInstance = null
    }
    // Initialize if there are awards
    if (competitionForm.value.awards.length > 0 && awardsContainerRef.value) {
      initAwardsSortable()
    }
  } else if (!newVal) {
    // Clean up when dialog closes
    if (awardsSortableInstance) {
      awardsSortableInstance.destroy()
      awardsSortableInstance = null
    }
  }
})

onMounted(() => {
  loadCompetitions()
})
</script>

<style scoped>
.admin-competitions {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.award-card {
  transition: all 0.3s ease;
}

.award-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.drag-handle {
  cursor: grab;
  transition: color 0.2s ease;
}

.drag-handle:hover {
  color: #409eff !important;
}

.drag-handle:active {
  cursor: grabbing;
}

:deep(.sortable-ghost) {
  opacity: 0.5;
  background: #f5f7fa;
}
</style>
