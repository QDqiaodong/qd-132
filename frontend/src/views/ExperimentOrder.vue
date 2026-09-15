<template>
  <div class="experiment-order">
    <el-card class="toolbar-card">
      <div class="card-header">
        <div class="card-title">
          <h2>实验顺序条</h2>
          <el-tag :type="isStaff ? 'primary' : 'success'" effect="plain" size="small">
            {{ isStaff ? '可选任一研学团' : '仅本团' }}
          </el-tag>
        </div>
        <div class="header-actions">
          <el-button @click="loadOrder">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
          <el-button
            type="primary"
            :disabled="!order"
            @click="printOrder"
          >
            <el-icon><Printer /></el-icon>
            打印顺序条
          </el-button>
        </div>
      </div>

      <el-form :inline="true" class="select-form">
        <el-form-item label="研学团">
          <el-select
            v-model="selectedGroupId"
            :placeholder="isStaff ? '请选择研学团' : ''"
            filterable
            style="width: 360px"
            @change="loadOrder"
          >
            <el-option
              v-for="group in groups"
              :key="group.id"
              :label="`${group.groupName}（${group.groupCode} · ${group.visitDate}）`"
              :value="group.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="order" class="order-card">
      <div class="order-sheet">
        <div class="sheet-header">
          <h1>航天科普馆 · 实验顺序条</h1>
          <p class="sheet-subtitle">带队老师请凭此条按序带队进入各实验项目</p>
        </div>

        <el-descriptions :column="3" border class="sheet-meta">
          <el-descriptions-item label="团号">{{ order.groupCode }}</el-descriptions-item>
          <el-descriptions-item label="团名">{{ order.groupName }}</el-descriptions-item>
          <el-descriptions-item label="学校">{{ order.schoolName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="带队老师">{{ order.contactPerson || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ order.contactPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="参观日期">{{ order.visitDate }}</el-descriptions-item>
          <el-descriptions-item label="团总人数">{{ order.totalStudents }} 人</el-descriptions-item>
          <el-descriptions-item label="已排人数">{{ order.allocatedStudents }} 人</el-descriptions-item>
          <el-descriptions-item label="实验项目数">{{ order.items.length }} 项</el-descriptions-item>
        </el-descriptions>

        <el-table :data="order.items" border stripe class="sheet-table">
          <el-table-column label="顺序" width="80" align="center">
            <template #default="scope">
              <el-tag effect="dark" size="small">第 {{ scope.row.sequence }} 项</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="deviceCode" label="设备编号" width="130" align="center" />
          <el-table-column prop="deviceName" label="实验设备" min-width="160" />
          <el-table-column label="实验时段" width="200" align="center">
            <template #default="scope">{{ scope.row.startTime }} - {{ scope.row.endTime }}</template>
          </el-table-column>
          <el-table-column prop="studentCount" label="人数" width="100" align="center">
            <template #default="scope">{{ scope.row.studentCount }} 人</template>
          </el-table-column>
        </el-table>

        <div v-if="order.items.length === 0" class="no-items">
          本团当前没有已配上的实验设备，请联系馆务安排配对
        </div>

        <div class="sheet-footer">
          <span>打印时间：{{ printTime }}</span>
          <span>带队老师签字：______________</span>
        </div>
      </div>
    </el-card>

    <el-card v-else-if="selectedGroupId !== null" class="order-card">
      <el-skeleton :rows="5" animated />
    </el-card>

    <el-card v-else class="order-card empty-tip">
      <el-icon :size="64" color="#ccc"><Tickets /></el-icon>
      <p>请先选择研学团，打开其实验顺序条</p>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Printer, Tickets } from '@element-plus/icons-vue'
import { allocationApi, studyGroupApi, type ExperimentOrder, type StudyGroup } from '@/api'
import { authState } from '@/auth'

const isStaff = computed(() => authState.user?.role === 'STAFF')

const groups = ref<StudyGroup[]>([])
const selectedGroupId = ref<number | null>(null)
const order = ref<ExperimentOrder | null>(null)

const printTime = computed(() => new Date().toLocaleString('zh-CN', { hour12: false }))

const errorMessage = (error: any, fallback: string) =>
  error?.response?.data?.error || fallback

const loadGroups = async () => {
  try {
    // 服务端按角色收窄：馆务拿到全部研学团，带队老师只拿到本团
    groups.value = await studyGroupApi.getAll()
    if (!isStaff.value && authState.user?.groupId != null) {
      // 带队老师进页即自动选定本团并打开顺序条，不能改选他团
      selectedGroupId.value = authState.user.groupId
      loadOrder()
    } else if (groups.value.length > 0) {
      selectedGroupId.value = groups.value[0].id
      loadOrder()
    }
  } catch (error: any) {
    ElMessage.error(errorMessage(error, '加载研学团失败'))
  }
}

const loadOrder = async () => {
  if (selectedGroupId.value === null) {
    order.value = null
    return
  }
  try {
    // 每次打开/刷新都实时查询当前有效占用（status=1），
    // 被取消的占用已置为 status=0，不会再出现在顺序条里
    order.value = await allocationApi.getExperimentOrder(selectedGroupId.value)
  } catch (error: any) {
    order.value = null
    ElMessage.error(errorMessage(error, '加载实验顺序条失败'))
  }
}

const printOrder = () => {
  if (!order.value) return
  window.print()
}

loadGroups()
</script>

<style scoped>
.experiment-order {
  padding: 20px;
}

.toolbar-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
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

.header-actions {
  display: flex;
  gap: 8px;
}

.order-sheet {
  padding: 10px 20px 20px;
}

.sheet-header {
  text-align: center;
  margin-bottom: 20px;
}

.sheet-header h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  letter-spacing: 4px;
}

.sheet-subtitle {
  margin: 0;
  color: #666;
  font-size: 13px;
}

.sheet-meta {
  margin-bottom: 20px;
}

.sheet-table {
  margin-bottom: 24px;
}

.no-items {
  text-align: center;
  padding: 40px;
  color: #999;
}

.sheet-footer {
  display: flex;
  justify-content: space-between;
  color: #666;
  font-size: 13px;
}

.empty-tip {
  text-align: center;
  padding: 60px 0;
}

.empty-tip p {
  margin-top: 16px;
  color: #999;
}

/* 打印时只留顺序条本身：隐藏侧栏、页头与操作区 */
@media print {
  .toolbar-card,
  .sidebar,
  .header {
    display: none !important;
  }

  .app-container,
  .main {
    display: block !important;
    height: auto !important;
    padding: 0 !important;
    background: #fff !important;
  }

  .order-card {
    border: none !important;
    box-shadow: none !important;
  }

  .order-sheet {
    padding: 0;
  }
}
</style>
