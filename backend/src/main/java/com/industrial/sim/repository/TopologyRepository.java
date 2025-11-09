package com.industrial.sim.repository;

import com.industrial.sim.entity.Topology;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopologyRepository extends JpaRepository<Topology, Long> {
    List<Topology> findByProjectCode(String projectCode);
    Optional<Topology> findByProjectCodeAndTopologyVersion(String projectCode, Integer version);
    
    @Query("SELECT MAX(t.topologyVersion) FROM Topology t WHERE t.projectCode = ?1")
    Integer findMaxVersionByProjectCode(String projectCode);
    
    @Query("SELECT t FROM Topology t WHERE t.projectCode = ?1 ORDER BY t.topologyVersion DESC LIMIT 1")
    Optional<Topology> findLatestByProjectCode(String projectCode);
}
