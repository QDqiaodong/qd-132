import axios, { type AxiosResponse } from 'axios'
import { authState, clearAuth, type AuthUser } from '@/auth'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 每次请求都携带当前身份，服务端据此按角色收窄可见范围
api.interceptors.request.use(config => {
  const user = authState.user
  if (user) {
    config.headers['X-User-Role'] = user.role
    if (user.role === 'TEACHER' && user.groupId != null) {
      config.headers['X-Group-Id'] = String(user.groupId)
    }
  }
  return config
})

api.interceptors.response.use(
  (response: AxiosResponse) => response.data,
  error => {
    console.error('API Error:', error)
    // 身份缺失或失效：清除本地身份并回到登录页
    if (error.response?.status === 401) {
      clearAuth()
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export interface Device {
  id: number
  deviceCode: string
  deviceName: string
  description: string
  experienceDuration: number
  minAge: number
  maxAge: number
  capacity: number
  status: number
}

export interface TimeSlot {
  id: number
  deviceId: number
  startTime: string
  endTime: string
  dayOfWeek: number
  available: boolean
  sortOrder: number
}

export interface StudyGroup {
  id: number
  groupCode: string
  groupName: string
  schoolName: string
  contactPerson: string
  contactPhone: string
  totalStudents: number
  averageAge: number
  visitDate: string
  status: number
  remark: string
}

export interface Allocation {
  id: number
  studyGroupId: number
  deviceId: number
  timeSlotId: number
  studentCount: number
  allocationTime: string
  status: number
  batchNumber: string
}

export interface AllocationResult {
  allocationId: number
  deviceId: number
  deviceCode: string
  deviceName: string
  timeSlotId: number
  startTime: string
  endTime: string
  studentCount: number
  batchNumber: string
}

export interface GroupAllocation {
  groupId: number
  groupCode: string
  groupName: string
  schoolName: string
  totalStudents: number
  averageAge: number
  visitDate: string
  allocations: AllocationResult[]
}

export const authApi = {
  login: (data: { role: 'STAFF' | 'TEACHER'; groupCode?: string }): Promise<AuthUser> =>
    api.post('/auth/login', data)
}

export const deviceApi = {
  getAll: (): Promise<Device[]> => api.get('/devices'),
  getActive: (): Promise<Device[]> => api.get('/devices/active'),
  getById: (id: number): Promise<Device> => api.get(`/devices/${id}`),
  getSuitable: (age: number): Promise<Device[]> => api.get(`/devices/suitable/${age}`),
  create: (data: Omit<Device, 'id'>): Promise<Device> => api.post('/devices', data),
  update: (id: number, data: Device): Promise<Device> => api.put(`/devices/${id}`, data),
  delete: (id: number): Promise<void> => api.delete(`/devices/${id}`),
  getSlots: (id: number): Promise<TimeSlot[]> => api.get(`/devices/${id}/slots`),
  addSlot: (id: number, data: Omit<TimeSlot, 'id' | 'deviceId'>): Promise<{ message: string }> => api.post(`/devices/${id}/slots`, data),
  updateSlot: (slotId: number, data: TimeSlot): Promise<{ message: string }> => api.put(`/devices/slots/${slotId}`, data),
  deleteSlot: (slotId: number): Promise<{ message: string }> => api.delete(`/devices/slots/${slotId}`)
}

export const studyGroupApi = {
  getAll: (): Promise<StudyGroup[]> => api.get('/study-groups'),
  getByStatus: (status: number): Promise<StudyGroup[]> => api.get(`/study-groups/status/${status}`),
  getByDate: (date: string): Promise<StudyGroup[]> => api.get(`/study-groups/date/${date}`),
  getById: (id: number): Promise<StudyGroup> => api.get(`/study-groups/${id}`),
  create: (data: Omit<StudyGroup, 'id'>): Promise<StudyGroup> => api.post('/study-groups', data),
  update: (id: number, data: StudyGroup): Promise<StudyGroup> => api.put(`/study-groups/${id}`, data),
  delete: (id: number): Promise<void> => api.delete(`/study-groups/${id}`)
}

export const allocationApi = {
  getAll: (): Promise<Allocation[]> => api.get('/allocations'),
  getByGroup: (groupId: number): Promise<Allocation[]> => api.get(`/allocations/group/${groupId}`),
  getGroupDetail: (groupId: number): Promise<GroupAllocation> => api.get(`/allocations/group/${groupId}/detail`),
  getAllGroups: (): Promise<GroupAllocation[]> => api.get('/allocations/all-groups'),
  getByDate: (date: string): Promise<Allocation[]> => api.get(`/allocations/date/${date}`),
  autoAllocate: (groupId: number): Promise<AllocationResult[]> => api.post(`/allocations/auto/${groupId}`),
  manualAllocate: (data: { studyGroupId: number; deviceId: number; timeSlotId: number; studentCount: number; status: number }): Promise<Allocation> => api.post('/allocations', data),
  update: (id: number, data: Allocation): Promise<Allocation> => api.put(`/allocations/${id}`, data),
  cancel: (id: number): Promise<{ message: string }> => api.delete(`/allocations/${id}`),
  cancelGroup: (groupId: number): Promise<{ message: string }> => api.delete(`/allocations/group/${groupId}`)
}