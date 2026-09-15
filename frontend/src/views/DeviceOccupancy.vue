<template>
  <div class="device-occupancy">
    <el-card>
      <div class="card-header">
        <div class="card-title">
          <h2>设备占用一览</h2>
          <el-tag type="primary" effect="plain" size="small">可见范围：全部设备</el-tag>
        </div>
        <div class="header-stats">
          <el-tag effect="plain">设备总数 {{ occupancyList.length }}</el-tag>
          <el-tag type="success" effect="plain">当日总容量 {{ totalCapacity }} 人</el-tag>
          <el-tag type="warning" effect="plain">已排 {{ totalAllocated }} 人</el-tag>
          <el-tag type="info" effect="plain">剩余 {{ totalRemaining }} 人</el-tag>
        </div>
      </div>

      <el-form :inline="true" class="search-form">
        <el-form-item label="参观日">
          <el-date-picker
            v-model="visitDate"
            type="date"
            placeholder="选择参观日"
            value-format="YYYY-MM-DD"
            :clearable="false"
            @change="loadOccupancy"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="loadOccupancy">刷新</el-button>
        </el-form-item>
      </el-form>

      <!-- 已排/剩余每次打开都按当前有效占用实时汇总，占用变动后刷新即与台账一致 -->
      <el-table v-loading="loading" :data="occupancyList" border stripe :row-class-name="rowClassName">
        <el-table-column prop="deviceCode" label="设备编号" width="110" />
        <el-table-column prop="deviceName" label="互动实验设备" min-width="150" />
        <el-table-column prop="slotCount" label="当日时段数" width="100" align="center" />
        <el-table-column label="已排人数" width="110" align="center">
          <template #default="scope">
            <span class="occupancy-num">{{ scope.row.allocatedStudents }}</span> 人
          </template>
        </el-table-column>
        <el-table-column label="当日容量" width="110" align="center">
          <template #default="scope">
            <span class="occupancy-num">{{ scope.row.totalCapacity }}</span> 人
          </template>
        </el-table-column>
        <el-table-column label="剩余容量" width="110" align="center">
          <template #default="scope">
            <span :class="scope.row.remainingCapacity > 0 ? 'remaining-num' : 'full-num'">
              {{ scope.row.remainingCapacity }}
            </span> 人
          </template>
        </el-table-column>
        <el-table-column label="占用情况" min-width="180" align="center">
          <template #default="scope">
            <el-progress
              :percentage="usagePercent(scope.row)"
              :status="progressStatus(scope.row)"
              :stroke-width="14"
            />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.remainingCapacity <= 0" type="danger" effect="dark">已满</el-tag>
            <el-tag v-else-if="usagePercent(scope.row) >= 80" type="warning" effect="plain">紧张</el-tag>
            <el-tag v-else type="success" effect="plain">充足</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && occupancyList.length === 0" class="empty-state">
        <p>所选参观日暂无设备数据</p>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { allocationApi, type DeviceOccupancy } from '@/api'

const today = () => {
  const d = new Date()
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

const visitDate = ref<string>(today())
const occupancyList = ref<DeviceOccupancy[]>([])
const loading = ref(false)

const totalCapacity = computed(() =>
  occupancyList.value.reduce((sum, item) => sum + item.totalCapacity, 0)
)
const totalAllocated = computed(() =>
  occupancyList.value.reduce((sum, item) => sum + item.allocatedStudents, 0)
)
const totalRemaining = computed(() =>
  occupancyList.value.reduce((sum, item) => sum + item.remainingCapacity, 0)
)

// 占用率按 已排/当日容量 计算；当日无时段（容量为 0）但有占用时视为满负荷
const usagePercent = (row: DeviceOccupancy) => {
  if (row.totalCapacity <= 0) return row.allocatedStudents > 0 ? 100 : 0
  return Math.min(100, Math.round((row.allocatedStudents / row.totalCapacity) * 100))
}

const progressStatus = (row: DeviceOccupancy) => {
  if (row.remainingCapacity <= 0) return 'exception'
  if (usagePercent(row) >= 80) return 'warning'
  return 'success'
}

const rowClassName = ({ row }: { row: DeviceOccupancy }) =>
  row.remainingCapacity <= 0 && (row.totalCapacity > 0 || row.allocatedStudents > 0) ? 'full-row' : ''

const loadOccupancy = async () => {
  if (!visitDate.value) return
  loading.value = true
  try {
    occupancyList.value = await allocationApi.getOccupancy(visitDate.value)
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.error || '加载设备占用失败')
  } finally {
    loading.value = false
  }
}

loadOccupancy()
</script>

<style scoped>
.device-occupancy {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-title h2 {
  margin: 0;
  font-size: 18px;
}

.header-stats {
  display: flex;
  gap: 10px;
}

.search-form {
  margin-bottom: 20px;
}

.occupancy-num {
  font-weight: bold;
  color: #303133;
}

.remaining-num {
  font-weight: bold;
  color: #67c23a;
}

.full-num {
  font-weight: bold;
  color: #f56c6c;
}

.empty-state {
  text-align: center;
  padding: 50px;
  color: #999;
}

:deep(.full-row) {
  background-color: #fef0f0 !important;
}

:deep(.full-row td) {
  background-color: #fef0f0 !important;
}
</style>
