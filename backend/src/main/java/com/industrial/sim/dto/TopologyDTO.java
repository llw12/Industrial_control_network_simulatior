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
