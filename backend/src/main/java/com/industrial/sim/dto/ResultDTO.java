package com.industrial.sim.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

public class ResultDTO {
    
    @Data
    public static class SimulationResultResp {
        private String metricName;
        private String metricType;
        private String sourceModule;
        private Double value;
        private List<Double> vectorDataJson;
        private String tags;
    }
    
    @Data
    public static class ResultListResp {
        private List<SimulationResultResp> list;
        private Long total;
    }
    
    @Data
    public static class AggregateResp {
        private Map<String, MetricAggregate> aggregates;
    }
    
    @Data
    public static class MetricAggregate {
        private Double min;
        private Double max;
        private Double avg;
        private Double sum;
        private Long count;
        private Map<String, Double> percentiles; // p95, p99, etc.
    }
    
    @Data
    public static class VectorSliceResp {
        private Integer total;
        private List<Double> slice;
        private Integer offset;
        private Integer limit;
    }
}
