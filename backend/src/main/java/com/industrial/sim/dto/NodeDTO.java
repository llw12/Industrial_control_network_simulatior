package com.industrial.sim.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

public class NodeDTO {
    
    @Data
    public static class SaveNodeReq {
        private String nodeId;
        private String nodeType;
        private Integer displayX;
        private Integer displayY;
        private Map<String, Object> params;
    }
    
    @Data
    public static class UpdateNodeReq {
        private Integer displayX;
        private Integer displayY;
        private Map<String, Object> params;
    }
    
    @Data
    public static class NodeResp {
        private String nodeId;
        private String nodeType;
        private Integer displayX;
        private Integer displayY;
        private Map<String, Object> params;
    }
    
    @Data
    public static class NodeListResp {
        private List<NodeResp> list;
    }
    
    @Data
    public static class BatchSaveReq {
        private List<SaveNodeReq> nodes;
    }
    
    @Data
    public static class ValidateReq {
        private List<String> nodeIds;
    }
    
    @Data
    public static class PortCheckReq {
        private List<Map<String, Object>> nodes;
    }
    
    @Data
    public static class PortCheckResp {
        private Boolean hasConflict;
        private List<String> conflicts;
    }
}
