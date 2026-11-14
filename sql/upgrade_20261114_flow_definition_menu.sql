-- =============================================
-- 恢复「流程管理 → 流程定义」菜单（版本管理全量入口）
-- 背景：版本管理能力原本只残留在「流程模型 → 设计器 → 版本标签行」，
--       一行标签撑不起多版本场景（无法列全、无法查看历史版本内容）；
--       flowable/definition/index 页面其实早已实现（版本列表/查看XML/备注/
--       挂起激活/设为当前），只是建菜单脚本里被注释掉，从未入库。
-- 日期：2026-11-14
-- =============================================
SET NAMES utf8mb4;

SET @fm = (SELECT menu_id FROM sys_menu WHERE menu_name = '流程管理' AND parent_id = 1 LIMIT 1);

-- 幂等清理
DELETE FROM sys_role_menu WHERE menu_id IN (SELECT menu_id FROM sys_menu WHERE perms LIKE 'flowable:definition%');
DELETE FROM sys_menu WHERE perms LIKE 'flowable:definition%';

-- ========== 1. 菜单 ==========
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('流程定义', @fm, 2, 'definition', 'flowable/definition/index', 1, 0, 'C', '0', '0', 'flowable:definition:list', 'component', 'admin', sysdate(), '已部署流程版本管理：多版本查看/查看XML/备注/挂起激活/设为活动版本');
SET @pm = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('版本查询', @pm, 1, '#', '', 1, 0, 'F', '0', '0', 'flowable:definition:query', '#', 'admin', sysdate(), '查看历史版本 XML / 节点清单'),
('版本状态', @pm, 2, '#', '', 1, 0, 'F', '0', '0', 'flowable:definition:state', '#', 'admin', sysdate(), '版本备注 / 挂起激活 / 设为活动版本');

-- 菜单顺序统一收敛：模型 → 流程定义 → 流程监控 → 表单 → 分类 → 紧急程度
UPDATE sys_menu SET order_num = CASE path
        WHEN 'model'      THEN 1
        WHEN 'definition' THEN 2
        WHEN 'monitor'    THEN 3
        WHEN 'form'       THEN 4
        WHEN 'category'   THEN 5
        WHEN 'urgency'    THEN 6
        ELSE order_num END
WHERE parent_id = @fm;

-- ========== 2. 角色授权：与「流程模型」保持同等范围 + 超级管理员 ==========
SET @defMenu = (SELECT menu_id FROM sys_menu WHERE perms = 'flowable:definition:list' LIMIT 1);
SET @defQuery = (SELECT menu_id FROM sys_menu WHERE perms = 'flowable:definition:query' LIMIT 1);
SET @defState = (SELECT menu_id FROM sys_menu WHERE perms = 'flowable:definition:state' LIMIT 1);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT rm.role_id, @defMenu FROM sys_role_menu rm
JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE m.perms = 'flowable:model:list';

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT rm.role_id, x.menu_id FROM sys_role_menu rm
JOIN sys_menu m ON m.menu_id = rm.menu_id
CROSS JOIN (SELECT @defQuery AS menu_id UNION ALL SELECT @defState) x
WHERE m.perms = 'flowable:model:list';

INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, @defMenu), (1, @defQuery), (1, @defState);

-- ========== 3. 校验 ==========
SELECT menu_id, menu_name, parent_id, order_num, path, component, perms
FROM sys_menu WHERE perms LIKE 'flowable:definition%'
ORDER BY order_num;

SELECT r.role_id, r.role_name, m.perms
FROM sys_role_menu rm
JOIN sys_role r ON r.role_id = rm.role_id
JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE m.perms LIKE 'flowable:definition%'
ORDER BY m.perms, r.role_id;

-- =============================================
-- 回滚：
-- DELETE FROM sys_role_menu WHERE menu_id IN (SELECT menu_id FROM sys_menu WHERE perms LIKE 'flowable:definition%');
-- DELETE FROM sys_menu WHERE perms LIKE 'flowable:definition%';
-- =============================================
