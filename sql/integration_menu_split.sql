-- =============================================================
-- 集成模块菜单拆分脚本：钉钉集成 / 企业微信集成 两个独立菜单
-- 版本: v1.0  日期: 2026-11-07
-- 依赖规范: unified-development-standards.md（3.4 菜单权限 M/C/F 三级）
-- 说明: 前端页面已拆分为 integration/attendance/index（钉钉）与 integration/wecom/index（企业微信），
--       两页复用同一业务面板组件；本脚本仅调整菜单结构与角色授权，可重复执行。
-- =============================================================

-- ----------------------------
-- 1. 钉钉菜单名称还原（integration_wecom.sql 曾将其临时改名为「第三方考勤集成」，拆分后恢复）
-- ----------------------------
UPDATE sys_menu SET menu_name = '钉钉集成', remark = '钉钉考勤同步与用户绑定'
WHERE menu_id = 9063010 AND menu_name <> '钉钉集成';

-- ----------------------------
-- 2. 新增菜单：企业微信集成
--    权限点复用 integration:attendance:list：接口层与钉钉共用同一套 Controller，
--    菜单可见性由 sys_role_menu 授权独立控制，无需新增权限字符串。
-- ----------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063030, '企业微信集成', 9063000, 2, 'wecom', 'integration/wecom/index', '', 1, 0, 'C', '0', '0', 'integration:attendance:list', 'wechat', 'admin', sysdate(), '企业微信考勤同步与用户绑定'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063030);

-- ----------------------------
-- 3. 角色授权：沿用已获钉钉菜单授权的角色，自动补授企业微信菜单
--    （按钮权限 integration:attendance:sync / query / config 两页共用，无需重复授权）
-- ----------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT r.role_id, 9063030 FROM sys_role_menu r
WHERE r.menu_id = 9063010
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = r.role_id AND x.menu_id = 9063030);

-- ----------------------------
-- 4. 校验：确认两个菜单与授权情况
-- ----------------------------
-- SELECT menu_id, menu_name, path, component, perms, order_num FROM sys_menu WHERE parent_id = 9063000 ORDER BY order_num;
-- SELECT menu_id, COUNT(*) AS role_cnt FROM sys_role_menu WHERE menu_id IN (9063010, 9063030) GROUP BY menu_id;
