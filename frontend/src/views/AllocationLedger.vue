<template>
  <div class="allocation-ledger">
    <el-card>
      <div class="card-header">
        <h2>配对台账</h2>
        <el-button type="primary" @click="openManualAllocateDialog">
          <el-icon><Plus /></el-icon>
          手动分配
        </el-button>
      </div>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="参观日期">
          <el-date-picker v-model="searchForm.visitDate" type="date" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="团号">
          <el-input v-model="searchForm.groupCode" placeholder="请输入团号" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadAllocations">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-timeline>
        <el-timeline-item
          v-for="group in groupAllocations"
          :key="group.groupId"
          :timestamp="group.visitDate"
          placement="top"
        >
          <el-card shadow="hover" class="group-card">
            <div class="group-header">
              <div class="group-info">
                <h3>{{ group.groupName }}</h3>
                <p>团号: {{ group.groupCode }} | 学校: {{ group.schoolName || '-' }}</p>
                <p>学生人数: {{ group.totalStudents }}人 | 平均年龄: {{ group.averageAge }}岁</p>
              </div>
              <div class="group-actions">
                <el-button size="small" @click="viewGroupDetail(group)">查看详情</el-button>
                <el-button size="small" type="danger" @click="cancelGroupAllocations(group.groupId)">取消分配</el-button>
                <el-button size="small" type="success" @click="reallocateGroup(group.groupId)">重新分配</el-button>
              </div>
            </div>
            
            <div v-if="group.allocations.length > 0">
              <el-table :data="group.allocations" border stripe size="small">
                <el-table-column prop="deviceCode" label="设备编号" width="120" />
                <el-table-column prop="deviceName" label="设备名称" width="150" />
                <el-table-column prop="startTime" label="开始时间" width="120" />
                <el-table-column prop="endTime" label="结束时间" width="120" />
                <el-table-column prop="studentCount" label="分配人数" width="100" />
                <el-table-column prop="batchNumber" label="批次号" />
                <el-table-column label="操作" width="120">
                  <template #default="scope">
                    <el-button size="small" type="danger" @click="cancelAllocation(scope.row.allocationId)">取消</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <div v-else class="no-allocation">
              <p>暂无分配记录，请点击"重新分配"进行自动配对</p>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>

      <div v-if="groupAllocations.length === 0" class="empty-state">
        <el-icon :size="64" color="#ccc"><Grid /></el-icon>
        <p>暂无分配记录</p>
        <p>请先添加研学团并进行自动分配</p>
      </div>
    </el-card>

    <el-dialog v-model="detailDialogVisible" title="分配详情" width="800px">
      <div v-if="selectedGroup">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="团号">{{ selectedGroup.groupCode }}</el-descriptions-item>
          <el-descriptions-item label="团名">{{ selectedGroup.groupName }}</el-descriptions-item>
          <el-descriptions-item label="学校名称">{{ selectedGroup.schoolName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="学生人数">{{ selectedGroup.totalStudents }}人</el-descriptions-item>
          <el-descriptions-item label="平均年龄">{{ selectedGroup.averageAge }}岁</el-descriptions-item>
          <el-descriptions-item label="参观日期">{{ selectedGroup.visitDate }}</el-descriptions-item>
        </el-descriptions>

        <el-table :data="selectedGroup.allocations" border stripe>
          <el-table-column prop="deviceCode" label="设备编号" width="120" />
          <el-table-column prop="deviceName" label="设备名称" width="150" />
          <el-table-column prop="startTime" label="开始时间" width="120" />
          <el-table-column prop="endTime" label="结束时间" width="120" />
          <el-table-column prop="studentCount" label="分配人数" width="100" />
          <el-table-column prop="batchNumber" label="批次号" />
        </el-table>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="manualDialogVisible" title="手动分配" width="600px">
      <el-form :model="manualForm" label-width="100px">
        <el-form-item label="研学团" required>
          <el-select v-model="manualForm.studyGroupId">
            <el-option v-for="group in studyGroups" :key="group.id" :label="group.groupName" :value="group.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备" required>
          <el-select v-model="manualForm.deviceId" @change="loadDeviceSlots">
            <el-option v-for="device in devices" :key="device.id" :label="device.deviceName" :value="device.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="时段" required>
          <el-select v-model="manualForm.timeSlotId">
            <el-option v-for="slot in deviceSlots" :key="slot.id" 
              :label="`${slot.startTime}-${slot.endTime}`" 
              :value="slot.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学生人数" required>
          <el-input-number v-model="manualForm.studentCount" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="manualDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveManualAllocation">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Grid } from '@element-plus/icons-vue'
import { allocationApi, studyGroupApi, deviceApi, type GroupAllocation, type StudyGroup, type Device, type TimeSlot } from '@/api'

const groupAllocations = ref<GroupAllocation[]>([])
const detailDialogVisible = ref(false)
const manualDialogVisible = ref(false)
const selectedGroup = ref<GroupAllocation | null>(null)
const studyGroups = ref<StudyGroup[]>([])
const devices = ref<Device[]>([])
const deviceSlots = ref<TimeSlot[]>([])

const searchForm = reactive({
  visitDate: '',
  groupCode: ''
})

const manualForm = reactive({
  studyGroupId: 0,
  deviceId: 0,
  timeSlotId: 0,
  studentCount: 1,
  status: 1
})

const loadAllocations = async () => {
  try {
    const data = await allocationApi.getAllGroups()
    let filtered = data
    if (searchForm.visitDate) {
      filtered = filtered.filter(g => g.visitDate === searchForm.visitDate)
    }
    if (searchForm.groupCode) {
      filtered = filtered.filter(g => g.groupCode.includes(searchForm.groupCode))
    }
    groupAllocations.value = filtered
  } catch (error) {
    ElMessage.error('加载分配记录失败')
  }
}

const viewGroupDetail = (group: GroupAllocation) => {
  selectedGroup.value = group
  detailDialogVisible.value = true
}

const cancelAllocation = async (allocationId: number) => {
  try {
    await ElMessageBox.confirm('确定要取消该分配吗？', '提示', { type: 'warning' })
    await allocationApi.cancel(allocationId)
    ElMessage.success('分配已取消')
    loadAllocations()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

const cancelGroupAllocations = async (groupId: number) => {
  try {
    await ElMessageBox.confirm('确定要取消该研学团所有分配吗？', '提示', { type: 'warning' })
    await allocationApi.cancelGroup(groupId)
    ElMessage.success('所有分配已取消')
    loadAllocations()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

const reallocateGroup = async (groupId: number) => {
  try {
    await allocationApi.autoAllocate(groupId)
    ElMessage.success('重新分配成功')
    loadAllocations()
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.error || '重新分配失败')
  }
}

const openManualAllocateDialog = async () => {
  try {
    studyGroups.value = await studyGroupApi.getAll()
    devices.value = await deviceApi.getActive()
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
  manualDialogVisible.value = true
}

const loadDeviceSlots = async () => {
  if (manualForm.deviceId > 0) {
    try {
      deviceSlots.value = await deviceApi.getSlots(manualForm.deviceId)
    } catch (error) {
      ElMessage.error('加载时段失败')
    }
  }
}

const saveManualAllocation = async () => {
  try {
    await allocationApi.manualAllocate(manualForm)
    ElMessage.success('手动分配成功')
    manualDialogVisible.value = false
    loadAllocations()
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.error || '手动分配失败')
  }
}

const resetSearch = () => {
  Object.assign(searchForm, { visitDate: '', groupCode: '' })
  loadAllocations()
}

loadAllocations()
</script>

<style scoped>
.allocation-ledger {
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

.group-card {
  margin-bottom: 20px;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.group-info h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
}

.group-info p {
  margin: 5px 0;
  font-size: 14px;
  color: #666;
}

.group-actions {
  display: flex;
  gap: 8px;
}

.no-allocation {
  text-align: center;
  padding: 20px;
  color: #999;
}

.empty-state {
  text-align: center;
  padding: 50px;
}

.empty-state p {
  margin: 10px 0;
  color: #999;
}
</style>