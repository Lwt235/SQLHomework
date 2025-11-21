<template>
  <div class="admin-judges">
    <el-card>
      <template #header>
        <span>评审管理</span>
      </template>

      <el-table :data="assignments" v-loading="loading" style="width: 100%">
        <el-table-column label="评委" width="120">
          <template #default="{ row }">
            {{ row.user?.realName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="作品" min-width="200">
          <template #default="{ row }">
            {{ row.submission?.submissionTitle || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="score" label="评分" width="100" />
        <el-table-column prop="comment" label="评语" min-width="200" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { judgeAPI } from '../../api'
import { ElMessage } from 'element-plus'

const assignments = ref([])
const loading = ref(false)

const loadAssignments = async () => {
  loading.value = true
  try {
    const response = await judgeAPI.getAllAssignments()
    if (response.success) {
      assignments.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载评审列表失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = (assignment) => {
  ElMessage.info('查看详情功能开发中')
}

onMounted(() => {
  loadAssignments()
})
</script>

<style scoped>
.admin-judges {
  padding: 20px;
}
</style>
