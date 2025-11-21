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
        <el-table-column prop="competitionStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.competitionStatus === 'draft'" type="info">草稿</el-tag>
            <el-tag v-else-if="row.competitionStatus === 'published'">已发布</el-tag>
            <el-tag v-else-if="row.competitionStatus === 'ongoing'" type="success">进行中</el-tag>
            <el-tag v-else-if="row.competitionStatus === 'finished'" type="warning">已结束</el-tag>
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
    <el-dialog v-model="showCreateDialog" title="竞赛信息" width="800px">
      <el-form :model="competitionForm" label-width="120px">
        <el-form-item label="竞赛名称">
          <el-input v-model="competitionForm.competitionTitle" />
        </el-form-item>
        <el-form-item label="简称">
          <el-input v-model="competitionForm.shortTitle" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="competitionForm.description" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="类别">
          <el-input v-model="competitionForm.category" />
        </el-form-item>
        <el-form-item label="级别">
          <el-select v-model="competitionForm.level">
            <el-option label="校级" value="school" />
            <el-option label="省级" value="provincial" />
            <el-option label="国家级" value="national" />
            <el-option label="国际级" value="international" />
          </el-select>
        </el-form-item>
        <el-form-item label="主办方">
          <el-input v-model="competitionForm.organizer" />
        </el-form-item>
        <el-form-item label="最大团队人数">
          <el-input-number v-model="competitionForm.maxTeamSize" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="saveCompetition">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { competitionAPI } from '../../api'
import { ElMessage } from 'element-plus'

const competitions = ref([])
const loading = ref(false)
const showCreateDialog = ref(false)
const competitionForm = ref({
  competitionTitle: '',
  shortTitle: '',
  description: '',
  category: '',
  level: 'school',
  organizer: '',
  maxTeamSize: 5
})

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
  try {
    const response = await competitionAPI.createCompetition(competitionForm.value)
    if (response.success) {
      ElMessage.success('竞赛创建成功')
      showCreateDialog.value = false
      loadCompetitions()
    }
  } catch (error) {
    ElMessage.error('保存竞赛失败')
  }
}

const editCompetition = (competition) => {
  ElMessage.info('编辑功能开发中')
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
</style>
