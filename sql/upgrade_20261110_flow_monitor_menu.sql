-- =============================================
-- 流程监控（系统管理员干预）菜单与权限
-- 背景：原「我的请求」页面给普通用户开放了「撤销」操作，撤销属于管理员动作，
--       现全部收回到「系统管理 → 流程管理 → 流程监控」，由系统管理员统一干预：
--       跳转任意节点 / 改派办理人 / 强制结束 / 撤销 / 删除。
-- 日期：2026-11-10
-- =============================================
SET NAMES utf8mb4;

-- ========== 1. 下线普通用户的「流程撤销」按钮权限 ==========
DELETE FROM sys_role_menu WHERE menu_id IN (SELECT menu_id FROM sys_menu WHERE perms = 'flowable:process:cancel');
DELETE FROM sys_menu WHERE perms = 'flowable:process:cancel';

-- ========== 2. 「流程管理 → 流程监控」菜单 ==========
SET @fm = (SELECT menu_id FROM sys_menu WHERE menu_name = '流程管理' AND parent_id = 1 LIMIT 1);

DELETE FROM sys_role_menu WHERE menu_id IN (SELECT menu_id FROM sys_menu WHERE menu_name = '流程监控' AND parent_id = @fm);
DELETE FROM sys_menu WHERE menu_name = '流程监控' AND parent_id = @fm;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('流程监控', @fm, 2, 'monitor', 'flowable/monitor/index', 1, 0, 'C', '0', '0', 'flowable:monitor:list', 'monitor', 'admin', sysdate(), '全流程实例监控与管理员干预（跳转节点/改派/强制结束/撤销/删除）');
SET @pm = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('监控查询', @pm, 1, '#', '', 1, 0, 'F', '0', '0', 'flowable:monitor:query',     '#', 'admin', sysdate(), '查看干预面板与节点信息'),
('流程干预', @pm, 2, '#', '', 1, 0, 'F', '0', '0', 'flowable:monitor:intervene', '#', 'admin', sysdate(), '强制跳转任意节点 / 改派办理人'),
('流程结束', @pm, 3, '#', '', 1, 0, 'F', '0', '0', 'flowable:monitor:end',       '#', 'admin', sysdate(), '强制结束 / 撤销流程'),
('流程删除', @pm, 4, '#', '', 1, 0, 'F', '0', '0', 'flowable:monitor:remove',    '#', 'admin', sysdate(), '删除流程实例及其历史记录');

-- ========== 3. 结果校验 ==========
SELECT menu_id, menu_name, parent_id, order_num, path, component, perms
FROM sys_menu WHERE perms LIKE 'flowable:monitor%' OR perms = 'flowable:process:cancel'
ORDER BY parent_id, order_num;

-- =============================================
-- 回滚（如需恢复「我的请求 → 流程撤销」按钮，注意后端接口已下线，仅恢复菜单无实际作用）
-- INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
-- VALUES (905847,'流程撤销',905827,2,'#','',1,0,'F','0','0','flowable:process:cancel','#','admin','2026-09-16 15:01:47','');
-- -- 角色授权需按需重新授权（原 47 个角色持有该权限）
-- INSERT INTO sys_role_menu (role_id,menu_id) SELECT role_id, 905847 FROM sys_role WHERE role_id IN (100,101,102,103,104);
-- 删除流程监控菜单：
-- DELETE FROM sys_role_menu WHERE menu_id IN (SELECT menu_id FROM sys_menu WHERE perms LIKE 'flowable:monitor%' OR parent_id = 9063091);
-- DELETE FROM sys_menu WHERE perms LIKE 'flowable:monitor%' OR parent_id = 9063091;
-- =============================================
