package com.industrial.sim.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.industrial.sim.dto.NodeDTO.*;
import com.industrial.sim.entity.Node;
import com.industrial.sim.repository.NodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NodeService {
    
    private final NodeRepository nodeRepository;
    private final ObjectMapper objectMapper;
    
    @Transactional
    public NodeResp saveNode(String projectCode, SaveNodeReq req) {
        Optional<Node> existingNode = nodeRepository.findByProjectCodeAndNodeId(projectCode, req.getNodeId());
        
        Node node;
        if (existingNode.isPresent()) {
            node = existingNode.get();
        } else {
            node = new Node();
            node.setProjectCode(projectCode);
            node.setNodeId(req.getNodeId());
        }
        
        node.setNodeType(req.getNodeType());
        node.setDisplayX(req.getDisplayX());
        node.setDisplayY(req.getDisplayY());
        
        try {
            node.setParamsJson(objectMapper.writeValueAsString(req.getParams()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("参数JSON序列化失败", e);
        }
        
        node = nodeRepository.save(node);
        return toNodeResp(node);
    }
    
    @Transactional
    public void batchSaveNodes(String projectCode, BatchSaveReq req) {
        for (SaveNodeReq nodeReq : req.getNodes()) {
            saveNode(projectCode, nodeReq);
        }
    }
    
    public NodeListResp listNodes(String projectCode) {
        List<Node> nodes = nodeRepository.findByProjectCode(projectCode);
        NodeListResp resp = new NodeListResp();
        resp.setList(nodes.stream().map(this::toNodeResp).collect(Collectors.toList()));
        return resp;
    }
    
    public NodeResp getNode(String projectCode, String nodeId) {
        Node node = nodeRepository.findByProjectCodeAndNodeId(projectCode, nodeId)
                .orElseThrow(() -> new RuntimeException("节点不存在"));
        return toNodeResp(node);
    }
    
    @Transactional
    public NodeResp updateNode(String projectCode, String nodeId, UpdateNodeReq req) {
        Node node = nodeRepository.findByProjectCodeAndNodeId(projectCode, nodeId)
                .orElseThrow(() -> new RuntimeException("节点不存在"));
        
        if (req.getDisplayX() != null) {
            node.setDisplayX(req.getDisplayX());
        }
        if (req.getDisplayY() != null) {
            node.setDisplayY(req.getDisplayY());
        }
        if (req.getParams() != null) {
            try {
                node.setParamsJson(objectMapper.writeValueAsString(req.getParams()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("参数JSON序列化失败", e);
            }
        }
        
        node = nodeRepository.save(node);
        return toNodeResp(node);
    }
    
    @Transactional
    public void deleteNode(String projectCode, String nodeId) {
        nodeRepository.deleteByProjectCodeAndNodeId(projectCode, nodeId);
    }
    
    public PortCheckResp checkPortConflicts(String projectCode, PortCheckReq req) {
        Map<String, Set<Integer>> nodePortMap = new HashMap<>();
        List<String> conflicts = new ArrayList<>();
        
        for (Map<String, Object> nodeData : req.getNodes()) {
            String nodeId = (String) nodeData.get("nodeId");
            Map<String, Object> params = (Map<String, Object>) nodeData.get("params");
            
            if (params != null && params.containsKey("apps")) {
                List<Map<String, Object>> apps = (List<Map<String, Object>>) params.get("apps");
                Set<Integer> ports = nodePortMap.computeIfAbsent(nodeId, k -> new HashSet<>());
                
                for (Map<String, Object> app : apps) {
                    Object localPortObj = app.get("localPort");
                    if (localPortObj != null) {
                        Integer localPort = localPortObj instanceof Integer ? 
                                (Integer) localPortObj : Integer.parseInt(localPortObj.toString());
                        
                        if (ports.contains(localPort)) {
                            conflicts.add(String.format("节点 %s 端口 %d 冲突", nodeId, localPort));
                        } else {
                            ports.add(localPort);
                        }
                    }
                }
            }
        }
        
        PortCheckResp resp = new PortCheckResp();
        resp.setHasConflict(!conflicts.isEmpty());
        resp.setConflicts(conflicts);
        return resp;
    }
    
    private NodeResp toNodeResp(Node node) {
        NodeResp resp = new NodeResp();
        resp.setNodeId(node.getNodeId());
        resp.setNodeType(node.getNodeType());
        resp.setDisplayX(node.getDisplayX());
        resp.setDisplayY(node.getDisplayY());
        
        try {
            if (node.getParamsJson() != null) {
                resp.setParams(objectMapper.readValue(node.getParamsJson(), Map.class));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("参数JSON反序列化失败", e);
        }
        
        return resp;
    }
}
