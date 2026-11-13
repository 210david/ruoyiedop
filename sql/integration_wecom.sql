-- =============================================================
-- 第三方集成模块（企业微信考勤同步）初始化脚本
-- 版本: v1.0  日期: 2026-11-07
-- 依赖规范: unified-development-standards.md（3.1 建表 / 3.4 菜单 / 7 SQL目录）
-- 说明: 绑定表与同步日志表复用 integration_attendance.sql，通过 connector_type='wecom' 区分，
--       本脚本仅新增企业微信参数、打卡来源字典、定时任务与菜单文案优化，可重复执行。
-- =============================================================

-- ----------------------------
-- 1. 企业微信集成参数（系统管理-参数设置 中维护）
--    corpId : 企业管理后台 → 我的企业 → 企业信息 → 企业ID
--    secret : 自建应用 Secret（应用管理 → 应用 → 自建）
--    使用前提：自建应用可见范围覆盖需同步成员；同步打卡需在「打卡 → 可调用接口的应用」中加入该应用；
--              自建应用服务器出口 IP 需加入「企业可信IP」
-- ----------------------------
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '企业微信集成开关', 'integration.wecom.enabled', 'false', 'Y', 'admin', sysdate(), '企业微信集成总开关(true/false)，启用前先配置 corpId/secret'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'integration.wecom.enabled');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '企业微信CorpId', 'integration.wecom.corpId', '', 'Y', 'admin', sysdate(), '企业微信 CorpId（我的企业 → 企业信息）'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'integration.wecom.corpId');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '企业微信Secret', 'integration.wecom.secret', '', 'Y', 'admin', sysdate(), '企业微信自建应用 Secret（仅管理员可见，注意保密）'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'integration.wecom.secret');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '企业微信匹配策略', 'integration.wecom.matchType', 'mobile', 'Y', 'admin', sysdate(),
       '通讯录自动匹配策略：mobile=手机号优先、工号兜底(默认) / jobNumber=工号优先 / auto=同mobile；工号取自成员别名 alias 或 userid'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'integration.wecom.matchType');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '企业微信模拟数据模式', 'integration.wecom.mockEnabled', 'false', 'Y', 'admin', sysdate(),
       '无真实凭据时开启：返回内置模拟通讯录与打卡数据，不调用企业微信接口；生产环境必须关闭，定时任务在该模式下自动跳过'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'integration.wecom.mockEnabled');

-- ----------------------------
-- 2. 打卡来源字典新增：6=企业微信同步（hr_clock_source；0考勤机/1补卡/2手动/3外出/4出差/5钉钉 已被占用）
-- ----------------------------
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 6, '企业微信同步', '6', 'hr_clock_source', '', 'primary', 'N', '0', 'admin', sysdate(), '企业微信打卡数据自动同步'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'hr_clock_source' AND dict_value = '6');

-- ----------------------------
-- 3. 定时任务（默认暂停状态 status='1'，配置好凭据后在 系统监控-定时任务 中启用）
--    与钉钉任务错开 10 分钟，避免同时段争抢数据库连接
-- ----------------------------
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
SELECT '企业微信通讯录同步', 'DEFAULT', 'wecomAttendanceSyncTask.syncContacts', '0 20 1 * * ?', '3', '1', '1', 'admin', sysdate(),
       '每日01:20同步企业微信通讯录并自动匹配员工（手机号优先、工号兜底）'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_job WHERE invoke_target = 'wecomAttendanceSyncTask.syncContacts');

INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
SELECT '企业微信考勤同步', 'DEFAULT', 'wecomAttendanceSyncTask.syncYesterday', '0 40 1 * * ?', '3', '1', '1', 'admin', sysdate(),
       '每日01:40同步昨日企业微信打卡数据到考勤模块'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_job WHERE invoke_target = 'wecomAttendanceSyncTask.syncYesterday');

-- ----------------------------
-- 4. 菜单（已由 integration_menu_split.sql 接管，此处不再改名）
--    历史做法是将菜单名改为「第三方考勤集成」共用一页；现已拆分为
--    「钉钉集成」（9063010 / integration/attendance/index）与
--    「企业微信集成」（9063030 / integration/wecom/index）两个独立菜单，
--    请执行 sql/integration_menu_split.sql，本节保留为空以免重复执行时回退菜单名。
-- ----------------------------

-- ----------------------------
-- 5. 一一绑定并发保护（可选，建议执行）
--    唯一索引在数据库层防止同一连接器下同一员工被并发绑定两次；
--    执行前请先运行「检查脚本」确认无重复，若有重复需先清理，否则建索引会失败。
-- ----------------------------
-- 检查是否存在重复绑定（正常应返回 0 行）
-- SELECT connector_type, employee_id, COUNT(*) AS cnt
--   FROM integration_user_binding
--  WHERE employee_id IS NOT NULL
--  GROUP BY connector_type, employee_id
-- HAVING COUNT(*) > 1;
--
-- ALTER TABLE integration_user_binding
--   ADD UNIQUE KEY uk_connector_employee (connector_type, employee_id);
