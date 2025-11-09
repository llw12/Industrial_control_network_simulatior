package com.industrial.sim.controller;

import com.industrial.sim.dto.ApiResponse;
import com.industrial.sim.dto.ConfigDTO.*;
import com.industrial.sim.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects/{projectCode}/configs")
@RequiredArgsConstructor
public class ConfigController {
    
    private final ConfigService configService;
    
    @GetMapping
    public ApiResponse<ConfigListResp> listConfigs(
            @PathVariable String projectCode,
            @RequestParam(required = false) String fileType) {
        return ApiResponse.success(configService.listConfigs(projectCode, fileType));
    }
    
    @PostMapping
    public ApiResponse<ConfigResp> saveConfig(
            @PathVariable String projectCode,
            @RequestBody SaveConfigReq req) {
        return ApiResponse.success(configService.saveConfig(projectCode, req));
    }
    
    @GetMapping("/{configId}")
    public ApiResponse<ConfigResp> getConfig(
            @PathVariable String projectCode,
            @PathVariable Long configId) {
        return ApiResponse.success(configService.getConfig(projectCode, configId));
    }
    
    @PutMapping("/{configId}")
    public ApiResponse<ConfigResp> updateConfig(
            @PathVariable String projectCode,
            @PathVariable Long configId,
            @RequestBody SaveConfigReq req) {
        return ApiResponse.success(configService.updateConfig(projectCode, configId, req));
    }
    
    @DeleteMapping("/{configId}")
    public ApiResponse<Void> deleteConfig(
            @PathVariable String projectCode,
            @PathVariable Long configId) {
        configService.deleteConfig(projectCode, configId);
        return ApiResponse.success(null);
    }
    
    @PostMapping("/validate")
    public ApiResponse<ValidateConfigResp> validateConfig(
            @PathVariable String projectCode,
            @RequestBody ValidateConfigReq req) {
        return ApiResponse.success(configService.validateConfig(req));
    }
}
