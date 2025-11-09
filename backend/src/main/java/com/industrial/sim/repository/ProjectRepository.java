package com.industrial.sim.repository;

import com.industrial.sim.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByProjectCode(String projectCode);
    boolean existsByProjectCode(String projectCode);
    boolean existsByProjectName(String projectName);
    Page<Project> findByProjectNameContaining(String keywords, Pageable pageable);
}
