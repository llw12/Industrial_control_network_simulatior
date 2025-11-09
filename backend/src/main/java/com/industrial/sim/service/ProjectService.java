package com.industrial.sim.service;

import com.industrial.sim.dto.ProjectDTO.*;
import com.industrial.sim.entity.Project;
import com.industrial.sim.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    
    private final ProjectRepository projectRepository;
    
    @Transactional
    public ProjectResp createProject(CreateProjectReq req) {
        // Check if project name already exists
        if (projectRepository.existsByProjectName(req.getProjectName())) {
            throw new RuntimeException("项目名已存在");
        }
        
        Project project = new Project();
        project.setProjectCode("PRJ_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        project.setProjectName(req.getProjectName());
        project.setSimTimeLimit(req.getSimTimeLimit());
        project.setDescription(req.getDescription());
        project.setCreateUser(req.getCreateUser());
        project.setStatus(0);
        
        project = projectRepository.save(project);
        return toProjectResp(project);
    }
    
    public ProjectResp getProject(String projectCode) {
        Project project = projectRepository.findByProjectCode(projectCode)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
        return toProjectResp(project);
    }
    
    public ProjectListResp listProjects(Integer page, Integer pageSize, String keywords) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Project> projectPage;
        
        if (keywords != null && !keywords.isEmpty()) {
            projectPage = projectRepository.findByProjectNameContaining(keywords, pageable);
        } else {
            projectPage = projectRepository.findAll(pageable);
        }
        
        ProjectListResp resp = new ProjectListResp();
        resp.setList(projectPage.getContent().stream()
                .map(this::toProjectResp)
                .collect(Collectors.toList()));
        resp.setTotal(projectPage.getTotalElements());
        return resp;
    }
    
    @Transactional
    public ProjectResp updateProject(String projectCode, UpdateProjectReq req) {
        Project project = projectRepository.findByProjectCode(projectCode)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
        
        if (req.getProjectName() != null) {
            project.setProjectName(req.getProjectName());
        }
        if (req.getSimTimeLimit() != null) {
            project.setSimTimeLimit(req.getSimTimeLimit());
        }
        if (req.getDescription() != null) {
            project.setDescription(req.getDescription());
        }
        
        project = projectRepository.save(project);
        return toProjectResp(project);
    }
    
    @Transactional
    public void deleteProject(String projectCode) {
        Project project = projectRepository.findByProjectCode(projectCode)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
        projectRepository.delete(project);
    }
    
    private ProjectResp toProjectResp(Project project) {
        ProjectResp resp = new ProjectResp();
        resp.setProjectCode(project.getProjectCode());
        resp.setProjectName(project.getProjectName());
        resp.setSimTimeLimit(project.getSimTimeLimit());
        resp.setStatus(project.getStatus());
        resp.setDescription(project.getDescription());
        resp.setCreateUser(project.getCreateUser());
        resp.setCreateTime(project.getCreateTime());
        resp.setUpdateTime(project.getUpdateTime());
        return resp;
    }
}
