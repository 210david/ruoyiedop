-- ============================================================
-- MMS 生产管控模块 - 菜单及按钮权限完善修复
-- 说明：
--   1. 清理重复的按钮权限（保留挂在页面菜单C下的，删除挂在目录M下的）
--   2. 补充缺失的按钮权限（dashboard:view, finish:confirm, oee:export）
--   3. 修复乱码菜单名称
--   4. 确保 admin 角色(role_id=1) 拥有所有 mms 权限
-- ============================================================

USE `ry-vue`;
SET NAMES utf8mb4;

-- ============================================================
-- 一、清理重复按钮权限
-- 删除挂在目录(M类型)下的重复F按钮，保留挂在页面(C类型)下的
-- ============================================================

-- 1.1 删除挂在5231(计划管理目录)下的重复按钮（demand/mps的query/add/edit/remove）
DELETE FROM sys_role_menu WHERE menu_id IN (5250, 5251, 5252, 5253, 5254, 5255, 5256, 5257);
DELETE FROM sys_menu WHERE menu_id IN (5250, 5251, 5252, 5253, 5254, 5255, 5256, 5257);

-- 1.2 删除挂在5232(工单管理目录)下的重复按钮（kit的query/add/edit/remove）
DELETE FROM sys_role_menu WHERE menu_id IN (5258, 5259, 5260, 5261);
DELETE FROM sys_menu WHERE menu_id IN (5258, 5259, 5260, 5261);

-- 1.3 删除挂在5233(生产执行目录)下的重复按钮
DELETE FROM sys_role_menu WHERE menu_id IN (
  5278, 5279, 5280, 5281,  -- issue重复
  5282, 5283, 5284, 5285,  -- return重复
  5286, 5287, 5288, 5289,  -- report重复
  5290, 5291, 5292, 5293,  -- qc重复
  5294, 5295, 5296, 5297,  -- abnormal重复
  5298, 5299, 5300, 5301,  -- downtime重复
  5306, 5307, 5308, 5309   -- finish重复
);
DELETE FROM sys_menu WHERE menu_id IN (
  5278, 5279, 5280, 5281,
  5282, 5283, 5284, 5285,
  5286, 5287, 5288, 5289,
  5290, 5291, 5292, 5293,
  5294, 5295, 5296, 5297,
  5298, 5299, 5300, 5301,
  5306, 5307, 5308, 5309
);

-- 1.4 删除挂在5230(基础数据目录)下的重复按钮（calendar）
DELETE FROM sys_role_menu WHERE menu_id IN (5246, 5247, 5248, 5249);
DELETE FROM sys_menu WHERE menu_id IN (5246, 5247, 5248, 5249);

-- 1.5 删除挂在5230下的重复按钮（factory:export，保留5178挂在5172下）
DELETE FROM sys_role_menu WHERE menu_id = 5240;
DELETE FROM sys_menu WHERE menu_id = 5240;

-- 1.6 删除外协的重复按钮（5206-5209 vs 5302-5305，都挂在5205下，删5302-5305）
DELETE FROM sys_role_menu WHERE menu_id IN (5302, 5303, 5304, 5305);
DELETE FROM sys_menu WHERE menu_id IN (5302, 5303, 5304, 5305);

-- 1.7 删除工单的重复按钮（5091-5095 vs 5265-5269，都挂在5090下，删5265-5269）
DELETE FROM sys_role_menu WHERE menu_id IN (5265, 5266, 5267, 5268, 5269);
DELETE FROM sys_menu WHERE menu_id IN (5265, 5266, 5267, 5268, 5269);

-- 1.8 删除挂在5000(顶层目录)下的重复按钮（bom:publish/bom:copy，保留挂在5010下的5218/5219）
DELETE FROM sys_role_menu WHERE menu_id IN (5324, 5325);
DELETE FROM sys_menu WHERE menu_id IN (5324, 5325);

-- ============================================================
-- 二、补充缺失的按钮权限
-- ============================================================

-- 2.1 生产看板：后端使用 mms:dashboard:view，但SQL中只有 mms:dashboard:query
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 500108, '看板视图', 5001, 1, '', '', '', 1, 0, 'F', '0', '0', 'mms:dashboard:view', '#', 'admin', sysdate(), '生产看板视图权限'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms` = 'mms:dashboard:view');

-- 2.2 完工入库：缺失 mms:finish:confirm（入库确认按钮）
--     使用ID 515501 避免与已有的5156(workorder:release)冲突
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 515501, '入库确认', 5150, 6, '', '', '', 1, 0, 'F', '0', '0', 'mms:finish:confirm', '#', 'admin', sysdate(), '完工入库确认'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms` = 'mms:finish:confirm');

-- 2.3 OEE分析：缺失 mms:oee:export（导出按钮）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 521501, 'OEE导出', 5215, 2, '', '', '', 1, 0, 'F', '0', '0', 'mms:oee:export', '#', 'admin', sysdate(), 'OEE分析导出'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms` = 'mms:oee:export');

-- ============================================================
-- 三、修复乱码菜单名称
-- ============================================================

-- 修复BOM导入按钮名称（5016显示为乱码）
UPDATE `sys_menu` SET `menu_name` = 'BOM导入' WHERE `menu_id` = 5016 AND `perms` = 'mms:bom:import';

-- 修复MPS按钮名称（5163-5166显示为乱码）
UPDATE `sys_menu` SET `menu_name` = '计划提交' WHERE `menu_id` = 5163 AND `perms` = 'mms:mps:confirm';
UPDATE `sys_menu` SET `menu_name` = '计划审批' WHERE `menu_id` = 5164 AND `perms` = 'mms:mps:approve';
UPDATE `sys_menu` SET `menu_name` = '计划发布' WHERE `menu_id` = 5165 AND `perms` = 'mms:mps:release';
UPDATE `sys_menu` SET `menu_name` = '计划取消' WHERE `menu_id` = 5166 AND `perms` = 'mms:mps:cancel';

-- 修复工单按钮名称（5156-5162显示为乱码）
UPDATE `sys_menu` SET `menu_name` = '工单下达' WHERE `menu_id` = 5156 AND `perms` = 'mms:workorder:release';
UPDATE `sys_menu` SET `menu_name` = '工单暂停' WHERE `menu_id` = 5157 AND `perms` = 'mms:workorder:pause';
UPDATE `sys_menu` SET `menu_name` = '工单恢复' WHERE `menu_id` = 5158 AND `perms` = 'mms:workorder:resume';
UPDATE `sys_menu` SET `menu_name` = '工单完工' WHERE `menu_id` = 5159 AND `perms` = 'mms:workorder:finish';
UPDATE `sys_menu` SET `menu_name` = '工单关闭' WHERE `menu_id` = 5160 AND `perms` = 'mms:workorder:close';
UPDATE `sys_menu` SET `menu_name` = '工单作废' WHERE `menu_id` = 5161 AND `perms` = 'mms:workorder:cancel';
UPDATE `sys_menu` SET `menu_name` = '工单拆分' WHERE `menu_id` = 5162 AND `perms` = 'mms:workorder:split';

-- 修复报工审核按钮名称（5167显示为乱码）
UPDATE `sys_menu` SET `menu_name` = '报工审核' WHERE `menu_id` = 5167 AND `perms` = 'mms:report:audit';

-- 修复异常按钮名称（5168-5169显示为乱码）
UPDATE `sys_menu` SET `menu_name` = '异常响应' WHERE `menu_id` = 5168 AND `perms` = 'mms:abnormal:respond';
UPDATE `sys_menu` SET `menu_name` = '异常关闭' WHERE `menu_id` = 5169 AND `perms` = 'mms:abnormal:resolve';

-- 修复齐套执行按钮名称（5170显示为乱码）
UPDATE `sys_menu` SET `menu_name` = '齐套执行' WHERE `menu_id` = 5170 AND `perms` = 'mms:kit:execute';

-- 修复排产按钮名称（902108-902112显示为乱码）
UPDATE `sys_menu` SET `menu_name` = '排产删除' WHERE `menu_id` = 902108 AND `perms` = 'mms:schedule:remove';
UPDATE `sys_menu` SET `menu_name` = '排产导出' WHERE `menu_id` = 902109 AND `perms` = 'mms:schedule:export';
UPDATE `sys_menu` SET `menu_name` = '排产确认' WHERE `menu_id` = 902110 AND `perms` = 'mms:schedule:confirm';
UPDATE `sys_menu` SET `menu_name` = '排产下达' WHERE `menu_id` = 902111 AND `perms` = 'mms:schedule:release';
UPDATE `sys_menu` SET `menu_name` = '排产取消' WHERE `menu_id` = 902112 AND `perms` = 'mms:schedule:cancel';

-- ============================================================
-- 四、确保所有 mms 按钮权限都分配给 admin 角色(role_id=1)
-- ============================================================

-- 先删除所有旧的 mms F类型按钮角色关联（防止残留孤立记录）
DELETE rm FROM sys_role_menu rm
INNER JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE rm.role_id = 1 AND m.perms LIKE 'mms:%' AND m.menu_type = 'F';

-- 重新插入所有 mms F类型按钮的 admin 角色关联
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE perms LIKE 'mms:%' AND menu_type = 'F' AND status = '0'
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- ============================================================
-- 五、验证结果
-- ============================================================

-- 检查是否还有重复
SELECT perms, COUNT(*) as cnt FROM sys_menu WHERE perms LIKE 'mms:%' AND menu_type = 'F' GROUP BY perms HAVING cnt > 1;

-- 检查缺失的权限（返回空表示全部齐全）
SELECT needed.perms as missing_perms FROM (
  SELECT 'mms:dashboard:view' as perms
  UNION SELECT 'mms:finish:confirm'
  UNION SELECT 'mms:oee:export'
  UNION SELECT 'mms:workorder:resume'
  UNION SELECT 'mms:workorder:finish'
  UNION SELECT 'mms:workorder:cancel'
  UNION SELECT 'mms:workorder:split'
  UNION SELECT 'mms:workorder:release'
  UNION SELECT 'mms:workorder:pause'
  UNION SELECT 'mms:workorder:close'
  UNION SELECT 'mms:report:audit'
  UNION SELECT 'mms:abnormal:respond'
  UNION SELECT 'mms:abnormal:resolve'
  UNION SELECT 'mms:abnormal:linkDowntime'
  UNION SELECT 'mms:return:confirm'
  UNION SELECT 'mms:issue:confirm'
  UNION SELECT 'mms:bom:import'
  UNION SELECT 'mms:route:copy'
  UNION SELECT 'mms:route:status'
  UNION SELECT 'mms:mps:confirm'
  UNION SELECT 'mms:mps:cancel'
  UNION SELECT 'mms:team:add'
  UNION SELECT 'mms:team:edit'
  UNION SELECT 'mms:team:remove'
  UNION SELECT 'mms:team:export'
  UNION SELECT 'mms:demand:confirm'
  UNION SELECT 'mms:demand:toplan'
) needed
LEFT JOIN sys_menu sm ON needed.perms = sm.perms AND sm.menu_type = 'F'
WHERE sm.menu_id IS NULL;

SELECT 'MMS菜单权限修复完成' as result;
