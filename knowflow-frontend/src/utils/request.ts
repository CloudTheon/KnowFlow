import axios, { type AxiosInstance, type AxiosRequestConfig, type InternalAxiosRequestConfig } from 'axios'
import { getToken, removeToken } from './auth'
import type { ApiResponse } from '@/types'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

const service: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  timeout: 60000,
})

// 请求拦截器 — 自动带上 Token
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// 响应拦截器 — 统一处理错误
service.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      removeToken()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  },
)

export function get<T>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.get(url, config)
}

export function post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.post(url, data, config)
}

export function put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.put(url, data, config)
}

export function del<T>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.delete(url, config)
}

export default service
