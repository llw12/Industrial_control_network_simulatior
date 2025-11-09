package com.industrial.sim.controller;

import com.industrial.sim.dto.ApiResponse;
import com.industrial.sim.dto.ProjectDTO.*;
import com.industrial.sim.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {
    
    private final ProjectService projectService;
    
    @PostMapping
    public ApiResponse<ProjectResp> createProject(@RequestBody CreateProjectReq req) {
        return ApiResponse.success(projectService.createProject(req));
    }
    
    @GetMapping
    public ApiResponse<ProjectListResp> listProjects(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keywords) {
        return ApiResponse.success(projectService.listProjects(page, pageSize, keywords));
    }
    
    @GetMapping("/{projectCode}")
    public ApiResponse<ProjectResp> getProject(@PathVariable String projectCode) {
        return ApiResponse.success(projectService.getProject(projectCode));
    }
    
    @PutMapping("/{projectCode}")
    public ApiResponse<ProjectResp> updateProject(
            @PathVariable String projectCode,
            @RequestBody UpdateProjectReq req) {
        return ApiResponse.success(projectService.updateProject(projectCode, req));
    }
    
    @DeleteMapping("/{projectCode}")
    public ApiResponse<Void> deleteProject(@PathVariable String projectCode) {
        projectService.deleteProject(projectCode);
        return ApiResponse.success(null);
    }
}
