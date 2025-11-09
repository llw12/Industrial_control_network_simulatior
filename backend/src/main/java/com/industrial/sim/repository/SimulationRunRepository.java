package com.industrial.sim.repository;

import com.industrial.sim.entity.SimulationRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SimulationRunRepository extends JpaRepository<SimulationRun, Long> {
    Optional<SimulationRun> findByRunId(String runId);
    Page<SimulationRun> findByProjectCode(String projectCode, Pageable pageable);
    Page<SimulationRun> findByStatus(Integer status, Pageable pageable);
    Page<SimulationRun> findByProjectCodeAndStatus(String projectCode, Integer status, Pageable pageable);
}
