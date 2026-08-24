-- ============================================================
-- 修复生产管控菜单结构
-- 1. 将"完工入库"(5150)从"完工分析"(5235)移到"生产执行"(5233)下
-- 2. 将完工入库的按钮权限(5306-5310)移到生产执行(5233)下
-- 3. 在"完工分析"(5235)下新增"工单完工分析"报表页面
-- 4. 清理9000系列废弃菜单
-- 5. 修复9050乱码
-- 数据库: ry-vue
-- ============================================================
USE `ry-vue`;
SET NAMES utf8mb4;

-- ============================================================
-- 1. 将完工入库(5150)从完工分析(5235)移到生产执行(5233)
-- ============================================================
UPDATE `sys_menu` SET parent_id = 5233, order_num = 7, icon = 'upload'
WHERE menu_id = 5150 AND parent_id = 5235;

-- 2. 将完工入库按钮权限移到生产执行(5233)下，调整order_num避免冲突
UPDATE `sys_menu` SET parent_id = 5233, order_num = 25 WHERE menu_id = 5306;  -- 完工查询
UPDATE `sys_menu` SET parent_id = 5233, order_num = 26 WHERE menu_id = 5307;  -- 完工新增
UPDATE `sys_menu` SET parent_id = 5233, order_num = 27 WHERE menu_id = 5308;  -- 完工修改
UPDATE `sys_menu` SET parent_id = 5233, order_num = 28 WHERE menu_id = 5309;  -- 完工删除

-- ============================================================
-- 3. 在完工分析(5235)下新增"工单完工分析"报表
-- ============================================================
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 5218, '工单完工分析', 5235, 5, 'finishAnalysis', 'mms/finishAnalysis/index', '', 1, 0, 'C', '0', '0', 'mms:finishAnalysis:list', 'data-analysis', 'admin', sysdate(), '工单完工分析报表'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE menu_id = 5218);

-- 工单完工分析按钮权限
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 5318, '完工分析查询', 5218, 1, '', '', '', 1, 0, 'F', '0', '0', 'mms:finishAnalysis:query', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE menu_id = 5318 AND parent_id = 5218);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 5319, '完工分析导出', 5218, 2, '', '', '', 1, 0, 'F', '0', '0', 'mms:finishAnalysis:export', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE menu_id = 5319 AND parent_id = 5218);

-- ============================================================
-- 4. 给admin角色分配新菜单权限
-- ============================================================
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 5218), (1, 5318), (1, 5319)
ON DUPLICATE KEY UPDATE role_id = role_id;

-- ============================================================
-- 5. 清理9000系列废弃菜单（之前错误创建的）
-- ============================================================
-- 先删除角色关联
DELETE FROM `sys_role_menu` WHERE menu_id IN (9050, 9051, 9052, 9053, 9054, 905101, 905201, 905202, 905301, 905302, 905401, 905402);

-- 删除按钮权限
DELETE FROM `sys_menu` WHERE menu_id IN (905101, 905201, 905202, 905301, 905302, 905401, 905402);

-- 删除子菜单
DELETE FROM `sys_menu` WHERE menu_id IN (9051, 9052, 9053, 9054);

-- 删除目录
DELETE FROM `sys_menu` WHERE menu_id = 9050;
