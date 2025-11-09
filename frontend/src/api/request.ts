import axios, { type AxiosInstance, type AxiosResponse } from 'axios'
import type { ApiResponse } from '@/types'

const request: AxiosInstance = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor
request.interceptors.request.use(
  (config) => {
    // Add auth token if needed
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse<any>>) => {
    const res = response.data
    
    if (res.code !== 0) {
      console.error('API Error:', res.message)
      return Promise.reject(new Error(res.message || 'Error'))
    }
    
    return response
  },
  (error) => {
    console.error('Request Error:', error.message)
    return Promise.reject(error)
  }
)

export default request
