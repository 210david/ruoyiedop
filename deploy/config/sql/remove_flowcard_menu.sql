-- ============================================================
-- 删除流转卡（Flow Card）菜单、权限、字典及数据表
-- 说明: 流转卡功能未启用，删除相关菜单、角色权限、字典和数据库表
-- 执行时间: 2026-08-25
-- ============================================================

USE `ry-vue`;
SET NAMES utf8mb4;

-- ============================================================
-- 一、删除流转卡菜单及按钮权限
-- ============================================================

-- 删除流转卡相关的所有菜单（C型菜单和F型按钮）
DELETE FROM sys_menu WHERE perms LIKE 'mms:flowcard:%';

-- 删除角色-菜单关联
DELETE FROM sys_role_menu WHERE menu_id IN (
    SELECT menu_id FROM sys_menu WHERE perms LIKE 'mms:flowcard:%'
);

-- 再次确保删除（如果上面因为外键顺序问题没删干净）
DELETE FROM sys_role_menu WHERE menu_id IN (
    SELECT menu_id FROM (
        SELECT menu_id FROM sys_menu WHERE perms LIKE 'mms:flowcard:%'
    ) t
);

-- 删除菜单本身
DELETE FROM sys_menu WHERE perms LIKE 'mms:flowcard:%';

-- ============================================================
-- 二、删除流转卡状态字典
-- ============================================================

-- 删除字典数据项
DELETE FROM sys_dict_data WHERE dict_type = 'mms_flowcard_status';

-- 删除字典类型
DELETE FROM sys_dict_type WHERE dict_type = 'mms_flowcard_status';

-- ============================================================
-- 三、删除编码规则中的流转卡编号规则
-- ============================================================
DELETE FROM mk_number_rule WHERE rule_code = 'mms_flow_card';

-- ============================================================
-- 四、删除流转卡数据表
-- ============================================================
DROP TABLE IF EXISTS mms_flow_card;

-- ============================================================
-- 五、验证
-- ============================================================
SELECT '=== 流转卡菜单删除完成 ===' AS result;
SELECT COUNT(*) AS remaining_flowcard_menus FROM sys_menu WHERE perms LIKE 'mms:flowcard:%';
SELECT COUNT(*) AS remaining_flowcard_dict FROM sys_dict_type WHERE dict_type = 'mms_flowcard_status';
SELECT COUNT(*) AS remaining_flowcard_rule FROM mk_number_rule WHERE rule_code = 'mms_flow_card';
