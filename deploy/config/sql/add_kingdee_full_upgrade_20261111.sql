-- ============================================================
-- 金蝶云星辰对接 — 完整数据库升级脚本
-- 幂等：所有操作都带 NOT EXISTS 判断，可重复执行
-- 日期：2026-11-11
-- ============================================================

-- ============================================================
-- 一、sys_config 金蝶配置项（密钥已用 AES 加密，加密密钥在 application.yml 的 ruoyi.fileEncryptionKey）
--   注意：首次部署时也可以只插 enabled=true，其余在前端"金蝶配置"页填写
-- ============================================================

-- 基础开关（明文，可直接 INSERT）
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '金蝶对接开关', 'kingdee.enabled', 'true', 'Y', 'admin', NOW(), '是否启用金蝶云星辰对接'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'kingdee.enabled');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '金蝶接口超时', 'kingdee.timeout', '30000', 'Y', 'admin', NOW(), 'API 超时毫秒数'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'kingdee.timeout');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '金蝶重试次数', 'kingdee.retry_times', '2', 'Y', 'admin', NOW(), 'API 失败重试次数'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'kingdee.retry_times');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT 'appSecret 刷新 cron', 'kingdee.secret_refresh_cron', '0 0 2,14 * * ?', 'Y', 'admin', NOW(), '每天凌晨 2 点、下午 2 点主动刷新'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'kingdee.secret_refresh_cron');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '同步 cron', 'kingdee.sync_cron', '0 */30 * * * ?', 'Y', 'admin', NOW(), '每 30 分钟自动同步一次'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'kingdee.sync_cron');

-- ★ 以下密钥字段：部署时请通过前端"金蝶配置"页填写，或用 SecureKeyUtils 加密后插入
-- INSERT INTO sys_config (config_key, config_value) VALUES
--   ('kingdee.app_key',          'C9A3xZEe'),
--   ('kingdee.client_id',        '367861'),
--   ('kingdee.outer_instance_id','602279552798035968'),
--   ('kingdee.base_url',         'https://api.kingdee.com/jdy'),
--   ('kingdee.router_addr',      'https://tf.jdy.com/'),
--   ('kingdee.app_secret',       'ENC:xxxxxxxxx'),   -- AES 加密
--   ('kingdee.client_secret',    'ENC:xxxxxxxxx');  -- AES 加密

-- ============================================================
-- 二、sys_user 补字段（git pull 远端实体新增的列）
-- ============================================================
ALTER TABLE sys_user
  ADD COLUMN IF NOT EXISTS leader_id              BIGINT       DEFAULT NULL COMMENT '直接领导ID',
  ADD COLUMN IF NOT EXISTS leader_name            VARCHAR(64)  DEFAULT NULL COMMENT '直接领导姓名',
  ADD COLUMN IF NOT EXISTS superior_leader_id     BIGINT       DEFAULT NULL COMMENT '上级领导ID',
  ADD COLUMN IF NOT EXISTS superior_leader_name   VARCHAR(64)  DEFAULT NULL COMMENT '上级领导姓名',
  ADD COLUMN IF NOT EXISTS charge_leader_id       BIGINT       DEFAULT NULL COMMENT '分管领导ID',
  ADD COLUMN IF NOT EXISTS charge_leader_name     VARCHAR(64)  DEFAULT NULL COMMENT '分管领导姓名';

-- ============================================================
-- 三、sys_logininfor msg 列加长（KingdeeSyncController 同步结果可能较长）
-- ============================================================
ALTER TABLE sys_logininfor MODIFY COLUMN msg VARCHAR(500) DEFAULT '' COMMENT '提示消息';

-- ============================================================
-- 四、mk_customer 补字段（同步时新增）
-- ============================================================
ALTER TABLE mk_customer
  ADD COLUMN IF NOT EXISTS credit_days          INT          DEFAULT 0   COMMENT '信用天数',
  ADD COLUMN IF NOT EXISTS credit_limit         DECIMAL(18,2) DEFAULT NULL COMMENT '信用额度',
  ADD COLUMN IF NOT EXISTS customer_type        VARCHAR(32)  DEFAULT NULL COMMENT '客户类型',
  ADD COLUMN IF NOT EXISTS parent_customer_id   VARCHAR(64)  DEFAULT NULL COMMENT '上级客户编码',
  ADD COLUMN IF NOT EXISTS contact_phone        VARCHAR(32)  DEFAULT NULL COMMENT '联系电话';

-- ============================================================
-- 五、integration 模块新表（金蝶对接配套）
-- ============================================================
CREATE TABLE IF NOT EXISTS integration_user_binding (
  id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id         BIGINT        NOT NULL COMMENT '若依 sys_user.user_id',
  account_type    VARCHAR(32)   NOT NULL COMMENT '账号类型：KINGDEE / WECOM / ...',
  external_id     VARCHAR(128)  DEFAULT NULL COMMENT '外部系统用户 ID',
  external_name   VARCHAR(128)  DEFAULT NULL COMMENT '外部系统用户名',
  ext_json        JSON          DEFAULT NULL COMMENT '扩展信息',
  bind_time       DATETIME      DEFAULT NULL COMMENT '绑定时间',
  unbind_time     DATETIME      DEFAULT NULL COMMENT '解绑时间',
  status          TINYINT       DEFAULT 1 COMMENT '1=有效 0=无效',
  create_by       VARCHAR(64)   DEFAULT '',
  create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
  update_by       VARCHAR(64)   DEFAULT '',
  update_time     DATETIME      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_type (user_id, account_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外部账号绑定表';

CREATE TABLE IF NOT EXISTS integration_sync_log (
  id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  source_system   VARCHAR(32)   NOT NULL COMMENT '来源：KINGDEE',
  target_table    VARCHAR(64)   NOT NULL COMMENT '目标表：mk_customer / mk_material / ...',
  sync_type       VARCHAR(16)   NOT NULL COMMENT 'MANUAL / SCHEDULED',
  direction       VARCHAR(8)    NOT NULL COMMENT 'PULL（拉取）',
  total_count     INT           DEFAULT 0 COMMENT '总数',
  inserted_count  INT           DEFAULT 0 COMMENT '新增数',
  updated_count   INT           DEFAULT 0 COMMENT '更新数',
  skipped_count   INT           DEFAULT 0 COMMENT '跳过数',
  error_count     INT           DEFAULT 0 COMMENT '错误数',
  duration_ms     INT           DEFAULT 0 COMMENT '耗时毫秒',
  status          VARCHAR(16)   NOT NULL COMMENT 'SUCCESS / PARTIAL / FAILED',
  detail          JSON          DEFAULT NULL COMMENT '详情（跳过原因/错误样本）',
  trigger_by      VARCHAR(64)   DEFAULT NULL COMMENT '触发人 / 定时任务',
  start_time      DATETIME      NOT NULL,
  end_time        DATETIME      DEFAULT NULL,
  create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_source_table_time (source_system, target_table, start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='集成同步日志表';

-- ============================================================
-- 六、金蝶完整菜单树（覆盖 add_kingdee_menu.sql 未覆盖的部分）
-- ============================================================

-- 一级菜单（add_kingdee_menu.sql 已处理，这里重复执行幂等）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '金蝶对接', 0, 20, 'kingdee', NULL, '', '', 1, 0, 'M', '0', '0', '', 'server',
       'admin', NOW(), '金蝶云星辰对接（基础数据同步）'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '金蝶对接' AND parent_id = 0);

-- 二级菜单：金蝶配置
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '金蝶配置', m.menu_id, 1, 'config', 'kingdee/index', '', '', 1, 0, 'C', '0', '0', 'kingdee:config:list', 'edit',
       'admin', NOW(), ''
FROM sys_menu m
WHERE m.menu_name = '金蝶对接' AND m.parent_id = 0
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'kingdee:config:list');

-- 按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '金蝶配置修改', m.menu_id, 1, 'F', '0', '0', 'kingdee:config:edit', '#', 'admin', NOW(), '保存配置 + 测试连通性'
FROM sys_menu m WHERE m.perms = 'kingdee:config:list' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'kingdee:config:edit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '客户同步', m.menu_id, 2, 'F', '0', '0', 'kingdee:sync:customer', '#', 'admin', NOW(), '手动触发客户全量同步'
FROM sys_menu m WHERE m.perms = 'kingdee:config:list' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'kingdee:sync:customer');

INSERT INTO sys_menu (menu_name, parent_id, order_num, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '物料同步', m.menu_id, 3, 'F', '0', '0', 'kingdee:sync:material', '#', 'admin', NOW(), '手动触发物料全量同步'
FROM sys_menu m WHERE m.perms = 'kingdee:config:list' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'kingdee:sync:material');

INSERT INTO sys_menu (menu_name, parent_id, order_num, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '供应商同步', m.menu_id, 4, 'F', '0', '0', 'kingdee:sync:supplier', '#', 'admin', NOW(), '手动触发供应商全量同步'
FROM sys_menu m WHERE m.perms = 'kingdee:config:list' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'kingdee:sync:supplier');

INSERT INTO sys_menu (menu_name, parent_id, order_num, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '全量同步', m.menu_id, 5, 'F', '0', '0', 'kingdee:sync:all', '#', 'admin', NOW(), '客户+物料+供应商一起同步'
FROM sys_menu m WHERE m.perms = 'kingdee:config:list' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'kingdee:sync:all');

-- 管理员角色授权（幂等）
DELETE FROM sys_role_menu
WHERE menu_id IN (
  SELECT menu_id FROM sys_menu
  WHERE perms LIKE 'kingdee:%'
     OR (menu_name = '金蝶对接' AND parent_id = 0)
);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE perms LIKE 'kingdee:%'
   OR (menu_name = '金蝶对接' AND parent_id = 0);

-- ============================================================
-- 七、金蝶同步定时任务（Quartz）
-- ============================================================
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
SELECT '金蝶基础数据同步', 'DEFAULT', 'kingdeeSyncJob.runAll()', '0 */30 * * * ?', '1', '1', '0', 'admin', NOW(), '每 30 分钟同步一次客户/物料/供应商'
WHERE NOT EXISTS (SELECT 1 FROM sys_job WHERE invoke_target = 'kingdeeSyncJob.runAll()');
