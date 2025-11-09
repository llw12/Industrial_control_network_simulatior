package com.industrial.sim.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "topology",
       uniqueConstraints = @UniqueConstraint(columnNames = {"project_code", "topology_version"}))
public class Topology {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "project_code", nullable = false, length = 64)
    private String projectCode;
    
    @Column(name = "topology_version", nullable = false)
    private Integer topologyVersion;
    
    @Column(name = "graph_json", columnDefinition = "MEDIUMTEXT")
    private String graphJson;
    
    @Column(name = "ned_file_path", length = 256)
    private String nedFilePath;
    
    @Column(name = "master_config_json", columnDefinition = "MEDIUMTEXT")
    private String masterConfigJson;
    
    @Column(name = "slave_config_json", columnDefinition = "MEDIUMTEXT")
    private String slaveConfigJson;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
