package com.industrial.sim.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "project")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "project_code", unique = true, nullable = false, length = 64)
    private String projectCode;
    
    @Column(name = "project_name", nullable = false, length = 128)
    private String projectName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "sim_time_limit", length = 32)
    private String simTimeLimit;
    
    @Column(columnDefinition = "TINYINT DEFAULT 0")
    private Integer status;
    
    @Column(name = "create_user", length = 64)
    private String createUser;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
