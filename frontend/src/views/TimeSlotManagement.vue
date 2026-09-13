<template>
  <div class="time-slot-management">
    <el-card>
      <div class="card-header">
        <h2>时段配置管理</h2>
      </div>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="设备">
          <el-select v-model="searchForm.deviceId" placeholder="全部设备">
            <el-option :label="'全部'" :value="0" />
            <el-option v-for="device in devices" :key="device.id" :label="device.deviceName" :value="device.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="星期">
          <el-select v-model="searchForm.dayOfWeek" placeholder="全部">
            <el-option :label="'全部'" :value="0" />
            <el-option v-for="day in weekDays" :key="day.value" :label="day.label" :value="day.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchSlots">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="timeSlots" border stripe>
        <el-table-column prop="deviceId" label="设备编号" width="100">
          <template #default="scope">
            {{ getDeviceCode(scope.row.deviceId) }}
          </template>
        </el-table-column>
        <el-table-column label="设备名称" width="150">
          <template #default="scope">
            {{ getDeviceName(scope.row.deviceId) }}
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="120" />
        <el-table-column prop="endTime" label="结束时间" width="120" />
        <el-table-column prop="dayOfWeek" label="星期" width="80">
          <template #default="scope">
            {{ getDayOfWeek(scope.row.dayOfWeek) }}
          </template>
        </el-table-column>
        <el-table-column prop="available" label="可用" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.available ? 'success' : 'danger'">
              {{ scope.row.available ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteSlot(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        layout="total, prev, pager, next"
        class="pagination"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" title="编辑时段" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="设备" required>
          <el-select v-model="form.deviceId">
            <el-option v-for="device in devices" :key="device.id" :label="device.deviceName" :value="device.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" required>
          <el-time-select v-model="form.startTime" :options="timeOptions" />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-time-select v-model="form.endTime" :options="timeOptions" />
        </el-form-item>
        <el-form-item label="星期" required>
          <el-select v-model="form.dayOfWeek">
            <el-option v-for="day in weekDays" :key="day.value" :label="day.label" :value="day.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="可用" required>
          <el-switch v-model="form.available" />
        </el-form-item>
        <el-form-item label="排序" required>
          <el-input-number v-model="form.sortOrder" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSlot">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deviceApi, type Device, type TimeSlot } from '@/api'

const devices = ref<Device[]>([])
const timeSlots = ref<TimeSlot[]>([])
const dialogVisible = ref(false)

const searchForm = reactive({
  deviceId: 0,
  dayOfWeek: 0
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: 0,
  deviceId: 0,
  startTime: '09:00',
  endTime: '10:00',
  dayOfWeek: 1,
  available: true,
  sortOrder: 1
})

const weekDays = [
  { value: 1, label: '周一' },
  { value: 2, label: '周二' },
  { value: 3, label: '周三' },
  { value: 4, label: '周四' },
  { value: 5, label: '周五' },
  { value: 6, label: '周六' },
  { value: 7, label: '周日' }
]

const timeOptions = [
  { label: '09:00', value: '09:00' },
  { label: '10:00', value: '10:00' },
  { label: '11:00', value: '11:00' },
  { label: '13:00', value: '13:00' },
  { label: '14:00', value: '14:00' },
  { label: '15:00', value: '15:00' },
  { label: '16:00', value: '16:00' },
  { label: '17:00', value: '17:00' }
]

const getDayOfWeek = (day: number) => {
  const days = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']
  return days[day] || ''
}

const getDeviceName = (deviceId: number) => {
  const device = devices.value.find(d => d.id === deviceId)
  return device ? device.deviceName : ''
}

const getDeviceCode = (deviceId: number) => {
  const device = devices.value.find(d => d.id === deviceId)
  return device ? device.deviceCode : ''
}

const loadDevices = async () => {
  try {
    devices.value = await deviceApi.getAll()
  } catch (error) {
    ElMessage.error('加载设备列表失败')
  }
}

const loadAllSlots = async () => {
  timeSlots.value = []
  for (const device of devices.value) {
    try {
      const slots = await deviceApi.getSlots(device.id)
      timeSlots.value = [...timeSlots.value, ...slots]
    } catch (error) {
      console.error('加载时段失败:', error)
    }
  }
  pagination.total = timeSlots.value.length
}

const searchSlots = () => {
  let filtered = [...timeSlots.value]
  if (searchForm.deviceId > 0) {
    filtered = filtered.filter(s => s.deviceId === searchForm.deviceId)
  }
  if (searchForm.dayOfWeek > 0) {
    filtered = filtered.filter(s => s.dayOfWeek === searchForm.dayOfWeek)
  }
  timeSlots.value = filtered
}

const resetSearch = async () => {
  Object.assign(searchForm, { deviceId: 0, dayOfWeek: 0 })
  await loadAllSlots()
}

const openEditDialog = (slot: TimeSlot) => {
  Object.assign(form, slot)
  dialogVisible.value = true
}

const saveSlot = async () => {
  try {
    await deviceApi.updateSlot(form.id, form)
    ElMessage.success('时段更新成功')
    dialogVisible.value = false
    await loadAllSlots()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const deleteSlot = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该时段吗？', '提示', { type: 'warning' })
    await deviceApi.deleteSlot(id)
    ElMessage.success('时段删除成功')
    await loadAllSlots()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(async () => {
  await loadDevices()
  await loadAllSlots()
})
</script>

<style scoped>
.time-slot-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>