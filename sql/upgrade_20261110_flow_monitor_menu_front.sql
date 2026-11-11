-- =============================================
-- 流程监控：前台「流程中心」增加入口（2026-11-10）
--
-- 背景：流程监控原先只挂在 系统管理 → 流程管理 → 流程监控，
--      管理员反馈层级太深找不到。本脚本在「流程中心」下再挂一个入口，
--      与后台入口指向同一个页面组件 flowable/monitor/index。
--
-- 权限：perms 仍为 flowable:monitor:list，只有超级管理员（admin 角色走 *:*:*）
--      或显式授权的角色可见，业务用户看不到该菜单。
-- 可重复执行：先按 (parent_id, path) 删除旧行再插入。
-- =============================================
SET NAMES utf8mb4;

-- 流程中心根菜单
SET @fc = (SELECT menu_id FROM sys_menu WHERE parent_id = 0 AND path = 'flow' LIMIT 1);

-- 清理旧的前台入口（幂等）
DELETE FROM sys_role_menu WHERE menu_id IN (
  SELECT menu_id FROM sys_menu WHERE parent_id = @fc AND path = 'monitor'
);
DELETE FROM sys_menu WHERE parent_id = @fc AND path = 'monitor';

-- 新增前台入口：流程中心 → 流程监控
-- route_name 必须显式指定：后台入口的 path 同为 monitor，若不指定会生成重名路由 Monitor，
-- vue-router 注册时后者覆盖前者，且 tags-view 的 keep-alive 缓存键会串台。
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('流程监控', @fc, 6, 'monitor', 'flowable/monitor/index', NULL, 'FlowMonitor', 1, 0,
        'C', '0', '0', 'flowable:monitor:list', 'monitor', 'admin', sysdate(), '', NULL,
        '管理员流程监控入口（与 系统管理→流程管理→流程监控 指向同一页面）');

-- ========== 校验 ==========
SELECT menu_id, parent_id, order_num, menu_name, path, component, perms, visible, status
FROM sys_menu
WHERE (parent_id = @fc AND path = 'monitor')
   OR perms = 'flowable:monitor:list'
ORDER BY parent_id, order_num;

-- ========== 回滚（如需下线前台入口，取消下面 3 行注释执行） ==========
-- DELETE FROM sys_role_menu WHERE menu_id IN (SELECT menu_id FROM sys_menu WHERE parent_id = @fc AND path = 'monitor');
-- DELETE FROM sys_menu WHERE parent_id = @fc AND path = 'monitor';
