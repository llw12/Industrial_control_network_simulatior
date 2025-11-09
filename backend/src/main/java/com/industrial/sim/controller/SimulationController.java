package com.industrial.sim.controller;

import com.industrial.sim.dto.ApiResponse;
import com.industrial.sim.dto.SimulationDTO.*;
import com.industrial.sim.service.SimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/simulations")
@RequiredArgsConstructor
public class SimulationController {
    
    private final SimulationService simulationService;
    
    @PostMapping("/precheck")
    public ApiResponse<PrecheckResp> precheck(@RequestBody PrecheckReq req) {
        return ApiResponse.success(simulationService.precheck(req));
    }
    
    @PostMapping("/start")
    public ApiResponse<SimulationResp> startSimulation(@RequestBody StartSimulationReq req) {
        return ApiResponse.success(simulationService.startSimulation(req));
    }
    
    @PostMapping("/{runId}/stop")
    public ApiResponse<Void> stopSimulation(
            @PathVariable String runId,
            @RequestBody(required = false) StopSimulationReq req) {
        simulationService.stopSimulation(runId, req);
        return ApiResponse.success(null);
    }
    
    @GetMapping
    public ApiResponse<SimulationListResp> listSimulations(
            @RequestParam(required = false) String projectCode,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return ApiResponse.success(simulationService.listSimulations(projectCode, status, page, pageSize));
    }
    
    @GetMapping("/{runId}")
    public ApiResponse<SimulationResp> getSimulation(@PathVariable String runId) {
        return ApiResponse.success(simulationService.getSimulation(runId));
    }
}
