<template>
  <div class="inventory-ledger">
    <el-card>
      <div class="card-header">
        <h2>设备盘点台账</h2>
        <div class="header-stats">
          <el-tag effect="plain">设备总数 {{ inventoryList.length }}</el-tag>
          <el-tag type="danger" effect="plain">盘点不符 {{ mismatchCount }}</el-tag>
          <el-tag type="success" effect="plain">相符 {{ matchedCount }}</el-tag>
          <el-tag type="info" effect="plain">未盘点 {{ uncountedCount }}</el-tag>
        </div>
      </div>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="设备编号">
          <el-input v-model="searchForm.deviceCode" placeholder="请输入设备编号" clearable />
        </el-form-item>
        <el-form-item label="设备名称">
          <el-input v-model="searchForm.deviceName" placeholder="请输入设备名称" clearable />
        </el-form-item>
        <el-form-item label="盘点状态">
          <el-select v-model="searchForm.status" placeholder="全部" style="width: 120px">
            <el-option label="全部" value="all" />
            <el-option label="盘点不符" value="mismatch" />
            <el-option label="相符" value="matched" />
            <el-option label="未盘点" value="uncounted" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadInventory">刷新</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="filteredList" border stripe :row-class-name="rowClassName">
        <el-table-column prop="deviceCode" label="设备编号" width="110" />
        <el-table-column prop="deviceName" label="互动实验设备" min-width="150" />
        <el-table-column prop="expectedParts" label="应出库配件件数" width="130" align="center">
          <template #default="scope">
            <span class="parts-num">{{ scope.row.expectedParts }}</span> 件
          </template>
        </el-table-column>
        <el-table-column label="实到件数" width="110" align="center">
          <template #default="scope">
            <span v-if="scope.row.actualParts !== null" class="parts-num">{{ scope.row.actualParts }}</span>
            <span v-else class="uncounted">未盘点</span>
          </template>
        </el-table-column>
        <el-table-column label="差异" width="150" align="center">
          <template #default="scope">
            <span v-if="scope.row.actualParts === null" class="uncounted">—</span>
            <span v-else-if="scope.row.mismatch" class="diff-num">
              {{ scope.row.differenceCount! > 0 ? '多' : '少' }}{{ Math.abs(scope.row.differenceCount!) }} 件
            </span>
            <span v-else class="matched-text">0 件</span>
          </template>
        </el-table-column>
        <el-table-column label="盘点状态" width="240">
          <template #default="scope">
            <!-- 两数对不上：行上标成盘点不符，并写清应出和实到各是多少；对得上的行不带这枚标记 -->
            <el-tag v-if="scope.row.mismatch" type="danger" effect="dark">
              盘点不符（应出 {{ scope.row.expectedParts }} / 实到 {{ scope.row.actualParts }}）
            </el-tag>
            <span v-else-if="scope.row.actualParts !== null" class="matched-text">相符</span>
            <span v-else class="uncounted">待盘点</span>
          </template>
        </el-table-column>
        <el-table-column label="盘点时间" width="170" align="center">
          <template #default="scope">
            {{ scope.row.inventoryTime ? formatTime(scope.row.inventoryTime) : '—' }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140">
          <template #default="scope">{{ scope.row.remark || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="openEditDialog(scope.row)">登记盘点</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="editDialogVisible" title="设备盘点登记" width="520px">
      <el-form :model="editForm" label-width="130px">
        <el-form-item label="设备编号">
          <el-input :model-value="editForm.deviceCode" disabled />
        </el-form-item>
        <el-form-item label="设备名称">
          <el-input :model-value="editForm.deviceName" disabled />
        </el-form-item>
        <el-form-item label="应出库配件件数" required>
          <el-input-number v-model="editForm.expectedParts" :min="0" :precision="0" />
        </el-form-item>
        <el-form-item label="实到件数" required>
          <el-input-number v-model="editForm.actualParts" :min="0" :precision="0" />
        </el-form-item>
        <!-- 按当前输入即时预览差异标记，保存后以服务端按新数字重算的结果为准 -->
        <el-form-item label="差异校验">
          <el-tag v-if="previewMismatch" type="danger" effect="dark">
            盘点不符（应出 {{ editForm.expectedParts }} / 实到 {{ editForm.actualParts }}，
            {{ previewDifference! > 0 ? '多' : '少' }}{{ Math.abs(previewDifference!) }} 件）
          </el-tag>
          <el-tag v-else type="success" effect="plain">数量相符，无差异</el-tag>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.remark" type="textarea" placeholder="差异原因或备注（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveInventory">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { inventoryApi, type DeviceInventory } from '@/api'

const inventoryList = ref<DeviceInventory[]>([])
const editDialogVisible = ref(false)
const saving = ref(false)

const searchForm = reactive({
  deviceCode: '',
  deviceName: '',
  status: 'all'
})

const editForm = reactive({
  id: 0,
  deviceId: 0,
  deviceCode: '',
  deviceName: '',
  expectedParts: 0,
  actualParts: 0,
  remark: ''
})

const mismatchCount = computed(() => inventoryList.value.filter(i => i.mismatch).length)
const matchedCount = computed(() =>
  inventoryList.value.filter(i => i.actualParts !== null && !i.mismatch).length
)
const uncountedCount = computed(() =>
  inventoryList.value.filter(i => i.actualParts === null).length
)

const filteredList = computed(() => {
  return inventoryList.value.filter(item => {
    if (searchForm.deviceCode && !item.deviceCode.includes(searchForm.deviceCode.trim())) return false
    if (searchForm.deviceName && !item.deviceName.includes(searchForm.deviceName.trim())) return false
    if (searchForm.status === 'mismatch' && !item.mismatch) return false
    if (searchForm.status === 'matched' && !(item.actualParts !== null && !item.mismatch)) return false
    if (searchForm.status === 'uncounted' && item.actualParts !== null) return false
    return true
  })
})

// 差异标记随输入的新数字实时重算，与服务端口径保持一致：应出 ≠ 实到即盘点不符
const previewDifference = computed(() => editForm.actualParts - editForm.expectedParts)
const previewMismatch = computed(() => previewDifference.value !== 0)

const rowClassName = ({ row }: { row: DeviceInventory }) =>
  row.mismatch ? 'mismatch-row' : ''

const formatTime = (time: string) => time.replace('T', ' ').slice(0, 16)

const loadInventory = async () => {
  try {
    inventoryList.value = await inventoryApi.getAll()
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.error || '加载盘点台账失败')
  }
}

const openEditDialog = (row: DeviceInventory) => {
  Object.assign(editForm, {
    id: row.id,
    deviceId: row.deviceId,
    deviceCode: row.deviceCode,
    deviceName: row.deviceName,
    expectedParts: row.expectedParts,
    actualParts: row.actualParts ?? 0,
    remark: row.remark || ''
  })
  editDialogVisible.value = true
}

const saveInventory = async () => {
  saving.value = true
  try {
    // 服务端按提交的新应出/实到重新计算差异并覆盖 mismatch 标记
    await inventoryApi.update(editForm.id, {
      expectedParts: editForm.expectedParts,
      actualParts: editForm.actualParts,
      remark: editForm.remark
    })
    ElMessage.success('盘点登记已保存，差异标记已按新数字重算')
    editDialogVisible.value = false
    await loadInventory()
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.error || '保存盘点失败')
  } finally {
    saving.value = false
  }
}

loadInventory()
</script>

<style scoped>
.inventory-ledger {
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

.header-stats {
  display: flex;
  gap: 10px;
}

.search-form {
  margin-bottom: 20px;
}

.parts-num {
  font-weight: bold;
  color: #303133;
}

.diff-num {
  color: #f56c6c;
  font-weight: bold;
}

.matched-text {
  color: #67c23a;
}

.uncounted {
  color: #909399;
}

:deep(.mismatch-row) {
  background-color: #fef0f0 !important;
}

:deep(.mismatch-row td) {
  background-color: #fef0f0 !important;
}
</style>
