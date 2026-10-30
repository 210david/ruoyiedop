-- =============================================================================
-- YHNY 菜单调整：物料管理复制到「生产管控→基础数据」（2026-10-30）
-- 背景：源禾农业不使用仓储管理模块，物料管理为通用基础数据，
--       参照"供应商管理复制到采购管理"的做法，复制菜单入口。
-- 内容：
--   1) 新增 C 菜单 9062240 物料管理（parent 5230 生产管控/基础数据，
--      component wms/material/index，perms wms:material:list，与原 2010 同组件同权限）
--   2) 新增 F 按钮 9062241~9062246（查询/新增/修改/删除/导出/导入）
--   3) sys_role_menu 授权同步（按原 2010~2016 的授权角色复制，共 28 条）
--   4) sys_module：wms（仓储管理）停用（源禾不使用该模块）
--   5) sys_user_home_layout：admin 门户布局移除 /wms
-- =============================================================================

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
VALUES (9062240,'物料管理',5230,9,'material','wms/material/index','','',1,0,'C','0','0','wms:material:list','component','admin',NOW(),'物料通用基础数据（源禾：由仓储管理复制，生产管控/基础数据入口）');

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) VALUES
(9062241,'物料查询',9062240,1,'#','','',NULL,1,0,'F','0','0','wms:material:query','#','admin',NOW(),''),
(9062242,'物料新增',9062240,2,'#','','',NULL,1,0,'F','0','0','wms:material:add','#','admin',NOW(),''),
(9062243,'物料修改',9062240,3,'#','','',NULL,1,0,'F','0','0','wms:material:edit','#','admin',NOW(),''),
(9062244,'物料删除',9062240,4,'#','','',NULL,1,0,'F','0','0','wms:material:remove','#','admin',NOW(),''),
(9062245,'物料导出',9062240,5,'#','','',NULL,1,0,'F','0','0','wms:material:export','#','admin',NOW(),''),
(9062246,'物料导入',9062240,6,'#','','',NULL,1,0,'F','0','0','wms:material:import','#','admin',NOW(),'');

INSERT INTO sys_role_menu (role_id,menu_id) SELECT rm.role_id,9062240 FROM sys_role_menu rm WHERE rm.menu_id=2010;
INSERT INTO sys_role_menu (role_id,menu_id) SELECT rm.role_id,9062241 FROM sys_role_menu rm WHERE rm.menu_id=2011;
INSERT INTO sys_role_menu (role_id,menu_id) SELECT rm.role_id,9062242 FROM sys_role_menu rm WHERE rm.menu_id=2012;
INSERT INTO sys_role_menu (role_id,menu_id) SELECT rm.role_id,9062243 FROM sys_role_menu rm WHERE rm.menu_id=2013;
INSERT INTO sys_role_menu (role_id,menu_id) SELECT rm.role_id,9062244 FROM sys_role_menu rm WHERE rm.menu_id=2014;
INSERT INTO sys_role_menu (role_id,menu_id) SELECT rm.role_id,9062245 FROM sys_role_menu rm WHERE rm.menu_id=2015;
INSERT INTO sys_role_menu (role_id,menu_id) SELECT rm.role_id,9062246 FROM sys_role_menu rm WHERE rm.menu_id=2016;

UPDATE sys_module SET status='1' WHERE module_key='wms';
UPDATE sys_user_home_layout SET module_codes='/system,/mms,/qms,/pms,/pdm,/mk,/ai' WHERE user_id=1;

-- =============================================================================
-- 回滚说明（手工按需执行）：
-- DELETE FROM sys_role_menu WHERE menu_id BETWEEN 9062240 AND 9062246;
-- DELETE FROM sys_menu WHERE menu_id BETWEEN 9062240 AND 9062246;
-- UPDATE sys_module SET status='0' WHERE module_key='wms';
-- UPDATE sys_user_home_layout SET module_codes='/system,/mms,/qms,/pms,/pdm,/mk,/ai,/wms' WHERE user_id=1;
-- =============================================================================
