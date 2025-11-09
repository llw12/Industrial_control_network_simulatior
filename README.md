# 工业控制网络仿真平台 (Industrial Control Network Simulation Platform)

基于 OMNeT++ 的工业控制网络仿真平台，提供完整的前后端解决方案。

## 项目结构

```
.
├── backend/                # Spring Boot 后端
│   ├── src/
│   │   └── main/
│   │       ├── java/com/industrial/sim/
│   │       │   ├── controller/      # REST API 控制器
│   │       │   ├── service/         # 业务逻辑服务
│   │       │   ├── repository/      # 数据访问层
│   │       │   ├── entity/          # JPA 实体
│   │       │   ├── dto/             # 数据传输对象
│   │       │   └── config/          # 配置类
│   │       └── resources/
│   │           └── application.yml  # 应用配置
│   ├── scripts/                     # 仿真脚本
│   └── pom.xml                      # Maven 配置
│
├── frontend/               # Vue3 前端
│   ├── src/
│   │   ├── views/          # 页面组件
│   │   ├── components/     # 可复用组件
│   │   ├── api/            # API 调用
│   │   ├── stores/         # Pinia 状态管理
│   │   ├── router/         # Vue Router 路由
│   │   └── types/          # TypeScript 类型定义
│   └── package.json        # 依赖配置
│
└── specification files/    # 接口规范文档
    ├── 工业控制网络仿真平台接口规范V1.1_Version2.md
    ├── openapi_Version2.yaml
    ├── ddl_Version2.sql
    └── types_Version2.ts
```

## 技术栈

### 后端
- **框架**: Spring Boot 3.1.5
- **数据库**: MySQL (业务数据) + SQLite (仿真结果)
- **ORM**: Spring Data JPA
- **构建工具**: Maven

### 前端
- **框架**: Vue 3 + TypeScript
- **状态管理**: Pinia
- **UI 组件**: Ant Design Vue
- **图形绘制**: G6 (拓扑编辑器)
- **构建工具**: Vite

## 快速开始

### 后端启动

1. 配置数据库
   ```bash
   # 创建数据库
   mysql -u root -p
   CREATE DATABASE industrial_sim;
   
   # 导入表结构
   mysql -u root -p industrial_sim < ddl_Version2.sql
   ```

2. 修改配置文件
   ```bash
   cd backend/src/main/resources
   # 编辑 application.yml，修改数据库连接信息
   ```

3. 编译运行
   ```bash
   cd backend
   mvn clean install
   mvn spring-boot:run
   ```

后端服务将在 `http://localhost:8080/api/v1` 启动

### 前端启动

1. 安装依赖
   ```bash
   cd frontend
   npm install
   ```

2. 启动开发服务器
   ```bash
   npm run dev
   ```

前端应用将在 `http://localhost:5173` 启动

## 核心功能

### 已实现功能

#### 后端
- ✅ 项目管理 (CRUD)
- ✅ 节点配置 (保存、批量操作、端口冲突检测)
- ✅ 拓扑版本管理
- ✅ NED/INI 文件生成
- ✅ Master/Slave 配置文件版本化管理
- ✅ 仿真预检查
- ✅ 仿真启动/停止 API
- ✅ 仿真脚本 (run_sim.sh, stop_sim.sh)

#### 前端
- ✅ 项目列表与创建
- ✅ 项目详情查看
- ✅ 拓扑编辑器页面 (G6 集成待完善)
- ✅ 仿真列表与详情
- ✅ NED/INI 预览
- ✅ API 封装与类型定义

### 待实现功能

- ⏳ WebSocket 实时日志推送
- ⏳ SQLite 仿真结果解析
- ⏳ 结果可视化 (图表、统计)
- ⏳ G6 拓扑编辑器完整实现
- ⏳ HIL 客户端配置界面
- ⏳ 任务队列与状态机
- ⏳ 抓包配置与管理

## API 文档

完整的 API 文档请参考 `openapi_Version2.yaml`

主要端点:
- `/api/v1/projects` - 项目管理
- `/api/v1/projects/{projectCode}/nodes` - 节点管理
- `/api/v1/projects/{projectCode}/topology` - 拓扑管理
- `/api/v1/projects/{projectCode}/configs` - 配置文件管理
- `/api/v1/simulations` - 仿真管理
- `/api/v1/simulation-results` - 结果查询

## 开发说明

### 添加新的 API 端点

1. 在 `backend/src/main/java/com/industrial/sim/dto/` 定义请求/响应 DTO
2. 在对应的 Service 类中实现业务逻辑
3. 在 Controller 中添加端点映射
4. 在前端 `src/api/` 添加对应的 API 调用方法
5. 更新 TypeScript 类型定义

### 数据库迁移

修改实体类后，JPA 会自动更新数据库表结构 (配置了 `ddl-auto: update`)。
生产环境建议使用专门的迁移工具如 Flyway 或 Liquibase。

## 部署

### 后端部署

```bash
cd backend
mvn clean package
java -jar target/simulation-platform-1.0.0.jar
```

### 前端部署

```bash
cd frontend
npm run build
# 将 dist/ 目录部署到 Web 服务器 (如 Nginx)
```

## 许可证

本项目遵循 MIT 许可证。

## 作者

llw12

## 更新日志

### Version 1.0.0 (2025-11-09)
- 初始版本发布
- 完成后端核心功能实现
- 完成前端基础页面开发
- 实现 NED/INI 生成逻辑
