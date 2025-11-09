package com.industrial.sim.repository;

import com.industrial.sim.entity.SimulationResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SimulationResultRepository extends JpaRepository<SimulationResult, Long> {
    List<SimulationResult> findByRunId(String runId);
    Page<SimulationResult> findByRunId(String runId, Pageable pageable);
    Page<SimulationResult> findByRunIdAndMetricType(String runId, String metricType, Pageable pageable);
    Page<SimulationResult> findByRunIdAndMetricNameContaining(String runId, String keyword, Pageable pageable);
    
    @Query("SELECT sr FROM SimulationResult sr WHERE sr.runId = ?1 AND sr.metricName IN ?2")
    List<SimulationResult> findByRunIdAndMetricNames(String runId, List<String> metricNames);
}
