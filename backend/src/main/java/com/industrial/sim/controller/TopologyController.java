package com.industrial.sim.controller;

import com.industrial.sim.dto.ApiResponse;
import com.industrial.sim.dto.TopologyDTO.*;
import com.industrial.sim.service.TopologyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects/{projectCode}")
@RequiredArgsConstructor
public class TopologyController {
    
    private final TopologyService topologyService;
    
    @PostMapping("/topology")
    public ApiResponse<TopologyResp> saveTopology(
            @PathVariable String projectCode,
            @RequestBody SaveTopologyReq req) {
        return ApiResponse.success(topologyService.saveTopology(projectCode, req));
    }
    
    @GetMapping("/topology/latest")
    public ApiResponse<TopologyResp> getLatestTopology(@PathVariable String projectCode) {
        return ApiResponse.success(topologyService.getLatestTopology(projectCode));
    }
    
    @GetMapping("/topology/{version}")
    public ApiResponse<TopologyResp> getTopologyByVersion(
            @PathVariable String projectCode,
            @PathVariable Integer version) {
        return ApiResponse.success(topologyService.getTopologyByVersion(projectCode, version));
    }
    
    @PostMapping("/preview/ned")
    public ApiResponse<PreviewResp> previewNed(
            @PathVariable String projectCode,
            @RequestBody PreviewReq req) {
        return ApiResponse.success(topologyService.previewNed(projectCode, req));
    }
    
    @PostMapping("/preview/ini")
    public ApiResponse<PreviewResp> previewIni(
            @PathVariable String projectCode,
            @RequestBody PreviewReq req) {
        return ApiResponse.success(topologyService.previewIni(projectCode, req));
    }
}
