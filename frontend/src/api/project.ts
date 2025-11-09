import request from './request'
import type { ApiResponse, Project, CreateProjectReq } from '@/types'

export const projectApi = {
  // Create project
  create(data: CreateProjectReq) {
    return request.post<ApiResponse<Project>>('/projects', data)
  },

  // List projects
  list(params: { page?: number; pageSize?: number; keywords?: string }) {
    return request.get<ApiResponse<{ list: Project[]; total: number }>>('/projects', { params })
  },

  // Get project detail
  get(projectCode: string) {
    return request.get<ApiResponse<Project>>(`/projects/${projectCode}`)
  },

  // Update project
  update(projectCode: string, data: Partial<CreateProjectReq>) {
    return request.put<ApiResponse<Project>>(`/projects/${projectCode}`, data)
  },

  // Delete project
  delete(projectCode: string) {
    return request.delete<ApiResponse<void>>(`/projects/${projectCode}`)
  }
}
