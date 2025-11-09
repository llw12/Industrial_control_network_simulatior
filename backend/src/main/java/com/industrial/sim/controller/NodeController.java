package com.industrial.sim.controller;

import com.industrial.sim.dto.ApiResponse;
import com.industrial.sim.dto.NodeDTO.*;
import com.industrial.sim.service.NodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects/{projectCode}/nodes")
@RequiredArgsConstructor
public class NodeController {
    
    private final NodeService nodeService;
    
    @GetMapping
    public ApiResponse<NodeListResp> listNodes(@PathVariable String projectCode) {
        return ApiResponse.success(nodeService.listNodes(projectCode));
    }
    
    @PostMapping
    public ApiResponse<NodeResp> saveNode(
            @PathVariable String projectCode,
            @RequestBody SaveNodeReq req) {
        return ApiResponse.success(nodeService.saveNode(projectCode, req));
    }
    
    @PostMapping("/batch")
    public ApiResponse<Void> batchSaveNodes(
            @PathVariable String projectCode,
            @RequestBody BatchSaveReq req) {
        nodeService.batchSaveNodes(projectCode, req);
        return ApiResponse.success(null);
    }
    
    @GetMapping("/{nodeId}")
    public ApiResponse<NodeResp> getNode(
            @PathVariable String projectCode,
            @PathVariable String nodeId) {
        return ApiResponse.success(nodeService.getNode(projectCode, nodeId));
    }
    
    @PutMapping("/{nodeId}")
    public ApiResponse<NodeResp> updateNode(
            @PathVariable String projectCode,
            @PathVariable String nodeId,
            @RequestBody UpdateNodeReq req) {
        return ApiResponse.success(nodeService.updateNode(projectCode, nodeId, req));
    }
    
    @DeleteMapping("/{nodeId}")
    public ApiResponse<Void> deleteNode(
            @PathVariable String projectCode,
            @PathVariable String nodeId) {
        nodeService.deleteNode(projectCode, nodeId);
        return ApiResponse.success(null);
    }
    
    @PostMapping("/ports/check")
    public ApiResponse<PortCheckResp> checkPortConflicts(
            @PathVariable String projectCode,
            @RequestBody PortCheckReq req) {
        return ApiResponse.success(nodeService.checkPortConflicts(projectCode, req));
    }
}
