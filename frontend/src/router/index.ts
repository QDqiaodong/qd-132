import { createRouter, createWebHistory } from 'vue-router'
import DeviceManagement from '@/views/DeviceManagement.vue'
import TimeSlotManagement from '@/views/TimeSlotManagement.vue'
import StudyGroupManagement from '@/views/StudyGroupManagement.vue'
import AllocationLedger from '@/views/AllocationLedger.vue'

const routes = [
  {
    path: '/',
    redirect: '/devices'
  },
  {
    path: '/devices',
    name: 'DeviceManagement',
    component: DeviceManagement
  },
  {
    path: '/time-slots',
    name: 'TimeSlotManagement',
    component: TimeSlotManagement
  },
  {
    path: '/study-groups',
    name: 'StudyGroupManagement',
    component: StudyGroupManagement
  },
  {
    path: '/allocations',
    name: 'AllocationLedger',
    component: AllocationLedger
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router