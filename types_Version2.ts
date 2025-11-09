// 前端 TypeScript 类型定义

export interface ApiResponse<T> {
  code: number;
  message?: string;
  data: T;
}

export interface Project {
  projectCode: string;
  projectName: string;
  simTimeLimit?: string;
  status: number; // 0 未运行 1 运行中 2 已完成 3 失败
  description?: string;
  createUser?: string;
}

export interface CreateProjectReq {
  projectName: string;
  simTimeLimit?: string;
  description?: string;
  createUser?: string;
}

export interface CaptureConfig {
  enable: boolean;
  moduleNamePatterns?: string; // "eth[0]" 等
  pcapFile?: string; // 默认 results/{nodeId}.pcap
}

export interface AppConfig {
  typename: string;
  localPort?: number;
  connectPort?: number;
  connectAddress?: string;
  startTime?: string;
  interval?: string;
  readInterval?: string;
  numConnect?: number;
  configFile?: string;
  slavesConfigPath?: string;
  remoteAddress?: string;
  remotePort?: number;
  seed?: number;
  modbusRequest?: string;
  sendTime?: string;
  reconnectInterval?: string;
}

export interface NodeParams {
  ip?: string; // 留空由 INI configurator 配置
  isHil?: boolean;
  eth?: {
    bitrate?: string;
    channelLength?: string;
    delay?: string;
  };
  capture?: CaptureConfig;
  apps: AppConfig[];
}

export interface NodeItem {
  nodeId: string;
  nodeType: string;
  displayX: number;
  displayY: number;
  params: NodeParams;
}

export interface SaveNodeReq extends NodeItem {}

export interface TopologyGraph {
  nodes: { id: string; type: string; x: number; y: number }[];
  edges: { source: string; target: string; linkType: string }[];
  meta: {
    numClients?: number;
    hilClientIndices?: number[];
    networkName?: string;
  };
}

export interface SaveTopologyReq {
  graph: TopologyGraph;
  masterConfigJson?: any;
  slaveConfigJson?: any;
}

export interface IniTemplateParams {
  simTimeLimit?: string;
  enableHardInLoop?: boolean;
  captureRules?: { nodeId: string; moduleNamePatterns?: string; pcapFile?: string }[];
}

export interface StartSimulationReq {
  projectCode: string;
  topologyVersion: number;
  iniTemplateParams?: IniTemplateParams;
  queued?: boolean;
}

export interface SimulationRun {
  runId: string;
  projectCode: string;
  topologyVersion: number;
  status: number; // 0 1 2 3 4
  startTime?: string;
  endTime?: string;
  iniFilePath?: string;
  nedFilePath?: string;
  logPath?: string;
  sqliteVectorPath?: string;
  sqliteScalarPath?: string;
  pcapPath?: string;
}

export interface SimulationResult {
  metricName: string;
  metricType: 'scalar' | 'vector' | 'custom';
  sourceModule: string;
  value?: number;
  vectorDataJson?: number[];
  tags?: string;
}

export interface VectorSliceResp {
  total: number;
  slice: number[];
  offset: number;
  limit: number;
}

export interface PrecheckResp {
  pass: boolean;
  errors: { type: string; nodeId?: string; detail: string }[];
  warnings: string[];
  hilClientIndices: number[];
  estimatedRuntimeSec?: number;
}