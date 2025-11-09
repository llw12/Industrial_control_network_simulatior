package com.industrial.sim.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "node", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"project_code", "node_id"}))
public class Node {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "project_code", nullable = false, length = 64)
    private String projectCode;
    
    @Column(name = "node_id", nullable = false, length = 64)
    private String nodeId;
    
    @Column(name = "node_type", nullable = false, length = 64)
    private String nodeType;
    
    @Column(name = "display_x")
    private Integer displayX;
    
    @Column(name = "display_y")
    private Integer displayY;
    
    @Column(name = "params_json", columnDefinition = "MEDIUMTEXT")
    private String paramsJson;
    
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
