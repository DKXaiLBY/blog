import axios from 'axios'
import { getToken, removeToken } from '@/utils/token'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(config => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        removeToken()
        window.location.href = '/login'
      }
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        removeToken()
        ElMessage.error('登录已过期，请重新登录')
        window.location.href = '/login'
      } else {
        ElMessage.error(data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查服务是否启动')
    }
    return Promise.reject(error)
  }
)

export default request
