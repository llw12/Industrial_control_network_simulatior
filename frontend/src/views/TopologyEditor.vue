<template>
  <div class="topology-editor-container">
    <a-page-header
      title="拓扑编辑器"
      sub-title="使用 G6 进行拓扑绘制"
      @back="goBack"
    >
      <template #extra>
        <a-button @click="previewNed">预览 NED</a-button>
        <a-button @click="previewIni">预览 INI</a-button>
        <a-button type="primary" @click="saveTopology">保存拓扑</a-button>
      </template>
    </a-page-header>

    <div class="editor-content">
      <a-card class="toolbar-card">
        <a-space>
          <a-button @click="addNode('OperatorStation')">添加操作站</a-button>
          <a-button @click="addNode('TsnSwitch')">添加交换机</a-button>
          <a-button @click="addNode('Server')">添加服务器</a-button>
          <a-button @click="addNode('Client')">添加客户端</a-button>
        </a-space>
      </a-card>

      <div ref="graphContainer" class="graph-container"></div>
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { topologyApi } from '@/api/topology'

const route = useRoute()
const router = useRouter()

const graphContainer = ref<HTMLElement | null>(null)
const previewVisible = ref(false)
const previewTitle = ref('')
const previewContent = ref('')

const projectCode = route.params.projectCode as string

const goBack = () => {
  router.push(`/projects/${projectCode}`)
}

const addNode = (nodeType: string) => {
  message.info(`添加节点: ${nodeType} (G6 集成待实现)`)
  // TODO: Integrate G6 for topology editing
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

const saveTopology = () => {
  message.success('保存拓扑 (功能待实现)')
  // TODO: Save topology with G6 graph data
}

onMounted(() => {
  // TODO: Initialize G6 graph
  message.info('拓扑编辑器初始化 (G6 集成待实现)')
})
</script>

<style scoped>
.topology-editor-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.editor-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 16px;
  background: #f0f2f5;
}

.toolbar-card {
  margin-bottom: 16px;
}

.graph-container {
  flex: 1;
  background: white;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}
</style>
