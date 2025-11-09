package com.industrial.sim.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

public class ProjectDTO {
    
    @Data
    public static class CreateProjectReq {
        private String projectName;
        private String simTimeLimit;
        private String description;
        private String createUser;
    }
    
    @Data
    public static class UpdateProjectReq {
        private String projectName;
        private String simTimeLimit;
        private String description;
    }
    
    @Data
    public static class ProjectResp {
        private String projectCode;
        private String projectName;
        private String simTimeLimit;
        private Integer status;
        private String description;
        private String createUser;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
    }
    
    @Data
    public static class ProjectListResp {
        private List<ProjectResp> list;
        private Long total;
    }
}
