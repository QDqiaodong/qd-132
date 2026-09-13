<template>
  <div class="app-container">
    <el-container>
      <el-aside width="220px" class="sidebar">
        <div class="logo">
          <el-icon :size="32" color="#409EFF"><Ship /></el-icon>
          <span>航天科普馆</span>
        </div>
        <el-menu :default-active="activeMenu" mode="vertical" router class="menu">
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
          <el-menu-item index="/allocations">
            <el-icon><Grid /></el-icon>
            <span>配对台账</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="header">
          <div class="header-title">研学团参观时段配对台账系统</div>
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
import { useRoute } from 'vue-router'
import { Ship, Monitor, Clock, User, Grid } from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => route.path)
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
  padding: 0 20px;
}

.header-title {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.main {
  background: #f5f7fa;
  padding: 20px;
}
</style>