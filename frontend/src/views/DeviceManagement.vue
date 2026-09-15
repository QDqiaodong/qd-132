<template>
  <div class="device-management">
    <el-card>
      <div class="card-header">
        <h2>设备管理</h2>
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>
          添加设备
        </el-button>
      </div>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="设备编号">
          <el-input v-model="searchForm.deviceCode" placeholder="请输入设备编号" />
        </el-form-item>
        <el-form-item label="设备名称">
          <el-input v-model="searchForm.deviceName" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部">
            <el-option :label="'启用'" :value="1" />
            <el-option :label="'停用'" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadDevices">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="devices" border stripe>
        <el-table-column prop="deviceCode" label="设备编号" width="120" />
        <el-table-column prop="deviceName" label="设备名称" width="150" />
        <el-table-column prop="description" label="设备描述" />
        <el-table-column prop="experienceDuration" label="体验时长(分钟)" width="120" />
        <el-table-column label="适配年龄段" width="120">
          <template #default="scope">
            {{ scope.row.minAge }}-{{ scope.row.maxAge }}岁
          </template>
        </el-table-column>
        <el-table-column prop="capacity" label="容量(人)" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteDevice(scope.row.id)">删除</el-button>
            <el-button size="small" @click="viewSlots(scope.row)">时段配置</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        layout="total, prev, pager, next"
        @current-change="loadDevices"
        class="pagination"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑设备' : '添加设备'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="设备编号" required>
          <el-input v-model="form.deviceCode" />
        </el-form-item>
        <el-form-item label="设备名称" required>
          <el-input v-model="form.deviceName" />
        </el-form-item>
        <el-form-item label="设备描述">
          <el-input type="textarea" v-model="form.description" />
        </el-form-item>
        <el-form-item label="体验时长(分钟)" required>
          <el-input-number v-model="form.experienceDuration" :min="5" :max="120" />
        </el-form-item>
        <el-form-item label="最小年龄(岁)" required>
          <el-input-number v-model="form.minAge" :min="3" :max="18" />
        </el-form-item>
        <el-form-item label="最大年龄(岁)" required>
          <el-input-number v-model="form.maxAge" :min="3" :max="18" />
        </el-form-item>
        <el-form-item label="容量(人)" required>
          <el-input-number v-model="form.capacity" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="状态" required>
          <el-select v-model="form.status">
            <el-option :label="'启用'" :value="1" />
            <el-option :label="'停用'" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveDevice">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="slotDialogVisible" title="时段配置" width="700px">
      <div v-if="currentDevice">
        <el-form :inline="true" class="slot-header">
          <el-form-item label="设备名称">
            <el-input :value="currentDevice.deviceName" disabled />
          </el-form-item>
        </el-form>
        <el-button type="primary" @click="openAddSlotDialog" class="add-slot-btn">
          <el-icon><Plus /></el-icon>
          添加时段
        </el-button>
        <el-table :data="timeSlots" border stripe>
          <el-table-column prop="startTime" label="开始时间" width="120" />
          <el-table-column prop="endTime" label="结束时间" width="120" />
          <el-table-column prop="dayOfWeek" label="星期" width="80">
            <template #default="scope">
              {{ getDayOfWeek(scope.row.dayOfWeek) }}
            </template>
          </el-table-column>
          <el-table-column prop="available" label="可用" width="80">
            <template #default="scope">
              <el-switch v-model="scope.row.available" />
            </template>
          </el-table-column>
          <el-table-column prop="sortOrder" label="排序" width="80" />
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button size="small" @click="editSlot(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteSlot(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <el-dialog v-model="slotFormVisible" :title="isSlotEdit ? '编辑时段' : '添加时段'" width="500px">
      <el-form :model="slotForm" label-width="100px">
        <el-form-item label="开始时间" required>
          <el-time-select v-model="slotForm.startTime" :options="timeOptions" />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-time-select v-model="slotForm.endTime" :options="timeOptions" />
        </el-form-item>
        <el-form-item label="星期" required>
          <el-select v-model="slotForm.dayOfWeek">
            <el-option v-for="day in weekDays" :key="day.value" :label="day.label" :value="day.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" required>
          <el-input-number v-model="slotForm.sortOrder" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="slotFormVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSlot">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { deviceApi, type Device, type TimeSlot } from '@/api'

const devices = ref<Device[]>([])
const dialogVisible = ref(false)
const slotDialogVisible = ref(false)
const slotFormVisible = ref(false)
const isEdit = ref(false)
const isSlotEdit = ref(false)
const currentDevice = ref<Device | null>(null)
const timeSlots = ref<TimeSlot[]>([])

const searchForm = reactive({
  deviceCode: '',
  deviceName: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: 0,
  deviceCode: '',
  deviceName: '',
  description: '',
  experienceDuration: 30,
  minAge: 6,
  maxAge: 12,
  capacity: 20,
  status: 1,
  version: 0
})

// 保存进行中标记：防止连点产生两个并发更新请求
const saving = ref(false)

const slotForm = reactive({
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

const loadDevices = async () => {
  try {
    const data = await deviceApi.getAll()
    devices.value = data
    pagination.total = data.length
  } catch (error) {
    ElMessage.error('加载设备失败')
  }
}

const openAddDialog = () => {
  isEdit.value = false
  Object.assign(form, {
    id: 0,
    deviceCode: '',
    deviceName: '',
    description: '',
    experienceDuration: 30,
    minAge: 6,
    maxAge: 12,
    capacity: 20,
    status: 1,
    version: 0
  })
  dialogVisible.value = true
}

const openEditDialog = (device: Device) => {
  isEdit.value = true
  Object.assign(form, device)
  dialogVisible.value = true
}

const saveDevice = async () => {
  if (saving.value) return
  // 快照本次提交内容：并发被拒时还能把对方已保存的稿子和自己这份一起展示
  const submitted = { ...form }
  saving.value = true
  try {
    let saved: Device
    if (isEdit.value) {
      saved = await deviceApi.update(form.id, { ...form })
      ElMessage.success('设备更新成功')
    } else {
      saved = await deviceApi.create({ ...form })
      ElMessage.success('设备添加成功')
    }
    dialogVisible.value = false
    // 保存成功后设备页必须带出刚保存的那一份：
    // 先把服务端返回的最新行（含新的 version）写进列表，再等待全量重新拉取完成，
    // 避免刷新请求在途时页面仍短暂显示上一份旧稿。
    const idx = devices.value.findIndex(d => d.id === saved.id)
    if (idx >= 0) {
      devices.value[idx] = saved
    }
    await loadDevices()
  } catch (error: any) {
    if (error?.response?.status === 409) {
      // 两人几乎同时改同一台：后到的这次没有落库。
      // 不关弹窗、不报成功，拉取先到者已保存的档案展示给后到的人看。
      let latest: Device | null = null
      try {
        latest = await deviceApi.getById(form.id)
      } catch {
        latest = null
      }
      if (latest) {
        await ElMessageBox.alert(
          h('div', [
            h('p', error.response.data?.error || '该设备刚被其他人保存过，您本次的修改未生效。'),
            h('p', { style: 'margin-top:10px;font-weight:bold;' }, '设备页上当前保留的讲解词：'),
            h('p', { style: 'white-space:pre-wrap;' }, latest.description || '（空）'),
            h('p', { style: 'margin-top:10px;font-weight:bold;' }, '您这次未保存的讲解词：'),
            h('p', { style: 'white-space:pre-wrap;' }, submitted.description || '（空）')
          ]),
          '保存被挡住：已有更新的版本先保存',
          { type: 'warning', confirmButtonText: '我知道了，按最新档案处理' }
        )
        // 确认后以服务端最新档案为准：设备页表格与弹窗都显示先到者保存的那一份，
        // 表单 version 同步为最新；此时关掉页面再打开，也不会把被挡住的内容当成已保存。
        const ci = devices.value.findIndex(d => d.id === latest!.id)
        if (ci >= 0) devices.value[ci] = latest
        Object.assign(form, latest)
        await loadDevices()
      } else {
        ElMessage.error('保存未生效，且未能获取最新设备档案，请重新打开设备页查看')
      }
    } else {
      ElMessage.error(error?.response?.data?.error || '操作失败')
    }
  } finally {
    saving.value = false
  }
}

const deleteDevice = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该设备吗？', '提示', { type: 'warning' })
    await deviceApi.delete(id)
    ElMessage.success('设备删除成功')
    loadDevices()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const viewSlots = async (device: Device) => {
  currentDevice.value = device
  try {
    timeSlots.value = await deviceApi.getSlots(device.id)
  } catch (error) {
    ElMessage.error('加载时段失败')
  }
  slotDialogVisible.value = true
}

const openAddSlotDialog = () => {
  isSlotEdit.value = false
  Object.assign(slotForm, {
    id: 0,
    deviceId: currentDevice.value?.id || 0,
    startTime: '09:00',
    endTime: '10:00',
    dayOfWeek: 1,
    available: true,
    sortOrder: timeSlots.value.length + 1
  })
  slotFormVisible.value = true
}

const editSlot = (slot: TimeSlot) => {
  isSlotEdit.value = true
  Object.assign(slotForm, slot)
  slotFormVisible.value = true
}

const saveSlot = async () => {
  try {
    if (isSlotEdit.value) {
      await deviceApi.updateSlot(slotForm.id, slotForm)
      ElMessage.success('时段更新成功')
    } else {
      await deviceApi.addSlot(currentDevice.value!.id, slotForm)
      ElMessage.success('时段添加成功')
    }
    slotFormVisible.value = false
    if (currentDevice.value) {
      timeSlots.value = await deviceApi.getSlots(currentDevice.value.id)
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const deleteSlot = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该时段吗？', '提示', { type: 'warning' })
    await deviceApi.deleteSlot(id)
    ElMessage.success('时段删除成功')
    if (currentDevice.value) {
      timeSlots.value = await deviceApi.getSlots(currentDevice.value.id)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const resetSearch = () => {
  Object.assign(searchForm, { deviceCode: '', deviceName: '', status: '' })
  loadDevices()
}

loadDevices()
</script>

<style scoped>
.device-management {
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

.slot-header {
  margin-bottom: 15px;
}

.add-slot-btn {
  margin-bottom: 15px;
}
</style>