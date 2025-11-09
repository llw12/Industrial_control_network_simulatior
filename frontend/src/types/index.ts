// API Response types
export interface ApiResponse<T> {
  code: number
  message?: string
  data: T
  traceId?: string
}

// Project types
export interface Project {
  projectCode: string
  projectName: string
  simTimeLimit?: string
  status: number
  description?: string
  createUser?: string
  createTime?: string
  updateTime?: string
}

export interface CreateProjectReq {
  projectName: string
  simTimeLimit?: string
  description?: string
  createUser?: string
}

// Node types
export interface NodeParams {
  ip?: string
  isHil?: boolean
  eth?: {
    bitrate?: string
    channelLength?: string
    delay?: string
  }
  capture?: {
    enable: boolean
    moduleNamePatterns?: string
    pcapFile?: string
  }
  apps: AppConfig[]
}

export interface AppConfig {
  typename: string
  localPort?: number
  connectPort?: number
  connectAddress?: string
  startTime?: string
  interval?: string
  readInterval?: string
  numConnect?: number
  configFile?: string
  slavesConfigPath?: string
  remoteAddress?: string
  remotePort?: number
  seed?: number
  modbusRequest?: string
  sendTime?: string
  reconnectInterval?: string
}

export interface NodeItem {
  nodeId: string
  nodeType: string
  displayX: number
  displayY: number
  params: NodeParams
}

// Topology types
export interface TopologyGraph {
  nodes: GraphNode[]
  edges: GraphEdge[]
  meta: GraphMeta
}

export interface GraphNode {
  id: string
  type: string
  x: number
  y: number
}

export interface GraphEdge {
  source: string
  target: string
  linkType?: string
}

export interface GraphMeta {
  numClients?: number
  hilClientIndices?: number[]
  networkName?: string
}

// Simulation types
export interface SimulationRun {
  runId: string
  projectCode: string
  topologyVersion: number
  status: number
  startTime?: string
  endTime?: string
  iniFilePath?: string
  nedFilePath?: string
  logPath?: string
}

export interface StartSimulationReq {
  projectCode: string
  topologyVersion: number
  iniTemplateParams?: {
    simTimeLimit?: string
    enableHardInLoop?: boolean
    captureRules?: Array<{
      nodeId: string
      moduleNamePatterns?: string
      pcapFile?: string
    }>
  }
  queued?: boolean
}

// Config types
export interface ConfigFile {
  id: number
  projectCode: string
  fileType: 'MASTER' | 'SLAVE'
  version: number
  contentJson: any
  description?: string
  createTime?: string
}
