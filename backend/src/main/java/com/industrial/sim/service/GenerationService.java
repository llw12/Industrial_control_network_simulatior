package com.industrial.sim.service;

import com.industrial.sim.dto.TopologyDTO.TopologyGraph;
import com.industrial.sim.dto.TopologyDTO.GraphNode;
import com.industrial.sim.dto.TopologyDTO.GraphEdge;
import com.industrial.sim.entity.Node;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenerationService {
    
    private static final Map<String, List<String>> APP_FIELD_MAP = Map.ofEntries(
        Map.entry("OperatorStationApp", List.of("localPort", "connectAddress", "connectPort", "startTime", "interval", "reconnectInterval")),
        Map.entry("OperatorStationApp2", List.of("localPort", "connectAddress", "connectPort", "modbusRequest", "sendTime", "seed", "reconnectInterval")),
        Map.entry("ModbusMasterApp", List.of("localPort", "connectport", "numConnect", "configFile", "readInterval")),
        Map.entry("ModbusTcpServerApp", List.of("localPort")),
        Map.entry("TransitApp", List.of("localPort")),
        Map.entry("ModbusSlaveApp", List.of("localPort", "slavesConfigPath")),
        Map.entry("ModbusSlaveHILApp", List.of("localPort", "remoteAddress", "remotePort", "slavesConfigPath"))
    );
    
    private static final Map<String, String> TYPE_MAP = Map.of(
        "OperatorStation", "OperatorStation",
        "TsnSwitch", "TsnSwitch",
        "Server", "TsnDevice",
        "Client", "TsnDevice"
    );
    
    public String generateNed(TopologyGraph graph, List<Node> nodes) {
        String networkName = normalizeNetworkName(graph.getMeta() != null && graph.getMeta().getNetworkName() != null ? 
                graph.getMeta().getNetworkName() : "ModbusTest1");
        
        long numClients = graph.getNodes().stream()
                .filter(n -> "Client".equals(n.getType()))
                .count();
        
        StringBuilder sb = new StringBuilder();
        sb.append("network ").append(networkName).append(" extends TsnNetworkBase\n");
        sb.append("{\n");
        sb.append("    parameters:\n");
        sb.append("        int numClients = ").append(numClients).append(";\n\n");
        sb.append("    submodules:\n");
        
        // Add non-client nodes
        for (GraphNode n : graph.getNodes()) {
            if (!"Client".equals(n.getType())) {
                String omnetType = TYPE_MAP.getOrDefault(n.getType(), n.getType());
                sb.append("        ").append(n.getId()).append(": ")
                  .append(omnetType)
                  .append(" { @display(\"p=").append(n.getX()).append(",").append(n.getY()).append("\"); }\n");
            }
        }
        
        // Add client array
        sb.append("        client[numClients]: TsnDevice { @display(\"p=652,329,r,80\"); }\n\n");
        
        sb.append("    connections allowunconnected:\n");
        
        // Add non-client connections
        Set<String> addedConnections = new HashSet<>();
        for (GraphEdge e : graph.getEdges()) {
            String src = e.getSource();
            String dst = e.getTarget();
            String key = src.compareTo(dst) < 0 ? src + "-" + dst : dst + "-" + src;
            
            if (addedConnections.add(key)) {
                sb.append("        ").append(src).append(".ethg++ <--> EthernetLink <--> ")
                  .append(dst).append(".ethg++;\n");
            }
        }
        
        // Add client connections to switches
        List<String> switches = graph.getNodes().stream()
                .filter(n -> "TsnSwitch".equals(n.getType()))
                .map(GraphNode::getId)
                .collect(Collectors.toList());
        
        if (!switches.isEmpty() && numClients > 0) {
            sb.append("\n        for i=0..numClients-1 {\n");
            for (String sw : switches) {
                sb.append("            client[i].ethg++ <--> EthernetLink <--> ")
                  .append(sw).append(".ethg++;\n");
            }
            sb.append("        }\n");
        }
        
        sb.append("}\n");
        return sb.toString();
    }
    
    public String generateIni(String projectCode, TopologyGraph graph, List<Node> nodes, 
                              String simTimeLimit, Map<String, Object> iniParams) {
        String networkName = normalizeNetworkName(graph.getMeta() != null && graph.getMeta().getNetworkName() != null ? 
                graph.getMeta().getNetworkName() : "ModbusTest1");
        
        StringBuilder sb = new StringBuilder();
        sb.append("[General]\n");
        sb.append("network = ").append(networkName).append("\n");
        sb.append("sim-time-limit = ").append(simTimeLimit != null ? simTimeLimit : "10s").append("\n\n");
        
        // Output managers
        sb.append("outputvectormanager-class = \"omnetpp::envir::SqliteOutputVectorManager\"\n");
        sb.append("outputscalarmanager-class = \"omnetpp::envir::SqliteOutputScalarManager\"\n\n");
        
        // Network parameters
        sb.append("**.crcMode = \"computed\"\n");
        sb.append("**.fcsMode = \"computed\"\n\n");
        
        // Ethernet settings
        sb.append("*.*.eth[*].bitrate = 100Mbps\n");
        sb.append("*.*.ethg$o[*].channel.length = 10m\n");
        sb.append("*.*.ethg$o[*].channel.ber = 0\n");
        sb.append("*.*.ethg$o[*].channel.per = 0\n\n");
        
        // TCP settings
        sb.append("**.tcp.typename = \"Tcp\"\n");
        sb.append("**.tcp.windowScalingSupport = true\n");
        sb.append("**.tcp.windowScalingFactor = 3\n");
        sb.append("**.tcp.timestampSupport = true\n");
        sb.append("**.tcp.tcpAlgorithmClass = \"TcpReno\"\n\n");
        
        // Non-client nodes configuration
        for (Node node : nodes) {
            if (!"Client".equals(node.getNodeType())) {
                sb.append(generateNodeConfig(node));
            }
        }
        
        // Client default configuration
        sb.append("# Client configuration\n");
        sb.append("*.client[*].numApps = 1\n");
        sb.append("*.client[*].app[0].typename = \"ModbusSlaveApp\"\n");
        sb.append("*.client[*].app[0].localPort = 502\n");
        sb.append("*.client[*].app[0].slavesConfigPath = \"SlaveConfig.json\"\n\n");
        
        // HIL configuration
        List<Integer> hilIndices = extractHilIndices(nodes);
        if (!hilIndices.isEmpty()) {
            sb.append("[HardInLoop]\n");
            sb.append("scheduler-class = \"inet::RealTimeScheduler\"\n\n");
            
            for (Integer idx : hilIndices) {
                sb.append("*.client[").append(idx).append("].app[0].typename = \"ModbusSlaveHILApp\"\n");
                // Get HIL config from node
                Node hilNode = findClientNodeByIndex(nodes, idx);
                if (hilNode != null) {
                    // Extract remoteAddress and remotePort from params
                    sb.append("*.client[").append(idx).append("].app[0].remoteAddress = \"192.168.1.100\"\n");
                    sb.append("*.client[").append(idx).append("].app[0].remotePort = 502\n");
                }
                sb.append("*.client[").append(idx).append("].app[0].slavesConfigPath = \"SlaveConfig.json\"\n\n");
            }
        }
        
        return sb.toString();
    }
    
    private String generateNodeConfig(Node node) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Node: ").append(node.getNodeId()).append("\n");
        sb.append("*.").append(node.getNodeId()).append(".numApps = 1\n");
        sb.append("*.").append(node.getNodeId()).append(".app[0].typename = \"ModbusTcpServerApp\"\n");
        sb.append("*.").append(node.getNodeId()).append(".app[0].localPort = 502\n\n");
        return sb.toString();
    }
    
    private List<Integer> extractHilIndices(List<Node> nodes) {
        List<Integer> indices = new ArrayList<>();
        int clientIndex = 0;
        for (Node node : nodes) {
            if ("Client".equals(node.getNodeType())) {
                // Check if HIL from params
                if (node.getParamsJson() != null && node.getParamsJson().contains("\"isHil\":true")) {
                    indices.add(clientIndex);
                }
                clientIndex++;
            }
        }
        return indices;
    }
    
    private Node findClientNodeByIndex(List<Node> nodes, int index) {
        int currentIndex = 0;
        for (Node node : nodes) {
            if ("Client".equals(node.getNodeType())) {
                if (currentIndex == index) {
                    return node;
                }
                currentIndex++;
            }
        }
        return null;
    }
    
    private String normalizeNetworkName(String name) {
        return name.replaceAll("[^a-zA-Z0-9_]", "_");
    }
}
