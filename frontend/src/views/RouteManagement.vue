<template>
  <div class="route-management">
    <el-card>
      <div class="card-header">
        <div class="card-title">
          <h2>体验动线</h2>
          <el-tag :type="isStaff ? 'primary' : 'success'" effect="plain" size="small">
            {{ isStaff ? '可见范围：全部研学团' : '可见范围：仅本团' }}
          </el-tag>
        </div>
        <el-button @click="loadRoutes">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <el-form :inline="true" class="search-form">
        <el-form-item label="是否已放行">
          <el-radio-group v-model="releasedFilter">
            <el-radio-button value="all">全部</el-radio-button>
            <el-radio-button value="released">已放行</el-radio-button>
            <el-radio-button value="unreleased">未放行</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="团号">
          <el-input v-model="groupCodeFilter" placeholder="请输入团号" clearable />
        </el-form-item>
      </el-form>

      <el-card
        v-for="route in filteredRoutes"
        :key="route.groupId"
        shadow="hover"
        class="route-card"
      >
        <div class="route-header">
          <div class="route-info">
            <h3>{{ route.groupName }}</h3>
            <p>团号: {{ route.groupCode }} | 学校: {{ route.schoolName || '-' }} | 参观日期: {{ route.visitDate }}</p>
            <p v-if="route.routeId">
              动线: {{ route.routeName }} | 共 {{ route.stops.length }} 站，已完成 {{ completedCount(route) }} 站
            </p>
          </div>
          <div v-if="isStaff" class="route-actions">
            <el-button size="small" type="primary" @click="openPlanDialog(route)">
              {{ route.routeId ? '重新编排' : '编排动线' }}
            </el-button>
            <el-button v-if="route.routeId" size="small" type="danger" @click="removeRoute(route)">
              删除动线
            </el-button>
          </div>
        </div>

        <template v-if="route.routeId">
          <el-table :data="visibleStops(route)" border stripe size="small">
            <el-table-column label="站序" width="80" align="center">
              <template #default="scope">
                <el-tag effect="dark" size="small">第 {{ scope.row.stopOrder }} 站</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="deviceCode" label="设备编号" width="120" />
            <el-table-column prop="deviceName" label="设备名称" min-width="140" />
            <el-table-column label="是否已放行" width="110" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.released ? 'success' : 'info'" size="small">
                  {{ scope.row.released ? '已放行' : '未放行' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="是否完成" width="100" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.completed ? 'success' : 'info'" size="small">
                  {{ scope.row.completed ? '已完成' : '未完成' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="放行时间" width="170">
              <template #default="scope">{{ formatTime(scope.row.releasedTime) }}</template>
            </el-table-column>
            <el-table-column label="完成时间" width="170">
              <template #default="scope">{{ formatTime(scope.row.completedTime) }}</template>
            </el-table-column>
            <el-table-column v-if="isStaff" label="操作" width="150" align="center">
              <template #default="scope">
                <el-tooltip
                  v-if="!scope.row.released"
                  :content="blockReason(route, scope.row)"
                  :disabled="canRelease(route, scope.row)"
                  placement="top"
                >
                  <span>
                    <el-button
                      size="small"
                      type="primary"
                      :disabled="!canRelease(route, scope.row)"
                      @click="releaseStop(route, scope.row)"
                    >放行</el-button>
                  </span>
                </el-tooltip>
                <el-button
                  v-else-if="!scope.row.completed"
                  size="small"
                  type="success"
                  @click="completeStop(scope.row)"
                >完成</el-button>
                <el-tag v-else type="success" size="small">已完成</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="visibleStops(route).length === 0" class="no-stop">
            <p>当前放行状态筛选下没有站点</p>
          </div>
        </template>
        <div v-else class="no-stop">
          <p v-if="isStaff">尚未编排动线，请点击"编排动线"为该团安排站序</p>
          <p v-else>本团尚未编排动线，请联系馆务人员</p>
        </div>
      </el-card>

      <div v-if="filteredRoutes.length === 0" class="empty-state">
        <el-icon :size="64" color="#ccc"><Guide /></el-icon>
        <p>暂无研学团</p>
        <p v-if="isStaff">请先在"研学团管理"中添加研学团</p>
        <p v-else>本团暂无动线信息，请联系馆务人员</p>
      </div>
    </el-card>

    <el-dialog v-model="planDialogVisible" :title="planForm.routeId ? '重新编排动线' : '编排动线'" width="700px">
      <el-form label-width="100px">
        <el-form-item label="研学团" required>
          <el-select v-model="planForm.groupId" :disabled="planForm.routeId !== null" style="width: 100%">
            <el-option v-for="route in routes" :key="route.groupId" :label="`${route.groupName}（${route.groupCode}）`" :value="route.groupId" />
          </el-select>
        </el-form-item>
        <el-form-item label="动线名称">
          <el-input v-model="planForm.routeName" placeholder="默认：参观动线" />
        </el-form-item>
        <el-form-item label="添加站点">
          <div class="stop-picker">
            <el-select v-model="pickedDeviceId" placeholder="选择设备" style="flex: 1">
              <el-option
                v-for="device in availableDevices"
                :key="device.id"
                :label="`${device.deviceName}（${device.deviceCode}）`"
                :value="device.id"
                :disabled="planStops.some(s => s.id === device.id)"
              />
            </el-select>
            <el-button type="primary" :disabled="pickedDeviceId === null" @click="addStop">添加</el-button>
          </div>
        </el-form-item>
        <el-form-item label="站序">
          <el-table :data="planStops" border size="small" style="width: 100%">
            <el-table-column label="站序" width="80" align="center">
              <template #default="scope">第 {{ scope.$index + 1 }} 站</template>
            </el-table-column>
            <el-table-column prop="deviceCode" label="设备编号" width="120" />
            <el-table-column prop="deviceName" label="设备名称" min-width="140" />
            <el-table-column label="操作" width="180" align="center">
              <template #default="scope">
                <el-button size="small" :disabled="scope.$index === 0" @click="moveStop(scope.$index, -1)">上移</el-button>
                <el-button size="small" :disabled="scope.$index === planStops.length - 1" @click="moveStop(scope.$index, 1)">下移</el-button>
                <el-button size="small" type="danger" @click="planStops.splice(scope.$index, 1)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>
      <el-alert
        v-if="planForm.routeId !== null"
        type="warning"
        :closable="false"
        title="重新编排将重置该团已放行与已完成的进度"
        style="margin-bottom: 10px"
      />
      <template #footer>
        <el-button @click="planDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="planStops.length === 0" @click="saveRoute">保存动线</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Guide } from '@element-plus/icons-vue'
import { routeApi, deviceApi, type RoutePlan, type RouteStop, type Device } from '@/api'
import { authState } from '@/auth'

const isStaff = computed(() => authState.user?.role === 'STAFF')

const routes = ref<RoutePlan[]>([])
const releasedFilter = ref<'all' | 'released' | 'unreleased'>('all')
const groupCodeFilter = ref('')

const planDialogVisible = ref(false)
const availableDevices = ref<Device[]>([])
const pickedDeviceId = ref<number | null>(null)
const planStops = ref<Device[]>([])
const planForm = ref<{ routeId: number | null; groupId: number; routeName: string }>({
  routeId: null,
  groupId: 0,
  routeName: ''
})

const errorMessage = (error: any, fallback: string) =>
  error?.response?.data?.error || fallback

const loadRoutes = async () => {
  try {
    // 服务端已按角色收窄：馆务返回全部研学团，带队老师只返回本团
    routes.value = await routeApi.getAll()
  } catch (error: any) {
    ElMessage.error(errorMessage(error, '加载动线失败'))
  }
}

/** 站序以后端数据为准，前端再按站序排序一次，保证刷新后展示顺序一致 */
const orderedStops = (route: RoutePlan): RouteStop[] =>
  [...route.stops].sort((a, b) => a.stopOrder - b.stopOrder)

/** 列表按是否已放行筛选 */
const visibleStops = (route: RoutePlan): RouteStop[] => {
  const stops = orderedStops(route)
  if (releasedFilter.value === 'released') return stops.filter(s => s.released)
  if (releasedFilter.value === 'unreleased') return stops.filter(s => !s.released)
  return stops
}

const filteredRoutes = computed(() => {
  if (!groupCodeFilter.value) return routes.value
  return routes.value.filter(r => r.groupCode.includes(groupCodeFilter.value))
})

const completedCount = (route: RoutePlan) =>
  route.stops.filter(s => s.completed).length

/** 未按站序完成上一站时不能放行：前面所有站都完成才允许放行本站 */
const canRelease = (route: RoutePlan, stop: RouteStop): boolean => {
  if (stop.released || stop.completed) return false
  return orderedStops(route).every(s => s.stopOrder >= stop.stopOrder || s.completed)
}

/** 拦住原因：找到站序在前且未完成的最近一站 */
const blockReason = (route: RoutePlan, stop: RouteStop): string => {
  const previous = orderedStops(route)
    .filter(s => s.stopOrder < stop.stopOrder && !s.completed)
    .pop()
  return previous ? `须先完成第 ${previous.stopOrder} 站后才能放行` : ''
}

const formatTime = (time: string | null) => (time ? time.replace('T', ' ') : '-')

const releaseStop = async (route: RoutePlan, stop: RouteStop) => {
  if (!canRelease(route, stop)) {
    ElMessage.warning(blockReason(route, stop) || '当前站点不能放行')
    return
  }
  try {
    await routeApi.releaseStop(stop.stopId)
    ElMessage.success(`第 ${stop.stopOrder} 站已放行`)
    loadRoutes()
  } catch (error: any) {
    ElMessage.error(errorMessage(error, '放行失败'))
  }
}

const completeStop = async (stop: RouteStop) => {
  try {
    await routeApi.completeStop(stop.stopId)
    ElMessage.success(`第 ${stop.stopOrder} 站已完成`)
    loadRoutes()
  } catch (error: any) {
    ElMessage.error(errorMessage(error, '标记完成失败'))
  }
}

const openPlanDialog = async (route: RoutePlan) => {
  try {
    availableDevices.value = await deviceApi.getActive()
  } catch (error: any) {
    ElMessage.error(errorMessage(error, '加载设备失败'))
    return
  }
  planForm.value = {
    routeId: route.routeId,
    groupId: route.groupId,
    routeName: route.routeName || ''
  }
  // 重新编排时带出原有站序，便于在现有动线上调整
  planStops.value = orderedStops(route)
    .map(s => availableDevices.value.find(d => d.id === s.deviceId))
    .filter((d): d is Device => d !== undefined)
  pickedDeviceId.value = null
  planDialogVisible.value = true
}

const addStop = () => {
  const device = availableDevices.value.find(d => d.id === pickedDeviceId.value)
  if (device && !planStops.value.some(s => s.id === device.id)) {
    planStops.value.push(device)
  }
  pickedDeviceId.value = null
}

const moveStop = (index: number, offset: number) => {
  const target = index + offset
  if (target < 0 || target >= planStops.value.length) return
  const stops = planStops.value
  ;[stops[index], stops[target]] = [stops[target], stops[index]]
}

const saveRoute = async () => {
  try {
    await routeApi.saveRoute(planForm.value.groupId, {
      routeName: planForm.value.routeName,
      deviceIds: planStops.value.map(d => d.id)
    })
    ElMessage.success('动线已保存')
    planDialogVisible.value = false
    loadRoutes()
  } catch (error: any) {
    ElMessage.error(errorMessage(error, '保存动线失败'))
  }
}

const removeRoute = async (route: RoutePlan) => {
  try {
    await ElMessageBox.confirm(`确定要删除「${route.groupName}」的动线吗？`, '提示', { type: 'warning' })
    await routeApi.deleteRoute(route.groupId)
    ElMessage.success('动线已删除')
    loadRoutes()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(errorMessage(error, '删除动线失败'))
    }
  }
}

loadRoutes()
</script>

<style scoped>
.route-management {
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

.search-form {
  margin-bottom: 20px;
}

.route-card {
  margin-bottom: 20px;
}

.route-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.route-info h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
}

.route-info p {
  margin: 5px 0;
  font-size: 14px;
  color: #666;
}

.route-actions {
  display: flex;
  gap: 8px;
}

.stop-picker {
  display: flex;
  gap: 8px;
  width: 100%;
}

.no-stop {
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
