import request from './request'
import type { ApiResponse, SimulationRun, StartSimulationReq } from '@/types'

export const simulationApi = {
  // Precheck
  precheck(data: { projectCode: string; topologyVersion: number }) {
    return request.post<ApiResponse<any>>('/simulations/precheck', data)
  },

  // Start simulation
  start(data: StartSimulationReq) {
    return request.post<ApiResponse<SimulationRun>>('/simulations/start', data)
  },

  // Stop simulation
  stop(runId: string, reason?: string) {
    return request.post<ApiResponse<void>>(`/simulations/${runId}/stop`, { reason })
  },

  // List simulations
  list(params: { projectCode?: string; status?: number; page?: number; pageSize?: number }) {
    return request.get<ApiResponse<{ list: SimulationRun[]; total: number }>>('/simulations', { params })
  },

  // Get simulation detail
  get(runId: string) {
    return request.get<ApiResponse<SimulationRun>>(`/simulations/${runId}`)
  }
}
