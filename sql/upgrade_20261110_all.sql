-- ============================================================
-- 升级脚本: upgrade_20261110_all.sql (完整版 v3)
-- 主题: 11-09 部门/人员字段 + 考试管理/系统集成/决策支持(BI)三模块建表与菜单 + 菜单授权补齐
-- 幂等: 可重复执行
-- 目标库: edop_yhny
-- ============================================================

USE edop_yhny;

-- ---------- PART 0: 部门/人员表字段调整(合并自 20261109 脚本, 幂等) ----------
-- ============================================================

-- ---------- 1. sys_dept 新增字段 ----------
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_dept' and column_name = 'dept_short_name');
set @sql := if(@col_exists = 0,
    'alter table sys_dept add column dept_short_name varchar(50) default null comment ''部门简称'' after leader',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_dept' and column_name = 'dept_code');
set @sql := if(@col_exists = 0,
    'alter table sys_dept add column dept_code varchar(50) default null comment ''部门编码'' after dept_short_name',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_dept' and column_name = 'charge_leader_id');
set @sql := if(@col_exists = 0,
    'alter table sys_dept add column charge_leader_id bigint default null comment ''分管领导(人员ID,关联sys_user.user_id)'' after dept_code',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_dept' and column_name = 'staff_count');
set @sql := if(@col_exists = 0,
    'alter table sys_dept add column staff_count int default null comment ''在职人员数量'' after charge_leader_id',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- ---------- 2. sys_dept 删除联系电话/邮箱 ----------
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_dept' and column_name = 'phone');
set @sql := if(@col_exists > 0,
    'alter table sys_dept drop column phone',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_dept' and column_name = 'email');
set @sql := if(@col_exists > 0,
    'alter table sys_dept drop column email',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- ---------- 3. sys_user 新增上级领导/分管领导 ----------
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_user' and column_name = 'superior_leader_id');
set @sql := if(@col_exists = 0,
    'alter table sys_user add column superior_leader_id bigint default null comment ''上级领导(人员ID,关联sys_user.user_id)'' after remark',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_user' and column_name = 'charge_leader_id');
set @sql := if(@col_exists = 0,
    'alter table sys_user add column charge_leader_id bigint default null comment ''分管领导(人员ID,关联sys_user.user_id)'' after superior_leader_id',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- ---------- 4. 回填 sys_dept.staff_count(在职人员数量 = 在职用户数) ----------
update sys_dept d
set d.staff_count = (
    select count(*) from sys_user u
    where u.dept_id = d.dept_id and u.status = '0' and u.del_flag = '0'
);

-- ---------- PART 1: 系统集成模块建表 ----------
-- =============================================================
-- 第三方集成模块（钉钉考勤同步）初始化脚本
-- 版本: v1.1  日期: 2026-11-07
-- 依赖规范: unified-development-standards.md（3.1 建表 / 3.4 菜单 / 7 SQL目录）
--
-- v1.1 变更（重要）：改为全幂等，可安全重复执行
--   1) 去掉 DROP TABLE，改 CREATE TABLE IF NOT EXISTS
--      —— 原脚本重复执行会清空 integration_user_binding 已绑定关系与同步日志；
--   2) 参数与定时任务改 INSERT ... SELECT ... WHERE NOT EXISTS
--      —— 原脚本重复执行会产生重复参数（+4）与重复任务（+2）；
--   3) 唯一索引 uk_connector_employee 见 integration_ensure_unique_index.sql。
--
-- 执行顺序：本脚本 → integration_wecom.sql → integration_ensure_unique_index.sql
--          → add_integration_menu.sql → integration_menu_split.sql
--          → module_switch_add_integration.sql
-- =============================================================

-- ----------------------------
-- 1. 外部平台用户绑定表
--    注意：禁止 DROP TABLE——本脚本可能在生产环境重复执行，
--          DROP 会直接清空已绑定关系，改用 IF NOT EXISTS
-- ----------------------------
CREATE TABLE IF NOT EXISTS integration_user_binding (
    binding_id      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '绑定ID',
    connector_type  VARCHAR(32)  NOT NULL DEFAULT 'dingtalk' COMMENT '连接器类型(dingtalk/wecom)',
    external_userid VARCHAR(64)  NOT NULL                COMMENT '外部平台用户ID(钉钉userid)',
    external_name   VARCHAR(64)  DEFAULT NULL            COMMENT '外部平台姓名',
    mobile          VARCHAR(32)  DEFAULT NULL            COMMENT '外部平台手机号',
    external_job_no VARCHAR(64)  DEFAULT NULL            COMMENT '外部平台工号(钉钉jobNumber)',
    employee_no     VARCHAR(64)  DEFAULT NULL            COMMENT '平台员工工号(匹配后回填)',
    employee_id     BIGINT       DEFAULT NULL            COMMENT '平台员工ID',
    match_type      VARCHAR(16)  DEFAULT NULL            COMMENT '匹配方式(auto自动/manual人工)',
    sync_time       DATETIME     DEFAULT NULL            COMMENT '最后同步时间',
    remark          VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    create_by       VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time     DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time     DATETIME     DEFAULT NULL            COMMENT '更新时间',
    PRIMARY KEY (binding_id),
    UNIQUE KEY uk_connector_user (connector_type, external_userid),
    KEY idx_employee (employee_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='外部平台用户绑定表';

-- ----------------------------
-- 2. 集成同步日志表
--    注意：禁止 DROP TABLE，重复执行不得清空同步日志
-- ----------------------------
CREATE TABLE IF NOT EXISTS integration_sync_log (
    log_id        BIGINT        NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    connector_type VARCHAR(32)  NOT NULL DEFAULT 'dingtalk' COMMENT '连接器类型',
    biz_type      VARCHAR(64)   NOT NULL                COMMENT '业务类型(contact_sync/attendance_sync)',
    biz_key       VARCHAR(128)  DEFAULT NULL            COMMENT '业务关键字(日期范围等)',
    status        CHAR(1)       NOT NULL DEFAULT '0'    COMMENT '状态(0成功 1失败)',
    result_msg    VARCHAR(1000) DEFAULT NULL            COMMENT '结果摘要',
    cost_ms       BIGINT        DEFAULT NULL            COMMENT '耗时(毫秒)',
    create_time   DATETIME      DEFAULT NULL            COMMENT '创建时间',
    PRIMARY KEY (log_id),
    KEY idx_type_time (connector_type, biz_type, create_time)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='集成同步日志表';

-- ----------------------------
-- 3. 钉钉集成参数（系统管理-参数设置 中维护；enabled=true 且凭据齐全后生效）
-- ----------------------------
-- 幂等：全部用 NOT EXISTS 判重，重复执行不会产生重复参数
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '钉钉集成开关', 'integration.dingtalk.enabled', 'false', 'Y', 'admin', sysdate(), '钉钉集成总开关(true/false)，启用前先配置 appKey/appSecret'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'integration.dingtalk.enabled');
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '钉钉集成AppKey', 'integration.dingtalk.appKey', '', 'Y', 'admin', sysdate(), '钉钉企业内部应用 AppKey'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'integration.dingtalk.appKey');
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '钉钉集成AppSecret', 'integration.dingtalk.appSecret', '', 'Y', 'admin', sysdate(), '钉钉企业内部应用 AppSecret（仅管理员可见，注意保密）'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'integration.dingtalk.appSecret');
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '钉钉集成匹配策略', 'integration.dingtalk.matchType', 'mobile', 'Y', 'admin', sysdate(), '通讯录自动匹配策略：mobile=按手机号 / jobNumber=按工号(需钉钉维护jobNumber) / auto=工号优先手机号兜底；切换仅影响未匹配绑定'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'integration.dingtalk.matchType');
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '钉钉模拟数据模式', 'integration.dingtalk.mockEnabled', 'false', 'Y', 'admin', sysdate(), '无真实凭据时开启：返回内置模拟通讯录与打卡数据，不调用钉钉接口；生产环境必须关闭，定时任务在该模式下自动跳过'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'integration.dingtalk.mockEnabled');

-- ----------------------------
-- 4. 打卡来源字典新增：5=钉钉同步（hr_clock_source；3/4 已被外出/出差占用，勿用）
-- ----------------------------
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '钉钉同步', '5', 'hr_clock_source', '', 'primary', 'N', '0', 'admin', sysdate(), '钉钉打卡数据自动同步'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'hr_clock_source' AND dict_value = '5');

-- ----------------------------
-- 5. 定时任务示例（默认暂停状态，配置好凭据后在 系统监控-定时任务 中启用）
-- ----------------------------
-- 幂等：按 invoke_target 判重，重复执行不会产生重复任务
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
SELECT '钉钉通讯录同步', 'DEFAULT', 'dingtalkAttendanceSyncTask.syncContacts', '0 10 1 * * ?', '3', '1', '1', 'admin', sysdate(), '每日01:10同步钉钉通讯录并自动匹配员工(按手机号)'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_job WHERE invoke_target = 'dingtalkAttendanceSyncTask.syncContacts');
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
SELECT '钉钉考勤同步', 'DEFAULT', 'dingtalkAttendanceSyncTask.syncYesterday', '0 30 1 * * ?', '3', '1', '1', 'admin', sysdate(), '每日01:30同步昨日打卡数据到考勤模块'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_job WHERE invoke_target = 'dingtalkAttendanceSyncTask.syncYesterday');

-- ----------------------------
-- 6. 使用说明（非SQL）
-- ----------------------------
-- ① 钉钉开放平台创建企业内部应用，开通「考勤」权限点，将 AppKey/AppSecret 填入参数设置；
-- ② 参数 integration.dingtalk.enabled 改为 true，刷新系统缓存（或重启）；
-- ③ 按 integration.dingtalk.matchType 策略（mobile/jobNumber/auto）执行
--    dingtalkAttendanceSyncTask.syncContacts 同步通讯录并自动匹配员工，未匹配者在
--    GET /integration/attendance/bindings 查看后用 PUT /integration/attendance/binding/bind 人工绑定；
-- ④ 执行 dingtalkAttendanceSyncTask.syncYesterday 验证打卡数据落库
--    （考勤记录列表来源列应显示「钉钉同步」，备注为「钉钉同步」）；
-- ⑤ 首次上线需回溯历史时调用 POST /integration/attendance/sync/{beginDate}/{endDate}（单次≤92天）。

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

-- ---------- PART 2: 决策支持(BI)模块建表+指标字典 ----------
-- =============================================
-- BI 决策支持子系统 - 全量SQL脚本 (阶段一)
-- 包含：4张业务表 + 指标字典初始数据(22个指标) + 菜单权限
-- 依据：doc/plans/PRD-决策支持模块-功能需求方案.md v1.2
-- 规范：统一开发规范 3.1/3.4/3.5/7（显式 COLLATE、M/C/F 三级菜单、统计类数据权限豁免声明）
-- =============================================

-- =============================================
-- 一、业务表 DDL
-- 数据权限声明（规范 3.5.1）：以下 4 张表均属「统计/追溯类」，豁免归属字段自动填充；
-- 其中 bi_metric_snapshot 携带 dept_id 作为统计维度（非归属），驾驶舱按用户数据权限过滤该维度。
-- =============================================

-- 1. 指标字典表
DROP TABLE IF EXISTS bi_metric_dict;
CREATE TABLE bi_metric_dict (
    id              bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    metric_code     varchar(50)  NOT NULL COMMENT '指标编码（唯一）',
    metric_name     varchar(100) NOT NULL COMMENT '指标名称',
    domain          varchar(20)  NOT NULL COMMENT '所属域（sales销售/bid投标/production生产/quality质量/purchase采购/inventory库存/equipment设备/safety安全/hr人力/synthesis综合）',
    caliber_desc    varchar(500) NOT NULL COMMENT '口径定义（文字描述，最终解释以本字段为准）',
    calc_ref        varchar(100) DEFAULT NULL COMMENT '计算实现引用（如 MkStatsProvider）',
    refresh_type    varchar(20)  NOT NULL DEFAULT 'snapshot' COMMENT '刷新方式（snapshot快照/realtime实时）',
    freq            varchar(10)  NOT NULL DEFAULT 'day' COMMENT '统计频率（day日/month月）',
    threshold_red    decimal(18,4) DEFAULT NULL COMMENT '红线阈值（P1预警启用）',
    threshold_yellow decimal(18,4) DEFAULT NULL COMMENT '黄线阈值（P1预警启用）',
    threshold_dir   varchar(10)  DEFAULT NULL COMMENT '阈值方向（lte低于预警/gte高于预警）',
    owner           varchar(50)  DEFAULT NULL COMMENT '指标责任人',
    status          char(1)      NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
    del_flag        char(1)      DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
    create_by       varchar(64)  DEFAULT '' COMMENT '创建者',
    create_time     datetime     DEFAULT NULL COMMENT '创建时间',
    update_by       varchar(64)  DEFAULT '' COMMENT '更新者',
    update_time     datetime     DEFAULT NULL COMMENT '更新时间',
    remark          varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_metric_code (metric_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='决策支持-指标字典';

-- 2. 指标字典变更留痕表（F1.1：谁改的口径、何时生效）
DROP TABLE IF EXISTS bi_metric_dict_log;
CREATE TABLE bi_metric_dict_log (
    id          bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    metric_code varchar(50) NOT NULL COMMENT '指标编码',
    change_type varchar(20) NOT NULL COMMENT '变更类型（create新增/update修改/disable停用）',
    field_name  varchar(50) DEFAULT NULL COMMENT '变更字段',
    old_value   varchar(500) DEFAULT NULL COMMENT '变更前内容',
    new_value   varchar(500) DEFAULT NULL COMMENT '变更后内容',
    change_by   varchar(64) DEFAULT '' COMMENT '变更人',
    change_time datetime    DEFAULT NULL COMMENT '变更时间',
    PRIMARY KEY (id),
    KEY idx_metric_code (metric_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='决策支持-指标字典变更留痕';

-- 3. 指标日快照表（F1.3：Quartz 每日 01:00 幂等写入，同日重跑先删后插）
DROP TABLE IF EXISTS bi_metric_snapshot;
CREATE TABLE bi_metric_snapshot (
    id            bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    metric_code   varchar(50) NOT NULL COMMENT '指标编码',
    metric_date   date        NOT NULL COMMENT '指标日期（快照粒度：日）',
    dept_id       bigint      NOT NULL DEFAULT 0 COMMENT '部门维度（sys_dept.dept_id；0=全部门汇总，避免 NULL 使唯一键失效）。统计维度列，非归属字段',
    metric_value  decimal(18,4) DEFAULT NULL COMMENT '指标值（无数据为 null，前端展示 —）',
    unit          varchar(20) DEFAULT NULL COMMENT '单位（元/个/%/小时）',
    data_flag     varchar(20) DEFAULT 'real' COMMENT '数据标记（real实测/estimate估算，如 DMS_OEE）',
    snapshot_time datetime    DEFAULT NULL COMMENT '快照时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_metric (metric_code, metric_date, dept_id),
    KEY idx_metric_date (metric_date),
    KEY idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='决策支持-指标日快照';

-- 4. 指标对账日志表（F1.5：每日快照后比对实时值与快照值）
DROP TABLE IF EXISTS bi_metric_check_log;
CREATE TABLE bi_metric_check_log (
    id             bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    metric_code    varchar(50) NOT NULL COMMENT '指标编码',
    check_date     date        NOT NULL COMMENT '对账日期',
    dept_id        bigint      DEFAULT NULL COMMENT '部门维度',
    snapshot_value decimal(18,4) DEFAULT NULL COMMENT '快照值',
    realtime_value decimal(18,4) DEFAULT NULL COMMENT '实时值',
    diff_value     decimal(18,4) DEFAULT NULL COMMENT '差异值',
    check_status   char(1)     NOT NULL DEFAULT '0' COMMENT '对账结果（0一致 1不一致）',
    create_time    datetime    DEFAULT NULL COMMENT '记录时间',
    PRIMARY KEY (id),
    KEY idx_check_date (check_date),
    KEY idx_metric_code (metric_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='决策支持-指标对账日志';

-- =============================================
-- 二、指标字典初始数据（22个，口径依据 PRD v1.2 §7 定稿）
-- 使用 WHERE NOT EXISTS 避免重复执行插入
-- =============================================

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'MK_SIG_AMT','月签约额','sales','当月生效合同金额合计','MkStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='MK_SIG_AMT');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'MK_RCPT_AMT','月回款额','sales','当月回款登记金额合计（mk_payment_plan 实际回款金额）','MkStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='MK_RCPT_AMT');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'MK_WIN_RATE','商机赢单率','sales','赢单商机数 / 关闭商机数','MkStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='MK_WIN_RATE');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'MK_LEAD_CVR','线索转化率','sales','成交线索 / 线索总数','MkStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='MK_LEAD_CVR');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'BID_WIN_RATE','中标率','bid','中标项目 / 参与项目','BidStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='BID_WIN_RATE');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'MMS_WO_RATE','工单完成率','production','已完工工单 / 应完工工单（扣除取消）','MmsStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='MMS_WO_RATE');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'MMS_EXCEPTION','生产异常单数','production','当日新增异常工单数','MmsStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='MMS_EXCEPTION');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'QMS_PASS_RATE','一次检验合格率','quality','一次合格数 / 已完成检验数（复用 QMS 看板 passRate 口径）','QmsStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='QMS_PASS_RATE');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'QMS_NCR_OPEN','未关闭NCR数','quality','状态≠关闭的 NCR 数量','QmsStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='QMS_NCR_OPEN');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'QMS_CAPA_RATE','CAPA按期关闭率','quality','按期关闭的 CAPA / 应关闭 CAPA（复用 QMS 看板 capaCloseRate 口径）','QmsStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='QMS_CAPA_RATE');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'PMS_ONTIME_RATE','到货及时率','purchase','按合同交期判定：按期到货订单行 / 总订单行（订单行粒度）【已确认 v1】','PmsStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='PMS_ONTIME_RATE');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'PMS_TOP_SUP','供应商集中度','purchase','Top5 供应商采购额占比','PmsStatsProvider','snapshot','month','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='PMS_TOP_SUP');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'WMS_TURNOVER','库存周转率','inventory','出库成本（按入库单价取价）/ 平均库存金额【已确认 v1】','WmsStatsProvider','snapshot','month','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='WMS_TURNOVER');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'WMS_ALERT_CNT','库存预警数','inventory','低库存+积压+临期+过期预警数量合计（复用 WMS 统一预警模型）','WmsStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='WMS_ALERT_CNT');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'WMS_SLOW_AMT','呆滞库存金额','inventory','超 90 天无出库记录的库存金额（不含委外/在制）【已确认 v1】','WmsStatsProvider','snapshot','month','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='WMS_SLOW_AMT');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'DMS_OEE','设备综合效率','equipment','估算口径（日运行16h假设），data_flag=estimate，接入实测采集后切换【已确认 v1：接受估算上线】。数据源 mms_oee_snapshot 表归属 MMS 模块，由 MmsStatsProvider 提供','MmsStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='DMS_OEE');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'DMS_DOWN_HRS','故障停机时长','equipment','当月故障工单停机时长合计','DmsStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='DMS_DOWN_HRS');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'SAFETY_RECT_RATE','隐患整改率','safety','已整改隐患 / 累计隐患','SafetyStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='SAFETY_RECT_RATE');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'HR_HEADCOUNT','在册人数','hr','在职状态员工数','HrStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='HR_HEADCOUNT');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'HR_LEAVE_RATE','月离职率','hr','当月离职人数 / 期初在职人数','HrStatsProvider','snapshot','month','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='HR_LEAVE_RATE');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'HR_COST_RATIO','人工成本占比','hr','当月人工成本 / 当月完工入库产值（MMS）【已确认 v1】','HrStatsProvider','snapshot','month','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='HR_COST_RATIO');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'OTC_CYCLE','现金周期','synthesis','合同签订到全额回清的平均天数，未回清合同不纳入；回款取 mk_payment_plan 实际回款【已确认 v1】','MkStatsProvider','snapshot','month','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='OTC_CYCLE');

-- =============================================
-- 三、菜单权限（规范 3.4：M/C/F 三级，权限标识 bi:*）
-- =============================================

-- 一级菜单（M=目录）
-- =============================================
-- BI 指标字典 - 口径说明补全（统计周期 + 计算口径）
-- 日期：2026-11-08
-- 背景：看板卡片未区分「当月 / 全量累计 / 时点存量」，同屏混用易误读
--       （如线索转化率为建库至今累计，而非当月转化率）
-- 说明：复用既有 caliber_desc 字段（varchar(500)，字典维护页可编辑、变更留痕），
--       不改表结构；按各 StatsProvider 实际取数代码核定，非按 freq 字段推断
--       （freq 表示快照频率，不等于统计周期）
-- 幂等：全部为按 metric_code 的 UPDATE，可重复执行
-- =============================================

-- ---------- 营销销售 sales ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：当月回款登记金额合计（mk_payment_plan 实际回款金额，按实际回款日期归月；已回款/部分回款均计入）'
WHERE metric_code = 'MK_RCPT_AMT';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：当月生效合同金额合计（按合同签订日期归月）'
WHERE metric_code = 'MK_SIG_AMT';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：全量累计（非当月）｜口径：成交线索 / 线索总数，取建库至今全部线索，无日期过滤'
WHERE metric_code = 'MK_LEAD_CVR';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：全量累计（非当月）｜口径：赢单商机数 / 已关闭商机数（赢单+输单），取全部历史商机，无日期过滤'
WHERE metric_code = 'MK_WIN_RATE';

-- ---------- 投标管理 bid ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：全量累计（非当月）｜口径：中标项目 / 已出结果项目数（中标 + 未中标 + 废标流标），取全部历史项目，无日期过滤'
WHERE metric_code = 'BID_WIN_RATE';

-- ---------- 生产制造 production ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：全量累计（非当月）｜口径：已完工工单 / 有效工单总数（不含草稿、已作废），取全部历史工单，无日期过滤'
WHERE metric_code = 'MMS_WO_RATE';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：当月新增异常工单数（全部状态，按上报时间归月）'
WHERE metric_code = 'MMS_EXCEPTION';

-- ---------- 设备管理 equipment ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：设备综合效率，估算口径（日运行 16 小时假设），按快照日期归月；接入实测采集后切换实测值'
WHERE metric_code = 'DMS_OEE';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：当月故障工单停机时长合计（按报修时间归月）'
WHERE metric_code = 'DMS_DOWN_HRS';

-- ---------- 质量管理 quality ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：全量累计（非当月）｜口径：一次检验合格数 / 已完成检验数，取全部历史检验任务，无日期过滤'
WHERE metric_code = 'QMS_PASS_RATE';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：时点存量｜口径：当前状态不等于“已关闭”的 NCR 数量（驳回亦视为未关闭），取当前快照时点'
WHERE metric_code = 'QMS_NCR_OPEN';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：全量累计（非当月）｜口径：已关闭 CAPA / CAPA 总数，取全部历史 CAPA 记录，无日期过滤'
WHERE metric_code = 'QMS_CAPA_RATE';

-- ---------- 采购供应 purchase ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：全量累计（非当月）｜口径：按期到货订单数 / 已到货订单数，“按期”以订单要求交期为基准，取全部已到货订单，无日期过滤'
WHERE metric_code = 'PMS_ONTIME_RATE';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：当月采购金额 Top5 供应商金额合计 / 当月采购总额（仅统计订单日期在当月、有供应商且金额有效的订单）'
WHERE metric_code = 'PMS_TOP_SUP';

-- ---------- 仓储库存 inventory ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：当月出库成本（按入库单价取价）/ 库存金额；平均库存金额以统计日时点库存金额近似'
WHERE metric_code = 'WMS_TURNOVER';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：时点存量｜口径：低库存 + 积压 + 临期 + 过期预警数量合计，取当前快照时点'
WHERE metric_code = 'WMS_ALERT_CNT';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：时点存量｜口径：超 90 天无出库记录的库存金额（不含委外/在制），取当前快照时点'
WHERE metric_code = 'WMS_SLOW_AMT';

-- ---------- 人力资源 hr ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：时点存量｜口径：当前在职状态员工数，取当前快照时点'
WHERE metric_code = 'HR_HEADCOUNT';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：当月离职人数 / 期初在职人数（按离职日期归月）'
WHERE metric_code = 'HR_LEAVE_RATE';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：当月人工成本 / 当月完工入库产值'
WHERE metric_code = 'HR_COST_RATIO';

-- ---------- 安全生产 safety ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：全量累计（非当月）｜口径：已整改隐患 / 应整改隐患总数（草稿、待审批、驳回不纳入分母），取全部历史隐患，无日期过滤'
WHERE metric_code = 'SAFETY_RECT_RATE';

-- ---------- 综合价值链 synthesis ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：合同签订到全额回清的平均天数；仅统计回清完成日期在当月的合同，未回清合同不纳入'
WHERE metric_code = 'OTC_CYCLE';

-- =============================================
-- 校验：确认 22 个指标均已标注统计周期
-- =============================================
-- SELECT metric_code, metric_name, domain, caliber_desc FROM bi_metric_dict
--  WHERE metric_code IN ('MK_RCPT_AMT','MK_SIG_AMT','MK_LEAD_CVR','MK_WIN_RATE','BID_WIN_RATE',
--        'MMS_WO_RATE','MMS_EXCEPTION','DMS_OEE','DMS_DOWN_HRS','QMS_PASS_RATE','QMS_NCR_OPEN',
--        'QMS_CAPA_RATE','PMS_ONTIME_RATE','PMS_TOP_SUP','WMS_TURNOVER','WMS_ALERT_CNT',
--        'WMS_SLOW_AMT','HR_HEADCOUNT','HR_LEAVE_RATE','HR_COST_RATIO','SAFETY_RECT_RATE','OTC_CYCLE')
--  ORDER BY domain, metric_code;

-- ---------- PART 3: 菜单本体(显式ID, INSERT IGNORE) ----------
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063000,'系统集成',0,90,'integration',NULL,'','',1,0,'M','0','0','','link','admin','2026-11-08 10:42:23','',NULL,'第三方集成管理目录');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063010,'钉钉集成',9063000,1,'attendance','integration/attendance/index','','',1,0,'C','0','0','integration:attendance:list','date','admin','2026-11-08 10:42:23','',NULL,'钉钉第三方集成（考勤同步、后续消息等）');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063011,'集成同步',9063010,1,'#','','','',1,0,'F','0','0','integration:attendance:sync','#','admin','2026-11-08 10:42:23','',NULL,'同步通讯录、打卡同步与人工绑定');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063012,'绑定查询',9063010,2,'#','','','',1,0,'F','0','0','integration:attendance:query','#','admin','2026-11-08 10:42:23','',NULL,'绑定关系明细查询');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063020,'集成配置',9063010,3,'#','','','',1,0,'F','0','0','integration:attendance:config','#','admin','2026-11-08 10:42:23','',NULL,'钉钉集成参数配置（AppKey/AppSecret/开关/匹配策略）');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063030,'企业微信集成',9063000,2,'wecom','integration/wecom/index','','',1,0,'C','0','0','integration:attendance:list','wechat','admin','2026-11-08 10:42:23','',NULL,'企业微信考勤同步与用户绑定');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063031,'决策支持',0,60,'/bi',NULL,NULL,'',1,0,'M','0','0',NULL,'chart','admin','2026-11-08 16:06:33','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063032,'决策驾驶舱',9063031,1,'dashboard','bi/dashboard/index',NULL,'BiDashboard',1,0,'C','0','0','bi:dashboard:view','dashboard','admin','2026-11-08 16:06:33','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063033,'指标字典',9063031,2,'metric','bi/metric/index',NULL,'BiMetric',1,0,'C','0','0','bi:metric:list','dict','admin','2026-11-08 16:06:33','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063034,'指标查询',9063033,1,'',NULL,NULL,'',1,0,'F','0','0','bi:metric:query','#','admin','2026-11-08 16:06:33','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063035,'指标新增',9063033,2,'',NULL,NULL,'',1,0,'F','0','0','bi:metric:add','#','admin','2026-11-08 16:06:33','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063036,'指标修改',9063033,3,'',NULL,NULL,'',1,0,'F','0','0','bi:metric:edit','#','admin','2026-11-08 16:06:33','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063037,'指标删除',9063033,4,'',NULL,NULL,'',1,0,'F','0','0','bi:metric:remove','#','admin','2026-11-08 16:06:33','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063038,'指标导出',9063033,5,'',NULL,NULL,'',1,0,'F','0','0','bi:metric:export','#','admin','2026-11-08 16:06:33','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063039,'对账查询',9063033,6,'',NULL,NULL,'',1,0,'F','0','0','bi:metric:check','#','admin','2026-11-08 16:06:33','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063040,'销售看板',9063049,3,'board-sales','bi/board/index',NULL,'BiBoardSales',1,0,'C','0','0','bi:board:sales:view','money','admin','2026-11-08 16:06:33','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063041,'采购看板',9063049,4,'board-purchase','bi/board/index',NULL,'BiBoardPurchase',1,0,'C','0','0','bi:board:purchase:view','shopping','admin','2026-11-08 16:06:33','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063042,'生产看板',9063049,5,'board-production','bi/board/index',NULL,'BiBoardProduction',1,0,'C','0','0','bi:board:production:view','build','admin','2026-11-08 16:06:33','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063043,'库存看板',9063049,6,'board-inventory','bi/board/index',NULL,'BiBoardInventory',1,0,'C','0','0','bi:board:inventory:view','dict','admin','2026-11-08 16:06:33','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063044,'质量看板',9063049,7,'board-quality','bi/board/index',NULL,'BiBoardQuality',1,0,'C','0','0','bi:board:quality:view','validCode','admin','2026-11-08 16:06:33','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063045,'安全看板',9063049,8,'board-safety','bi/board/index',NULL,'BiBoardSafety',1,0,'C','0','0','bi:board:safety:view','warning','admin','2026-11-08 16:06:33','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063046,'设备看板',9063049,9,'board-equipment','bi/board/index',NULL,'BiBoardEquipment',1,0,'C','0','0','bi:board:equipment:view','server','admin','2026-11-08 16:06:33','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063047,'人力看板',9063049,10,'board-hr','bi/board/index',NULL,'BiBoardHr',1,0,'C','0','0','bi:board:hr:view','education','admin','2026-11-08 16:06:33','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063048,'投标看板',9063049,11,'board-bid','bi/board/index',NULL,'BiBoardBid',1,0,'C','0','0','bi:board:bid:view','documentation','admin','2026-11-08 16:06:33','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063049,'指标看板分析',9063031,3,'board',NULL,NULL,'',1,0,'M','0','0','','dashboard','admin','2026-11-08 21:28:14','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063050,'统计分析',9063031,4,'statistics',NULL,NULL,'',1,0,'M','0','0','','chart','admin','2026-11-08 21:28:14','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063052,'仓储管理',9063050,1,'wms',NULL,NULL,'',1,0,'M','0','0','','scan','admin','2026-11-08 21:28:15','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063053,'设备管理',9063050,2,'dms',NULL,NULL,'',1,0,'M','0','0','','server','admin','2026-11-08 21:28:15','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063054,'营销管理',9063050,3,'mk',NULL,NULL,'',1,0,'M','0','0','','money','admin','2026-11-08 21:28:15','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063055,'采购管理',9063050,4,'pms',NULL,NULL,'',1,0,'M','0','0','','shopping','admin','2026-11-08 21:28:15','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063056,'安全生产管理',9063050,5,'safety',NULL,NULL,'',1,0,'M','0','0','','shield','admin','2026-11-08 21:28:15','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063057,'质量管理',9063050,6,'qms',NULL,NULL,'',1,0,'M','0','0','','chart','admin','2026-11-08 21:28:15','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063058,'生产管控',9063050,7,'mms',NULL,NULL,'',1,0,'M','0','0','','tool','admin','2026-11-08 21:28:15','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063059,'产品设计',9063050,8,'pdm',NULL,NULL,'',1,0,'M','0','0','','component','admin','2026-11-08 21:28:15','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063060,'人力资源管理',9063050,9,'hr',NULL,NULL,'',1,0,'M','0','0','','people','admin','2026-11-08 21:28:15','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063061,'AI中心',9063050,10,'ai',NULL,NULL,'',1,0,'M','0','0','','skill','admin','2026-11-08 21:28:15','',NULL,'');
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9063062,'财务管理',9063050,11,'fin',NULL,NULL,'',1,0,'M','0','0','','money','admin','2026-11-08 21:28:15','',NULL,'');
-- ---------- 1. 菜单本体（ID 映射：标准 -> 分支） ----------
-- 9063048 质量数据报表   -> 9063100 (父 4813 报表中心)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063100, '质量数据报表', 4813, 1, 'list', 'qms/report/index', '', '', 1, 0, 'C', '0', '0', 'qms:report:list', 'documentation', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063100);

-- 9063052 产品设计驾驶舱 -> 9063101 (父 11000 产品设计)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063101, '产品设计驾驶舱', 11000, 99, 'statistics', 'pdm/statistics/index', '', '', 1, 0, 'C', '0', '0', 'pdm:statistics:view', 'chart', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063101);

-- 9063053 人力资源统计报表 -> 9063102 (父 905538 人力资源管理)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063102, '人力资源统计报表', 905538, 8, 'statistics', 'hr/statistics/index', '', '', 1, 0, 'C', '0', '0', 'hr:statistics:list', 'chart', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063102);

-- 9063061 设备大屏       -> 9063103 (父 3006 统计分析)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063103, '设备大屏', 3006, 2, 'screen', 'dms/equipment/screen', '', '', 1, 0, 'C', '0', '0', 'dms:equipment:query', 'monitor', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063103);

-- 9063064 推广统计分析   -> 9063104 (父 4006 数据分析)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063104, '推广统计分析', 4006, 2, 'promotion', 'mk/promotionReport/index', '', '', 1, 0, 'C', '0', '0', 'marketing:promotionReport:view', 'dashboard', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063104);

-- 9063066 质量管理驾驶舱 -> 9063105 (父 4813 报表中心)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063105, '质量管理驾驶舱', 4813, 2, 'dashboard', 'qms/dashboard/index', '', '', 1, 0, 'C', '0', '0', 'qms:dashboard:view', 'dashboard', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063105);

-- 9063067 生产驾驶舱     -> 9063106 (父 5000 生产管控)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063106, '生产驾驶舱', 5000, 10, 'dashboard', 'mms/dashboard/index', '', '', 1, 0, 'C', '0', '0', 'mms:dashboard:list', 'dashboard', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063106);

-- 9063070 投标数据分析   -> 9063107 (父 905886 投标管理)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063107, '投标数据分析', 905886, 8, 'dashboard', 'bid/dashboard/index', '', '', 1, 0, 'C', '0', '0', 'bid:dashboard:view', 'chart', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063107);

-- 自增保护: 显式ID已到9063107, 后续考试菜单自增从9063200起
ALTER TABLE sys_menu AUTO_INCREMENT = 9063200;

-- ---------- PART 4: 在线考试模块建表+菜单+种子 ----------
-- ============================================================
-- 安全生产 · 在线考试模块 全量SQL脚本
-- 包含：7张业务表DDL + 字典类型/数据 + 菜单权限
-- 依据：《安全生产模块考试功能开发需求方案》v1.1
-- 规范：表前缀 biz_safety_*（与安全模块一致）、COLLATE=utf8mb4_general_ci（§3.6强制）、
--       字段注释字典标注（§3.3.1强制）、数据权限归属字段（§3.5强制）
-- 幂等：可重复执行（DROP IF EXISTS + DELETE 再 INSERT）
-- ============================================================

-- =============================================
-- 零、清理旧数据（防止重复执行报错）
-- =============================================
DELETE FROM sys_role_menu WHERE menu_id IN (
    SELECT menu_id FROM sys_menu WHERE perms LIKE 'safety:exam:%'
);
DELETE FROM sys_menu WHERE perms LIKE 'safety:exam:%';

DELETE FROM sys_dict_data WHERE dict_type LIKE 'safety_exam_%';
DELETE FROM sys_dict_type WHERE dict_type LIKE 'safety_exam_%';

-- =============================================
-- 一、业务表 DDL（7张）
-- =============================================

-- 1. 题库分类表（主数据/共享资源，豁免数据权限归属字段）
DROP TABLE IF EXISTS biz_safety_exam_category;
CREATE TABLE biz_safety_exam_category (
    category_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '分类ID',
    parent_id          BIGINT       DEFAULT 0                COMMENT '父分类ID（支持两级）',
    category_name      VARCHAR(50)  NOT NULL                 COMMENT '分类名称',
    order_num          INT          DEFAULT 0                COMMENT '显示顺序',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    status             CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (category_id),
    KEY idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试题库分类表';

-- 2. 题库表（主数据/共享资源，豁免数据权限归属字段）
DROP TABLE IF EXISTS biz_safety_exam_question;
CREATE TABLE biz_safety_exam_question (
    question_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '题目ID',
    question_type      CHAR(1)      NOT NULL                 COMMENT '题型（字典 safety_exam_question_type）',
    content            TEXT         NOT NULL                 COMMENT '题干',
    options            TEXT         NOT NULL                 COMMENT '选项（JSON数组，如["A内容","B内容"]；判断题固定["正确","错误"]）',
    answer             VARCHAR(20)  NOT NULL                 COMMENT '正确答案（如 A / ABD / 对）',
    analysis           VARCHAR(500)                          COMMENT '答案解析',
    category_id        BIGINT                                COMMENT '题目分类ID',
    category_name      VARCHAR(50)                           COMMENT '分类名称（冗余）',
    difficulty         CHAR(1)      DEFAULT '2'              COMMENT '难度（字典 safety_exam_difficulty）',
    job_types          VARCHAR(200)                          COMMENT '适用工种（逗号分隔，字典 safety_job_type）',
    status             CHAR(1)      DEFAULT '0'              COMMENT '状态（0启用 1停用）',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (question_id),
    KEY idx_category (category_id),
    KEY idx_type_diff (question_type, difficulty)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='安全生产考试题库表';

-- 3. 试卷表（主数据，发布后题目集合冻结）
DROP TABLE IF EXISTS biz_safety_exam_paper;
CREATE TABLE biz_safety_exam_paper (
    paper_id           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '试卷ID',
    paper_name         VARCHAR(100) NOT NULL                 COMMENT '试卷名称',
    course_id          BIGINT                                COMMENT '关联培训课程ID（biz_safety_training_course）',
    course_name        VARCHAR(255)                          COMMENT '课程名称（冗余）',
    total_score        DECIMAL(5,1) DEFAULT 100.0            COMMENT '总分（按题目分值自动计算）',
    pass_score         DECIMAL(5,1) DEFAULT 60.0             COMMENT '及格分',
    duration           INT          DEFAULT 60               COMMENT '考试时长（分钟）',
    shuffle_question   CHAR(1)      DEFAULT 'N'              COMMENT '题目乱序（Y是 N否）',
    shuffle_option     CHAR(1)      DEFAULT 'N'              COMMENT '选项乱序（Y是 N否）',
    switch_limit       INT          DEFAULT 0                COMMENT '切屏限制次数（0不限）',
    show_analysis      CHAR(1)      DEFAULT 'Y'              COMMENT '交卷后展示解析（Y是 N否）',
    status             CHAR(1)      DEFAULT '0'              COMMENT '试卷状态（字典 safety_exam_paper_status）',
    publish_by         VARCHAR(64)                           COMMENT '发布人',
    publish_time       DATETIME                              COMMENT '发布时间',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (paper_id),
    KEY idx_course (course_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试试卷表';

-- 4. 试卷题目明细表（快照式：发布时冻结题干/选项/答案，判分依据）
DROP TABLE IF EXISTS biz_safety_exam_paper_question;
CREATE TABLE biz_safety_exam_paper_question (
    id                 BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
    paper_id           BIGINT       NOT NULL                 COMMENT '试卷ID',
    question_id        BIGINT       NOT NULL                 COMMENT '来源题目ID（biz_safety_exam_question）',
    question_type      CHAR(1)      NOT NULL                 COMMENT '题型（字典 safety_exam_question_type）',
    content            TEXT         NOT NULL                 COMMENT '题干快照',
    options            TEXT         NOT NULL                 COMMENT '选项快照（JSON数组）',
    answer             VARCHAR(20)  NOT NULL                 COMMENT '答案快照（判分依据）',
    analysis           VARCHAR(500)                          COMMENT '解析快照',
    score              DECIMAL(4,1) DEFAULT 1.0              COMMENT '本题分值',
    sort_no            INT          DEFAULT 0                COMMENT '题目顺序号',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_paper (paper_id),
    KEY idx_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='试卷题目明细表（快照）';

-- 5. 考试场次表（事务单据：必须带 dept_id，接入 @DataScope）
DROP TABLE IF EXISTS biz_safety_exam;
CREATE TABLE biz_safety_exam (
    exam_id            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '考试ID',
    exam_name          VARCHAR(100) NOT NULL                 COMMENT '考试名称',
    paper_id           BIGINT       NOT NULL                 COMMENT '试卷ID',
    paper_name         VARCHAR(100)                          COMMENT '试卷名称（冗余）',
    record_id          BIGINT                                COMMENT '关联培训记录ID（biz_safety_training_record，可空）',
    start_time         DATETIME     NOT NULL                 COMMENT '考试开始时间',
    end_time           DATETIME     NOT NULL                 COMMENT '考试结束时间（窗口截止）',
    exam_status        CHAR(1)      DEFAULT '0'              COMMENT '考试状态（字典 safety_exam_exam_status）',
    publish_by         VARCHAR(64)                           COMMENT '发布人',
    publish_time       DATETIME                              COMMENT '发布时间',
    dept_id            BIGINT                                COMMENT '归属部门ID（数据权限，创建时自动填充）',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (exam_id),
    KEY idx_paper (paper_id),
    KEY idx_record (record_id),
    KEY idx_status (exam_status),
    KEY idx_start_end (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试场次表';

-- 6. 考生考试记录表（人员归属型：dept_id + user_id，接入 @DataScope）
DROP TABLE IF EXISTS biz_safety_exam_record;
CREATE TABLE biz_safety_exam_record (
    record_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '考试记录ID',
    exam_id            BIGINT       NOT NULL                 COMMENT '考试ID',
    paper_id           BIGINT       NOT NULL                 COMMENT '试卷ID',
    user_id            BIGINT       NOT NULL                 COMMENT '考生用户ID（关联sys_user）',
    user_name          VARCHAR(128)                          COMMENT '考生姓名（冗余）',
    worker_id          BIGINT                                COMMENT '关联员工ID（biz_safety_worker，可空）',
    attendee_id        BIGINT                                COMMENT '关联培训参训人ID（biz_safety_training_attendee，可空）',
    start_time         DATETIME                              COMMENT '本人开始答题时间',
    submit_time        DATETIME                              COMMENT '提交时间',
    used_seconds       INT                                   COMMENT '实际用时（秒）',
    submit_type        CHAR(1)                               COMMENT '提交方式（字典 safety_exam_submit_type）',
    total_score        DECIMAL(5,1)                          COMMENT '总得分',
    is_pass            CHAR(1)                               COMMENT '是否合格（0不合格 1合格）',
    judge_status       CHAR(1)      DEFAULT '0'              COMMENT '判分状态（0未交卷 1已自动判分）',
    switch_count       INT          DEFAULT 0                COMMENT '切屏次数',
    exam_result        CHAR(1)      DEFAULT '0'              COMMENT '考生状态（字典 safety_exam_result）',
    dept_id            BIGINT                                COMMENT '归属部门ID（数据权限）',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (record_id),
    UNIQUE KEY uk_exam_user (exam_id, user_id),
    KEY idx_user (user_id),
    KEY idx_exam (exam_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考生考试记录表';

-- 7. 答题明细表（追溯数据，随考试记录权限）
DROP TABLE IF EXISTS biz_safety_exam_answer;
CREATE TABLE biz_safety_exam_answer (
    answer_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '答题明细ID',
    record_id          BIGINT       NOT NULL                 COMMENT '考试记录ID',
    paper_question_id  BIGINT       NOT NULL                 COMMENT '试卷题目明细ID',
    user_answer        VARCHAR(20)                           COMMENT '考生答案（如 A / ABD；空=未答）',
    is_correct         CHAR(1)                               COMMENT '是否正确（1正确 0错误）',
    score              DECIMAL(4,1) DEFAULT 0                COMMENT '本题得分',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (answer_id),
    UNIQUE KEY uk_record_question (record_id, paper_question_id),
    KEY idx_record (record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试答题明细表';

-- =============================================
-- 二、字典类型
-- =============================================
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('考试题型', 'safety_exam_question_type', '0', 'admin', NOW(), '考试题目题型'),
('考试题目难度', 'safety_exam_difficulty', '0', 'admin', NOW(), '考试题目难度等级'),
('试卷状态', 'safety_exam_paper_status', '0', 'admin', NOW(), '考试试卷状态'),
('考试状态', 'safety_exam_exam_status', '0', 'admin', NOW(), '考试场次状态'),
('考生考试状态', 'safety_exam_result', '0', 'admin', NOW(), '考生在考试中的状态'),
('考试提交方式', 'safety_exam_submit_type', '0', 'admin', NOW(), '考生交卷方式');

-- =============================================
-- 三、字典数据
-- =============================================
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
-- 考试题型
(NULL, 1, '单选题', '1', 'safety_exam_question_type', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '多选题', '2', 'safety_exam_question_type', '', 'warning', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '判断题', '3', 'safety_exam_question_type', '', 'success', 'N', '0', 'admin', NOW(), ''),

-- 考试题目难度
(NULL, 1, '简单', '1', 'safety_exam_difficulty', '', 'info', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '中等', '2', 'safety_exam_difficulty', '', 'primary', 'Y', '0', 'admin', NOW(), ''),
(NULL, 3, '困难', '3', 'safety_exam_difficulty', '', 'danger', 'N', '0', 'admin', NOW(), ''),

-- 试卷状态
(NULL, 1, '草稿', '0', 'safety_exam_paper_status', '', 'info', 'Y', '0', 'admin', NOW(), ''),
(NULL, 2, '已发布', '1', 'safety_exam_paper_status', '', 'success', 'N', '0', 'admin', NOW(), '发布后题目集合冻结'),
(NULL, 3, '停用', '2', 'safety_exam_paper_status', '', 'danger', 'N', '0', 'admin', NOW(), ''),

-- 考试状态
(NULL, 1, '草稿', '0', 'safety_exam_exam_status', '', 'info', 'Y', '0', 'admin', NOW(), ''),
(NULL, 2, '已发布', '1', 'safety_exam_exam_status', '', 'primary', 'N', '0', 'admin', NOW(), '未到开始时间'),
(NULL, 3, '考试中', '2', 'safety_exam_exam_status', '', 'warning', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '已结束', '3', 'safety_exam_exam_status', '', 'success', 'N', '0', 'admin', NOW(), ''),
(NULL, 5, '已撤回', '4', 'safety_exam_exam_status', '', 'danger', 'N', '0', 'admin', NOW(), '仅未开始的场次可撤回'),

-- 考生考试状态
(NULL, 1, '未开始', '0', 'safety_exam_result', '', 'info', 'Y', '0', 'admin', NOW(), ''),
(NULL, 2, '进行中', '1', 'safety_exam_result', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '已交卷', '2', 'safety_exam_result', '', 'success', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '缺考', '3', 'safety_exam_result', '', 'danger', 'N', '0', 'admin', NOW(), '窗口结束无答题记录'),
(NULL, 5, '已判分', '4', 'safety_exam_result', '', 'success', 'N', '0', 'admin', NOW(), '成绩已回写'),

-- 考试提交方式
(NULL, 1, '手动提交', '1', 'safety_exam_submit_type', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '超时自动提交', '2', 'safety_exam_submit_type', '', 'warning', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '切屏强制交卷', '3', 'safety_exam_submit_type', '', 'danger', 'N', '0', 'admin', NOW(), '切屏超限自动交卷');

-- =============================================
-- 四、菜单权限（教育培训下新建「考试管理」二级目录，置于学时统计之前；权限域独立为 safety:exam:*）
-- =============================================

-- 获取教育培训目录ID
SELECT @trainingMenuId := menu_id FROM sys_menu WHERE menu_name = '教育培训' AND parent_id IN (SELECT menu_id FROM sys_menu WHERE menu_name = '安全生产管理' AND parent_id = 0);

-- ---- 「考试管理」二级目录（教育培训下排序6，位于学时统计之前） ----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '考试管理', @trainingMenuId, 6, 'exam', 'M', '0', '0', '', 'form', 'admin', NOW(), '考试功能子目录'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '考试管理' AND parent_id = @trainingMenuId AND menu_type = 'M');
SELECT @examDirMenuId := menu_id FROM sys_menu WHERE menu_name = '考试管理' AND parent_id = @trainingMenuId AND menu_type = 'M';

-- 学时统计顺移至考试管理之后（存在才更新）
UPDATE sys_menu SET order_num = 7 WHERE menu_name = '学时统计' AND parent_id = @trainingMenuId;

-- ---- 题库管理（考试管理下排序1） ----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('题库管理', @examDirMenuId, 1, 'examQuestion', 'safety/exam/question/index', 1, 0, 'C', '0', '0', 'safety:exam:question:list', 'education', 'admin', NOW(), '考试题库管理菜单');
SET @questionMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time, remark) VALUES
('题库查询', @questionMenuId, 1, '#', 'F', '0', '0', 'safety:exam:question:query',  'admin', NOW(), ''),
('题库新增', @questionMenuId, 2, '#', 'F', '0', '0', 'safety:exam:question:add',    'admin', NOW(), ''),
('题库修改', @questionMenuId, 3, '#', 'F', '0', '0', 'safety:exam:question:edit',   'admin', NOW(), ''),
('题库删除', @questionMenuId, 4, '#', 'F', '0', '0', 'safety:exam:question:remove', 'admin', NOW(), ''),
('题库导入', @questionMenuId, 5, '#', 'F', '0', '0', 'safety:exam:question:import', 'admin', NOW(), ''),
('题库导出', @questionMenuId, 6, '#', 'F', '0', '0', 'safety:exam:question:export', 'admin', NOW(), '');

-- ---- 试卷管理（考试管理下排序2） ----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('试卷管理', @examDirMenuId, 2, 'examPaper', 'safety/exam/paper/index', 1, 0, 'C', '0', '0', 'safety:exam:paper:list', 'form', 'admin', NOW(), '考试试卷管理菜单');
SET @paperMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time, remark) VALUES
('试卷查询', @paperMenuId, 1, '#', 'F', '0', '0', 'safety:exam:paper:query',   'admin', NOW(), ''),
('试卷新增', @paperMenuId, 2, '#', 'F', '0', '0', 'safety:exam:paper:add',     'admin', NOW(), ''),
('试卷修改', @paperMenuId, 3, '#', 'F', '0', '0', 'safety:exam:paper:edit',    'admin', NOW(), ''),
('试卷删除', @paperMenuId, 4, '#', 'F', '0', '0', 'safety:exam:paper:remove',  'admin', NOW(), ''),
('试卷发布', @paperMenuId, 5, '#', 'F', '0', '0', 'safety:exam:paper:publish', 'admin', NOW(), '发布后题目集合冻结');

-- ---- 考试场次（考试管理下排序3） ----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('考试场次', @examDirMenuId, 3, 'examManage', 'safety/exam/manage/index', 1, 0, 'C', '0', '0', 'safety:exam:list', 'date-range', 'admin', NOW(), '考试场次管理菜单');
SET @examMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time, remark) VALUES
('考试查询',     @examMenuId, 1, '#', 'F', '0', '0', 'safety:exam:query',    'admin', NOW(), ''),
('考试新增',     @examMenuId, 2, '#', 'F', '0', '0', 'safety:exam:add',      'admin', NOW(), ''),
('考试修改',     @examMenuId, 3, '#', 'F', '0', '0', 'safety:exam:edit',     'admin', NOW(), ''),
('考试删除',     @examMenuId, 4, '#', 'F', '0', '0', 'safety:exam:remove',   'admin', NOW(), ''),
('考试发布',     @examMenuId, 5, '#', 'F', '0', '0', 'safety:exam:publish',  'admin', NOW(), ''),
('考试强制结束', @examMenuId, 6, '#', 'F', '0', '0', 'safety:exam:forceEnd', 'admin', NOW(), ''),
('成绩导出',     @examMenuId, 7, '#', 'F', '0', '0', 'safety:exam:export',   'admin', NOW(), ''),
('成绩统计',     @examMenuId, 8, '#', 'F', '0', '0', 'safety:exam:stats',    'admin', NOW(), ''),
('成绩调分',     @examMenuId, 9, '#', 'F', '0', '0', 'safety:exam:score:edit', 'admin', NOW(), '人工调分留痕');

-- ---- 我的考试（考生端，考试管理下排序4，需授权给普通员工角色） ----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('我的考试', @examDirMenuId, 4, 'examTake', 'safety/exam/take/index', 1, 0, 'C', '0', '0', 'safety:exam:myList', 'checkbox', 'admin', NOW(), '考生端我的考试菜单');
SET @takeMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time, remark) VALUES
('在线答题', @takeMenuId, 1, '#', 'F', '0', '0', 'safety:exam:take', 'admin', NOW(), '进入考试/答题/提交/查分');

-- =============================================
-- 五、角色授权
-- =============================================
-- 管理员角色（role_id=2，与 safety_all.sql 口径一致）分配全部考试权限（含考试管理目录）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu_id FROM sys_menu WHERE perms LIKE 'safety:exam:%'
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 2);

-- 普通员工角色如需在线考试，请在【系统管理→角色管理】为对应角色勾选
-- 「教育培训→我的考试」菜单及其「在线答题」按钮权限

-- =============================================
-- 六、定时任务（考试状态兜底：状态流转 + 缺考标记，幂等可重复）
-- =============================================
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
SELECT '考试状态兜底任务', 'DEFAULT', 'safetyExamTask.run', '0 * * * * ?', '3', '1', '0', 'admin', NOW(), '场次状态流转与缺考标记（幂等），建议每分钟'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_job WHERE invoke_target = 'safetyExamTask.run');
-- =====================================================================
-- 安全生产模块-考试功能 P1 增量升级脚本（FR-7/8/10/11/12/13）
-- 依赖：safety_exam_all.sql（P0 全量脚本）已执行
-- 幂等性：可重复执行；新增列通过 information_schema 判断
-- 执行库：ry-vue
-- =====================================================================

-- ---------------------------------------------------------------------
-- 一、biz_safety_exam 增加补考相关字段（FR-8 补考管理）
--   retake_from_id  补考来源场次ID（NULL=非补考）
--   retake_count    补考代数（0=首次考试，1=第1次补考...）
--   score_strategy  成绩覆盖策略（1=取最高 2=取最新，回写参训人时生效）
-- ---------------------------------------------------------------------
SET @col_exists := (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_safety_exam' AND column_name = 'retake_from_id');
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE biz_safety_exam ADD COLUMN retake_from_id bigint NULL COMMENT ''补考来源场次ID'' AFTER record_id',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_safety_exam' AND column_name = 'retake_count');
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE biz_safety_exam ADD COLUMN retake_count int NOT NULL DEFAULT 0 COMMENT ''补考代数（0=首次考试）'' AFTER retake_from_id',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_safety_exam' AND column_name = 'score_strategy');
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE biz_safety_exam ADD COLUMN score_strategy char(1) NULL DEFAULT ''2'' COMMENT ''成绩覆盖策略（1取最高 2取最新）'' AFTER retake_count',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- 二、交卷方式字典补充「无操作锁卷」（FR-13 答题页基础防护）
-- ---------------------------------------------------------------------
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '无操作锁卷', '4', 'safety_exam_submit_type', NULL, 'warning', 'N', '0', 'admin', NOW(), 'FR-13：无操作超时自动锁卷交卷'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_exam_submit_type' AND dict_value = '4');
-- =====================================================================
-- 安全生产模块-考试功能 增量升级：考生来源双模式
--   1) biz_safety_exam 增加 attendee_source（1=来自培训 2=手动添加）
--   2) 新增 biz_safety_exam_user 手动考试名单表
-- 依赖：safety_exam_all.sql 已执行
-- 幂等性：可重复执行；新增列通过 information_schema 判断
-- 执行库：ry-vue
-- =====================================================================

-- 一、biz_safety_exam 增加考生来源字段
SET @col_exists := (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_safety_exam' AND column_name = 'attendee_source');
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE biz_safety_exam ADD COLUMN attendee_source char(1) NOT NULL DEFAULT ''1'' COMMENT ''考生来源（1=来自培训 2=手动添加）'' AFTER record_id',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 二、手动考试名单表
CREATE TABLE IF NOT EXISTS biz_safety_exam_user (
    exam_user_id       BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    exam_id            BIGINT       NOT NULL                 COMMENT '考试场次ID（biz_safety_exam）',
    user_id            BIGINT       NOT NULL                 COMMENT '系统用户ID（sys_user）',
    user_name          VARCHAR(64)                           COMMENT '用户显示名（冗余）',
    dept_id            BIGINT                                COMMENT '归属部门ID（数据权限）',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    PRIMARY KEY (exam_user_id),
    UNIQUE KEY uk_exam_user (exam_id, user_id),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试手动添加人员表';
-- ============================================================
-- 食品行业安全考试题库种子数据（幂等，可重复执行）
-- 内容：1 个一级分类 + 5 个二级分类 + 29 道题（单选11/多选4/判断14）
-- 判重规则：分类按名称+父级，题目按题干 content
-- ============================================================

-- 1. 一级分类：食品安全生产
INSERT INTO biz_safety_exam_category (parent_id, category_name, order_num, status, del_flag, create_by, create_time)
SELECT 0, '食品安全生产', 1, '0', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_category WHERE category_name = '食品安全生产' AND parent_id = 0 AND del_flag = '0');

SET @food_root := (SELECT category_id FROM biz_safety_exam_category WHERE category_name = '食品安全生产' AND parent_id = 0 AND del_flag = '0' LIMIT 1);

-- 2. 二级分类（幂等）
INSERT INTO biz_safety_exam_category (parent_id, category_name, order_num, status, del_flag, create_by, create_time)
SELECT @food_root, '食品安全法规', 1, '0', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_category WHERE category_name = '食品安全法规' AND parent_id = @food_root AND del_flag = '0');

INSERT INTO biz_safety_exam_category (parent_id, category_name, order_num, status, del_flag, create_by, create_time)
SELECT @food_root, '卫生与消毒规范', 2, '0', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_category WHERE category_name = '卫生与消毒规范' AND parent_id = @food_root AND del_flag = '0');

INSERT INTO biz_safety_exam_category (parent_id, category_name, order_num, status, del_flag, create_by, create_time)
SELECT @food_root, '仓储与物料管理', 3, '0', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_category WHERE category_name = '仓储与物料管理' AND parent_id = @food_root AND del_flag = '0');

INSERT INTO biz_safety_exam_category (parent_id, category_name, order_num, status, del_flag, create_by, create_time)
SELECT @food_root, '添加剂与质量管理', 4, '0', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_category WHERE category_name = '添加剂与质量管理' AND parent_id = @food_root AND del_flag = '0');

INSERT INTO biz_safety_exam_category (parent_id, category_name, order_num, status, del_flag, create_by, create_time)
SELECT @food_root, '消防与作业安全', 5, '0', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_category WHERE category_name = '消防与作业安全' AND parent_id = @food_root AND del_flag = '0');

SET @cat_law := (SELECT category_id FROM biz_safety_exam_category WHERE category_name = '食品安全法规' AND parent_id = @food_root AND del_flag = '0' LIMIT 1);
SET @cat_hyg := (SELECT category_id FROM biz_safety_exam_category WHERE category_name = '卫生与消毒规范' AND parent_id = @food_root AND del_flag = '0' LIMIT 1);
SET @cat_wh  := (SELECT category_id FROM biz_safety_exam_category WHERE category_name = '仓储与物料管理' AND parent_id = @food_root AND del_flag = '0' LIMIT 1);
SET @cat_qa  := (SELECT category_id FROM biz_safety_exam_category WHERE category_name = '添加剂与质量管理' AND parent_id = @food_root AND del_flag = '0' LIMIT 1);
SET @cat_fire:= (SELECT category_id FROM biz_safety_exam_category WHERE category_name = '消防与作业安全' AND parent_id = @food_root AND del_flag = '0' LIMIT 1);

-- 3. 题目（幂等：按题干判重）
-- 语法：INSERT INTO ... SELECT ... FROM DUAL WHERE NOT EXISTS (...)

-- ============ 食品安全法规 ============
-- 单选1（简单）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '从事食品生产活动，应当依法取得（　）。',
'["营业执照","食品生产许可证","卫生登记证","安全生产许可证"]', 'B',
'《食品安全法》规定，从事食品生产活动应当依法取得食品生产许可证。',
@cat_law, '食品安全法规', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '从事食品生产活动，应当依法取得（　）。' AND del_flag = '0');

-- 单选2（中等）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '食品生产许可证的有效期为（　）年。',
'["3","5","8","10"]', 'B',
'《食品生产许可管理办法》规定，食品生产许可证有效期为5年。',
@cat_law, '食品安全法规', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '食品生产许可证的有效期为（　）年。' AND del_flag = '0');

-- 单选3（困难）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '企业发现生产的食品不符合食品安全标准时，正确的做法是（　）。',
'["继续生产，降低损失","立即停止生产、召回问题食品并按规定报告","降价促销尽快处理","更换包装后继续销售"]', 'B',
'应立即停止生产、主动召回，并按规定向监管部门报告，防止危害扩大。',
@cat_law, '食品安全法规', '3', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '企业发现生产的食品不符合食品安全标准时，正确的做法是（　）。' AND del_flag = '0');

-- 单选4（中等）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '发生食品安全事故后，涉事企业应当（　）。',
'["立即采取措施防止事故扩大，并按规定及时报告","隐瞒不报，内部消化处理","等事故平息后再向上级汇报","只通知相关客户即可"]', 'A',
'发生食品安全事故的单位应当立即处置、防止事故扩大，并按规定及时向监管部门报告，不得隐瞒。',
@cat_law, '食品安全法规', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '发生食品安全事故后，涉事企业应当（　）。' AND del_flag = '0');

-- 多选1（中等）ABCD
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '2', '食品生产企业应当建立的食品安全管理制度包括（　）。',
'["食品安全自查制度","从业人员健康管理制度","进货查验记录制度","不安全食品召回制度"]', 'ABCD',
'以上四项均为《食品安全法》要求食品生产经营者建立的食品安全管理制度。',
@cat_law, '食品安全法规', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '食品生产企业应当建立的食品安全管理制度包括（　）。' AND del_flag = '0');

-- 判断1（简单）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '从业人员应当取得健康证明后方可从事接触直接入口食品的工作。',
'["正确","错误"]', '对',
'《食品安全法》规定，从事接触直接入口食品工作的从业人员应当每年进行健康检查，取得健康证明后方可上岗。',
@cat_law, '食品安全法规', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '从业人员应当取得健康证明后方可从事接触直接入口食品的工作。' AND del_flag = '0');

-- ============ 卫生与消毒规范 ============
-- 单选5（简单）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '接触直接入口食品的人员正确的洗手消毒做法是（　）。',
'["用清水冲一下即可","按规程洗手后使用消毒液消毒并烘干双手","佩戴手套后可以不洗手","洗完手用衣袖擦干"]', 'B',
'应按规程洗手并使用消毒液消毒，防止手部污染直接入口食品。',
@cat_hyg, '卫生与消毒规范', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '接触直接入口食品的人员正确的洗手消毒做法是（　）。' AND del_flag = '0');

-- 单选6（中等）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '进入食品生产车间前的正确做法是（　）。',
'["直接进入车间开始作业","更换工作服、佩戴口罩和工作帽，洗手消毒后经消毒通道进入","穿便服快速进入","只需简单洗手即可进入"]', 'B',
'进入车间应严格执行更衣、戴口罩帽子、洗手消毒等卫生程序，防止人员带入污染。',
@cat_hyg, '卫生与消毒规范', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '进入食品生产车间前的正确做法是（　）。' AND del_flag = '0');

-- 单选7（中等）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '生产车间使用清洗消毒剂的要求是（　）。',
'["任意采购价格便宜的即可","使用符合要求的食品级清洗消毒产品，按规定浓度配制并专柜存放、专人管理","与食品原料放在一起方便取用","浓度越高消毒效果越好，无需按比例配制"]', 'B',
'清洗消毒剂应符合相关标准，按规定浓度配制，专柜存放、专人管理，防止误用和污染食品。',
@cat_hyg, '卫生与消毒规范', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '生产车间使用清洗消毒剂的要求是（　）。' AND del_flag = '0');

-- 多选2（中等）ABCD
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '2', '进入车间人员的个人卫生要求包括（　）。',
'["持有效健康证上岗","穿戴清洁的工作服、帽，头发不外露","不得佩戴首饰手表，不留长指甲、不涂指甲油","不得在车间内吸烟和饮食"]', 'ABCD',
'以上均为食品生产人员基本的个人卫生要求，目的是防止人员对食品造成污染。',
@cat_hyg, '卫生与消毒规范', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '进入车间人员的个人卫生要求包括（　）。' AND del_flag = '0');

-- 判断2（简单）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '患有痢疾、伤寒、甲型病毒性肝炎等消化道传染病的人员，不得从事接触直接入口食品的工作。',
'["正确","错误"]', '对',
'《食品安全法》明确规定，患有国务院卫生行政部门规定的有碍食品安全疾病的人员，不得从事接触直接入口食品的工作。',
@cat_hyg, '卫生与消毒规范', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '患有痢疾、伤寒、甲型病毒性肝炎等消化道传染病的人员，不得从事接触直接入口食品的工作。' AND del_flag = '0');

-- 判断3（简单）错
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '为了节省时间，工作人员可以穿着工作服直接进出卫生间。',
'["正确","错误"]', '错',
'进出卫生间必须脱去工作服，回来后重新洗手消毒，防止交叉污染。',
@cat_hyg, '卫生与消毒规范', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '为了节省时间，工作人员可以穿着工作服直接进出卫生间。' AND del_flag = '0');

-- 判断4（简单）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '直接接触食品的设备、工具和容器应按规程定期清洗消毒，并如实记录。',
'["正确","错误"]', '对',
'食品接触面应定期清洗消毒并记录，保证卫生状态可追溯。',
@cat_hyg, '卫生与消毒规范', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '直接接触食品的设备、工具和容器应按规程定期清洗消毒，并如实记录。' AND del_flag = '0');

-- ============ 仓储与物料管理 ============
-- 单选8（中等）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '仓库内物料码放一般要求离地面不低于（　）厘米、离墙面不低于（　）厘米。',
'["5、10","10、30","30、50","50、50"]', 'B',
'通行仓储卫生要求：物料离地不低于10cm、离墙不低于30cm，便于清洁、通风和检查。',
@cat_wh, '仓储与物料管理', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '仓库内物料码放一般要求离地面不低于（　）厘米、离墙面不低于（　）厘米。' AND del_flag = '0');

-- 多选3（中等）ABC
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '2', '食品仓库日常管理的基本要求包括（　）。',
'["分区分类存放，离墙离地","定期检查记录温湿度","先进先出，账物相符","与有毒有害化学品混放，提高装卸效率"]', 'ABC',
'仓库应分区分类、离墙离地存放，定期记录温湿度，执行先进先出；严禁与有毒有害物品混放。',
@cat_wh, '仓储与物料管理', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '食品仓库日常管理的基本要求包括（　）。' AND del_flag = '0');

-- 判断5（简单）错
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '原料、辅料、包装材料可以与农药、清洁剂等化学品存放在同一仓库。',
'["正确","错误"]', '错',
'化学品必须单独存放并明显标识，与食品原料、辅料、包装材料严格隔离，防止化学污染。',
@cat_wh, '仓储与物料管理', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '原料、辅料、包装材料可以与农药、清洁剂等化学品存放在同一仓库。' AND del_flag = '0');

-- 判断6（简单）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '物料发放应遵循先进先出的原则。',
'["正确","错误"]', '对',
'先进先出可防止原料过期变质，保证物料在保质期内使用。',
@cat_wh, '仓储与物料管理', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '物料发放应遵循先进先出的原则。' AND del_flag = '0');

-- 判断7（中等）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '仓库发现霉变或超过保质期的原料，应立即隔离存放并按规定处理，不得投入生产。',
'["正确","错误"]', '对',
'霉变、过期原料存在食品安全隐患，必须隔离标识并按规定处理，严禁流入生产环节。',
@cat_wh, '仓储与物料管理', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '仓库发现霉变或超过保质期的原料，应立即隔离存放并按规定处理，不得投入生产。' AND del_flag = '0');

-- ============ 添加剂与质量管理 ============
-- 单选9（中等）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '食品添加剂的使用应当符合（　）的要求。',
'["GB 2760《食品安全国家标准 食品添加剂使用标准》","GB 14881《食品生产通用卫生规范》","GB 7718《预包装食品标签通则》","企业内部自定标准"]', 'A',
'GB 2760规定了食品添加剂的允许品种、使用范围和最大使用量，是添加剂使用的强制标准。',
@cat_qa, '添加剂与质量管理', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '食品添加剂的使用应当符合（　）的要求。' AND del_flag = '0');

-- 判断8（简单）错
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '为了改善产品口感，食品添加剂可以超范围、超限量使用。',
'["正确","错误"]', '错',
'食品添加剂必须严格按照GB 2760规定的品种、范围和限量使用，超范围超限量使用属于违法行为。',
@cat_qa, '添加剂与质量管理', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '为了改善产品口感，食品添加剂可以超范围、超限量使用。' AND del_flag = '0');

-- 判断9（简单）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '企业应按规定对产品进行出厂检验，检验合格后方可出厂销售。',
'["正确","错误"]', '对',
'《食品安全法》要求食品生产企业对其产品进行检验，合格后方可出厂销售。',
@cat_qa, '添加剂与质量管理', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '企业应按规定对产品进行出厂检验，检验合格后方可出厂销售。' AND del_flag = '0');

-- 判断10（困难）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '食品生产过程的相关记录和凭证，保存期限不得少于产品保质期满后六个月；没有明确保质期的，保存期限不得少于二年。',
'["正确","错误"]', '对',
'《食品安全法》规定，记录和凭证保存期限不得少于产品保质期满后六个月；没有明确保质期的，保存期限不得少于二年。',
@cat_qa, '添加剂与质量管理', '3', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '食品生产过程的相关记录和凭证，保存期限不得少于产品保质期满后六个月；没有明确保质期的，保存期限不得少于二年。' AND del_flag = '0');

-- ============ 消防与作业安全 ============
-- 单选10（简单）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '使用干粉灭火器灭火时，应站在（　），将喷嘴对准火焰（　）喷射。',
'["上风方向、根部","下风方向、上部","上风方向、上部","任意方向、中部"]', 'A',
'应站在上风方向，对准火焰根部喷射，才能有效灭火并保障自身安全。',
@cat_fire, '消防与作业安全', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '使用干粉灭火器灭火时，应站在（　），将喷嘴对准火焰（　）喷射。' AND del_flag = '0');

-- 单选11（中等）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '车间内驾驶叉车的安全要求，正确的是（　）。',
'["速度不限，尽快完成搬运","限速行驶，禁止载人，转弯减速鸣笛","可以顺路搭载同事","倒车时无需观察后方"]', 'B',
'叉车应限速行驶、禁止载人，转弯和通道口减速鸣笛，防止碰撞伤人。',
@cat_fire, '消防与作业安全', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '车间内驾驶叉车的安全要求，正确的是（　）。' AND del_flag = '0');

-- 多选4（简单）ABCD
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '2', '灭火的基本方法包括（　）。',
'["冷却灭火法","窒息灭火法","隔离灭火法","化学抑制灭火法"]', 'ABCD',
'灭火的四种基本方法：冷却、窒息、隔离、化学抑制，实际扑救常组合使用。',
@cat_fire, '消防与作业安全', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '灭火的基本方法包括（　）。' AND del_flag = '0');

-- 判断11（简单）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '灭火时应将灭火剂喷射到火焰根部，而不是火焰上部。',
'["正确","错误"]', '对',
'对准火焰根部喷射才能隔绝燃烧源头，喷向上部无法有效灭火。',
@cat_fire, '消防与作业安全', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '灭火时应将灭火剂喷射到火焰根部，而不是火焰上部。' AND del_flag = '0');

-- 判断12（中等）错
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '电动叉车可以停放在存放易燃易爆物品的仓库内充电。',
'["正确","错误"]', '错',
'充电过程可能产生电火花，易燃易爆场所严禁充电作业，应在专用充电区域进行。',
@cat_fire, '消防与作业安全', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '电动叉车可以停放在存放易燃易爆物品的仓库内充电。' AND del_flag = '0');

-- 判断13（中等）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '电气设备发生火灾时，应先切断电源，再使用干粉或二氧化碳灭火器扑救，严禁用水直接扑救。',
'["正确","错误"]', '对',
'带电灭火有触电危险，必须先断电；水导电，严禁用水扑救电气火灾。',
@cat_fire, '消防与作业安全', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '电气设备发生火灾时，应先切断电源，再使用干粉或二氧化碳灭火器扑救，严禁用水直接扑救。' AND del_flag = '0');

-- 判断14（简单）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '员工发现食品安全隐患或安全事故隐患，应立即报告车间负责人或安全管理人员。',
'["正确","错误"]', '对',
'发现隐患及时报告是每名员工的职责，便于第一时间处置、防止事故扩大。',
@cat_fire, '消防与作业安全', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '员工发现食品安全隐患或安全事故隐患，应立即报告车间负责人或安全管理人员。' AND del_flag = '0');
-- 考试菜单授权: 普通角色(role_id=2, 与开发库一致)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, m.menu_id FROM sys_menu m
WHERE m.perms LIKE 'safety:exam%%' AND m.menu_type IN ('M','C','F')
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = 2 AND x.menu_id = m.menu_id);

-- ---------- PART 5: sys_module 模块开关 ----------
-- =============================================================================
-- 模块开关补齐脚本：决策支持（bi）、系统集成（integration）
-- 背景：决策支持（顶层菜单 9062263「决策支持」）与系统集成（顶层菜单
--       9063000「系统集成」）两个模块已上线，但 sys_module 未登记，导致
--       「系统管理 → 模块开关」页面缺少这两项，无法按模块停用/启用；
--       同时系统集成的 4 个定时任务 module_key 为空，即使登记模块也无法级联。
-- 适用：已执行过 module_switch_init_ryvue.sql 的环境（增量执行，幂等，可重复跑）
-- 执行：mysql -uroot -p 库名 < module_switch_add_bi_integration.sql
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 一、登记决策支持模块（幂等：已存在则跳过）
--     root_menu_id = 9062263（决策支持目录），级联范围含驾驶舱/指标字典/各业务看板
-- -----------------------------------------------------------------------------
INSERT INTO `sys_module` (`module_name`, `module_key`, `root_menu_id`, `status`, `is_core`, `order_num`, `create_by`, `create_time`, `remark`)
SELECT '决策支持', 'bi', 9062263, '0', '0', 27, 'admin', sysdate(), '决策支持模块（BI），级联菜单 9062263 及其子菜单、定时任务 module_key=bi'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_module` WHERE `module_key` = 'bi');

-- -----------------------------------------------------------------------------
-- 二、登记系统集成模块（幂等）
--     root_menu_id = 9063000（系统集成目录），级联范围含钉钉集成 / 企业微信集成
-- -----------------------------------------------------------------------------
INSERT INTO `sys_module` (`module_name`, `module_key`, `root_menu_id`, `status`, `is_core`, `order_num`, `create_by`, `create_time`, `remark`)
SELECT '系统集成', 'integration', 9063000, '0', '0', 28, 'admin', sysdate(), '系统集成模块（钉钉/企业微信），级联菜单 9063000 及其子菜单、定时任务 module_key=integration'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_module` WHERE `module_key` = 'integration');

-- -----------------------------------------------------------------------------
-- 三、定时任务回填 module_key（级联停用/恢复依赖该字段，为空则不会被级联）
--     3.1 系统集成：钉钉通讯录/考勤同步、企业微信通讯录/考勤同步
--     3.2 决策支持：BI 指标快照/对账/阈值预警（新建时已写 bi，此处兜底历史数据）
-- -----------------------------------------------------------------------------
UPDATE `sys_job` SET `module_key` = 'integration'
 WHERE (`module_key` IS NULL OR `module_key` = '')
   AND (   `invoke_target` LIKE 'dingtalkAttendanceSyncTask%'
        OR `invoke_target` LIKE 'wecomAttendanceSyncTask%');

UPDATE `sys_job` SET `module_key` = 'bi'
 WHERE (`module_key` IS NULL OR `module_key` = '')
   AND `invoke_target` LIKE 'biMetricSnapshotTask%';

-- -----------------------------------------------------------------------------
-- 四、角色回填 module_key
--     说明：这两模块当前未设专属角色（菜单权限复用现有角色 + sys_role_menu 授权），
--           无角色级联对象，故默认不处理。后续若新增 bi_*/integration_* 角色，
--           可解除下方注释执行回填。
-- -----------------------------------------------------------------------------
-- UPDATE `sys_role` SET `module_key` = 'bi'          WHERE `role_key` LIKE 'bi\_%'          AND (`module_key` IS NULL OR `module_key` = '');
-- UPDATE `sys_role` SET `module_key` = 'integration' WHERE `role_key` LIKE 'integration\_%' AND (`module_key` IS NULL OR `module_key` = '');

-- -----------------------------------------------------------------------------
-- 五、执行结果复核
-- -----------------------------------------------------------------------------
-- 5.1 模块清单（应能看到 bi / integration，job_count 分别为 3 / 4）
SELECT m.module_id, m.module_name, m.module_key, m.root_menu_id, m.status, m.is_core, m.order_num,
       (SELECT COUNT(*) FROM `sys_role` r WHERE r.module_key = m.module_key AND r.del_flag = '0') AS role_count,
       (SELECT COUNT(*) FROM `sys_job` j WHERE j.module_key = m.module_key) AS job_count
  FROM `sys_module` m
 ORDER BY m.order_num;

-- 5.2 级联菜单范围（含目录、页面与按钮级权限，实际范围随菜单增减变化）
WITH RECURSIVE sub_menu AS (
  SELECT menu_id FROM `sys_menu` WHERE menu_id = 9062263
  UNION ALL
  SELECT cm.menu_id FROM `sys_menu` cm INNER JOIN sub_menu sm ON cm.parent_id = sm.menu_id
)
SELECT '决策支持(bi)' AS module_name, COUNT(*) AS cascade_menu_count FROM sub_menu;

WITH RECURSIVE sub_menu2 AS (
  SELECT menu_id FROM `sys_menu` WHERE menu_id = 9063000
  UNION ALL
  SELECT cm.menu_id FROM `sys_menu` cm INNER JOIN sub_menu2 sm ON cm.parent_id = sm.menu_id
)
SELECT '系统集成(integration)' AS module_name, COUNT(*) AS cascade_menu_count FROM sub_menu2;

-- 5.3 定时任务归属复核（应无 NULL）
SELECT job_id, job_name, invoke_target, module_key, status FROM `sys_job`
 WHERE invoke_target LIKE 'biMetricSnapshotTask%'
    OR invoke_target LIKE 'dingtalkAttendanceSyncTask%'
    OR invoke_target LIKE 'wecomAttendanceSyncTask%'
 ORDER BY job_id;

-- =============================================================================
-- 回滚脚本（仅注释备用，需要时手工解除注释执行）
-- 注意：回滚后这两个模块将从模块开关页面消失，其定时任务不再受模块级停用控制
-- =============================================================================
-- DELETE FROM `sys_module` WHERE `module_key` IN ('bi', 'integration');
-- UPDATE `sys_job` SET `module_key` = NULL
--  WHERE `invoke_target` LIKE 'dingtalkAttendanceSyncTask%'
--     OR `invoke_target` LIKE 'wecomAttendanceSyncTask%';

-- ---------- PART 6: 菜单角色授权补齐 ----------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT v.r, v.m FROM (
SELECT 100 r, 9063031 m UNION ALL SELECT 100 r, 9063050 m UNION ALL SELECT 100 r, 9063052 m UNION ALL SELECT 100 r, 9063053 m UNION ALL SELECT 100 r, 9063056 m UNION ALL SELECT 100 r, 9063058 m UNION ALL SELECT 100 r, 9063060 m UNION ALL SELECT 104 r, 9063031 m UNION ALL SELECT 104 r, 9063050 m UNION ALL SELECT 104 r, 9063052 m UNION ALL SELECT 104 r, 9063053 m UNION ALL SELECT 104 r, 9063054 m UNION ALL SELECT 104 r, 9063058 m UNION ALL SELECT 110 r, 9063031 m UNION ALL SELECT 110 r, 9063050 m UNION ALL SELECT 110 r, 9063056 m UNION ALL SELECT 111 r, 9063031 m UNION ALL SELECT 111 r, 9063050 m UNION ALL SELECT 111 r, 9063056 m UNION ALL SELECT 120 r, 9063031 m UNION ALL SELECT 120 r, 9063050 m UNION ALL SELECT 120 r, 9063057 m UNION ALL SELECT 121 r, 9063031 m UNION ALL SELECT 121 r, 9063050 m UNION ALL SELECT 121 r, 9063057 m UNION ALL SELECT 122 r, 9063031 m UNION ALL SELECT 122 r, 9063050 m UNION ALL SELECT 122 r, 9063057 m UNION ALL SELECT 123 r, 9063031 m UNION ALL SELECT 123 r, 9063050 m UNION ALL SELECT 123 r, 9063057 m UNION ALL SELECT 123 r, 9063058 m UNION ALL SELECT 124 r, 9063031 m UNION ALL SELECT 124 r, 9063050 m UNION ALL SELECT 124 r, 9063057 m UNION ALL SELECT 125 r, 9063031 m UNION ALL SELECT 125 r, 9063050 m UNION ALL SELECT 125 r, 9063057 m UNION ALL SELECT 126 r, 9063031 m UNION ALL SELECT 126 r, 9063050 m UNION ALL SELECT 126 r, 9063057 m UNION ALL SELECT 126 r, 9063058 m UNION ALL SELECT 200 r, 9063031 m UNION ALL SELECT 200 r, 9063050 m UNION ALL SELECT 200 r, 9063053 m UNION ALL SELECT 201 r, 9063031 m UNION ALL SELECT 202 r, 9063031 m UNION ALL SELECT 203 r, 9063031 m UNION ALL SELECT 300 r, 9063031 m UNION ALL SELECT 300 r, 9063050 m UNION ALL SELECT 300 r, 9063054 m UNION ALL SELECT 301 r, 9063031 m UNION ALL SELECT 301 r, 9063050 m UNION ALL SELECT 301 r, 9063054 m UNION ALL SELECT 302 r, 9063031 m UNION ALL SELECT 303 r, 9063031 m UNION ALL SELECT 304 r, 9063031 m UNION ALL SELECT 400 r, 9063031 m UNION ALL SELECT 400 r, 9063050 m UNION ALL SELECT 400 r, 9063052 m UNION ALL SELECT 401 r, 9063031 m UNION ALL SELECT 401 r, 9063050 m UNION ALL SELECT 401 r, 9063052 m UNION ALL SELECT 402 r, 9063031 m UNION ALL SELECT 402 r, 9063050 m UNION ALL SELECT 402 r, 9063052 m UNION ALL SELECT 403 r, 9063031 m UNION ALL SELECT 403 r, 9063050 m UNION ALL SELECT 403 r, 9063052 m UNION ALL SELECT 404 r, 9063031 m UNION ALL SELECT 404 r, 9063050 m UNION ALL SELECT 404 r, 9063052 m UNION ALL SELECT 500 r, 9063031 m UNION ALL SELECT 501 r, 9063031 m UNION ALL SELECT 502 r, 9063031 m UNION ALL SELECT 503 r, 9063031 m UNION ALL SELECT 504 r, 9063031 m UNION ALL SELECT 505 r, 9063031 m UNION ALL SELECT 600 r, 9063031 m UNION ALL SELECT 600 r, 9063050 m UNION ALL SELECT 600 r, 9063058 m UNION ALL SELECT 601 r, 9063031 m UNION ALL SELECT 601 r, 9063050 m UNION ALL SELECT 601 r, 9063058 m UNION ALL SELECT 602 r, 9063031 m UNION ALL SELECT 602 r, 9063050 m UNION ALL SELECT 602 r, 9063058 m UNION ALL SELECT 603 r, 9063031 m UNION ALL SELECT 610 r, 9063031 m UNION ALL SELECT 610 r, 9063050 m UNION ALL SELECT 610 r, 9063062 m UNION ALL SELECT 611 r, 9063031 m UNION ALL SELECT 611 r, 9063050 m UNION ALL SELECT 611 r, 9063062 m UNION ALL SELECT 612 r, 9063031 m UNION ALL SELECT 612 r, 9063050 m UNION ALL SELECT 612 r, 9063062 m
) v
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = v.r AND x.menu_id = v.m);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT v.r, v.m FROM (
SELECT 100 r, 9063031 m UNION ALL SELECT 104 r, 9063031 m UNION ALL SELECT 110 r, 9063031 m UNION ALL SELECT 111 r, 9063031 m UNION ALL SELECT 120 r, 9063031 m UNION ALL SELECT 121 r, 9063031 m UNION ALL SELECT 122 r, 9063031 m UNION ALL SELECT 123 r, 9063031 m UNION ALL SELECT 124 r, 9063031 m UNION ALL SELECT 125 r, 9063031 m UNION ALL SELECT 126 r, 9063031 m UNION ALL SELECT 200 r, 9063031 m UNION ALL SELECT 201 r, 9063031 m UNION ALL SELECT 202 r, 9063031 m UNION ALL SELECT 203 r, 9063031 m UNION ALL SELECT 300 r, 9063031 m UNION ALL SELECT 301 r, 9063031 m UNION ALL SELECT 302 r, 9063031 m UNION ALL SELECT 303 r, 9063031 m UNION ALL SELECT 304 r, 9063031 m UNION ALL SELECT 400 r, 9063031 m UNION ALL SELECT 401 r, 9063031 m UNION ALL SELECT 402 r, 9063031 m UNION ALL SELECT 403 r, 9063031 m UNION ALL SELECT 404 r, 9063031 m UNION ALL SELECT 500 r, 9063031 m UNION ALL SELECT 501 r, 9063031 m UNION ALL SELECT 502 r, 9063031 m UNION ALL SELECT 503 r, 9063031 m UNION ALL SELECT 504 r, 9063031 m UNION ALL SELECT 505 r, 9063031 m UNION ALL SELECT 600 r, 9063031 m UNION ALL SELECT 601 r, 9063031 m UNION ALL SELECT 602 r, 9063031 m UNION ALL SELECT 603 r, 9063031 m UNION ALL SELECT 610 r, 9063031 m UNION ALL SELECT 611 r, 9063031 m UNION ALL SELECT 612 r, 9063031 m UNION ALL SELECT 701 r, 9063031 m UNION ALL SELECT 702 r, 9063031 m UNION ALL SELECT 703 r, 9063031 m UNION ALL SELECT 100 r, 9063050 m UNION ALL SELECT 104 r, 9063050 m UNION ALL SELECT 110 r, 9063050 m UNION ALL SELECT 111 r, 9063050 m UNION ALL SELECT 120 r, 9063050 m UNION ALL SELECT 121 r, 9063050 m UNION ALL SELECT 122 r, 9063050 m UNION ALL SELECT 123 r, 9063050 m UNION ALL SELECT 124 r, 9063050 m UNION ALL SELECT 125 r, 9063050 m UNION ALL SELECT 126 r, 9063050 m UNION ALL SELECT 200 r, 9063050 m UNION ALL SELECT 300 r, 9063050 m UNION ALL SELECT 301 r, 9063050 m UNION ALL SELECT 400 r, 9063050 m UNION ALL SELECT 401 r, 9063050 m UNION ALL SELECT 402 r, 9063050 m UNION ALL SELECT 403 r, 9063050 m UNION ALL SELECT 404 r, 9063050 m UNION ALL SELECT 600 r, 9063050 m UNION ALL SELECT 601 r, 9063050 m UNION ALL SELECT 602 r, 9063050 m UNION ALL SELECT 610 r, 9063050 m UNION ALL SELECT 611 r, 9063050 m UNION ALL SELECT 612 r, 9063050 m UNION ALL SELECT 701 r, 9063050 m UNION ALL SELECT 702 r, 9063050 m UNION ALL SELECT 703 r, 9063050 m UNION ALL SELECT 100 r, 9063052 m UNION ALL SELECT 104 r, 9063052 m UNION ALL SELECT 400 r, 9063052 m UNION ALL SELECT 401 r, 9063052 m UNION ALL SELECT 402 r, 9063052 m UNION ALL SELECT 403 r, 9063052 m UNION ALL SELECT 404 r, 9063052 m UNION ALL SELECT 100 r, 9063053 m UNION ALL SELECT 104 r, 9063053 m UNION ALL SELECT 200 r, 9063053 m UNION ALL SELECT 104 r, 9063054 m UNION ALL SELECT 300 r, 9063054 m UNION ALL SELECT 301 r, 9063054 m UNION ALL SELECT 100 r, 9063056 m UNION ALL SELECT 110 r, 9063056 m UNION ALL SELECT 111 r, 9063056 m UNION ALL SELECT 120 r, 9063057 m UNION ALL SELECT 121 r, 9063057 m UNION ALL SELECT 122 r, 9063057 m UNION ALL SELECT 123 r, 9063057 m UNION ALL SELECT 124 r, 9063057 m UNION ALL SELECT 125 r, 9063057 m UNION ALL SELECT 126 r, 9063057 m UNION ALL SELECT 100 r, 9063058 m UNION ALL SELECT 104 r, 9063058 m UNION ALL SELECT 123 r, 9063058 m UNION ALL SELECT 126 r, 9063058 m UNION ALL SELECT 600 r, 9063058 m UNION ALL SELECT 601 r, 9063058 m UNION ALL SELECT 602 r, 9063058 m UNION ALL SELECT 701 r, 9063059 m UNION ALL SELECT 702 r, 9063059 m UNION ALL SELECT 703 r, 9063059 m UNION ALL SELECT 100 r, 9063060 m UNION ALL SELECT 610 r, 9063062 m UNION ALL SELECT 611 r, 9063062 m UNION ALL SELECT 612 r, 9063062 m
) v
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = v.r AND x.menu_id = v.m);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT v.r, v.m FROM (
SELECT 1 r, 9063100 m UNION ALL SELECT 1 r, 9063101 m UNION ALL SELECT 1 r, 9063102 m UNION ALL SELECT 1 r, 9063103 m UNION ALL SELECT 1 r, 9063104 m UNION ALL SELECT 1 r, 9063105 m UNION ALL SELECT 1 r, 9063106 m UNION ALL SELECT 1 r, 9063107 m UNION ALL SELECT 100 r, 9063102 m UNION ALL SELECT 104 r, 9063103 m UNION ALL SELECT 120 r, 9063100 m UNION ALL SELECT 120 r, 9063105 m UNION ALL SELECT 121 r, 9063100 m UNION ALL SELECT 121 r, 9063105 m UNION ALL SELECT 122 r, 9063100 m UNION ALL SELECT 122 r, 9063105 m UNION ALL SELECT 123 r, 9063100 m UNION ALL SELECT 123 r, 9063105 m UNION ALL SELECT 124 r, 9063100 m UNION ALL SELECT 124 r, 9063105 m UNION ALL SELECT 125 r, 9063100 m UNION ALL SELECT 125 r, 9063105 m UNION ALL SELECT 126 r, 9063100 m UNION ALL SELECT 126 r, 9063105 m UNION ALL SELECT 200 r, 9063103 m UNION ALL SELECT 203 r, 9063103 m UNION ALL SELECT 701 r, 9063101 m UNION ALL SELECT 702 r, 9063101 m UNION ALL SELECT 703 r, 9063101 m
) v
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = v.r AND x.menu_id = v.m);

-- ---------- 校验 ----------
SELECT 'NEW_MENUS' tag, COUNT(*) cnt FROM sys_menu WHERE menu_id BETWEEN 9063100 AND 9063107;
SELECT 'EXAM_TABLES' tag, COUNT(*) cnt FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name LIKE 'biz_safety_exam%';
SELECT 'INTEG_TABLES' tag, COUNT(*) cnt FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name LIKE 'integration%';
SELECT 'BI_TABLES' tag, COUNT(*) cnt FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name LIKE 'bi_metric%';



-- ---------- PART 7: BI 三级页面菜单同步(决策支持子树区间外43项, 与开发库整行同步: 改名/挂新目录, 幂等) ----------
UPDATE sys_menu SET menu_name='仓储统计分析', parent_id=9063052, order_num=1, path='statistics', component='wms/statistics/index', query='', route_name='WmsStatistics', is_frame=1, is_cache=0, menu_type='C', visible='0', status='0', perms='wms:statistics:list', icon='chart', remark='仓库管理统计报表' WHERE menu_id=2110;
UPDATE sys_menu SET menu_name='统计查询', parent_id=2110, order_num=1, path='', component='', query='', route_name='', is_frame=1, is_cache=0, menu_type='F', visible='0', status='1', perms='wms:statistics:query', icon='#', remark='' WHERE menu_id=2111;
UPDATE sys_menu SET menu_name='设备统计分析', parent_id=9063053, order_num=20, path='overview', component='dms/dashboard/index', query='', route_name='DmsDashboard', is_frame=1, is_cache=0, menu_type='C', visible='0', status='0', perms='dms:dashboard:list', icon='chart', remark='KPI看板' WHERE menu_id=3090;
UPDATE sys_menu SET menu_name='设备数据大屏', parent_id=9063053, order_num=21, path='screen', component='dms/equipment/screen', query='', route_name='DmsEquipmentScreen', is_frame=1, is_cache=1, menu_type='C', visible='0', status='0', perms='dms:equipment:query', icon='monitor', remark='设备全生命周期监控大屏' WHERE menu_id=3091;
UPDATE sys_menu SET menu_name='库存流水报表', parent_id=9063053, order_num=1, path='partflow', component='dms/partstock/index', query='', route_name='DmsPartFlow', is_frame=1, is_cache=0, menu_type='C', visible='0', status='0', perms='dms:partstock:list', icon='chart', remark='' WHERE menu_id=3108;
UPDATE sys_menu SET menu_name='库存查询-query', parent_id=3108, order_num=1, path='', component='', query='', route_name='', is_frame=1, is_cache=0, menu_type='F', visible='0', status='0', perms='dms:partstock:query', icon='#', remark='' WHERE menu_id=3115;
UPDATE sys_menu SET menu_name='库存导出', parent_id=3108, order_num=2, path='', component='', query='', route_name='', is_frame=1, is_cache=0, menu_type='F', visible='0', status='0', perms='dms:partstock:export', icon='#', remark='' WHERE menu_id=3116;
UPDATE sys_menu SET menu_name='记录删除', parent_id=3108, order_num=99, path='', component='', query='', route_name='', is_frame=1, is_cache=0, menu_type='F', visible='0', status='0', perms='dms:partstock:remove', icon='#', remark='备件出入库记录删除权限（入库/出库/流水共用）' WHERE menu_id=3124;
UPDATE sys_menu SET menu_name='营销统计分析', parent_id=9063054, order_num=22, path='dashboard', component='mk/dashboard/index', query='', route_name='MkDashboard', is_frame=1, is_cache=0, menu_type='C', visible='0', status='0', perms='marketing:report:view', icon='chart', remark='' WHERE menu_id=4120;
UPDATE sys_menu SET menu_name='销售人员分析', parent_id=9063054, order_num=1, path='salesperson', component='mk/salespersonReport/index', query=NULL, route_name='', is_frame=1, is_cache=0, menu_type='C', visible='0', status='0', perms='marketing:report:view', icon='chart', remark='销售人员多维度分析统计报表' WHERE menu_id=4156;
UPDATE sys_menu SET menu_name='经销商专项报表', parent_id=9063054, order_num=2, path='dealerReport', component='mk/dealerReport/index', query='', route_name='MkDealerReport', is_frame=1, is_cache=0, menu_type='C', visible='0', status='0', perms='marketing:dealerReport:list', icon='chart', remark='分经销商销量/返利/账龄/流向汇总' WHERE menu_id=4215;
UPDATE sys_menu SET menu_name='报表导出', parent_id=4215, order_num=1, path='', component='', query='', route_name='', is_frame=1, is_cache=0, menu_type='F', visible='0', status='0', perms='marketing:dealerReport:export', icon='#', remark='' WHERE menu_id=4216;
UPDATE sys_menu SET menu_name='采购统计分析', parent_id=9063055, order_num=1, path='pms-dashboard', component='pms/dashboard/index', query='', route_name='PmsDashboard', is_frame=1, is_cache=0, menu_type='C', visible='0', status='0', perms='pms:dashboard:view', icon='chart', remark='采购数据看板' WHERE menu_id=4461;
UPDATE sys_menu SET menu_name='质量统计分析', parent_id=9063057, order_num=26, path='dashboard', component='qms/dashboard/index', query=NULL, route_name='QmsDashboard', is_frame=1, is_cache=0, menu_type='C', visible='0', status='0', perms='qms:dashboard:view', icon='dashboard', remark='质量KPI看板' WHERE menu_id=4544;
UPDATE sys_menu SET menu_name='推广分析', parent_id=9063054, order_num=23, path='promotion', component='mk/promotionReport/index', query='', route_name='MkPromotionReport', is_frame=1, is_cache=0, menu_type='C', visible='0', status='0', perms='marketing:promotionReport:view', icon='dashboard', remark='' WHERE menu_id=4583;
UPDATE sys_menu SET menu_name='安全生产驾驶舱', parent_id=9063056, order_num=28, path='dashboard', component='safety/dashboard/index', query=NULL, route_name='SafetyDashboard', is_frame=1, is_cache=0, menu_type='C', visible='0', status='0', perms='safety:dashboard:list', icon='dashboard', remark='安全驾驶舱菜单' WHERE menu_id=4726;
UPDATE sys_menu SET menu_name='数据报表', parent_id=9063057, order_num=1, path='list', component='qms/report/index', query='', route_name='QmsReportList', is_frame=1, is_cache=0, menu_type='C', visible='0', status='0', perms='qms:report:list', icon='documentation', remark='质量统计报表' WHERE menu_id=4851;
UPDATE sys_menu SET menu_name='学时统计', parent_id=9063056, order_num=1, path='hours', component='safety/training/hours/index', query=NULL, route_name='', is_frame=1, is_cache=0, menu_type='C', visible='0', status='0', perms='safety:training:hours:list', icon='chart', remark='学时统计菜单' WHERE menu_id=4870;
UPDATE sys_menu SET menu_name='生产统计分析', parent_id=9063058, order_num=27, path='dashboard', component='mms/dashboard/index', query='', route_name='', is_frame=1, is_cache=0, menu_type='C', visible='0', status='0', perms='mms:dashboard:list', icon='dashboard', remark='' WHERE menu_id=5001;
UPDATE sys_menu SET menu_name='OEE分析', parent_id=9063058, order_num=1, path='oee', component='mms/oee/index', query='', route_name='', is_frame=1, is_cache=0, menu_type='C', visible='0', status='0', perms='mms:oee:list', icon='chart', remark='OEE设备效率分析' WHERE menu_id=5215;
UPDATE sys_menu SET menu_name='生产日报', parent_id=9063058, order_num=2, path='daily', component='mms/daily/index', query='', route_name='', is_frame=1, is_cache=0, menu_type='C', visible='0', status='0', perms='mms:daily:list', icon='documentation', remark='生产日报' WHERE menu_id=5216;
UPDATE sys_menu SET menu_name='工单完工分析', parent_id=9063058, order_num=3, path='finishAnalysis', component='mms/finishAnalysis/index', query='', route_name='', is_frame=1, is_cache=0, menu_type='C', visible='0', status='0', perms='mms:finishAnalysis:list', icon='chart', remark='工单完工分析报表' WHERE menu_id=5345;
UPDATE sys_menu SET menu_name='完工分析查询', parent_id=5345, order_num=1, path='', component='', query='', route_name='', is_frame=1, is_cache=0, menu_type='F', visible='0', status='0', perms='mms:finishAnalysis:query', icon='#', remark='' WHERE menu_id=5346;
UPDATE sys_menu SET menu_name='完工分析导出', parent_id=5345, order_num=2, path='', component='', query='', route_name='', is_frame=1, is_cache=0, menu_type='F', visible='0', status='0', perms='mms:finishAnalysis:export', icon='#', remark='' WHERE menu_id=5347;
UPDATE sys_menu SET menu_name='统计分析', parent_id=9063059, order_num=1, path='statistics', component='pdm/statistics/index', query='', route_name='PdmStatistics', is_frame=1, is_cache=0, menu_type='C', visible='0', status='0', perms='pdm:statistics:view', icon='chart', remark='产品设计统计分析菜单' WHERE menu_id=11440;
UPDATE sys_menu SET menu_name='统计导出', parent_id=11440, order_num=1, path='', component='', query='', route_name='', is_frame=1, is_cache=0, menu_type='F', visible='0', status='0', perms='pdm:statistics:export', icon='#', remark='' WHERE menu_id=11447;
UPDATE sys_menu SET menu_name='看板视图', parent_id=5001, order_num=1, path='', component='', query='', route_name='', is_frame=1, is_cache=0, menu_type='F', visible='0', status='0', perms='mms:dashboard:view', icon='#', remark='生产看板视图权限' WHERE menu_id=500108;
UPDATE sys_menu SET menu_name='OEE导出', parent_id=5215, order_num=2, path='', component='', query='', route_name='', is_frame=1, is_cache=0, menu_type='F', visible='0', status='0', perms='mms:oee:export', icon='#', remark='OEE分析导出' WHERE menu_id=521501;
UPDATE sys_menu SET menu_name='学时统计导出', parent_id=4870, order_num=1, path='#', component='', query=NULL, route_name='', is_frame=1, is_cache=0, menu_type='F', visible='0', status='0', perms='safety:training:hours:export', icon='#', remark='' WHERE menu_id=905403;
UPDATE sys_menu SET menu_name='用量统计', parent_id=9063061, order_num=1, path='usage', component='ai/usage/index', query=NULL, route_name='AiUsage', is_frame=1, is_cache=0, menu_type='C', visible='0', status='0', perms='ai:center:dashboard', icon='chart', remark='' WHERE menu_id=905791;
UPDATE sys_menu SET menu_name='AI报告初稿', parent_id=4851, order_num=100, path='', component=NULL, query=NULL, route_name='', is_frame=1, is_cache=0, menu_type='F', visible='0', status='0', perms='qms:ai:reportDraft', icon='#', remark='' WHERE menu_id=905865;
UPDATE sys_menu SET menu_name='AI生产日报', parent_id=5216, order_num=100, path='', component=NULL, query=NULL, route_name='', is_frame=1, is_cache=0, menu_type='F', visible='0', status='0', perms='mms:ai:dailyReport', icon='#', remark='' WHERE menu_id=905869;
UPDATE sys_menu SET menu_name='投标分析', parent_id=9063054, order_num=24, path='dashboard', component='bid/dashboard/index', query=NULL, route_name='BidDashboard', is_frame=1, is_cache=0, menu_type='C', visible='0', status='0', perms='bid:dashboard:view', icon='chart', remark='' WHERE menu_id=905894;
UPDATE sys_menu SET menu_name='资产负债表', parent_id=9063062, order_num=1, path='balanceSheet', component='fin/report/index', query='', route_name='', is_frame=1, is_cache=0, menu_type='C', visible='0', status='0', perms='fin:report:assets', icon='chart', remark='原财务报表页签独立菜单' WHERE menu_id=906110;
UPDATE sys_menu SET menu_name='利润表', parent_id=9063062, order_num=2, path='incomeStatement', component='fin/report/index', query='', route_name='', is_frame=1, is_cache=0, menu_type='C', visible='0', status='0', perms='fin:report:profit', icon='chart', remark='原财务报表页签独立菜单' WHERE menu_id=906111;
UPDATE sys_menu SET menu_name='现金流量表', parent_id=9063062, order_num=3, path='cashFlow', component='fin/report/index', query='', route_name='', is_frame=1, is_cache=0, menu_type='C', visible='0', status='0', perms='fin:report:cashflow', icon='chart', remark='原财务报表页签独立菜单' WHERE menu_id=906112;
UPDATE sys_menu SET menu_name='往来对账', parent_id=9063062, order_num=4, path='reconciliation', component='fin/report/index', query='', route_name='', is_frame=1, is_cache=0, menu_type='C', visible='0', status='0', perms='fin:report:ledger', icon='chart', remark='原财务报表页签独立菜单' WHERE menu_id=906113;
UPDATE sys_menu SET menu_name='账龄分析', parent_id=9063062, order_num=5, path='aging', component='fin/report/index', query='', route_name='', is_frame=1, is_cache=0, menu_type='C', visible='0', status='0', perms='fin:report:aging', icon='chart', remark='原财务报表页签独立菜单' WHERE menu_id=906114;
UPDATE sys_menu SET menu_name='统计报表', parent_id=9063060, order_num=1, path='statistics', component='hr/statistics/index', query=NULL, route_name='HrStatistics', is_frame=1, is_cache=0, menu_type='C', visible='0', status='0', perms='hr:statistics:list', icon='chart', remark='HR统计报表菜单' WHERE menu_id=906148;
UPDATE sys_menu SET menu_name='报表查询', parent_id=906148, order_num=1, path='#', component=NULL, query=NULL, route_name='', is_frame=1, is_cache=0, menu_type='F', visible='0', status='0', perms='hr:statistics:list', icon='#', remark='' WHERE menu_id=906149;
UPDATE sys_menu SET menu_name='报表导出', parent_id=906148, order_num=2, path='#', component=NULL, query=NULL, route_name='', is_frame=1, is_cache=0, menu_type='F', visible='0', status='0', perms='hr:statistics:export', icon='#', remark='' WHERE menu_id=906150;
UPDATE sys_menu SET menu_name='产出率核算查询', parent_id=5345, order_num=3, path='#', component='', query='', route_name='', is_frame=1, is_cache=0, menu_type='F', visible='0', status='0', perms='food:yield:list', icon='#', remark='干腌火腿产出率核算（在完工分析页内）' WHERE menu_id=9062180;
UPDATE sys_menu SET menu_name='产出率核算导出', parent_id=5345, order_num=4, path='#', component='', query='', route_name='', is_frame=1, is_cache=0, menu_type='F', visible='0', status='0', perms='food:yield:export', icon='#', remark='' WHERE menu_id=9062181;

-- PART 7 授权(按生产角色集冻结, INSERT IGNORE 幂等)
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,9063400);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,9063401);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,9063402);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,9063403);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,3108);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,3115);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,3116);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,3124);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,9063404);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,4156);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,4215);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,9063405);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,9063406);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,9063407);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,9063408);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,4851);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,4870);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,9063409);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,5215);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,5216);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,5345);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,5346);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,5347);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,11440);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,11447);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,9063410);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,521501);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,905403);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,905791);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,905865);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,905869);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,9063411);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,906148);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,906149);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,906150);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,9062180);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1,9062181);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (100,9063400);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (100,9063401);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (100,9063402);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (100,9063403);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (100,3108);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (100,3115);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (100,3116);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (100,9063404);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (100,4156);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (100,9063405);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (100,9063409);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (100,5215);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (100,5216);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (100,5345);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (100,5346);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (100,5347);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (100,9063410);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (100,521501);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (100,905869);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (101,4215);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (101,4216);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (104,9063400);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (104,9063401);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (104,9063402);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (104,9063403);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (104,3108);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (104,3115);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (104,3116);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (104,3124);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (104,9063404);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (104,4156);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (104,9063409);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (104,5215);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (104,5216);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (104,5345);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (104,5346);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (104,5347);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (104,9063410);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (104,521501);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (104,905869);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (110,9063408);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (110,4870);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (111,9063408);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (111,4870);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (120,9063406);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (120,4851);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (120,9063409);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (120,9063410);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (120,905865);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (121,9063406);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (121,4851);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (121,9063409);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (121,9063410);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (121,905865);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (122,9063406);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (122,4851);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (122,9063409);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (122,9063410);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (122,905865);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (123,9063406);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (123,4851);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (123,9063409);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (123,5215);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (123,5216);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (123,5345);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (123,5346);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (123,5347);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (123,9063410);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (123,521501);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (123,905865);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (123,905869);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (124,9063406);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (124,4851);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (124,9063409);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (124,9063410);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (124,905865);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (125,9063406);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (125,4851);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (125,9063409);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (125,9063410);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (125,905865);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (126,9063406);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (126,4851);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (126,9063409);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (126,5215);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (126,5216);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (126,5345);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (126,5346);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (126,5347);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (126,9063410);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (126,521501);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (126,905865);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (126,905869);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (200,9063402);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (200,9063403);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (200,3108);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (200,3115);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (200,3116);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (200,3124);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (201,9063402);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (202,9063402);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (203,9063402);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (203,9063403);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (203,9063409);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (203,9063410);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (300,9063404);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (300,4156);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (300,4215);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (300,4216);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (301,9063404);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (301,4156);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (301,4215);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (301,4216);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (302,9063404);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (303,9063404);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (303,4215);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (303,4216);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (304,9063404);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (400,9063400);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (400,9063401);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (400,9063409);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (400,9063410);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (401,9063400);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (401,9063401);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (401,9063409);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (401,9063410);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (402,9063400);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (402,9063401);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (402,9063409);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (402,9063410);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (403,9063400);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (403,9063401);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (403,9063409);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (403,9063410);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (404,9063400);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (404,9063401);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (404,9063409);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (404,9063410);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (500,9063405);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (500,9063409);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (500,9063410);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (501,9063405);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (501,9063409);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (501,9063410);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (502,9063405);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (502,9063409);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (502,9063410);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (503,9063405);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (504,9063405);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (505,9063405);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (600,9063409);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (600,5215);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (600,5216);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (600,5345);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (600,5346);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (600,5347);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (600,9063410);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (600,521501);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (600,905869);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (601,9063409);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (601,5215);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (601,5216);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (601,9063410);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (601,521501);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (601,905869);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (602,9063409);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (602,5215);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (602,9063410);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (603,9063409);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (603,9063410);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (610,906110);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (610,906111);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (610,906112);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (610,906113);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (610,906114);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (611,906110);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (611,906111);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (611,906112);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (611,906113);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (611,906114);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (612,906113);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (612,906114);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (700,906148);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (700,906149);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (700,906150);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (701,11440);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (701,11447);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (702,11440);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (702,11447);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (703,11440);

-- PART 7 校验: BI 各二级目录下三级菜单数
SELECT 'BI_3RD' tag, COUNT(*) cnt FROM sys_menu WHERE parent_id BETWEEN 9063052 AND 9063062;
