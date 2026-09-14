<template>
  <div class="study-group-management">
    <el-card class="attendance-card">
      <div class="attendance-bar">
        <h3 class="attendance-title">每日到馆人数</h3>
        <el-date-picker
          v-model="attendanceDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="选择参观日期"
          @change="loadAttendance"
        />
        <el-button type="primary" :disabled="!attendanceDate" @click="loadAttendance">查询</el-button>
        <div v-if="attendance" class="attendance-result">
          <span class="attendance-date">{{ attendance.visitDate }}</span>
          已预约研学团
          <strong>{{ attendance.groupCount }}</strong> 个，到馆学生共
          <strong class="attendance-number">{{ attendance.totalStudents }}</strong> 人
        </div>
      </div>
    </el-card>

    <el-card>
      <div class="card-header">
        <h2>研学团管理</h2>
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>
          添加研学团
        </el-button>
      </div>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="团号">
          <el-input v-model="searchForm.groupCode" placeholder="请输入团号" />
        </el-form-item>
        <el-form-item label="团名">
          <el-input v-model="searchForm.groupName" placeholder="请输入团名" />
        </el-form-item>
        <el-form-item label="参观日期">
          <el-date-picker v-model="searchForm.visitDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部">
            <el-option :label="'已预约'" :value="1" />
            <el-option :label="'已取消'" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadGroups">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="groups" border stripe>
        <el-table-column prop="groupCode" label="团号" width="120" />
        <el-table-column prop="groupName" label="团名" width="150" />
        <el-table-column prop="schoolName" label="学校名称" width="150" />
        <el-table-column prop="contactPerson" label="带队老师" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="totalStudents" label="学生人数" width="100" />
        <el-table-column prop="averageAge" label="平均年龄" width="100" />
        <el-table-column prop="visitDate" label="参观日期" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '已预约' : '已取消' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <el-button size="small" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteGroup(scope.row.id)">删除</el-button>
            <el-button
              size="small"
              type="success"
              :loading="allocatingGroupId === scope.row.id"
              :disabled="allocatingGroupId !== null"
              @click="autoAllocate(scope.row.id)"
            >自动分配</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        layout="total, prev, pager, next"
        @current-change="loadGroups"
        class="pagination"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑研学团' : '添加研学团'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="团号" required>
          <el-input v-model="form.groupCode" />
        </el-form-item>
        <el-form-item label="团名" required>
          <el-input v-model="form.groupName" />
        </el-form-item>
        <el-form-item label="学校名称">
          <el-input v-model="form.schoolName" />
        </el-form-item>
        <el-form-item label="带队老师">
          <el-input v-model="form.contactPerson" placeholder="请输入带队老师姓名" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.contactPhone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="学生人数" required>
          <el-input-number v-model="form.totalStudents" :min="1" />
        </el-form-item>
        <el-form-item label="平均年龄(岁)" required>
          <el-input-number v-model="form.averageAge" :min="3" :max="18" />
        </el-form-item>
        <el-form-item label="参观日期" required>
          <el-date-picker v-model="form.visitDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="状态" required>
          <el-select v-model="form.status">
            <el-option :label="'已预约'" :value="1" />
            <el-option :label="'已取消'" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="form.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveGroup">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="allocateDialogVisible" title="自动分配结果" width="700px">
      <div v-if="allocationResults.length > 0">
        <el-table :data="allocationResults" border stripe>
          <el-table-column prop="deviceCode" label="设备编号" width="120" />
          <el-table-column prop="deviceName" label="设备名称" width="150" />
          <el-table-column prop="startTime" label="开始时间" width="120" />
          <el-table-column prop="endTime" label="结束时间" width="120" />
          <el-table-column prop="studentCount" label="分配人数" width="100" />
          <el-table-column prop="batchNumber" label="批次号" />
        </el-table>
      </div>
      <div v-else>
        <p>暂无分配结果</p>
      </div>
      <template #footer>
        <el-button @click="allocateDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { studyGroupApi, allocationApi, type StudyGroup, type AllocationResult, type DailyAttendance } from '@/api'

const groups = ref<StudyGroup[]>([])
const dialogVisible = ref(false)
const allocateDialogVisible = ref(false)
const isEdit = ref(false)
const allocationResults = ref<AllocationResult[]>([])
// 正在提交自动分配的研学团 id，非空时禁用所有分配按钮，连点只生效一次
const allocatingGroupId = ref<number | null>(null)

// 选定的参观日与当天到馆人数汇总
const todayString = () => {
  const now = new Date()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${now.getFullYear()}-${month}-${day}`
}
const attendanceDate = ref(todayString())
const attendance = ref<DailyAttendance | null>(null)

const searchForm = reactive({
  groupCode: '',
  groupName: '',
  visitDate: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: 0,
  groupCode: '',
  groupName: '',
  schoolName: '',
  contactPerson: '',
  contactPhone: '',
  totalStudents: 30,
  averageAge: 10,
  visitDate: '',
  status: 1,
  remark: ''
})

const loadGroups = async () => {
  try {
    const data = await studyGroupApi.getAll()
    groups.value = data
    pagination.total = data.length
  } catch (error) {
    ElMessage.error('加载研学团失败')
  }
}

// 按选定参观日实时汇总到馆人数；人数改动后再次调用即按新人数重算
const loadAttendance = async () => {
  if (!attendanceDate.value) {
    attendance.value = null
    return
  }
  try {
    attendance.value = await studyGroupApi.getDailyAttendance(attendanceDate.value)
  } catch (error) {
    ElMessage.error('加载到馆人数失败')
  }
}

const openAddDialog = () => {
  isEdit.value = false
  Object.assign(form, {
    id: 0,
    groupCode: '',
    groupName: '',
    schoolName: '',
    contactPerson: '',
    contactPhone: '',
    totalStudents: 30,
    averageAge: 10,
    visitDate: '',
    status: 1,
    remark: ''
  })
  dialogVisible.value = true
}

const openEditDialog = (group: StudyGroup) => {
  isEdit.value = true
  Object.assign(form, group)
  dialogVisible.value = true
}

const saveGroup = async () => {
  try {
    if (isEdit.value) {
      await studyGroupApi.update(form.id, form)
      ElMessage.success('研学团更新成功')
    } else {
      await studyGroupApi.create(form)
      ElMessage.success('研学团添加成功')
    }
    dialogVisible.value = false
    loadGroups()
    // 团人数或参观日期可能已变更，到馆人数按新数据重算
    loadAttendance()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const deleteGroup = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该研学团吗？', '提示', { type: 'warning' })
    await studyGroupApi.delete(id)
    ElMessage.success('研学团删除成功')
    loadGroups()
    loadAttendance()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const autoAllocate = async (groupId: number) => {
  // 提交中直接忽略重复点击，保证连点只生效一次
  if (allocatingGroupId.value !== null) return
  // 带队老师、联系电话为必填项，缺项时明确提示缺哪一项，不发请求
  const group = groups.value.find(g => g.id === groupId)
  const missing: string[] = []
  if (!group?.contactPerson?.trim()) missing.push('带队老师')
  if (!group?.contactPhone?.trim()) missing.push('联系电话')
  if (missing.length > 0) {
    ElMessage.error(`该研学团未填写${missing.join('和')}，请先补全后再提交自动分配`)
    return
  }
  allocatingGroupId.value = groupId
  try {
    const results = await allocationApi.autoAllocate(groupId)
    allocationResults.value = results
    allocateDialogVisible.value = true
    ElMessage.success('自动分配成功')
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.error || '自动分配失败')
  } finally {
    allocatingGroupId.value = null
  }
}

const resetSearch = () => {
  Object.assign(searchForm, { groupCode: '', groupName: '', visitDate: '', status: '' })
  loadGroups()
}

loadGroups()
loadAttendance()
</script>

<style scoped>
.study-group-management {
  padding: 20px;
}

.attendance-card {
  margin-bottom: 20px;
}

.attendance-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.attendance-title {
  margin: 0;
  font-size: 16px;
}

.attendance-result {
  font-size: 15px;
  color: #303133;
}

.attendance-date {
  font-weight: 600;
  margin-right: 8px;
}

.attendance-number {
  color: #409eff;
  font-size: 20px;
  margin: 0 4px;
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