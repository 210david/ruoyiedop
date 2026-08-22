-- ============================================================
-- MMS 班组管理模块 - 建表 + 菜单 + 字典 + 编号规则
-- 说明: 班组管理是生产管控基础数据，为派工管理/报工管理提供班组数据源
-- 执行时间: 2026-08-21
-- ============================================================

USE `ry-vue`;
SET NAMES utf8mb4;

-- ============================================================
-- 一、建表：mms_team（班组表）
-- ============================================================
CREATE TABLE IF NOT EXISTS `mms_team` (
    `team_id`        BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '班组ID',
    `team_code`      VARCHAR(50)  NOT NULL                COMMENT '班组编号',
    `team_name`      VARCHAR(100) NOT NULL                COMMENT '班组名称',
    `resource_id`    BIGINT(20)   DEFAULT NULL            COMMENT '所属产能单元ID',
    `leader`         VARCHAR(50)  DEFAULT ''              COMMENT '班组长',
    `phone`          VARCHAR(30)  DEFAULT ''              COMMENT '联系电话',
    `member_count`   INT          DEFAULT 0               COMMENT '班组人数',
    `status`         CHAR(1)      DEFAULT '0'             COMMENT '状态（0启用 1停用）',
    `del_flag`       CHAR(1)      DEFAULT '0'             COMMENT '删除标志（0存在 2删除）',
    `create_by`      VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    `create_time`    DATETIME     DEFAULT NULL            COMMENT '创建时间',
    `update_by`      VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    `update_time`    DATETIME     DEFAULT NULL            COMMENT '更新时间',
    `remark`         VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    PRIMARY KEY (`team_id`),
    UNIQUE KEY `uk_team_code` (`team_code`),
    KEY `idx_team_name` (`team_name`),
    KEY `idx_resource_id` (`resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班组管理表';

-- ============================================================
-- 二、字典数据：mms_team_status（班组状态）
-- ============================================================
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '班组状态', 'mms_team_status', '0', 'admin', sysdate(), '班组启用停用状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_team_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '已启用', '0', 'mms_team_status', '', 'success', 'Y', '0', 'admin', sysdate(), '已启用'),
(2, '已停用', '1', 'mms_team_status', '', 'danger',  'N', '0', 'admin', sysdate(), '已停用')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- ============================================================
-- 三、编号规则：mms_team（班组编号）
-- ============================================================
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'mms_team', 'mms', '班组编号', 'TM', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '班组编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code = 'mms_team');

-- ============================================================
-- 四、菜单：班组管理（菜单类型 C，挂在MMS主目录 5000 下）
-- ============================================================
-- 查找 MMS 主菜单 ID（parent_id = 5000 为生产管控主目录）
-- 班组管理菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '班组管理', 5000, 3, 'team', 'mms/team/index', '', '', 1, 0, 'C', '0', '0', 'mms:team:list', 'peoples', 'admin', sysdate(), '班组管理菜单'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:team:list');

-- 班组管理按钮权限（菜单类型 F）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '班组查询', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:team:list' LIMIT 1) t), 1, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:team:query', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:team:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '班组新增', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:team:list' LIMIT 1) t), 2, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:team:add', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:team:add');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '班组修改', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:team:list' LIMIT 1) t), 3, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:team:edit', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:team:edit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '班组删除', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:team:list' LIMIT 1) t), 4, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:team:remove', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:team:remove');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '班组导出', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:team:list' LIMIT 1) t), 5, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:team:export', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:team:export');

-- ============================================================
-- 五、角色菜单分配（admin 角色 = 1）
-- ============================================================
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'mms:team:%'
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);

-- ============================================================
-- 六、清除 Redis 字典缓存
-- ============================================================
-- redis-cli DEL sys_dict:mms_team_status
-- 或者直接清除所有字典缓存：
-- redis-cli KEYS "sys_dict:*" | xargs redis-cli DEL
