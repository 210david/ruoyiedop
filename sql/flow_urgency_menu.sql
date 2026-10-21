-- ----------------------------
-- 紧急程度管理 菜单与权限（P0 改造配套）
-- 挂载位置：系统管理 → 流程管理 → 紧急程度管理
-- 说明：管理 flow_urgency_level 配置表（系统内置字段 urgency 的可选档位）
-- 注意：管理员角色（admin）权限通配 *:*:*，无需单独授权；此处仅为菜单可见性
-- ----------------------------

-- 菜单：紧急程度管理（C 类型，页面）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('紧急程度管理', 905829, 5, 'urgency', 'flowable/urgency/index', 1, 0, 'C', '0', '0', 'flowable:urgency:list', 'star', 'admin', sysdate(), '流程紧急程度配置（系统字段 urgency 档位）');

-- 按钮权限：查询 / 新增 / 修改 / 删除
SET @urgencyMenuId = (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'flowable:urgency:list' AND parent_id = 905829 LIMIT 1) t);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('紧急程度查询', @urgencyMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'flowable:urgency:query', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('紧急程度新增', @urgencyMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'flowable:urgency:add', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('紧急程度修改', @urgencyMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'flowable:urgency:edit', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('紧急程度删除', @urgencyMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'flowable:urgency:remove', '#', 'admin', sysdate(), '');

-- ----------------------------
-- 回退语句
-- ----------------------------
-- DELETE FROM sys_role_menu WHERE menu_id IN (SELECT menu_id FROM sys_menu WHERE perms LIKE 'flowable:urgency%');
-- DELETE FROM sys_menu WHERE perms LIKE 'flowable:urgency%' OR (parent_id = 905829 AND path = 'urgency');
