package com.industrial.sim.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "config_file",
       uniqueConstraints = @UniqueConstraint(columnNames = {"project_code", "file_type", "version"}))
public class ConfigFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "project_code", nullable = false, length = 64)
    private String projectCode;
    
    @Column(name = "file_type", nullable = false, length = 32)
    private String fileType; // MASTER / SLAVE
    
    @Column(nullable = false)
    private Integer version;
    
    @Column(name = "content_json", columnDefinition = "MEDIUMTEXT")
    private String contentJson;
    
    @Column(length = 256)
    private String description;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
