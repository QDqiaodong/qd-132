import { reactive } from 'vue'

export type Role = 'STAFF' | 'TEACHER'

export interface AuthUser {
  role: Role
  /** 带队老师所属研学团 id，馆务为 null */
  groupId: number | null
  groupName: string | null
  groupCode: string | null
}

const STORAGE_KEY = 'space-museum-auth'

function load(): AuthUser | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? (JSON.parse(raw) as AuthUser) : null
  } catch {
    return null
  }
}

/** 当前登录身份，全局响应式；刷新页面后从 localStorage 恢复 */
export const authState = reactive<{ user: AuthUser | null }>({
  user: load()
})

export function setAuth(user: AuthUser) {
  authState.user = user
  localStorage.setItem(STORAGE_KEY, JSON.stringify(user))
}

export function clearAuth() {
  authState.user = null
  localStorage.removeItem(STORAGE_KEY)
}

export function isStaff(): boolean {
  return authState.user?.role === 'STAFF'
}
