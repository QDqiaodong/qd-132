<template>
  <div class="login-page">
    <el-card class="login-card">
      <div class="login-header">
        <el-icon :size="36" color="#409EFF"><Ship /></el-icon>
        <h2>航天科普馆配对台账</h2>
        <p>请选择身份登录，可见范围将按角色收窄</p>
      </div>

      <el-form label-position="top" @submit.prevent>
        <el-form-item label="登录身份">
          <el-radio-group v-model="form.role">
            <el-radio-button value="STAFF">馆务</el-radio-button>
            <el-radio-button value="TEACHER">带队老师</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="form.role === 'TEACHER'" label="本团团号">
          <el-input
            v-model="form.groupCode"
            placeholder="请输入所带研学团的团号，如 GRP001"
            @keyup.enter="submit"
          />
        </el-form-item>

        <el-button type="primary" class="login-button" :loading="loading" @click="submit">
          登录
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Ship } from '@element-plus/icons-vue'
import { authApi } from '@/api'
import { setAuth } from '@/auth'

const router = useRouter()

const form = reactive<{ role: 'STAFF' | 'TEACHER'; groupCode: string }>({
  role: 'STAFF',
  groupCode: ''
})
const loading = ref(false)

const submit = async () => {
  if (form.role === 'TEACHER' && !form.groupCode.trim()) {
    ElMessage.warning('带队老师登录请填写本团团号')
    return
  }
  loading.value = true
  try {
    const user = await authApi.login({
      role: form.role,
      groupCode: form.role === 'TEACHER' ? form.groupCode.trim() : undefined
    })
    setAuth(user)
    ElMessage.success(user.role === 'STAFF' ? '馆务登录成功，可查看全部研学团配对' : `登录成功，仅可查看本团（${user.groupName}）的配对`)
    router.push('/allocations')
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.error || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
}

.login-card {
  width: 420px;
}

.login-header {
  text-align: center;
  margin-bottom: 24px;
}

.login-header h2 {
  margin: 12px 0 8px;
}

.login-header p {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.login-button {
  width: 100%;
}
</style>
