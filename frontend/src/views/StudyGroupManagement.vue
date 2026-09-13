<template>
  <div class="study-group-management">
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
          <el-date-picker v-model="searchForm.visitDate" type="date" placeholder="选择日期" />
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
        <el-table-column prop="contactPerson" label="联系人" width="100" />
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
            <el-button size="small" type="success" @click="autoAllocate(scope.row.id)">自动分配</el-button>
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
        <el-form-item label="联系人">
          <el-input v-model="form.contactPerson" />
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
          <el-date-picker v-model="form.visitDate" type="date" placeholder="选择日期" />
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
import { studyGroupApi, allocationApi, type StudyGroup, type AllocationResult } from '@/api'

const groups = ref<StudyGroup[]>([])
const dialogVisible = ref(false)
const allocateDialogVisible = ref(false)
const isEdit = ref(false)
const allocationResults = ref<AllocationResult[]>([])

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
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const autoAllocate = async (groupId: number) => {
  try {
    const results = await allocationApi.autoAllocate(groupId)
    allocationResults.value = results
    allocateDialogVisible.value = true
    ElMessage.success('自动分配成功')
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.error || '自动分配失败')
  }
}

const resetSearch = () => {
  Object.assign(searchForm, { groupCode: '', groupName: '', visitDate: '', status: '' })
  loadGroups()
}

loadGroups()
</script>

<style scoped>
.study-group-management {
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