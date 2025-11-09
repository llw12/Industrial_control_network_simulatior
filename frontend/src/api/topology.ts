import request from './request'
import type { ApiResponse, TopologyGraph } from '@/types'

export const topologyApi = {
  // Save topology
  save(projectCode: string, data: { graph: TopologyGraph; masterConfigJson?: any; slaveConfigJson?: any }) {
    return request.post<ApiResponse<any>>(`/projects/${projectCode}/topology`, data)
  },

  // Get latest topology
  getLatest(projectCode: string) {
    return request.get<ApiResponse<any>>(`/projects/${projectCode}/topology/latest`)
  },

  // Get topology by version
  getByVersion(projectCode: string, version: number) {
    return request.get<ApiResponse<any>>(`/projects/${projectCode}/topology/${version}`)
  },

  // Preview NED
  previewNed(projectCode: string, topologyVersion?: number) {
    return request.post<ApiResponse<{ content: string }>>(
      `/projects/${projectCode}/preview/ned`,
      { topologyVersion }
    )
  },

  // Preview INI
  previewIni(projectCode: string, topologyVersion?: number) {
    return request.post<ApiResponse<{ content: string }>>(
      `/projects/${projectCode}/preview/ini`,
      { topologyVersion }
    )
  }
}
