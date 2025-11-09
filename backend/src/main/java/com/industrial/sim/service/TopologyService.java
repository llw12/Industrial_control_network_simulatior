package com.industrial.sim.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.industrial.sim.dto.TopologyDTO.*;
import com.industrial.sim.entity.Node;
import com.industrial.sim.entity.Topology;
import com.industrial.sim.repository.NodeRepository;
import com.industrial.sim.repository.TopologyRepository;
import com.industrial.sim.util.PathUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TopologyService {
    
    private final TopologyRepository topologyRepository;
    private final NodeRepository nodeRepository;
    private final GenerationService generationService;
    private final ObjectMapper objectMapper;
    
    @Value("${simulation.data-dir:/data}")
    private String dataDir;
    
    @Transactional
    public TopologyResp saveTopology(String projectCode, SaveTopologyReq req) {
        // Get next version number
        Integer maxVersion = topologyRepository.findMaxVersionByProjectCode(projectCode);
        int nextVersion = (maxVersion == null) ? 1 : maxVersion + 1;
        
        // Get nodes for generation
        List<Node> nodes = nodeRepository.findByProjectCode(projectCode);
        
        // Generate NED file
        String nedContent = generationService.generateNed(req.getGraph(), nodes);
        String nedFilePath = saveNedFile(projectCode, nextVersion, nedContent);
        
        Topology topology = new Topology();
        topology.setProjectCode(projectCode);
        topology.setTopologyVersion(nextVersion);
        
        try {
            topology.setGraphJson(objectMapper.writeValueAsString(req.getGraph()));
            topology.setMasterConfigJson(req.getMasterConfigJson() != null ? 
                    objectMapper.writeValueAsString(req.getMasterConfigJson()) : null);
            topology.setSlaveConfigJson(req.getSlaveConfigJson() != null ? 
                    objectMapper.writeValueAsString(req.getSlaveConfigJson()) : null);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON序列化失败", e);
        }
        
        topology.setNedFilePath(nedFilePath);
        topology = topologyRepository.save(topology);
        
        return toTopologyResp(topology);
    }
    
    public TopologyResp getLatestTopology(String projectCode) {
        Topology topology = topologyRepository.findLatestByProjectCode(projectCode)
                .orElseThrow(() -> new RuntimeException("拓扑不存在"));
        return toTopologyResp(topology);
    }
    
    public TopologyResp getTopologyByVersion(String projectCode, Integer version) {
        Topology topology = topologyRepository.findByProjectCodeAndTopologyVersion(projectCode, version)
                .orElseThrow(() -> new RuntimeException("拓扑版本不存在"));
        return toTopologyResp(topology);
    }
    
    public PreviewResp previewNed(String projectCode, PreviewReq req) {
        Topology topology;
        if (req.getTopologyVersion() != null) {
            topology = topologyRepository.findByProjectCodeAndTopologyVersion(projectCode, req.getTopologyVersion())
                    .orElseThrow(() -> new RuntimeException("拓扑版本不存在"));
        } else {
            topology = topologyRepository.findLatestByProjectCode(projectCode)
                    .orElseThrow(() -> new RuntimeException("拓扑不存在"));
        }
        
        try {
            TopologyGraph graph = objectMapper.readValue(topology.getGraphJson(), TopologyGraph.class);
            List<Node> nodes = nodeRepository.findByProjectCode(projectCode);
            String nedContent = generationService.generateNed(graph, nodes);
            
            PreviewResp resp = new PreviewResp();
            resp.setContent(nedContent);
            return resp;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON反序列化失败", e);
        }
    }
    
    public PreviewResp previewIni(String projectCode, PreviewReq req) {
        Topology topology;
        if (req.getTopologyVersion() != null) {
            topology = topologyRepository.findByProjectCodeAndTopologyVersion(projectCode, req.getTopologyVersion())
                    .orElseThrow(() -> new RuntimeException("拓扑版本不存在"));
        } else {
            topology = topologyRepository.findLatestByProjectCode(projectCode)
                    .orElseThrow(() -> new RuntimeException("拓扑不存在"));
        }
        
        try {
            TopologyGraph graph = objectMapper.readValue(topology.getGraphJson(), TopologyGraph.class);
            List<Node> nodes = nodeRepository.findByProjectCode(projectCode);
            String iniContent = generationService.generateIni(projectCode, graph, nodes, "10s", null);
            
            PreviewResp resp = new PreviewResp();
            resp.setContent(iniContent);
            return resp;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON反序列化失败", e);
        }
    }
    
    private String saveNedFile(String projectCode, int version, String content) {
        try {
            Path baseDir = Paths.get(dataDir);
            Path projectDir = PathUtils.createSafePath(baseDir, "projects", projectCode, "topology");
            Files.createDirectories(projectDir);
            
            String filename = String.format("topology_v%d.ned", version);
            Path nedFile = PathUtils.createSafePath(projectDir, filename);
            Files.writeString(nedFile, content);
            
            return nedFile.toString();
        } catch (IOException e) {
            throw new RuntimeException("NED文件保存失败", e);
        } catch (SecurityException e) {
            throw new RuntimeException("路径安全检查失败", e);
        }
    }
    
    private TopologyResp toTopologyResp(Topology topology) {
        TopologyResp resp = new TopologyResp();
        resp.setProjectCode(topology.getProjectCode());
        resp.setTopologyVersion(topology.getTopologyVersion());
        resp.setNedFilePath(topology.getNedFilePath());
        
        try {
            if (topology.getGraphJson() != null) {
                resp.setGraph(objectMapper.readValue(topology.getGraphJson(), TopologyGraph.class));
            }
            if (topology.getMasterConfigJson() != null) {
                resp.setMasterConfigJson(objectMapper.readValue(topology.getMasterConfigJson(), Map.class));
            }
            if (topology.getSlaveConfigJson() != null) {
                resp.setSlaveConfigJson(objectMapper.readValue(topology.getSlaveConfigJson(), Map.class));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON反序列化失败", e);
        }
        
        return resp;
    }
}
