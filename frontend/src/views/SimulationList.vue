<template>
  <div class="simulation-list-container">
    <a-page-header
      title="仿真列表"
      @back="goBack"
    >
      <template #extra>
        <a-button type="primary" @click="startSimulation">启动仿真</a-button>
      </template>
    </a-page-header>

    <div class="content">
      <a-table
        :columns="columns"
        :data-source="simulations"
        :pagination="pagination"
        :loading="loading"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" @click="viewSimulation(record)">查看</a-button>
              <a-button
                v-if="record.status === 1"
                size="small"
                danger
                @click="stopSimulation(record)"
              >
                停止
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
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
const simulations = ref<SimulationRun[]>([])
const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0
})

const projectCode = route.params.projectCode as string

const columns = [
  { title: '运行ID', dataIndex: 'runId', key: 'runId' },
  { title: '拓扑版本', dataIndex: 'topologyVersion', key: 'topologyVersion' },
  { title: '状态', key: 'status' },
  { title: '开始时间', dataIndex: 'startTime', key: 'startTime' },
  { title: '结束时间', dataIndex: 'endTime', key: 'endTime' },
  { title: '操作', key: 'action', width: 200 }
]

const loadSimulations = async () => {
  loading.value = true
  try {
    const response = await simulationApi.list({
      projectCode,
      page: pagination.value.current,
      pageSize: pagination.value.pageSize
    })
    simulations.value = response.data.data.list
    pagination.value.total = response.data.data.total
  } catch (error) {
    message.error('加载仿真列表失败')
  } finally {
    loading.value = false
  }
}

const handleTableChange = (pag: any) => {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  loadSimulations()
}

const goBack = () => {
  router.push(`/projects/${projectCode}`)
}

const startSimulation = () => {
  message.info('启动仿真功能待实现')
  // TODO: Start simulation
}

const viewSimulation = (simulation: SimulationRun) => {
  router.push(`/simulations/${simulation.runId}`)
}

const stopSimulation = async (simulation: SimulationRun) => {
  try {
    await simulationApi.stop(simulation.runId)
    message.success('仿真已停止')
    loadSimulations()
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
  loadSimulations()
})
</script>

<style scoped>
.simulation-list-container {
  height: 100%;
  background: #f0f2f5;
}

.content {
  padding: 24px;
}
</style>
