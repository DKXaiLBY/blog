<template>
  <div class="login-wrap">
    <el-card class="login-card" shadow="always">
      <h2>管理员登录</h2>
      <el-form @keyup.enter="handleLogin">
        <el-form-item>
          <el-input v-model="username" placeholder="用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="password" type="password" placeholder="密码" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" style="width:100%" @click="handleLogin" :loading="loading">登 录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login as loginApi } from '@/api/auth'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const username = ref('')
const password = ref('')
const loading = ref(false)

const handleLogin = async () => {
  if (!username.value || !password.value) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const res = await loginApi(username.value, password.value)
    userStore.login(res.data.token)
    ElMessage.success('登录成功')
    router.push('/admin')
  } catch { /* handled */ }
  finally { loading.value = false }
}
</script>

<style scoped>
.login-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
}

.login-card {
  width: 380px;
  border-radius: 12px;
}

.login-card h2 {
  text-align: center;
  margin-bottom: 24px;
  font-size: 22px;
}

@media (max-width: 420px) {
  .login-card { width: 90%; }
}
</style>
