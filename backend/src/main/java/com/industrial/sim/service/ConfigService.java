package com.industrial.sim.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.industrial.sim.dto.ConfigDTO.*;
import com.industrial.sim.entity.ConfigFile;
import com.industrial.sim.repository.ConfigFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConfigService {
    
    private final ConfigFileRepository configFileRepository;
    private final ObjectMapper objectMapper;
    
    @Transactional
    public ConfigResp saveConfig(String projectCode, SaveConfigReq req) {
        // Get next version
        Integer maxVersion = configFileRepository.findMaxVersionByProjectCodeAndFileType(
                projectCode, req.getFileType());
        int nextVersion = (maxVersion == null) ? 1 : maxVersion + 1;
        
        ConfigFile configFile = new ConfigFile();
        configFile.setProjectCode(projectCode);
        configFile.setFileType(req.getFileType());
        configFile.setVersion(nextVersion);
        configFile.setDescription(req.getDescription());
        
        try {
            configFile.setContentJson(objectMapper.writeValueAsString(req.getContentJson()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("配置JSON序列化失败", e);
        }
        
        configFile = configFileRepository.save(configFile);
        return toConfigResp(configFile);
    }
    
    public ConfigListResp listConfigs(String projectCode, String fileType) {
        List<ConfigFile> configs;
        if (fileType != null) {
            configs = configFileRepository.findByProjectCodeAndFileType(projectCode, fileType);
        } else {
            configs = configFileRepository.findByProjectCodeAndFileType(projectCode, "MASTER");
            configs.addAll(configFileRepository.findByProjectCodeAndFileType(projectCode, "SLAVE"));
        }
        
        ConfigListResp resp = new ConfigListResp();
        resp.setList(configs.stream().map(this::toConfigResp).collect(Collectors.toList()));
        return resp;
    }
    
    public ConfigResp getConfig(String projectCode, Long configId) {
        ConfigFile configFile = configFileRepository.findById(configId)
                .orElseThrow(() -> new RuntimeException("配置文件不存在"));
        
        if (!configFile.getProjectCode().equals(projectCode)) {
            throw new RuntimeException("配置文件不属于该项目");
        }
        
        return toConfigResp(configFile);
    }
    
    @Transactional
    public ConfigResp updateConfig(String projectCode, Long configId, SaveConfigReq req) {
        ConfigFile configFile = configFileRepository.findById(configId)
                .orElseThrow(() -> new RuntimeException("配置文件不存在"));
        
        if (!configFile.getProjectCode().equals(projectCode)) {
            throw new RuntimeException("配置文件不属于该项目");
        }
        
        if (req.getDescription() != null) {
            configFile.setDescription(req.getDescription());
        }
        
        if (req.getContentJson() != null) {
            try {
                configFile.setContentJson(objectMapper.writeValueAsString(req.getContentJson()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("配置JSON序列化失败", e);
            }
        }
        
        configFile = configFileRepository.save(configFile);
        return toConfigResp(configFile);
    }
    
    @Transactional
    public void deleteConfig(String projectCode, Long configId) {
        ConfigFile configFile = configFileRepository.findById(configId)
                .orElseThrow(() -> new RuntimeException("配置文件不存在"));
        
        if (!configFile.getProjectCode().equals(projectCode)) {
            throw new RuntimeException("配置文件不属于该项目");
        }
        
        configFileRepository.delete(configFile);
    }
    
    public ValidateConfigResp validateConfig(ValidateConfigReq req) {
        ValidateConfigResp resp = new ValidateConfigResp();
        List<String> errors = new ArrayList<>();
        
        try {
            // Basic validation: check if it's valid JSON and has required fields
            Map<String, Object> config = req.getContentJson();
            
            if ("MASTER".equals(req.getFileType())) {
                // Validate master config structure
                if (!config.containsKey("masters")) {
                    errors.add("缺少 masters 字段");
                }
            } else if ("SLAVE".equals(req.getFileType())) {
                // Validate slave config structure
                if (!config.containsKey("slaves")) {
                    errors.add("缺少 slaves 字段");
                }
            }
            
            resp.setValid(errors.isEmpty());
            resp.setErrors(errors);
            return resp;
        } catch (Exception e) {
            errors.add("配置格式错误: " + e.getMessage());
            resp.setValid(false);
            resp.setErrors(errors);
            return resp;
        }
    }
    
    private ConfigResp toConfigResp(ConfigFile configFile) {
        ConfigResp resp = new ConfigResp();
        resp.setId(configFile.getId());
        resp.setProjectCode(configFile.getProjectCode());
        resp.setFileType(configFile.getFileType());
        resp.setVersion(configFile.getVersion());
        resp.setDescription(configFile.getDescription());
        resp.setCreateTime(configFile.getCreateTime());
        
        try {
            if (configFile.getContentJson() != null) {
                resp.setContentJson(objectMapper.readValue(configFile.getContentJson(), Map.class));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("配置JSON反序列化失败", e);
        }
        
        return resp;
    }
}
