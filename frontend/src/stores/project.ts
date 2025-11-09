import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Project } from '@/types'

export const useProjectStore = defineStore('project', () => {
  const currentProject = ref<Project | null>(null)
  const projects = ref<Project[]>([])

  function setCurrentProject(project: Project | null) {
    currentProject.value = project
  }

  function setProjects(list: Project[]) {
    projects.value = list
  }

  return {
    currentProject,
    projects,
    setCurrentProject,
    setProjects
  }
})
