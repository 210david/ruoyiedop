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
