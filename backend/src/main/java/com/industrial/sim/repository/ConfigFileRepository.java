package com.industrial.sim.repository;

import com.industrial.sim.entity.ConfigFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfigFileRepository extends JpaRepository<ConfigFile, Long> {
    List<ConfigFile> findByProjectCodeAndFileType(String projectCode, String fileType);
    Optional<ConfigFile> findByProjectCodeAndFileTypeAndVersion(String projectCode, String fileType, Integer version);
    
    @Query("SELECT MAX(c.version) FROM ConfigFile c WHERE c.projectCode = ?1 AND c.fileType = ?2")
    Integer findMaxVersionByProjectCodeAndFileType(String projectCode, String fileType);
}
