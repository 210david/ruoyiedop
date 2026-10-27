-- =============================================
-- 流程分类管理菜单（系统管理→流程管理→流程分类管理）
-- 分类数据复用字典 flowable_category（模型/定义/流程树共用）
-- 管理员角色为超级管理员，权限通配，无需单独授权
-- =============================================
SET NAMES utf8mb4;

SET @fm = (SELECT menu_id FROM sys_menu WHERE menu_name = '流程管理' AND parent_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '系统管理' AND parent_id = 0 LIMIT 1) LIMIT 1);

-- 清理旧记录（幂等）
DELETE FROM sys_menu WHERE menu_name = '流程分类管理' AND perms LIKE 'flowable:category%';
DELETE FROM sys_menu WHERE perms LIKE 'flowable:category:%' AND menu_name IN ('分类查询','分类新增','分类修改','分类删除');

-- 流程分类管理（排在流程模型前，order_num=1；原流程模型/定义/表单保持 1/2/3 不冲突则插为 4）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('流程分类管理', @fm, 4, 'category', 'flowable/category/index', 1, 0, 'C', '0', '0', 'flowable:category:list', 'tree', 'admin', sysdate(), '流程分类维护（字典flowable_category，含引用校验）');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('分类查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'flowable:category:query',  '#', 'admin', sysdate(), ''),
('分类新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'flowable:category:add',    '#', 'admin', sysdate(), ''),
('分类修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'flowable:category:edit',   '#', 'admin', sysdate(), ''),
('分类删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'flowable:category:remove', '#', 'admin', sysdate(), '');
