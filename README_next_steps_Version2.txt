实施步骤建议：
1) 初始化数据库（执行 ddl.sql）。
2) 导入 openapi.yaml，用 openapi-generator 生成后端接口桩代码与前端 API 类型。
3) 实现核心服务：
   - GenerationService：NED/INI 生成 + 校验（端口冲突、HIL 必填、配置 JSON 校验）。
   - SimulationOrchestrator：状态机 + 任务队列 + 脚本执行 + WebSocket 日志推送。
   - ResultParser：SQLite 解析（scalar/vector），写入 simulation_result。
4) 前端集成：
   - G6 拓扑编辑 + 节点抽屉（isHil、apps、capture）。
   - 预览 NED/INI；启动前调用 /simulations/precheck。
   - 日志 WebSocket；结果页（列表、聚合、vector 分块、导出）。
5) 抓包任意节点：在节点 params.capture 中配置，INI 生成时写入 pcapRecorder 段。
6) 完成后补充单元/集成测试、错误码对齐、性能调优（向量分块、日志缓冲）。