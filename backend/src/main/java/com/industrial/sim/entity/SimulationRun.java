package com.industrial.sim.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "simulation_run")
public class SimulationRun {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "run_id", unique = true, nullable = false, length = 64)
    private String runId;
    
    @Column(name = "project_code", nullable = false, length = 64)
    private String projectCode;
    
    @Column(name = "topology_version", nullable = false)
    private Integer topologyVersion;
    
    @Column(name = "ini_file_path", length = 256)
    private String iniFilePath;
    
    @Column(name = "ned_file_path", length = 256)
    private String nedFilePath;
    
    @Column(columnDefinition = "TINYINT DEFAULT 0")
    private Integer status; // 0 STARTING 1 RUNNING 2 FINISHED 3 FAILED 4 STOPPED
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "log_path", length = 256)
    private String logPath;
    
    @Column(name = "sqlite_vector_path", length = 256)
    private String sqliteVectorPath;
    
    @Column(name = "sqlite_scalar_path", length = 256)
    private String sqliteScalarPath;
    
    @Column(name = "pcap_path", length = 256)
    private String pcapPath;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
