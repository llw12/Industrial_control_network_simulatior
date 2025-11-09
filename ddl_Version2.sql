-- 数据库建表 SQL (MySQL 示例)

CREATE TABLE project (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_code VARCHAR(64) NOT NULL UNIQUE,
  project_name VARCHAR(128) NOT NULL,
  description TEXT,
  sim_time_limit VARCHAR(32),
  status TINYINT DEFAULT 0,
  create_user VARCHAR(64),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE node (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_code VARCHAR(64) NOT NULL,
  node_id VARCHAR(64) NOT NULL,
  node_type VARCHAR(64) NOT NULL,
  display_x INT,
  display_y INT,
  params_json MEDIUMTEXT, -- 包含 isHil、apps、capture 等
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_project_node (project_code, node_id),
  KEY idx_node_project (project_code)
);

CREATE TABLE topology (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_code VARCHAR(64) NOT NULL,
  topology_version INT NOT NULL,
  graph_json MEDIUMTEXT,
  ned_file_path VARCHAR(256),
  master_config_json MEDIUMTEXT,
  slave_config_json MEDIUMTEXT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_project_version (project_code, topology_version),
  KEY idx_topology_project (project_code)
);

CREATE TABLE config_file (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_code VARCHAR(64) NOT NULL,
  file_type VARCHAR(32) NOT NULL, -- MASTER / SLAVE
  version INT NOT NULL,
  content_json MEDIUMTEXT,
  description VARCHAR(256),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_project_type_version (project_code, file_type, version)
);

CREATE TABLE simulation_run (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  run_id VARCHAR(64) NOT NULL UNIQUE,
  project_code VARCHAR(64) NOT NULL,
  topology_version INT NOT NULL,
  ini_file_path VARCHAR(256),
  ned_file_path VARCHAR(256),
  status TINYINT DEFAULT 0, -- 0 STARTING 1 RUNNING 2 FINISHED 3 FAILED 4 STOPPED
  start_time DATETIME,
  end_time DATETIME,
  log_path VARCHAR(256),
  sqlite_vector_path VARCHAR(256),
  sqlite_scalar_path VARCHAR(256),
  pcap_path VARCHAR(256),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_sim_run_project (project_code)
);

CREATE TABLE simulation_result (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  run_id VARCHAR(64) NOT NULL,
  metric_name VARCHAR(128) NOT NULL,
  metric_type VARCHAR(32) NOT NULL, -- scalar / vector / custom
  source_module VARCHAR(128),
  value DOUBLE,
  vector_data_json MEDIUMTEXT,
  tags VARCHAR(256),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_run_metric (run_id, metric_name)
);