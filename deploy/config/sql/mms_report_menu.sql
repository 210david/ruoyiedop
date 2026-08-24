-- ============================================================
-- 生产管控 - 统计报表子目录菜单重构
-- 将原 9050 生产看板(C) 改为 统计报表目录(M)
-- 在其下创建：生产看板、OEE分析、生产日报、工单完工分析
-- 数据库: ry-vue
-- ============================================================
USE `ry-vue`;
SET NAMES utf8mb4;

-- ============================================================
-- 1. 将原 9050 由 C(页面) 改为 M(目录)，名称改为"统计报表"
-- ============================================================
UPDATE `sys_menu` SET 
    menu_name = '统计报表',
    menu_type = 'M',
    component = NULL,
    perms = '',
    icon = 'chart',
    remark = '统计报表目录',
    path = 'report'
WHERE menu_id = 9050;

-- 如果 9050 不存在则插入
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 9050, '统计报表', 9000, 5, 'report', NULL, '', 1, 0, 'M', '0', '0', '', 'chart', 'admin', sysdate(), '统计报表目录'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE menu_id = 9050);

-- ============================================================
-- 2. 统计报表子菜单
-- ============================================================

-- 2.1 生产看板（从原9050页面迁移为子菜单）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 9051, '生产看板', 9050, 1, 'dashboard', 'mms/dashboard/index', '', 1, 0, 'C', '0', '0', 'mms:dashboard:list', 'monitor', 'admin', sysdate(), '生产运营看板'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE menu_id = 9051);

-- 2.2 OEE分析（迁移到统计报表下）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 9052, 'OEE分析', 9050, 2, 'oee', 'mms/oee/index', '', 1, 0, 'C', '0', '0', 'mms:oee:list', 'chart', 'admin', sysdate(), '设备OEE分析报表'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE menu_id = 9052);

-- 2.3 生产日报（迁移到统计报表下）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 9053, '生产日报', 9050, 3, 'daily', 'mms/daily/index', '', 1, 0, 'C', '0', '0', 'mms:daily:list', 'documentation', 'admin', sysdate(), '生产日报统计'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE menu_id = 9053);

-- 2.4 工单完工分析（新增）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 9054, '工单完工分析', 9050, 4, 'finishAnalysis', 'mms/finishAnalysis/index', '', 1, 0, 'C', '0', '0', 'mms:finishAnalysis:list', 'data-analysis', 'admin', sysdate(), '工单完工分析报表'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE menu_id = 9054);

-- ============================================================
-- 3. 按钮权限
-- ============================================================

-- 生产看板按钮
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 905101, '看板查询', 9051, 1, '', '', '', 1, 0, 'F', '0', '0', 'mms:dashboard:query', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE menu_id = 905101);

-- OEE分析按钮
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 905201, 'OEE查询', 9052, 1, '', '', '', 1, 0, 'F', '0', '0', 'mms:oee:query', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE menu_id = 905201);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 905202, 'OEE导出', 9052, 2, '', '', '', 1, 0, 'F', '0', '0', 'mms:oee:export', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE menu_id = 905202);

-- 生产日报按钮
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 905301, '日报查询', 9053, 1, '', '', '', 1, 0, 'F', '0', '0', 'mms:daily:query', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE menu_id = 905301);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 905302, '日报导出', 9053, 2, '', '', '', 1, 0, 'F', '0', '0', 'mms:daily:export', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE menu_id = 905302);

-- 工单完工分析按钮
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 905401, '完工分析查询', 9054, 1, '', '', '', 1, 0, 'F', '0', '0', 'mms:finishAnalysis:query', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE menu_id = 905401);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 905402, '完工分析导出', 9054, 2, '', '', '', 1, 0, 'F', '0', '0', 'mms:finishAnalysis:export', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE menu_id = 905402);

-- ============================================================
-- 4. 给admin角色分配权限
-- ============================================================
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 9050), (1, 9051), (1, 9052), (1, 9053), (1, 9054),
(1, 905101), (1, 905201), (1, 905202), (1, 905301), (1, 905302),
(1, 905401), (1, 905402)
ON DUPLICATE KEY UPDATE role_id = role_id;
