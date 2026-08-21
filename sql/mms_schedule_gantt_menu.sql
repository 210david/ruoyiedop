-- ============================================================
-- MMS 排产管理 - 甘特图工作台菜单
-- 执行时间: 2026-08-19
-- 说明: 为甘特图排产工作台添加菜单入口
-- ============================================================

USE `ry-vue`;
SET NAMES utf8mb4;

-- ============================================================
-- 菜单：甘特图排产工作台（挂在"计划管理"目录下，parent_id=5231）
-- ============================================================
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '排产工作台', 5231, 0, 'gantt', 'mms/schedule/gantt', '', 'ScheduleGantt', 1, 0, 'C', '0', '0', 'mms:schedule:list', 'date', 'admin', sysdate(), '甘特图排产工作台'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:schedule:list' AND path = 'gantt');
