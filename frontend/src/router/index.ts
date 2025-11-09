import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/projects'
    },
    {
      path: '/projects',
      name: 'Projects',
      component: () => import('@/views/ProjectList.vue')
    },
    {
      path: '/projects/:projectCode',
      name: 'ProjectDetail',
      component: () => import('@/views/ProjectDetail.vue')
    },
    {
      path: '/projects/:projectCode/topology',
      name: 'TopologyEditor',
      component: () => import('@/views/TopologyEditor.vue')
    },
    {
      path: '/projects/:projectCode/simulations',
      name: 'Simulations',
      component: () => import('@/views/SimulationList.vue')
    },
    {
      path: '/simulations/:runId',
      name: 'SimulationDetail',
      component: () => import('@/views/SimulationDetail.vue')
    }
  ]
})

export default router
