-- =============================================
-- 流程监控：新增「重新发起」「流程复活」两个权限点
-- 背景：Flowable 在流程结束（正常结束 / 强制结束 / 撤销 / 中断）后会删除
--       act_ru_execution、act_ru_task，仅保留 act_hi_* 历史数据，
--       因此「干预」（运行时节点跳转）对已结束实例无法生效。补两个动作：
--       1) 重新发起（flowable:monitor:restart，默认开放给持有「流程干预」的角色）
--          以原单据表单数据复制一条新流程，新单据、新流程编号，与原流程互不影响。
--       2) 流程复活（flowable:monitor:revive，仅超级管理员 role_id = 1）
--          用历史变量重建运行实例（沿用原 businessKey），并停在指定节点，
--          同一张单据继续流转，原实例历史保留可追溯。
-- 日期：2026-11-14
-- =============================================
SET NAMES utf8mb4;

-- ========== 1. 定位「流程监控」菜单 ==========
SET @pm = (SELECT menu_id FROM sys_menu WHERE perms = 'flowable:monitor:list' LIMIT 1);

-- 幂等：重复执行先清理
DELETE FROM sys_role_menu WHERE menu_id IN
  (SELECT menu_id FROM sys_menu WHERE perms IN ('flowable:monitor:restart', 'flowable:monitor:revive'));
DELETE FROM sys_menu WHERE perms IN ('flowable:monitor:restart', 'flowable:monitor:revive');

-- ========== 2. 新增按钮权限 ==========
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('流程重新发起', @pm, 5, '#', '', 1, 0, 'F', '0', '0', 'flowable:monitor:restart', '#', 'admin', sysdate(), '以原单表单数据新建一条流程（新单、新流程编号）'),
('流程复活',     @pm, 6, '#', '', 1, 0, 'F', '0', '0', 'flowable:monitor:revive',  '#', 'admin', sysdate(), '已结束/已中断流程重建运行实例并停在指定节点（强权限）');

SET @restartId = (SELECT menu_id FROM sys_menu WHERE perms = 'flowable:monitor:restart' LIMIT 1);
SET @reviveId  = (SELECT menu_id FROM sys_menu WHERE perms = 'flowable:monitor:revive' LIMIT 1);

-- ========== 3. 角色授权 ==========
-- 3.1 重新发起：与「流程干预」保持同等授权范围
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT rm.role_id, @restartId
FROM sys_role_menu rm
JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE m.perms = 'flowable:monitor:intervene';

-- 3.2 超级管理员角色（role_id = 1）：两个动作都给（admin 走 *:*:* 不依赖此表，此处保证菜单树完整）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, @restartId), (1, @reviveId);

-- ========== 4. 结果校验 ==========
SELECT menu_id, menu_name, parent_id, order_num, menu_type, perms, remark
FROM sys_menu WHERE perms LIKE 'flowable:monitor%'
ORDER BY parent_id, order_num;

SELECT r.role_id, r.role_name, m.perms
FROM sys_role_menu rm
JOIN sys_role r ON r.role_id = rm.role_id
JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE m.perms IN ('flowable:monitor:restart', 'flowable:monitor:revive')
ORDER BY m.perms, r.role_id;

-- =============================================
-- 回滚：
-- DELETE FROM sys_role_menu WHERE menu_id IN (SELECT menu_id FROM sys_menu WHERE perms IN ('flowable:monitor:restart','flowable:monitor:revive'));
-- DELETE FROM sys_menu WHERE perms IN ('flowable:monitor:restart','flowable:monitor:revive');
-- =============================================
