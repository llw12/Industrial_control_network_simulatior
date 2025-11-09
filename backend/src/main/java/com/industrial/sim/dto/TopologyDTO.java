package com.industrial.sim.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

public class TopologyDTO {
    
    @Data
    public static class TopologyGraph {
        private List<GraphNode> nodes;
        private List<GraphEdge> edges;
        private GraphMeta meta;
    }
    
    @Data
    public static class GraphNode {
        private String id;
        private String type;
        private Integer x;
        private Integer y;
        private Boolean isHil;
        private Integer localPort;
        private EthConfig eth;
        private CaptureConfig capture;
        private List<Map<String, Object>> apps;
    }
    
    @Data
    public static class EthConfig {
        private String bitrate;
        private String channelLength;
        private String delay;
        private Double ber;
        private Double per;
    }
    
    @Data
    public static class CaptureConfig {
        private Boolean enable;
        private String moduleNamePatterns;
        private String pcapFile;
    }
    
    @Data
    public static class GraphEdge {
        private String source;
        private String target;
        private String linkType;
    }
    
    @Data
    public static class GraphMeta {
        private Integer numClients;
        private List<Integer> hilClientIndices;
        private String networkName;
        private String masterConfig;
        private String slaveConfig;
    }
    
    @Data
    public static class SaveTopologyReq {
        private TopologyGraph graph;
        private Map<String, Object> masterConfigJson;
        private Map<String, Object> slaveConfigJson;
    }
    
    @Data
    public static class TopologyResp {
        private String projectCode;
        private Integer topologyVersion;
        private TopologyGraph graph;
        private String nedFilePath;
        private Map<String, Object> masterConfigJson;
        private Map<String, Object> slaveConfigJson;
    }
    
    @Data
    public static class PreviewReq {
        private Integer topologyVersion;
    }
    
    @Data
    public static class PreviewResp {
        private String content;
    }
}
