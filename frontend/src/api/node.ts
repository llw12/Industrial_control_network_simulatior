import request from './request'
import type { ApiResponse, NodeItem } from '@/types'

export const nodeApi = {
  // List nodes
  list(projectCode: string) {
    return request.get<ApiResponse<{ list: NodeItem[] }>>(`/projects/${projectCode}/nodes`)
  },

  // Save node
  save(projectCode: string, data: NodeItem) {
    return request.post<ApiResponse<NodeItem>>(`/projects/${projectCode}/nodes`, data)
  },

  // Batch save nodes
  batchSave(projectCode: string, nodes: NodeItem[]) {
    return request.post<ApiResponse<void>>(`/projects/${projectCode}/nodes/batch`, { nodes })
  },

  // Get node
  get(projectCode: string, nodeId: string) {
    return request.get<ApiResponse<NodeItem>>(`/projects/${projectCode}/nodes/${nodeId}`)
  },

  // Update node
  update(projectCode: string, nodeId: string, data: Partial<NodeItem>) {
    return request.put<ApiResponse<NodeItem>>(`/projects/${projectCode}/nodes/${nodeId}`, data)
  },

  // Delete node
  delete(projectCode: string, nodeId: string) {
    return request.delete<ApiResponse<void>>(`/projects/${projectCode}/nodes/${nodeId}`)
  },

  // Check port conflicts
  checkPorts(projectCode: string, nodes: any[]) {
    return request.post<ApiResponse<{ hasConflict: boolean; conflicts: string[] }>>(
      `/projects/${projectCode}/nodes/ports/check`,
      { nodes }
    )
  }
}
