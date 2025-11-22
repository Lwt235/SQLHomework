<template>
  <div class="admin-users">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-space>
            <el-button type="warning" @click="loadInactiveUsers">
              待审核用户 ({{ inactiveCount }})
            </el-button>
            <el-button @click="loadAllUsers">全部用户</el-button>
          </el-space>
        </div>
      </template>

      <el-table :data="users" v-loading="loading" style="width: 100%">
        <el-table-column prop="userId" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="school" label="学校" width="150" />
        <el-table-column prop="department" label="院系" width="150" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="userStatus" label="状态" width="100" fixed="right">
          <template #default="{ row }">
            <el-tag v-if="row.userStatus === 'active'" type="success">激活</el-tag>
            <el-tag v-else-if="row.userStatus === 'inactive'" type="warning">未激活</el-tag>
            <el-tag v-else-if="row.userStatus === 'suspended'" type="danger">暂停</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.userStatus === 'inactive'" 
              type="success" 
              size="small" 
              @click="activateUser(row.userId)"
              :disabled="row.userId === authStore.user?.userId"
            >
              激活
            </el-button>
            <el-button 
              v-if="row.userStatus === 'active'" 
              type="warning" 
              size="small" 
              @click="suspendUser(row.userId)"
              :disabled="row.userId === authStore.user?.userId"
            >
              暂停
            </el-button>
            <el-button 
              v-if="row.userStatus === 'suspended'" 
              type="success" 
              size="small" 
              @click="activateUser(row.userId)"
              :disabled="row.userId === authStore.user?.userId"
            >
              激活
            </el-button>
            <el-button 
              type="primary" 
              size="small" 
              @click="showRoleDialog(row)"
              :disabled="row.userId === authStore.user?.userId"
            >
              分配权限
            </el-button>
            <el-popconfirm
              v-if="row.userId !== authStore.user?.userId"
              title="确定删除该用户吗？"
              @confirm="deleteUser(row.userId)"
            >
              <template #reference>
                <el-button type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Role Assignment Dialog -->
    <el-dialog v-model="roleDialogVisible" title="分配权限" width="500px">
      <div v-if="selectedUser">
        <p><strong>用户：</strong>{{ selectedUser.username }} ({{ selectedUser.realName }})</p>
        <el-form>
          <el-form-item label="选择角色">
            <el-checkbox-group v-model="selectedRoles">
              <el-checkbox 
                v-for="role in availableRoles" 
                :key="role.roleId" 
                :label="role.roleId"
              >
                {{ role.roleName }} ({{ role.roleCode }})
              </el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="roleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="assignRoles">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userAPI } from '../../api'
import { useAuthStore } from '../../stores/auth'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const users = ref([])
const loading = ref(false)
const inactiveCount = ref(0)
const roleDialogVisible = ref(false)
const selectedUser = ref(null)
const selectedRoles = ref([])
const availableRoles = ref([])

const loadAllUsers = async () => {
  loading.value = true
  try {
    const response = await userAPI.getAllUsers()
    if (response.success) {
      users.value = response.data
      inactiveCount.value = users.value.filter(u => u.userStatus === 'inactive').length
    }
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const loadInactiveUsers = async () => {
  loading.value = true
  try {
    const response = await userAPI.getInactiveUsers()
    if (response.success) {
      users.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载待审核用户失败')
  } finally {
    loading.value = false
  }
}

const loadRoles = async () => {
  try {
    const response = await userAPI.getAllRoles()
    if (response.success) {
      availableRoles.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载角色列表失败')
  }
}

const showRoleDialog = async (user) => {
  selectedUser.value = user
  roleDialogVisible.value = true
  
  // Load user's current roles
  try {
    const response = await userAPI.getUserRoles(user.userId)
    if (response.success) {
      selectedRoles.value = response.data.map(role => role.roleId)
    }
  } catch (error) {
    ElMessage.error('加载用户角色失败')
  }
}

const assignRoles = async () => {
  try {
    const response = await userAPI.assignRoles(selectedUser.value.userId, selectedRoles.value)
    if (response.success) {
      ElMessage.success('权限分配成功')
      roleDialogVisible.value = false
      loadAllUsers()
    }
  } catch (error) {
    ElMessage.error('权限分配失败')
  }
}

const activateUser = async (userId) => {
  try {
    const response = await userAPI.activateUser(userId)
    if (response.success) {
      ElMessage.success('用户已激活')
      loadAllUsers()
    }
  } catch (error) {
    ElMessage.error('激活用户失败')
  }
}

const suspendUser = async (userId) => {
  try {
    const response = await userAPI.suspendUser(userId)
    if (response.success) {
      ElMessage.success('用户已暂停')
      loadAllUsers()
    }
  } catch (error) {
    ElMessage.error('暂停用户失败')
  }
}

const deleteUser = async (userId) => {
  try {
    const response = await userAPI.deleteUser(userId)
    if (response.success) {
      ElMessage.success('用户已删除')
      loadAllUsers()
    }
  } catch (error) {
    ElMessage.error('删除用户失败')
  }
}

onMounted(() => {
  loadAllUsers()
  loadRoles()
})
</script>

<style scoped>
.admin-users {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
