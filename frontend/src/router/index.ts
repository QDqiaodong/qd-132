import { createRouter, createWebHistory } from 'vue-router'
import DeviceManagement from '@/views/DeviceManagement.vue'
import TimeSlotManagement from '@/views/TimeSlotManagement.vue'
import StudyGroupManagement from '@/views/StudyGroupManagement.vue'
import AllocationLedger from '@/views/AllocationLedger.vue'
import InventoryLedger from '@/views/InventoryLedger.vue'
import RouteManagement from '@/views/RouteManagement.vue'
import LoginView from '@/views/LoginView.vue'
import { authState } from '@/auth'

const routes = [
  {
    path: '/',
    redirect: '/allocations'
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView
  },
  {
    path: '/devices',
    name: 'DeviceManagement',
    component: DeviceManagement,
    meta: { staffOnly: true }
  },
  {
    path: '/time-slots',
    name: 'TimeSlotManagement',
    component: TimeSlotManagement,
    meta: { staffOnly: true }
  },
  {
    path: '/study-groups',
    name: 'StudyGroupManagement',
    component: StudyGroupManagement,
    meta: { staffOnly: true }
  },
  {
    path: '/inventory',
    name: 'InventoryLedger',
    component: InventoryLedger,
    meta: { staffOnly: true }
  },
  {
    path: '/allocations',
    name: 'AllocationLedger',
    component: AllocationLedger
  },
  {
    path: '/routes',
    name: 'RouteManagement',
    component: RouteManagement
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(to => {
  const user = authState.user
  // 未登录只能去登录页
  if (!user) {
    return to.path === '/login' ? true : '/login'
  }
  // 已登录访问登录页则回到台账
  if (to.path === '/login') {
    return '/allocations'
  }
  // 带队老师只开放配对台账，管理类页面一律回到台账
  if (user.role === 'TEACHER' && to.meta.staffOnly) {
    return '/allocations'
  }
  return true
})

export default router
