<template>
  <div class="simulation-detail-container">
    <a-page-header
      title="仿真详情"
      @back="goBack"
    >
      <template #extra>
        <a-button v-if="simulation?.status === 1" danger @click="stopSimulation">
          停止仿真
        </a-button>
      </template>
    </a-page-header>

    <div class="content">
      <a-card v-if="simulation" :loading="loading" title="基本信息">
        <a-descriptions bordered :column="2">
          <a-descriptions-item label="运行ID">
            {{ simulation.runId }}
          </a-descriptions-item>
          <a-descriptions-item label="项目编号">
            {{ simulation.projectCode }}
          </a-descriptions-item>
          <a-descriptions-item label="拓扑版本">
            {{ simulation.topologyVersion }}
          </a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="getStatusColor(simulation.status)">
              {{ getStatusText(simulation.status) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="开始时间">
            {{ simulation.startTime }}
          </a-descriptions-item>
          <a-descriptions-item label="结束时间">
            {{ simulation.endTime || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="INI文件路径" :span="2">
            {{ simulation.iniFilePath }}
          </a-descriptions-item>
          <a-descriptions-item label="NED文件路径" :span="2">
            {{ simulation.nedFilePath }}
          </a-descriptions-item>
        </a-descriptions>
      </a-card>

      <a-card title="运行日志" style="margin-top: 16px">
        <div class="log-container">
          <pre>{{ logs }}</pre>
        </div>
      </a-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { simulationApi } from '@/api/simulation'
import type { SimulationRun } from '@/types'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const simulation = ref<SimulationRun | null>(null)
const logs = ref('日志加载中...\n(WebSocket 实时日志待实现)')

const runId = route.params.runId as string

const loadSimulation = async () => {
  loading.value = true
  try {
    const response = await simulationApi.get(runId)
    simulation.value = response.data.data
  } catch (error) {
    message.error('加载仿真详情失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

const stopSimulation = async () => {
  try {
    await simulationApi.stop(runId)
    message.success('仿真已停止')
    loadSimulation()
  } catch (error) {
    message.error('停止仿真失败')
  }
}

const getStatusColor = (status: number) => {
  const colors = ['default', 'processing', 'success', 'error', 'warning']
  return colors[status] || 'default'
}

const getStatusText = (status: number) => {
  const texts = ['启动中', '运行中', '已完成', '失败', '已停止']
  return texts[status] || '未知'
}

onMounted(() => {
  loadSimulation()
  // TODO: Connect WebSocket for real-time logs
})
</script>

<style scoped>
.simulation-detail-container {
  height: 100%;
  background: #f0f2f5;
}

.content {
  padding: 24px;
}

.log-container {
  max-height: 400px;
  overflow-y: auto;
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 16px;
  border-radius: 4px;
}

.log-container pre {
  margin: 0;
  font-family: 'Courier New', monospace;
  font-size: 12px;
}
</style>
