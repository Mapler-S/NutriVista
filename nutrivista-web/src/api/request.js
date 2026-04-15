/**
 * Axios 实例封装
 * 统一请求拦截、响应拦截、错误处理、loading 状态管理
 */

import axios from 'axios'
import { ElMessage, ElLoading } from 'element-plus'

// ===== Axios 实例 =====
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: Number(import.meta.env.VITE_API_TIMEOUT) || 15000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8',
  },
})

// ===== Loading 管理 =====
let loadingInstance = null
let loadingCount = 0

function showLoading() {
  if (loadingCount === 0) {
    loadingInstance = ElLoading.service({
      lock: true,
      background: 'rgba(255, 255, 255, 0.6)',
      svg: `<path class="path" d="M 30 15 L 28 17 M 25.61 25.61 A 15 15, 0, 0, 1, 15 30 A 15 15, 0, 1, 1, 27.99 7.5 L 15 15" style="stroke-width: 4px; fill: rgba(0, 0, 0, 0)"/>`,
      svgViewBox: '0 0 30 30',
    })
  }
  loadingCount++
}

function hideLoading() {
  loadingCount = Math.max(0, loadingCount - 1)
  if (loadingCount === 0 && loadingInstance) {
    loadingInstance.close()
    loadingInstance = null
  }
}

// ===== 请求拦截器 =====
request.interceptors.request.use(
  (config) => {
    // 注入 JWT token（预留）
    const token = localStorage.getItem('nv_token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }

    // 显示 loading（可通过 config.hideLoading = true 跳过）
    if (!config.hideLoading) {
      showLoading()
    }

    return config
  },
  (error) => {
    hideLoading()
    return Promise.reject(error)
  }
)

// ===== 响应拦截器 =====
request.interceptors.response.use(
  (response) => {
    hideLoading()
    const res = response.data

    // 后端统一格式：{ code, message, data, timestamp }
    if (res.code !== undefined && res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || 'Error'))
    }

    return res
  },
  (error) => {
    hideLoading()

    if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请检查网络连接')
      return Promise.reject(error)
    }

    if (!error.response) {
      ElMessage.error('网络异常，无法连接到服务器')
      return Promise.reject(error)
    }

    const { status, data } = error.response

    switch (status) {
      case 400:
        ElMessage.error(data?.message || '请求参数错误')
        break
      case 401:
        ElMessage.error('登录已过期，请重新登录')
        localStorage.removeItem('nv_token')
        // router.push('/login')  // 预留：跳转登录页
        break
      case 403:
        ElMessage.error('权限不足')
        break
      case 404:
        ElMessage.error(data?.message || '请求的资源不存在')
        break
      case 500:
        ElMessage.error('服务器内部错误，请稍后再试')
        break
      default:
        ElMessage.error(data?.message || `请求失败 (${status})`)
    }

    return Promise.reject(error)
  }
)

export default request
