-- ============================================================
-- 金蝶云星辰对接 — 完整数据库升级脚本
-- 幂等：所有操作可重复执行无副作用
-- 日期：2026-11-11（修复 MySQL 8.0 兼容性 + 删除冲突表）
-- 兼容：MySQL 8.0.28 实测通过
-- ============================================================

USE edop_yhny;

-- ============================================================
-- 一、sys_config 金蝶配置项（密钥已用 AES 加密，加密密钥在 application.yml 的 ruoyi.fileEncryptionKey）
--   注意：首次部署时也可以只插 enabled=true，其余在前端"金蝶配置"页填写
-- ============================================================

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
--   幂等模式：information_schema 判断 + prepare/execute
-- ============================================================

-- leader_id
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_user' and column_name = 'leader_id');
set @sql := if(@col_exists = 0,
    'alter table sys_user add column leader_id bigint default null comment ''直接领导ID''',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- leader_name
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_user' and column_name = 'leader_name');
set @sql := if(@col_exists = 0,
    'alter table sys_user add column leader_name varchar(64) default null comment ''直接领导姓名''',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- superior_leader_id
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_user' and column_name = 'superior_leader_id');
set @sql := if(@col_exists = 0,
    'alter table sys_user add column superior_leader_id bigint default null comment ''上级领导ID''',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- superior_leader_name
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_user' and column_name = 'superior_leader_name');
set @sql := if(@col_exists = 0,
    'alter table sys_user add column superior_leader_name varchar(64) default null comment ''上级领导姓名''',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- charge_leader_id
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_user' and column_name = 'charge_leader_id');
set @sql := if(@col_exists = 0,
    'alter table sys_user add column charge_leader_id bigint default null comment ''分管领导ID''',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- charge_leader_name
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_user' and column_name = 'charge_leader_name');
set @sql := if(@col_exists = 0,
    'alter table sys_user add column charge_leader_name varchar(64) default null comment ''分管领导姓名''',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- 校验：6 列全部应为 1
SELECT 'SYS_USER_LEADER_COLS' tag, COUNT(*) cnt FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'sys_user'
  AND column_name IN ('leader_id','leader_name','superior_leader_id','superior_leader_name','charge_leader_id','charge_leader_name');

-- ============================================================
-- 三、sys_logininfor msg 列加长（KingdeeSyncController 同步结果可能较长）
--   此句重复执行无副作用，无需 IF NOT EXISTS
-- ============================================================
ALTER TABLE sys_logininfor MODIFY COLUMN msg VARCHAR(500) DEFAULT '' COMMENT '提示消息';

-- ============================================================
-- 四、mk_customer 补字段（同步时新增）
--   幂等模式：information_schema 判断 + prepare/execute
-- ============================================================

-- credit_days
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'mk_customer' and column_name = 'credit_days');
set @sql := if(@col_exists = 0,
    'alter table mk_customer add column credit_days int default 0 comment ''信用天数''',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- credit_limit
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'mk_customer' and column_name = 'credit_limit');
set @sql := if(@col_exists = 0,
    'alter table mk_customer add column credit_limit decimal(18,2) default null comment ''信用额度''',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- customer_type
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'mk_customer' and column_name = 'customer_type');
set @sql := if(@col_exists = 0,
    'alter table mk_customer add column customer_type varchar(32) default null comment ''客户类型''',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- parent_customer_id
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'mk_customer' and column_name = 'parent_customer_id');
set @sql := if(@col_exists = 0,
    'alter table mk_customer add column parent_customer_id varchar(64) default null comment ''上级客户编码''',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- contact_phone
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'mk_customer' and column_name = 'contact_phone');
set @sql := if(@col_exists = 0,
    'alter table mk_customer add column contact_phone varchar(32) default null comment ''联系电话''',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- 校验：5 列全部应为 1
SELECT 'MK_CUSTOMER_KD_COLS' tag, COUNT(*) cnt FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'mk_customer'
  AND column_name IN ('credit_days','credit_limit','customer_type','parent_customer_id','contact_phone');

-- ============================================================
-- 五、integration_user_binding / integration_sync_log — 已删除
--   原脚本定义与 sql/integration_attendance.sql（生产已执行）结构冲突，
--   且 ruoyi-kingdee 模块零引用这两张表，留着会在新环境首装埋雷。
--   如需绑定/日志表，请在 ruoyi-integration 模块维护或另起脚本。
-- ============================================================

-- ============================================================
-- 六、金蝶完整菜单树（幂等 NOT EXISTS 写法）
-- ============================================================

-- 一级菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063108, '金蝶云星辰集成', 9063000, 3, 'kingdee', NULL, '', '', 1, 0, 'M', '0', '0', '', 'server',
       'admin', NOW(), '金蝶云星辰对接（基础数据同步）'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063108);

-- 历史环境修正: 已按旧结构(一级菜单"金蝶对接")建过菜单的库, 统一迁移为系统集成子菜单
UPDATE sys_menu SET menu_name = '金蝶云星辰集成', parent_id = 9063000, order_num = 3
WHERE menu_id = 9063108 AND (parent_id <> 9063000 OR menu_name <> '金蝶云星辰集成');

-- 二级菜单：金蝶配置
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '金蝶配置', m.menu_id, 1, 'setting', 'kingdee/index', '', '', 1, 0, 'C', '0', '0', 'kingdee:config:list', 'edit',
       'admin', NOW(), ''
FROM sys_menu m
WHERE m.menu_id = 9063108
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'kingdee:config:list');

-- 按钮权限（5 个）
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

-- 管理员角色授权（幂等：先删后插）
DELETE FROM sys_role_menu
WHERE menu_id IN (
  SELECT menu_id FROM sys_menu
  WHERE perms LIKE 'kingdee:%'
     OR (menu_id = 9063108)
);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE perms LIKE 'kingdee:%'
   OR (menu_id = 9063108);

-- 校验：金蝶菜单数应为 7（1 个 M + 1 个 C + 5 个 F）
SELECT 'KINGDEE_MENU_COUNT' tag, COUNT(*) cnt FROM sys_menu WHERE perms LIKE 'kingdee:%' OR (menu_id = 9063108);

-- ============================================================
-- 七、金蝶同步定时任务（Quartz）
-- ============================================================
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
SELECT '金蝶基础数据同步', 'DEFAULT', 'kingdeeSyncJob.runAll()', '0 */30 * * * ?', '1', '1', '0', 'admin', NOW(), '每 30 分钟同步一次客户/物料/供应商'
WHERE NOT EXISTS (SELECT 1 FROM sys_job WHERE invoke_target = 'kingdeeSyncJob.runAll()');

-- 校验
SELECT 'KINGDEE_JOB' tag, job_name, invoke_target, cron_expression FROM sys_job WHERE invoke_target = 'kingdeeSyncJob.runAll()';
