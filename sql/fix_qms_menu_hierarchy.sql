-- =============================================
-- 修复QMS增强菜单层级
-- 将质量看板、车间管理、电子签名从顶级移到质量管理父菜单下
-- =============================================

-- 1. 质量看板(4544) -> 质量管理(4727)下, order_num=0
UPDATE sys_menu SET parent_id = 4727, order_num = 0 WHERE menu_id = 4544;

-- 2. 车间管理(4539) -> 质量主数据(4728)下, order_num=6
UPDATE sys_menu SET parent_id = 4728, order_num = 6 WHERE menu_id = 4539;

-- 3. 电子签名(4545) -> 质量管理(4727)下, order_num=4
UPDATE sys_menu SET parent_id = 4727, order_num = 4 WHERE menu_id = 4545;

-- 4. 删除乱码重复菜单(4538)
DELETE FROM sys_menu WHERE menu_id = 4538;

-- 5. 确保角色admin拥有所有QMS菜单权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id FROM sys_menu m
WHERE (m.perms LIKE 'qms:%' OR m.path = 'qms' OR m.parent_id IN (
    SELECT menu_id FROM sys_menu WHERE menu_name = '质量管理' AND parent_id = 0
))
AND m.menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);
