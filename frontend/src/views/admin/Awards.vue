<template>
  <div class="admin-awards">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>奖项管理</span>
          <el-button type="primary" @click="showCreateDialog = true">创建奖项</el-button>
        </div>
      </template>

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
        <el-table-column label="竞赛" min-width="200">
          <template #default="{ row }">
            {{ row.competition?.competitionTitle || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="grantAward(row)">授予奖项</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Create Award Dialog -->
    <el-dialog v-model="showCreateDialog" title="创建奖项" width="600px">
      <el-form :model="awardForm" label-width="120px">
        <el-form-item label="竞赛ID">
          <el-input-number v-model="awardForm.competitionId" :min="1" />
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
        <el-form-item label="评选标准">
          <el-input v-model="awardForm.criteriaDescription" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createAward">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { awardAPI } from '../../api'
import { ElMessage } from 'element-plus'

const awards = ref([])
const loading = ref(false)
const showCreateDialog = ref(false)
const awardForm = ref({
  competitionId: 1,
  awardName: '',
  awardLevel: 'first',
  criteriaDescription: ''
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

const createAward = async () => {
  try {
    const response = await awardAPI.createAward(awardForm.value)
    if (response.success) {
      ElMessage.success('奖项创建成功')
      showCreateDialog.value = false
      loadAwards()
    }
  } catch (error) {
    ElMessage.error('创建奖项失败')
  }
}

const grantAward = (award) => {
  ElMessage.info('授予奖项功能开发中')
}

onMounted(() => {
  loadAwards()
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
</style>
