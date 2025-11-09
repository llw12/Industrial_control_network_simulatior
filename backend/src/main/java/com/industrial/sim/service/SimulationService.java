package com.industrial.sim.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.industrial.sim.dto.SimulationDTO.*;
import com.industrial.sim.dto.TopologyDTO.TopologyGraph;
import com.industrial.sim.entity.Node;
import com.industrial.sim.entity.SimulationRun;
import com.industrial.sim.entity.Topology;
import com.industrial.sim.repository.NodeRepository;
import com.industrial.sim.repository.SimulationRunRepository;
import com.industrial.sim.repository.TopologyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SimulationService {
    
    private final SimulationRunRepository simulationRunRepository;
    private final TopologyRepository topologyRepository;
    private final NodeRepository nodeRepository;
    private final GenerationService generationService;
    private final ObjectMapper objectMapper;
    
    @Value("${simulation.data-dir:/data}")
    private String dataDir;
    
    public PrecheckResp precheck(PrecheckReq req) {
        PrecheckResp resp = new PrecheckResp();
        List<ErrorDetail> errors = new java.util.ArrayList<>();
        List<String> warnings = new java.util.ArrayList<>();
        
        // Check topology exists
        Topology topology = topologyRepository.findByProjectCodeAndTopologyVersion(
                req.getProjectCode(), req.getTopologyVersion())
                .orElseThrow(() -> new RuntimeException("拓扑版本不存在"));
        
        // Get nodes
        List<Node> nodes = nodeRepository.findByProjectCode(req.getProjectCode());
        
        // Check client count
        long clientCount = nodes.stream()
                .filter(n -> "Client".equals(n.getNodeType()))
                .count();
        
        if (clientCount == 0) {
            ErrorDetail error = new ErrorDetail();
            error.setType("NO_CLIENTS");
            error.setDetail("无有效客户端");
            errors.add(error);
        }
        
        // Extract HIL indices
        List<Integer> hilIndices = new java.util.ArrayList<>();
        int clientIndex = 0;
        for (Node node : nodes) {
            if ("Client".equals(node.getNodeType())) {
                if (node.getParamsJson() != null && node.getParamsJson().contains("\"isHil\":true")) {
                    hilIndices.add(clientIndex);
                }
                clientIndex++;
            }
        }
        
        resp.setPass(errors.isEmpty());
        resp.setErrors(errors);
        resp.setWarnings(warnings);
        resp.setHilClientIndices(hilIndices);
        resp.setEstimatedRuntimeSec(60);
        
        return resp;
    }
    
    @Transactional
    public SimulationResp startSimulation(StartSimulationReq req) {
        // Precheck first
        PrecheckReq precheckReq = new PrecheckReq();
        precheckReq.setProjectCode(req.getProjectCode());
        precheckReq.setTopologyVersion(req.getTopologyVersion());
        PrecheckResp precheckResp = precheck(precheckReq);
        
        if (!precheckResp.getPass()) {
            throw new RuntimeException("预检查失败: " + precheckResp.getErrors().get(0).getDetail());
        }
        
        // Get topology
        Topology topology = topologyRepository.findByProjectCodeAndTopologyVersion(
                req.getProjectCode(), req.getTopologyVersion())
                .orElseThrow(() -> new RuntimeException("拓扑版本不存在"));
        
        // Generate run ID
        String runId = "run_" + UUID.randomUUID().toString().substring(0, 8);
        
        // Create run directory
        Path runDir = Paths.get(dataDir, "projects", req.getProjectCode(), "runs", runId);
        try {
            Files.createDirectories(runDir);
        } catch (IOException e) {
            throw new RuntimeException("创建运行目录失败", e);
        }
        
        // Generate INI and NED files
        List<Node> nodes = nodeRepository.findByProjectCode(req.getProjectCode());
        
        String iniContent;
        String nedContent;
        try {
            TopologyGraph graph = objectMapper.readValue(topology.getGraphJson(), TopologyGraph.class);
            
            String simTimeLimit = req.getIniTemplateParams() != null && req.getIniTemplateParams().getSimTimeLimit() != null ?
                    req.getIniTemplateParams().getSimTimeLimit() : "10s";
            
            iniContent = generationService.generateIni(req.getProjectCode(), graph, nodes, simTimeLimit, null);
            nedContent = generationService.generateNed(graph, nodes);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("拓扑JSON解析失败", e);
        }
        
        // Save INI and NED to run directory
        Path iniPath = runDir.resolve("omnetpp.ini");
        Path nedPath = runDir.resolve("topology.ned");
        
        try {
            Files.writeString(iniPath, iniContent);
            Files.writeString(nedPath, nedContent);
        } catch (IOException e) {
            throw new RuntimeException("保存配置文件失败", e);
        }
        
        // Create simulation run record
        SimulationRun run = new SimulationRun();
        run.setRunId(runId);
        run.setProjectCode(req.getProjectCode());
        run.setTopologyVersion(req.getTopologyVersion());
        run.setIniFilePath(iniPath.toString());
        run.setNedFilePath(nedPath.toString());
        run.setStatus(0); // STARTING
        run.setStartTime(LocalDateTime.now());
        run.setLogPath(runDir.resolve("simulation.log").toString());
        run.setSqliteVectorPath(runDir.resolve("results.vec").toString());
        run.setSqliteScalarPath(runDir.resolve("results.sca").toString());
        run.setPcapPath(runDir.resolve("results").toString());
        
        run = simulationRunRepository.save(run);
        
        // TODO: Start simulation script asynchronously
        
        return toSimulationResp(run);
    }
    
    @Transactional
    public void stopSimulation(String runId, StopSimulationReq req) {
        SimulationRun run = simulationRunRepository.findByRunId(runId)
                .orElseThrow(() -> new RuntimeException("仿真实例不存在"));
        
        if (run.getStatus() == 2 || run.getStatus() == 3 || run.getStatus() == 4) {
            throw new RuntimeException("仿真已结束");
        }
        
        // TODO: Execute stop script
        
        run.setStatus(4); // STOPPED
        run.setEndTime(LocalDateTime.now());
        simulationRunRepository.save(run);
    }
    
    public SimulationListResp listSimulations(String projectCode, Integer status, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<SimulationRun> runPage;
        
        if (projectCode != null && status != null) {
            runPage = simulationRunRepository.findByProjectCodeAndStatus(projectCode, status, pageable);
        } else if (projectCode != null) {
            runPage = simulationRunRepository.findByProjectCode(projectCode, pageable);
        } else if (status != null) {
            runPage = simulationRunRepository.findByStatus(status, pageable);
        } else {
            runPage = simulationRunRepository.findAll(pageable);
        }
        
        SimulationListResp resp = new SimulationListResp();
        resp.setList(runPage.getContent().stream()
                .map(this::toSimulationResp)
                .collect(Collectors.toList()));
        resp.setTotal(runPage.getTotalElements());
        return resp;
    }
    
    public SimulationResp getSimulation(String runId) {
        SimulationRun run = simulationRunRepository.findByRunId(runId)
                .orElseThrow(() -> new RuntimeException("仿真实例不存在"));
        return toSimulationResp(run);
    }
    
    private SimulationResp toSimulationResp(SimulationRun run) {
        SimulationResp resp = new SimulationResp();
        resp.setRunId(run.getRunId());
        resp.setProjectCode(run.getProjectCode());
        resp.setTopologyVersion(run.getTopologyVersion());
        resp.setStatus(run.getStatus());
        resp.setStartTime(run.getStartTime());
        resp.setEndTime(run.getEndTime());
        resp.setIniFilePath(run.getIniFilePath());
        resp.setNedFilePath(run.getNedFilePath());
        resp.setLogPath(run.getLogPath());
        return resp;
    }
}
