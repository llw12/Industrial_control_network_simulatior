/**
 * 伪代码：NED / INI 生成服务（仅示意）
 * 说明：用于梳理生成流程与校验点；不直接可编译。
 */
class GenerationService {

    static final Map<String, List<String>> APP_FIELD_MAP = Map.of(
        "OperatorStationApp", List.of("localPort","connectAddress","connectPort","startTime","interval","reconnectInterval"),
        "OperatorStationApp2", List.of("localPort","connectAddress","connectPort","modbusRequest","sendTime","seed","reconnectInterval"),
        "ModbusMasterApp", List.of("localPort","connectport","numConnect","configFile","readInterval"),
        "ModbusTcpServerApp", List.of("localPort"),
        "TransitApp", List.of("localPort"),
        "ModbusSlaveApp", List.of("localPort","slavesConfigPath"),
        "ModbusSlaveHILApp", List.of("localPort","remoteAddress","remotePort","slavesConfigPath")
    );

    String generateNed(TopologyGraph graph) {
        String networkName = normalize(graph.meta.networkName, "ModbusTest1");
        int numClients = (int) graph.nodes.stream().filter(n -> eq(n.type, "Client")).count();

        StringBuilder sb = new StringBuilder();
        sb.append("network ").append(networkName).append(" extends TsnNetworkBase\n{\n");
        sb.append("    parameters:\n        int numClients;\n");
        sb.append("    submodules:\n");
        for (Node n : graph.nodes) {
            if (!eq(n.type, "Client")) {
                sb.append("        ").append(n.id).append(": ")
                  .append(resolveType(n.type)).append(" { @display(\"p=")
                  .append(n.x).append(",").append(n.y).append("\"); }\n");
            }
        }
        sb.append("        client[numClients]: TsnDevice { @display(\"p=652,329,r,80\"); }\n");

        sb.append("    connections allowunconnected:\n");
        Set<String> uniq = new HashSet<>();
        for (Edge e : graph.edges) {
            String key = sort(e.source, e.target);
            if (uniq.add(key)) {
                sb.append("        ").append(e.source).append(".ethg++ <--> EthernetLink <--> ")
                  .append(e.target).append(".ethg++;\n");
            }
        }
        List<String> switches = graph.nodes.stream().filter(n -> eq(n.type, "TsnSwitch"))
            .map(n -> n.id).toList();
        if (!switches.isEmpty()) {
            sb.append("\n        for i=0..numClients-1 {\n");
            for (String sw : switches) {
                sb.append("            client[i].ethg++ <--> EthernetLink <--> ").append(sw).append(".ethg++;\n");
            }
            sb.append("        }\n");
        }
        sb.append("}\n");
        return sb.toString().replace("numClients", String.valueOf(numClients));
    }

    String generateIni(Project project, TopologyGraph graph, List<NodeConfig> nodes, IniTemplateParams tp) {
        String networkName = normalize(graph.meta.networkName, "ModbusTest1");

        StringBuilder g = new StringBuilder();
        g.append("[General]\n");
        g.append("network = ").append(networkName).append("\n");
        g.append("sim-time-limit = ").append(defaultStr(tp.simTimeLimit, project.simTimeLimit, "10s")).append("\n");
        g.append("outputvectormanager-class=\"omnetpp::envir::SqliteOutputVectorManager\"\n");
        g.append("outputscalarmanager-class=\"omnetpp::envir::SqliteOutputScalarManager\"\n");
        g.append("**.crcMode = \"computed\"\n**.fcsMode = \"computed\"\n");
        g.append("*.*.eth[*].bitrate = 100Mbps\n");
        g.append("*.*.ethg$o[*].channel.length = 10m\n");
        g.append("*.*.ethg$o[*].channel.ber = 0\n");
        g.append("*.*.ethg$o[*].channel.per = 0\n");
        // TCP 固定
        g.append("**.tcp.typename = \"Tcp\"\n");
        g.append("**.tcp.windowScalingSupport = true\n");
        g.append("**.tcp.windowScalingFactor = 3\n");
        g.append("**.tcp.timestampSupport = true\n");
        g.append("**.tcp.tcpAlgorithmClass = \"TcpReno\"\n");

        // 非客户端节点应用
        for (NodeConfig nc : nodes) {
            if (!nc.isClient()) {
                int numApps = nc.apps.size();
                g.append("*.").append(nc.nodeId).append(".numApps = ").append(numApps).append("\n");
                for (int i = 0; i < numApps; i++) {
                    App a = nc.apps.get(i);
                    appendApp(g, nc.nodeId, i, a);
                }
                // 通用抓包
                if (nc.capture != null && nc.capture.enable) {
                    g.append("*.").append(nc.nodeId).append(".numPcapRecorders = 1\n");
                    if (notBlank(nc.capture.moduleNamePatterns)) {
                        g.append("*.").append(nc.nodeId).append(".pcapRecorder[0].moduleNamePatterns = \"")
                          .append(nc.capture.moduleNamePatterns).append("\"\n");
                    }
                    g.append("*.").append(nc.nodeId).append(".pcapRecorder[0].pcapFile = \"results/")
                      .append(nc.nodeId).append(".pcap\"\n");
                }
            }
        }

        // 客户端统一设置 + 抓包（若设置）
        g.append("*.client[*].numApps = 1\n");
        g.append("*.client[*].app[0].typename = \"ModbusSlaveApp\"\n");
        g.append("*.client[*].app[0].localPort = 502\n");
        g.append("*.client[*].app[0].slavesConfigPath = \"SlaveConfig.json\"\n");

        List<Integer> hil = hilIndices(nodes);
        StringBuilder hilSec = new StringBuilder();
        if (!hil.isEmpty()) {
            hilSec.append("[HardInLoop]\n");
            hilSec.append("scheduler-class = \"inet::RealTimeScheduler\"\n");
            for (Integer i : hil) {
                HilInfo h = hilInfo(i, nodes);
                hilSec.append("*.client[").append(i).append("].app[0].typename = \"ModbusSlaveHILApp\"\n");
                hilSec.append("*.client[").append(i).append("].app[0].remoteAddress = \"").append(h.addr).append("\"\n");
                hilSec.append("*.client[").append(i).append("].app[0].remotePort = ").append(h.port).append("\n");
                hilSec.append("*.client[").append(i).append("].app[0].slavesConfigPath = \"SlaveConfig.json\"\n");
            }
        }
        return g.toString() + (hilSec.length() > 0 ? "\n" + hilSec : "");
    }

    void appendApp(StringBuilder sb, String nodeId, int idx, App a) {
        sb.append("*.").append(nodeId).append(".app[").append(idx).append("].typename = \"")
          .append(a.typename).append("\"\n");
        List<String> fields = APP_FIELD_MAP.getOrDefault(a.typename, List.of());
        for (String f : fields) {
            Object v = a.get(f);
            if (v != null) {
                sb.append("*.").append(nodeId).append(".app[").append(idx).append("].").append(f)
                  .append(" = ").append(format(v)).append("\n");
            }
        }
    }

    // 辅助方法（normalize, defaultStr, hilIndices, hilInfo, format, eq, etc.）省略
}