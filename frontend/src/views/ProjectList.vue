<template>
  <div class="project-list-container">
    <a-layout>
      <a-layout-header class="header">
        <h1>工业控制网络仿真平台</h1>
      </a-layout-header>
      <a-layout-content class="content">
        <div class="toolbar">
          <a-input-search
            v-model:value="searchKeyword"
            placeholder="搜索项目名称"
            style="width: 300px"
            @search="loadProjects"
          />
          <a-button type="primary" @click="showCreateModal">
            <template #icon><PlusOutlined /></template>
            新建项目
          </a-button>
        </div>

        <a-table
          :columns="columns"
          :data-source="projects"
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
                <a-button size="small" @click="viewProject(record)">查看</a-button>
                <a-button size="small" @click="editTopology(record)">拓扑编辑</a-button>
                <a-button size="small" danger @click="deleteProject(record)">删除</a-button>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-layout-content>
    </a-layout>

    <!-- Create Project Modal -->
    <a-modal
      v-model:open="createModalVisible"
      title="新建项目"
      @ok="handleCreateProject"
    >
      <a-form :model="newProject" layout="vertical">
        <a-form-item label="项目名称" required>
          <a-input v-model:value="newProject.projectName" placeholder="请输入项目名称" />
        </a-form-item>
        <a-form-item label="仿真时长">
          <a-input v-model:value="newProject.simTimeLimit" placeholder="如: 10s, 1h" />
        </a-form-item>
        <a-form-item label="项目描述">
          <a-textarea v-model:value="newProject.description" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { projectApi } from '@/api/project'
import type { Project } from '@/types'

const router = useRouter()

const loading = ref(false)
const projects = ref<Project[]>([])
const searchKeyword = ref('')
const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0
})

const createModalVisible = ref(false)
const newProject = ref({
  projectName: '',
  simTimeLimit: '10s',
  description: ''
})

const columns = [
  { title: '项目编号', dataIndex: 'projectCode', key: 'projectCode' },
  { title: '项目名称', dataIndex: 'projectName', key: 'projectName' },
  { title: '仿真时长', dataIndex: 'simTimeLimit', key: 'simTimeLimit' },
  { title: '状态', key: 'status' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', key: 'action', width: 250 }
]

const loadProjects = async () => {
  loading.value = true
  try {
    const response = await projectApi.list({
      page: pagination.value.current,
      pageSize: pagination.value.pageSize,
      keywords: searchKeyword.value
    })
    projects.value = response.data.data.list
    pagination.value.total = response.data.data.total
  } catch (error) {
    message.error('加载项目列表失败')
  } finally {
    loading.value = false
  }
}

const handleTableChange = (pag: any) => {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  loadProjects()
}

const showCreateModal = () => {
  createModalVisible.value = true
  newProject.value = {
    projectName: '',
    simTimeLimit: '10s',
    description: ''
  }
}

const handleCreateProject = async () => {
  try {
    await projectApi.create(newProject.value)
    message.success('项目创建成功')
    createModalVisible.value = false
    loadProjects()
  } catch (error) {
    message.error('项目创建失败')
  }
}

const viewProject = (project: Project) => {
  router.push(`/projects/${project.projectCode}`)
}

const editTopology = (project: Project) => {
  router.push(`/projects/${project.projectCode}/topology`)
}

const deleteProject = async (project: Project) => {
  try {
    await projectApi.delete(project.projectCode)
    message.success('项目删除成功')
    loadProjects()
  } catch (error) {
    message.error('项目删除失败')
  }
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
  loadProjects()
})
</script>

<style scoped>
.project-list-container {
  height: 100%;
}

.header {
  background: #001529;
  color: white;
  display: flex;
  align-items: center;
  padding: 0 24px;
}

.header h1 {
  color: white;
  margin: 0;
  font-size: 20px;
}

.content {
  padding: 24px;
  background: #f0f2f5;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}
</style>
