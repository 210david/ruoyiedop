-- =============================================
-- QMS 模块补充功能 SQL 脚本
-- 包含：车间/产线/工位表、电子签名记录表、定时任务注册、菜单注册
-- =============================================

-- 1. 车间/产线/工位表
DROP TABLE IF EXISTS qms_workshop;
CREATE TABLE qms_workshop (
    workshop_id      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '节点ID',
    parent_id        BIGINT       DEFAULT 0                    COMMENT '父节点ID（0=根节点）',
    workshop_code    VARCHAR(64)  NOT NULL                     COMMENT '节点编码',
    workshop_name    VARCHAR(128) NOT NULL                     COMMENT '节点名称',
    node_type        CHAR(1)      NOT NULL                     COMMENT '层级类型（1车间 2产线 3工位 4检验点）',
    order_num        INT          DEFAULT 0                    COMMENT '排序号',
    status           CHAR(1)      DEFAULT '0'                  COMMENT '状态（0正常 1停用）',
    del_flag         CHAR(1)      DEFAULT '0'                  COMMENT '删除标志（0存在 2删除）',
    create_by        VARCHAR(64)  DEFAULT ''                   COMMENT '创建者',
    create_time      DATETIME                                  COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''                   COMMENT '更新者',
    update_time      DATETIME                                  COMMENT '更新时间',
    remark           VARCHAR(500)                              COMMENT '备注',
    PRIMARY KEY (workshop_id),
    UNIQUE KEY uk_workshop_code (workshop_code),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车间/产线/工位表';

-- 2. 电子签名记录表
DROP TABLE IF EXISTS qms_esig_record;
CREATE TABLE qms_esig_record (
    esig_id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '签名记录ID',
    biz_type         VARCHAR(32)  NOT NULL                     COMMENT '业务类型（insp_judge=检验判定, ncr_approve=NCR审批, ncr_disposition=NCR处置, ncr_verify=NCR验证）',
    biz_id           BIGINT       NOT NULL                     COMMENT '业务ID（检验任务ID / NCR ID）',
    biz_no           VARCHAR(64)                              COMMENT '业务编号（任务编号 / NCR编号）',
    user_id          BIGINT                                   COMMENT '签名用户ID',
    user_name        VARCHAR(64)                              COMMENT '签名用户名',
    nick_name        VARCHAR(100)                             COMMENT '签名用户姓名',
    sig_meaning      VARCHAR(128)                             COMMENT '签名含义（如：检验员判定、质量经理审批、处置执行等）',
    sig_opinion      VARCHAR(500)                             COMMENT '签名意见/备注',
    sig_result       VARCHAR(16)                              COMMENT '签名结果（pass=通过, fail=不通过, reject=驳回）',
    sig_ip           VARCHAR(64)                              COMMENT '签名IP地址',
    sig_time         DATETIME      NOT NULL                     COMMENT '签名时间',
    create_by        VARCHAR(64)  DEFAULT ''                   COMMENT '创建者',
    create_time      DATETIME                                  COMMENT '创建时间',
    remark           VARCHAR(500)                              COMMENT '备注',
    PRIMARY KEY (esig_id),
    KEY idx_biz (biz_type, biz_id),
    KEY idx_sig_time (sig_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电子签名记录表';

-- 3. 注册定时任务
-- IQC检验任务自动生成（每天凌晨1点）
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark) VALUES
('QMS IQC任务自动生成', 'DEFAULT', 'qmsTask.autoGenerateIqcTasks', '0 0 1 * * ?', '3', '1', '0', 'admin', sysdate(), '每天凌晨1点扫描需IQC检验的物料属性，自动生成检验任务');

-- 检验任务逾期检查（每天凌晨2点）
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark) VALUES
('QMS检验任务逾期检查', 'DEFAULT', 'qmsTask.checkOverdueTasks', '0 0 2 * * ?', '3', '1', '0', 'admin', sysdate(), '每天凌晨2点检查待检状态超过3天的检验任务并标记逾期');

-- 4. 注册菜单（车间/产线/工位管理）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '车间管理', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '质量主数据' LIMIT 1) AS temp), 6, 'workshop', 'qms/workshop/index', 1, 0, 'C', '0', '0', 'qms:workshop:list', 'office-building', 'admin', sysdate(), '车间/产线/工位层级管理'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '车间管理');

-- 注册菜单按钮（车间管理）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '车间查询', menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'qms:workshop:query', '#', 'admin', sysdate()
FROM sys_menu WHERE menu_name = '车间管理' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '车间查询');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '车间新增', menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'qms:workshop:add', '#', 'admin', sysdate()
FROM sys_menu WHERE menu_name = '车间管理' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '车间新增');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '车间修改', menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'qms:workshop:edit', '#', 'admin', sysdate()
FROM sys_menu WHERE menu_name = '车间管理' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '车间修改');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '车间删除', menu_id, 4, '#', '', 1, 0, 'F', '0', '0', 'qms:workshop:remove', '#', 'admin', sysdate()
FROM sys_menu WHERE menu_name = '车间管理' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '车间删除');

-- 注册菜单（质量看板）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '质量看板', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '质量管理' AND parent_id = 0 LIMIT 1) AS temp), 1, 'dashboard', 'qms/dashboard/index', 1, 0, 'C', '0', '0', 'qms:dashboard:view', 'dashboard', 'admin', sysdate(), '质量KPI看板'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '质量看板');

-- 注册菜单（电子签名记录）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '电子签名', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '质量管理' AND parent_id = 0 LIMIT 1) AS temp), 9, 'esig', 'qms/esig/index', 1, 0, 'C', '0', '0', 'qms:esig:list', 'password', 'admin', sysdate(), '电子签名记录查询'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '电子签名');

-- 注册菜单按钮（电子签名）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '签名查询', menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'qms:esig:query', '#', 'admin', sysdate()
FROM sys_menu WHERE menu_name = '电子签名' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '签名查询');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '签名删除', menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'qms:esig:remove', '#', 'admin', sysdate()
FROM sys_menu WHERE menu_name = '电子签名' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '签名删除');

-- 5. 字典数据补充
-- 车间节点类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '车间节点类型', 'qms_workshop_node_type', '0', 'admin', sysdate(), '车间/产线/工位/检验点层级类型'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'qms_workshop_node_type');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '车间', '1', 'qms_workshop_node_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'qms_workshop_node_type' AND dict_value = '1');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '产线', '2', 'qms_workshop_node_type', '', 'success', 'N', '0', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'qms_workshop_node_type' AND dict_value = '2');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '工位', '3', 'qms_workshop_node_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'qms_workshop_node_type' AND dict_value = '3');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '检验点', '4', 'qms_workshop_node_type', '', 'info', 'N', '0', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'qms_workshop_node_type' AND dict_value = '4');

-- 电子签名业务类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '电子签名业务类型', 'qms_esig_biz_type', '0', 'admin', sysdate(), '电子签名关联的业务类型'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'qms_esig_biz_type');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '检验判定', 'insp_judge', 'qms_esig_biz_type', '', 'primary', 'N', '0', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'qms_esig_biz_type' AND dict_value = 'insp_judge');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, 'NCR审批', 'ncr_approve', 'qms_esig_biz_type', '', 'success', 'N', '0', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'qms_esig_biz_type' AND dict_value = 'ncr_approve');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, 'NCR处置', 'ncr_disposition', 'qms_esig_biz_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'qms_esig_biz_type' AND dict_value = 'ncr_disposition');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, 'NCR验证', 'ncr_verify', 'qms_esig_biz_type', '', 'info', 'N', '0', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'qms_esig_biz_type' AND dict_value = 'ncr_verify');

-- 6. 初始化示例数据（车间/产线/工位）
INSERT INTO qms_workshop (parent_id, workshop_code, workshop_name, node_type, order_num, status, create_by, create_time) VALUES
(0, 'WS001', '总装车间', '1', 1, '0', 'admin', sysdate()),
(1, 'LINE001', '总装线A', '2', 1, '0', 'admin', sysdate()),
(1, 'LINE002', '总装线B', '2', 2, '0', 'admin', sysdate()),
(2, 'STA001', '工位01', '3', 1, '0', 'admin', sysdate()),
(2, 'STA002', '工位02', '3', 2, '0', 'admin', sysdate());