-- 流程台账前台菜单改造：一个台账对应「流程台账」目录下的一个二级菜单
-- 日期：2026-11-18

-- 1. flow_ledger 增加关联菜单ID
ALTER TABLE flow_ledger ADD COLUMN menu_id bigint(20) DEFAULT NULL COMMENT '前台菜单ID' AFTER order_num;

-- 2. 「流程台账」由菜单改为目录（作为二级菜单挂载点）
UPDATE sys_menu SET menu_type = 'M', component = NULL, perms = '' WHERE menu_id = 9064600;

-- 3. 为已有台账生成二级菜单
INSERT INTO sys_menu(parent_id, menu_name, order_num, path, component, `query`, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT 9064600, l.ledger_name, IFNULL(l.order_num, 0), CONCAT('l', l.ledger_id), 'flowable/ledger/portal',
       CONCAT('{"ledgerId":', l.ledger_id, '}'), '1', '0', 'C', '0', IFNULL(l.status, '0'),
       'flowable:ledger:view', 'table', 'admin', sysdate()
  FROM flow_ledger l
 WHERE l.del_flag = '0' AND (l.menu_id IS NULL OR l.menu_id = 0);

-- 4. 回写台账关联菜单ID
UPDATE flow_ledger l
  JOIN sys_menu m ON m.parent_id = 9064600 AND m.path = CONCAT('l', l.ledger_id)
   SET l.menu_id = m.menu_id
 WHERE l.del_flag = '0' AND (l.menu_id IS NULL OR l.menu_id = 0);

-- 5. 二级菜单授权：沿用「流程台账」目录已授权的角色
INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT DISTINCT rm.role_id, m.menu_id
  FROM sys_role_menu rm
  JOIN sys_menu m ON m.parent_id = 9064600 AND m.menu_type = 'C'
 WHERE rm.menu_id = 9064600;
