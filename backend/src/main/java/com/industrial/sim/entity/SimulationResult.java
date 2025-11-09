package com.industrial.sim.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "simulation_result")
public class SimulationResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "run_id", nullable = false, length = 64)
    private String runId;
    
    @Column(name = "metric_name", nullable = false, length = 128)
    private String metricName;
    
    @Column(name = "metric_type", nullable = false, length = 32)
    private String metricType; // scalar / vector / custom
    
    @Column(name = "source_module", length = 128)
    private String sourceModule;
    
    @Column
    private Double value;
    
    @Column(name = "vector_data_json", columnDefinition = "MEDIUMTEXT")
    private String vectorDataJson;
    
    @Column(length = 256)
    private String tags;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
