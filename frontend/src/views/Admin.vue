<template>
  <div class="admin-container">
    <el-container>
      <el-aside width="200px">
        <el-menu
          :default-active="$route.path"
          :router="true"
          class="admin-menu"
        >
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/competitions">
            <el-icon><Trophy /></el-icon>
            <span>竞赛管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/registrations">
            <el-icon><Document /></el-icon>
            <span>报名管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/judges">
            <el-icon><Edit /></el-icon>
            <span>评审管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/awards">
            <el-icon><Medal /></el-icon>
            <span>奖项管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
</script>

<style scoped>
.admin-container {
  background: #fff;
  border-radius: 4px;
  min-height: calc(100vh - 100px);
}

.admin-menu {
  height: 100%;
  border-right: 1px solid #e8e8e8;
}
</style>
