<template>
  <div class="topology-editor-container">
    <a-page-header
      title="拓扑编辑器"
      sub-title="使用 G6 进行拓扑绘制"
      @back="goBack"
    >
      <template #extra>
        <a-button @click="clearGraph">清空画布</a-button>
        <a-button @click="previewNed">预览 NED</a-button>
        <a-button @click="previewIni">预览 INI</a-button>
        <a-button type="primary" @click="saveTopology" :loading="saving">保存拓扑</a-button>
      </template>
    </a-page-header>

    <div class="editor-content">
      <a-row :gutter="16">
        <a-col :span="18">
          <a-card class="toolbar-card">
            <a-space>
              <a-button @click="addNode('OperatorStation')">
                <template #icon>🖥️</template>
                添加操作站
              </a-button>
              <a-button @click="addNode('TsnSwitch')">
                <template #icon>🔀</template>
                添加交换机
              </a-button>
              <a-button @click="addNode('Server')">
                <template #icon>🖧</template>
                添加服务器
              </a-button>
              <a-button @click="addNode('Client')">
                <template #icon>💻</template>
                添加客户端
              </a-button>
              <a-divider type="vertical" />
              <a-button @click="enableEdgeMode" :type="edgeMode ? 'primary' : 'default'">
                <template #icon>🔗</template>
                连线模式
              </a-button>
              <a-button @click="deleteSelected" danger>
                <template #icon>🗑️</template>
                删除选中
              </a-button>
            </a-space>
          </a-card>
          <div ref="graphContainer" class="graph-container"></div>
        </a-col>
        
        <a-col :span="6">
          <a-card title="节点配置" v-if="selectedNode" class="config-panel">
            <a-tabs v-model:activeKey="configTabKey" size="small">
              <a-tab-pane key="basic" tab="基本信息">
                <a-form layout="vertical" size="small">
                  <a-form-item label="节点ID">
                    <a-input v-model:value="selectedNode.id" disabled />
                  </a-form-item>
                  <a-form-item label="节点类型">
                    <a-input v-model:value="selectedNode.type" disabled />
                  </a-form-item>
                </a-form>
              </a-tab-pane>
              
              <a-tab-pane key="network" tab="网络配置">
                <a-form layout="vertical" size="small">
                  <a-divider orientation="left">以太网设置</a-divider>
                  <a-form-item label="链路速率 (bitrate)">
                    <a-input v-model:value="selectedNode.eth.bitrate" placeholder="100Mbps" @change="updateNodeConfig" />
                  </a-form-item>
                  <a-form-item label="链路长度 (length)">
                    <a-input v-model:value="selectedNode.eth.channelLength" placeholder="10m" @change="updateNodeConfig" />
                  </a-form-item>
                  <a-form-item label="误码率 (BER)">
                    <a-input-number v-model:value="selectedNode.eth.ber" :min="0" :max="1" :step="0.0001" style="width: 100%" @change="updateNodeConfig" />
                  </a-form-item>
                  <a-form-item label="误包率 (PER)">
                    <a-input-number v-model:value="selectedNode.eth.per" :min="0" :max="1" :step="0.0001" style="width: 100%" @change="updateNodeConfig" />
                  </a-form-item>
                </a-form>
              </a-tab-pane>
              
              <a-tab-pane key="pcap" tab="抓包配置">
                <a-form layout="vertical" size="small">
                  <a-form-item label="启用抓包">
                    <a-switch v-model:checked="selectedNode.capture.enable" @change="updateNodeConfig" />
                  </a-form-item>
                  <a-form-item label="抓包接口" v-if="selectedNode.capture.enable">
                    <a-input v-model:value="selectedNode.capture.moduleNamePatterns" placeholder='eth[0]' @change="updateNodeConfig" />
                  </a-form-item>
                  <a-form-item label="输出文件" v-if="selectedNode.capture.enable">
                    <a-input v-model:value="selectedNode.capture.pcapFile" placeholder="results/node.pcap" @change="updateNodeConfig" />
                  </a-form-item>
                </a-form>
              </a-tab-pane>
              
              <a-tab-pane key="apps" tab="应用配置">
                <a-form layout="vertical" size="small">
                  <div v-for="(app, index) in selectedNode.apps" :key="index" style="margin-bottom: 16px; padding: 8px; border: 1px solid #f0f0f0; border-radius: 4px;">
                    <a-divider orientation="left">应用 {{ index + 1 }}</a-divider>
                    
                    <a-form-item label="应用类型">
                      <a-select v-model:value="app.typename" @change="updateNodeConfig" style="width: 100%">
                        <a-select-option v-if="selectedNode.type === 'OperatorStation'" value="OperatorStationApp">OperatorStationApp</a-select-option>
                        <a-select-option v-if="selectedNode.type === 'OperatorStation'" value="OperatorStationApp2">OperatorStationApp2</a-select-option>
                        <a-select-option v-if="selectedNode.type === 'Server'" value="ModbusMasterApp">ModbusMasterApp</a-select-option>
                        <a-select-option v-if="selectedNode.type === 'Server'" value="ModbusTcpServerApp">ModbusTcpServerApp</a-select-option>
                        <a-select-option v-if="selectedNode.type === 'Server'" value="TransitApp">TransitApp</a-select-option>
                        <a-select-option v-if="selectedNode.type === 'Client'" value="ModbusSlaveApp">ModbusSlaveApp</a-select-option>
                        <a-select-option v-if="selectedNode.type === 'Client'" value="ModbusSlaveHILApp">ModbusSlaveHILApp</a-select-option>
                      </a-select>
                    </a-form-item>
                    
                    <a-form-item label="本地端口">
                      <a-input-number v-model:value="app.localPort" :min="1" :max="65535" style="width: 100%" @change="updateNodeConfig" />
                    </a-form-item>
                    
                    <template v-if="app.typename === 'OperatorStationApp'">
                      <a-form-item label="连接地址">
                        <a-input v-model:value="app.connectAddress" placeholder="server" @change="updateNodeConfig" />
                      </a-form-item>
                      <a-form-item label="连接端口">
                        <a-input-number v-model:value="app.connectPort" :min="1" :max="65535" style="width: 100%" @change="updateNodeConfig" />
                      </a-form-item>
                      <a-form-item label="启动时间">
                        <a-input v-model:value="app.startTime" placeholder="1s" @change="updateNodeConfig" />
                      </a-form-item>
                      <a-form-item label="发送间隔">
                        <a-input v-model:value="app.interval" placeholder="0.5s" @change="updateNodeConfig" />
                      </a-form-item>
                      <a-form-item label="重连间隔">
                        <a-input v-model:value="app.reconnectInterval" placeholder="2s" @change="updateNodeConfig" />
                      </a-form-item>
                    </template>
                    
                    <template v-if="app.typename === 'OperatorStationApp2'">
                      <a-form-item label="连接地址">
                        <a-input v-model:value="app.connectAddress" placeholder="server" @change="updateNodeConfig" />
                      </a-form-item>
                      <a-form-item label="连接端口">
                        <a-input-number v-model:value="app.connectPort" :min="1" :max="65535" style="width: 100%" @change="updateNodeConfig" />
                      </a-form-item>
                      <a-form-item label="Modbus请求">
                        <a-textarea v-model:value="app.modbusRequest" :rows="2" @change="updateNodeConfig" />
                      </a-form-item>
                      <a-form-item label="发送时间">
                        <a-input v-model:value="app.sendTime" placeholder="4 5" @change="updateNodeConfig" />
                      </a-form-item>
                      <a-form-item label="随机种子">
                        <a-input-number v-model:value="app.seed" style="width: 100%" @change="updateNodeConfig" />
                      </a-form-item>
                      <a-form-item label="重连间隔">
                        <a-input v-model:value="app.reconnectInterval" placeholder="2s" @change="updateNodeConfig" />
                      </a-form-item>
                    </template>
                    
                    <template v-if="app.typename === 'ModbusMasterApp'">
                      <a-form-item label="连接端口">
                        <a-input-number v-model:value="app.connectPort" :min="1" :max="65535" style="width: 100%" @change="updateNodeConfig" />
                      </a-form-item>
                      <a-form-item label="连接数量">
                        <a-input-number v-model:value="app.numConnect" :min="1" style="width: 100%" @change="updateNodeConfig" />
                      </a-form-item>
                      <a-form-item label="配置文件">
                        <a-input v-model:value="app.configFile" placeholder="MasterConfig.json" @change="updateNodeConfig" />
                      </a-form-item>
                      <a-form-item label="读取间隔">
                        <a-input v-model:value="app.readInterval" placeholder="0.5s" @change="updateNodeConfig" />
                      </a-form-item>
                    </template>
                    
                    <template v-if="app.typename === 'ModbusSlaveApp' || app.typename === 'ModbusSlaveHILApp'">
                      <a-form-item label="从站配置文件">
                        <a-input v-model:value="app.slavesConfigPath" placeholder="SlaveConfig.json" @change="updateNodeConfig" />
                      </a-form-item>
                      <template v-if="app.typename === 'ModbusSlaveHILApp'">
                        <a-form-item label="远程地址">
                          <a-input v-model:value="app.remoteAddress" placeholder="113.54.185.18" @change="updateNodeConfig" />
                        </a-form-item>
                        <a-form-item label="远程端口">
                          <a-input-number v-model:value="app.remotePort" :min="1" :max="65535" style="width: 100%" @change="updateNodeConfig" />
                        </a-form-item>
                      </template>
                    </template>
                    
                    <a-button size="small" danger @click="removeApp(index)" style="margin-top: 8px">删除应用</a-button>
                  </div>
                  <a-button size="small" type="dashed" @click="addApp" style="width: 100%">+ 添加应用</a-button>
                </a-form>
              </a-tab-pane>
            </a-tabs>
          </a-card>
          <a-card title="拓扑信息" v-else class="config-panel">
            <a-descriptions :column="1" size="small">
              <a-descriptions-item label="节点数">{{ graphData.nodes.length }}</a-descriptions-item>
              <a-descriptions-item label="连接数">{{ graphData.edges.length }}</a-descriptions-item>
              <a-descriptions-item label="客户端数">{{ clientCount }}</a-descriptions-item>
            </a-descriptions>
            
            <a-divider />
            
            <a-space direction="vertical" style="width: 100%">
              <a-button type="primary" @click="showMasterConfigModal" block>编辑 MasterConfig</a-button>
              <a-button type="primary" @click="showSlaveConfigModal" block>编辑 SlaveConfig</a-button>
            </a-space>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- Preview Modal -->
    <a-modal
      v-model:open="previewVisible"
      :title="previewTitle"
      width="800px"
      :footer="null"
    >
      <pre style="max-height: 600px; overflow: auto">{{ previewContent }}</pre>
    </a-modal>
    
    <!-- MasterConfig Modal -->
    <a-modal
      v-model:open="masterConfigVisible"
      title="MasterConfig.json 配置"
      width="800px"
      @ok="saveMasterConfig"
    >
      <a-textarea
        v-model:value="masterConfigContent"
        :rows="20"
        placeholder="请输入 MasterConfig.json 内容"
        style="font-family: monospace"
      />
    </a-modal>
    
    <!-- SlaveConfig Modal -->
    <a-modal
      v-model:open="slaveConfigVisible"
      title="SlaveConfig.json 配置"
      width="800px"
      @ok="saveSlaveConfig"
    >
      <a-textarea
        v-model:value="slaveConfigContent"
        :rows="20"
        placeholder="请输入 SlaveConfig.json 内容"
        style="font-family: monospace"
      />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import G6, { Graph } from '@antv/g6'
import { topologyApi } from '@/api/topology'
import { nodeApi } from '@/api/node'
import type { TopologyGraph, NodeItem } from '@/types'

const route = useRoute()
const router = useRouter()

const graphContainer = ref<HTMLElement | null>(null)
const previewVisible = ref(false)
const previewTitle = ref('')
const previewContent = ref('')
const saving = ref(false)
const edgeMode = ref(false)

const projectCode = route.params.projectCode as string

let graph: Graph | null = null
let nodeIdCounter = 1
let edgeIdCounter = 1

const graphData = reactive<TopologyGraph>({
  nodes: [],
  edges: [],
  meta: {
    numClients: 0,
    hilClientIndices: [],
    networkName: 'ModbusTest1'
  }
})

const selectedNode = ref<any>(null)
const configTabKey = ref('basic')
const masterConfigVisible = ref(false)
const slaveConfigVisible = ref(false)
const masterConfigContent = ref('')
const slaveConfigContent = ref('')

const clientCount = computed(() => {
  return graphData.nodes.filter(n => n.type === 'Client').length
})

const initGraph = () => {
  if (!graphContainer.value) return

  const width = graphContainer.value.scrollWidth
  const height = graphContainer.value.scrollHeight || 600

  graph = new G6.Graph({
    container: graphContainer.value,
    width,
    height,
    modes: {
      default: ['drag-canvas', 'zoom-canvas', 'drag-node', 'click-select']
    },
    defaultNode: {
      size: 60,
      labelCfg: {
        position: 'bottom',
        offset: 5
      }
    },
    defaultEdge: {
      type: 'line',
      style: {
        stroke: '#aaa',
        lineWidth: 2
      }
    },
    layout: {
      type: 'grid',
      preventOverlap: true,
      nodeSize: 60
    }
  })

  // Node style configuration
  graph.node((node: any) => {
    const typeStyles: Record<string, any> = {
      OperatorStation: {
        style: { fill: '#5B8FF9', stroke: '#1E5BC6' },
        label: node.id
      },
      TsnSwitch: {
        style: { fill: '#5AD8A6', stroke: '#12906B' },
        label: node.id
      },
      Server: {
        style: { fill: '#F6BD16', stroke: '#C88E00' },
        label: node.id
      },
      Client: {
        style: { fill: '#E86452', stroke: '#BF2F24' },
        label: node.id
      }
    }
    
    return {
      ...typeStyles[node.type] || typeStyles.Client,
      size: 60
    }
  })

  // Click node event
  graph.on('node:click', (e) => {
    const node = e.item
    if (!node) return

    if (edgeMode.value && graph) {
      // Edge creation mode
      const model = node.getModel()
      if (!selectedNode.value) {
        selectedNode.value = model
        graph.setItemState(node, 'selected', true)
      } else {
        // Create edge
        const edgeId = `edge_${edgeIdCounter++}`
        const newEdge = {
          source: selectedNode.value.id,
          target: model.id,
          linkType: 'ethernet'
        }
        graphData.edges.push(newEdge)
        graph.addItem('edge', {
          id: edgeId,
          source: selectedNode.value.id,
          target: model.id
        })
        
        graph.setItemState(selectedNode.value.id, 'selected', false)
        selectedNode.value = null
        edgeMode.value = false
        message.success('连接已创建')
      }
    } else {
      // Normal selection mode
      const model = node.getModel()
      selectedNode.value = {
        id: model.id,
        type: model.type,
        isHil: model.isHil || false,
        localPort: model.localPort || 502,
        eth: model.eth || {
          bitrate: '100Mbps',
          channelLength: '10m',
          ber: 0,
          per: 0
        },
        capture: model.capture || {
          enable: false,
          moduleNamePatterns: 'eth[0]',
          pcapFile: `results/${model.id}.pcap`
        },
        apps: model.apps || [getDefaultApp(model.type)]
      }
      configTabKey.value = 'basic'
    }
  })

  // Click canvas to deselect
  graph.on('canvas:click', () => {
    if (!edgeMode.value) {
      selectedNode.value = null
      graph?.getNodes().forEach(node => {
        graph?.setItemState(node, 'selected', false)
      })
    }
  })

  // Node drag end - update position
  graph.on('node:dragend', (e) => {
    const node = e.item
    if (!node) return
    
    const model = node.getModel()
    const nodeIndex = graphData.nodes.findIndex(n => n.id === model.id)
    if (nodeIndex !== -1) {
      graphData.nodes[nodeIndex].x = model.x as number
      graphData.nodes[nodeIndex].y = model.y as number
    }
  })

  // Load existing topology if available
  loadTopology()
}

const loadTopology = async () => {
  try {
    const response = await topologyApi.getLatest(projectCode)
    const topology = response.data.data
    
    if (topology && topology.graph) {
      graphData.nodes = topology.graph.nodes || []
      graphData.edges = topology.graph.edges || []
      graphData.meta = topology.graph.meta || { numClients: 0, hilClientIndices: [], networkName: 'ModbusTest1' }
      
      // Update node counter
      if (graphData.nodes.length > 0) {
        const maxId = Math.max(...graphData.nodes.map(n => {
          const match = n.id.match(/\d+$/)
          return match ? parseInt(match[0]) : 0
        }))
        nodeIdCounter = maxId + 1
      }
      
      // Render graph
      renderGraph()
    }
  } catch (error) {
    // No existing topology, start fresh
    console.log('No existing topology found')
  }
}

const renderGraph = () => {
  if (!graph) return
  
  const g6Nodes = graphData.nodes.map(node => ({
    id: node.id,
    type: node.type,
    x: node.x,
    y: node.y,
    isHil: node.isHil || false,
    localPort: node.localPort || 502
  }))
  
  const g6Edges = graphData.edges.map((edge, index) => ({
    id: `edge_${index}`,
    source: edge.source,
    target: edge.target
  }))
  
  graph.data({ nodes: g6Nodes, edges: g6Edges })
  graph.render()
}

const addNode = (nodeType: string) => {
  if (!graph) return

  const nodeId = `${nodeType.toLowerCase()}_${nodeIdCounter++}`
  const x = 200 + Math.random() * 300
  const y = 200 + Math.random() * 200

  const newNode = {
    id: nodeId,
    type: nodeType,
    x,
    y
  }

  graphData.nodes.push(newNode)
  
  graph.addItem('node', {
    id: nodeId,
    type: nodeType,
    x,
    y,
    isHil: false,
    localPort: 502
  })

  message.success(`已添加${nodeType}节点`)
}

const enableEdgeMode = () => {
  edgeMode.value = !edgeMode.value
  selectedNode.value = null
  
  if (edgeMode.value) {
    message.info('连线模式已开启，点击两个节点进行连接')
  } else {
    message.info('连线模式已关闭')
    graph?.getNodes().forEach(node => {
      graph?.setItemState(node, 'selected', false)
    })
  }
}

const deleteSelected = () => {
  if (!graph) return
  
  const selectedNodes = graph.findAllByState('node', 'selected')
  if (selectedNodes.length === 0) {
    message.warning('请先选择要删除的节点')
    return
  }
  
  selectedNodes.forEach(node => {
    const model = node.getModel()
    // Remove from graphData
    const nodeIndex = graphData.nodes.findIndex(n => n.id === model.id)
    if (nodeIndex !== -1) {
      graphData.nodes.splice(nodeIndex, 1)
    }
    
    // Remove edges connected to this node
    graphData.edges = graphData.edges.filter(e => 
      e.source !== model.id && e.target !== model.id
    )
    
    // Remove from graph
    graph?.removeItem(node)
  })
  
  selectedNode.value = null
  message.success('节点已删除')
}

const clearGraph = () => {
  if (!graph) return
  
  graph.clear()
  graphData.nodes = []
  graphData.edges = []
  selectedNode.value = null
  nodeIdCounter = 1
  edgeIdCounter = 1
  
  message.success('画布已清空')
}

const updateNodeConfig = () => {
  if (!selectedNode.value || !graph) return
  
  const node = graph.findById(selectedNode.value.id)
  if (node) {
    const model = node.getModel()
    graph.updateItem(node, {
      ...model,
      isHil: selectedNode.value.isHil,
      localPort: selectedNode.value.localPort,
      eth: selectedNode.value.eth,
      capture: selectedNode.value.capture,
      apps: selectedNode.value.apps
    })
    
    // Update graphData as well
    const nodeIndex = graphData.nodes.findIndex(n => n.id === selectedNode.value.id)
    if (nodeIndex !== -1) {
      graphData.nodes[nodeIndex] = {
        ...graphData.nodes[nodeIndex],
        isHil: selectedNode.value.isHil,
        localPort: selectedNode.value.localPort,
        eth: selectedNode.value.eth,
        capture: selectedNode.value.capture,
        apps: selectedNode.value.apps
      }
    }
  }
  
  message.success('节点配置已更新')
}

const getDefaultApp = (nodeType: string) => {
  const defaultApps: Record<string, any> = {
    OperatorStation: {
      typename: 'OperatorStationApp',
      localPort: 2000,
      connectAddress: 'server',
      connectPort: 1000,
      startTime: '1s',
      interval: '0.5s',
      reconnectInterval: '2s'
    },
    Server: {
      typename: 'ModbusTcpServerApp',
      localPort: 1000
    },
    Client: {
      typename: 'ModbusSlaveApp',
      localPort: 502,
      slavesConfigPath: 'SlaveConfig.json'
    }
  }
  return defaultApps[nodeType] || { typename: 'ModbusTcpServerApp', localPort: 502 }
}

const addApp = () => {
  if (!selectedNode.value) return
  selectedNode.value.apps.push(getDefaultApp(selectedNode.value.type))
  updateNodeConfig()
}

const removeApp = (index: number) => {
  if (!selectedNode.value) return
  selectedNode.value.apps.splice(index, 1)
  updateNodeConfig()
}

const showMasterConfigModal = () => {
  // Load existing master config or show template
  const template = {
    connectArray: [
      {
        ipAddress: '10.0.0.7',
        numSlave: 2,
        slaves: [
          {
            slaveId: 1,
            numBitGroup: 1,
            numInputBitGroup: 0,
            numRegisterGroup: 1,
            numInputRegisterGroup: 0,
            bitGroup: [
              {
                startAddress: 0,
                number: 4,
                data: [1, 0, 1, 0]
              }
            ],
            inputBitGroup: [],
            registerGroup: [
              {
                startAddress: 10,
                number: 10,
                data: [4660, 22136, 0, 0, 0, 0, 0, 0, 0, 0]
              }
            ],
            inputRegisterGroup: []
          }
        ]
      }
    ]
  }
  masterConfigContent.value = JSON.stringify(template, null, 2)
  masterConfigVisible.value = true
}

const showSlaveConfigModal = () => {
  // Load existing slave config or show template
  const template = {
    connectArray: [
      {
        ipAddress: '10.0.0.7',
        numSlave: 1,
        slaves: [
          {
            slaveId: 1,
            numBitGroup: 1,
            numInputBitGroup: 0,
            numRegisterGroup: 1,
            numInputRegisterGroup: 0,
            bitGroup: [
              {
                startAddress: 0,
                number: 4,
                data: [1, 0, 1, 0]
              }
            ],
            inputBitGroup: [],
            registerGroup: [
              {
                startAddress: 10,
                number: 10,
                data: [4660, 22136, 0, 0, 0, 0, 0, 0, 0, 0]
              }
            ],
            inputRegisterGroup: []
          }
        ]
      }
    ]
  }
  slaveConfigContent.value = JSON.stringify(template, null, 2)
  slaveConfigVisible.value = true
}

const saveMasterConfig = () => {
  try {
    JSON.parse(masterConfigContent.value)
    graphData.meta.masterConfig = masterConfigContent.value
    masterConfigVisible.value = false
    message.success('MasterConfig 已保存')
  } catch (error) {
    message.error('JSON 格式错误，请检查')
  }
}

const saveSlaveConfig = () => {
  try {
    JSON.parse(slaveConfigContent.value)
    graphData.meta.slaveConfig = slaveConfigContent.value
    slaveConfigVisible.value = false
    message.success('SlaveConfig 已保存')
  } catch (error) {
    message.error('JSON 格式错误，请检查')
  }
}

const goBack = () => {
  router.push(`/projects/${projectCode}`)
}

const previewNed = async () => {
  try {
    const response = await topologyApi.previewNed(projectCode)
    previewTitle.value = 'NED 文件预览'
    previewContent.value = response.data.data.content
    previewVisible.value = true
  } catch (error) {
    message.error('预览 NED 失败')
  }
}

const previewIni = async () => {
  try {
    const response = await topologyApi.previewIni(projectCode)
    previewTitle.value = 'INI 文件预览'
    previewContent.value = response.data.data.content
    previewVisible.value = true
  } catch (error) {
    message.error('预览 INI 失败')
  }
}

const saveTopology = async () => {
  if (graphData.nodes.length === 0) {
    message.warning('拓扑为空，无法保存')
    return
  }
  
  saving.value = true
  
  try {
    // Update meta information
    graphData.meta.numClients = clientCount.value
    graphData.meta.hilClientIndices = graphData.nodes
      .map((node, index) => node.type === 'Client' && node.isHil ? index : -1)
      .filter(index => index !== -1)
    
    // Save nodes with configurations
    const nodeItems: NodeItem[] = graphData.nodes.map(node => ({
      nodeId: node.id,
      nodeType: node.type,
      displayX: Math.round(node.x),
      displayY: Math.round(node.y),
      params: {
        isHil: node.isHil || false,
        eth: node.eth || {
          bitrate: '100Mbps',
          channelLength: '10m',
          ber: 0,
          per: 0
        },
        capture: node.capture || {
          enable: false,
          moduleNamePatterns: 'eth[0]',
          pcapFile: `results/${node.id}.pcap`
        },
        apps: node.apps || [getDefaultApp(node.type)]
      }
    }))
    
    // Batch save nodes
    await nodeApi.batchSave(projectCode, nodeItems)
    
    // Save topology
    await topologyApi.save(projectCode, {
      graph: {
        nodes: graphData.nodes,
        edges: graphData.edges,
        meta: graphData.meta
      }
    })
    
    message.success('拓扑保存成功')
  } catch (error: any) {
    message.error('保存拓扑失败: ' + (error.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  initGraph()
})

onBeforeUnmount(() => {
  if (graph) {
    graph.destroy()
  }
})
</script>

<style scoped>
.topology-editor-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
}

.editor-content {
  flex: 1;
  padding: 16px;
  overflow: hidden;
}

.toolbar-card {
  margin-bottom: 16px;
}

.graph-container {
  height: calc(100vh - 280px);
  background: white;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  border: 1px solid #e8e8e8;
}

.config-panel {
  height: calc(100vh - 280px);
  overflow-y: auto;
}

:deep(.g6-tooltip) {
  background: rgba(0, 0, 0, 0.75);
  color: white;
  padding: 8px 12px;
  border-radius: 4px;
}
</style>
