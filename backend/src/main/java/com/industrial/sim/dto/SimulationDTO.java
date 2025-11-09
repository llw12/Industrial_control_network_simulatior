package com.industrial.sim.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SimulationDTO {
    
    @Data
    public static class IniTemplateParams {
        private String simTimeLimit;
        private Boolean enableHardInLoop;
        private List<CaptureRule> captureRules;
    }
    
    @Data
    public static class CaptureRule {
        private String nodeId;
        private String moduleNamePatterns;
        private String pcapFile;
    }
    
    @Data
    public static class StartSimulationReq {
        private String projectCode;
        private Integer topologyVersion;
        private IniTemplateParams iniTemplateParams;
        private Boolean queued;
    }
    
    @Data
    public static class StopSimulationReq {
        private String reason;
    }
    
    @Data
    public static class PrecheckReq {
        private String projectCode;
        private Integer topologyVersion;
    }
    
    @Data
    public static class PrecheckResp {
        private Boolean pass;
        private List<ErrorDetail> errors;
        private List<String> warnings;
        private List<Integer> hilClientIndices;
        private Integer estimatedRuntimeSec;
    }
    
    @Data
    public static class ErrorDetail {
        private String type;
        private String nodeId;
        private String detail;
    }
    
    @Data
    public static class SimulationResp {
        private String runId;
        private String projectCode;
        private Integer topologyVersion;
        private Integer status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String iniFilePath;
        private String nedFilePath;
        private String logPath;
    }
    
    @Data
    public static class SimulationListResp {
        private List<SimulationResp> list;
        private Long total;
    }
}
