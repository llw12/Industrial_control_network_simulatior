package com.industrial.sim.repository;

import com.industrial.sim.entity.Node;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NodeRepository extends JpaRepository<Node, Long> {
    List<Node> findByProjectCode(String projectCode);
    Optional<Node> findByProjectCodeAndNodeId(String projectCode, String nodeId);
    void deleteByProjectCodeAndNodeId(String projectCode, String nodeId);
    void deleteByProjectCode(String projectCode);
}
