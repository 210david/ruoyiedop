-- ============================================================================
-- 传丰食品项目：范围外模块临时关闭（最终口径经用户确认）
--
-- 保留模块：营销管理 / 生产管控 / 质量管理 / 采购管理 / 仓储管理 / AI中心
--           + 平台基础功能：系统管理、消息中心（系统工具此前已停用）
-- 关闭模块：招投标管理 / 设备管理 / 安全生产管理 / 人力资源管理 / 流程中心 /
--           财务管理
-- 同步暂停设备管理相关定时任务：100-104
-- 说明：仅停用菜单(sys_menu.status='1')与暂停任务(sys_job.status='1')，
--       不动代码与数据，随时可恢复。用户重新登录后生效。
-- ============================================================================

-- 【关闭】范围外模块菜单树（含全部子菜单）
WITH RECURSIVE sub AS (
  SELECT menu_id FROM sys_menu
  WHERE menu_id IN (905886, 3000, 4638, 905538, 905824, 905938)
  UNION ALL
  SELECT m.menu_id FROM sys_menu m JOIN sub s ON m.parent_id = s.menu_id
)
UPDATE sys_menu SET status = '1' WHERE menu_id IN (SELECT menu_id FROM sub);

-- 【关闭】设备管理相关定时任务（PM工单、SLA、点检、数采模拟）
UPDATE sys_job SET status = '1' WHERE job_id IN (100, 101, 102, 103, 104);

-- ==================== 恢复（需要时执行以下语句） ====================
-- 【恢复】模块菜单树
-- WITH RECURSIVE sub2 AS (
--   SELECT menu_id FROM sys_menu
--   WHERE menu_id IN (905886, 3000, 4638, 905538, 905824, 905938)
--   UNION ALL
--   SELECT m.menu_id FROM sys_menu m JOIN sub2 s ON m.parent_id = s.menu_id
-- )
-- UPDATE sys_menu SET status = '0' WHERE menu_id IN (SELECT menu_id FROM sub2);

-- 【恢复】定时任务
-- UPDATE sys_job SET status = '0' WHERE job_id IN (100, 101, 102, 103, 104);
-- 注：104 设备数据采集模拟在关闭前即为暂停状态，如需恢复请单独确认。
