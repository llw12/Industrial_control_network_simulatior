package com.industrial.sim.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ConfigDTO {
    
    @Data
    public static class SaveConfigReq {
        private String fileType; // MASTER / SLAVE
        private Map<String, Object> contentJson;
        private String description;
    }
    
    @Data
    public static class ValidateConfigReq {
        private String fileType;
        private Map<String, Object> contentJson;
    }
    
    @Data
    public static class ValidateConfigResp {
        private Boolean valid;
        private List<String> errors;
    }
    
    @Data
    public static class ConfigResp {
        private Long id;
        private String projectCode;
        private String fileType;
        private Integer version;
        private Map<String, Object> contentJson;
        private String description;
        private LocalDateTime createTime;
    }
    
    @Data
    public static class ConfigListResp {
        private List<ConfigResp> list;
    }
}
