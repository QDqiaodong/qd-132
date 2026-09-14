<template>
  <router-view v-if="!authState.user" />
  <div v-else class="app-container">
    <el-container>
      <el-aside width="220px" class="sidebar">
        <div class="logo">
          <el-icon :size="32" color="#409EFF"><Ship /></el-icon>
          <span>航天科普馆</span>
        </div>
        <el-menu :default-active="activeMenu" mode="vertical" router class="menu">
          <template v-if="isStaff">
            <el-menu-item index="/devices">
              <el-icon><Monitor /></el-icon>
              <span>设备管理</span>
            </el-menu-item>
            <el-menu-item index="/time-slots">
              <el-icon><Clock /></el-icon>
              <span>时段配置</span>
            </el-menu-item>
            <el-menu-item index="/study-groups">
              <el-icon><User /></el-icon>
              <span>研学团管理</span>
            </el-menu-item>
          </template>
          <el-menu-item index="/allocations">
            <el-icon><Grid /></el-icon>
            <span>配对台账</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="header">
          <div class="header-title">研学团参观时段配对台账系统</div>
          <div class="header-user">
            <el-tag :type="isStaff ? 'primary' : 'success'" effect="plain">
              {{ userLabel }}
            </el-tag>
            <el-button size="small" @click="logout">退出登录</el-button>
          </div>
        </el-header>
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Ship, Monitor, Clock, User, Grid } from '@element-plus/icons-vue'
import { authState, clearAuth } from '@/auth'

const route = useRoute()
const router = useRouter()
const activeMenu = computed(() => route.path)
const isStaff = computed(() => authState.user?.role === 'STAFF')
const userLabel = computed(() => {
  const user = authState.user
  if (!user) return ''
  return user.role === 'STAFF'
    ? '馆务 · 全部研学团'
    : `带队老师 · 仅本团（${user.groupName || user.groupCode}）`
})

const logout = () => {
  clearAuth()
  router.push('/login')
}
</script>

<style scoped>
.app-container {
  height: 100vh;
  display: flex;
}

.sidebar {
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  color: white;
  display: flex;
  flex-direction: column;
}

.logo {
  padding: 20px;
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.menu {
  flex: 1;
  margin-top: 20px;
}

.header {
  background: white;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-title {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.header-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.main {
  background: #f5f7fa;
  padding: 20px;
}
</style>
