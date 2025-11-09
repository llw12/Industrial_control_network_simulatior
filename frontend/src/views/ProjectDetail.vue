<template>
  <div class="project-detail-container">
    <a-page-header
      title="项目详情"
      @back="goBack"
    >
      <template #extra>
        <a-button @click="editTopology">拓扑编辑</a-button>
        <a-button type="primary" @click="viewSimulations">仿真列表</a-button>
      </template>
    </a-page-header>

    <a-card v-if="project" :loading="loading" style="margin: 24px">
      <a-descriptions bordered :column="2">
        <a-descriptions-item label="项目编号">
          {{ project.projectCode }}
        </a-descriptions-item>
        <a-descriptions-item label="项目名称">
          {{ project.projectName }}
        </a-descriptions-item>
        <a-descriptions-item label="仿真时长">
          {{ project.simTimeLimit }}
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="getStatusColor(project.status)">
            {{ getStatusText(project.status) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间" :span="2">
          {{ project.createTime }}
        </a-descriptions-item>
        <a-descriptions-item label="项目描述" :span="2">
          {{ project.description || '-' }}
        </a-descriptions-item>
      </a-descriptions>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { projectApi } from '@/api/project'
import type { Project } from '@/types'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const project = ref<Project | null>(null)

const projectCode = route.params.projectCode as string

const loadProject = async () => {
  loading.value = true
  try {
    const response = await projectApi.get(projectCode)
    project.value = response.data.data
  } catch (error) {
    message.error('加载项目详情失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/projects')
}

const editTopology = () => {
  router.push(`/projects/${projectCode}/topology`)
}

const viewSimulations = () => {
  router.push(`/projects/${projectCode}/simulations`)
}

const getStatusColor = (status: number) => {
  const colors = ['default', 'processing', 'success', 'error']
  return colors[status] || 'default'
}

const getStatusText = (status: number) => {
  const texts = ['未运行', '运行中', '已完成', '失败']
  return texts[status] || '未知'
}

onMounted(() => {
  loadProject()
})
</script>

<style scoped>
.project-detail-container {
  height: 100%;
  background: #f0f2f5;
}
</style>
