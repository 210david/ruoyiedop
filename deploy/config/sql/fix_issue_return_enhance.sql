-- ============================================================
-- 领料/退料管理增强 SQL
-- 1. 退料管理菜单图标修复（rollback/reset 不在SVG图标列表中，改为 swap）
-- 2. 新增退料状态字典 mms_return_status（独立于领料状态）
-- 3. 退料表状态默认值修正
-- 执行时间: 2026-08-23
-- ============================================================

USE `ry-vue`;
SET NAMES utf8mb4;

-- ============================================================
-- 1. 修复退料管理菜单图标
--    原图标 rollback / reset 不在SVG列表中，改为 swap
-- ============================================================
UPDATE sys_menu SET icon = 'swap' WHERE menu_name = '退料管理' AND perms = 'mms:return:list';

-- ============================================================
-- 2. 新增退料状态字典 mms_return_status
--    退料状态独立于领料状态：0待退料、1已退料
-- ============================================================

-- 字典类型（幂等插入）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '退料状态', 'mms_return_status', '0', 'admin', sysdate(), '退料状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_return_status');

-- 更新已有字典类型名称
UPDATE sys_dict_type SET dict_name = '退料状态', remark = '退料状态' WHERE dict_type = 'mms_return_status';

-- 字典数据（幂等插入，先删后插）
DELETE FROM sys_dict_data WHERE dict_type = 'mms_return_status';
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '待退料', '0', 'mms_return_status', '', 'info',    'Y', '0', 'admin', sysdate(), '待退料'),
(2, '已退料', '1', 'mms_return_status', '', 'success', 'N', '0', 'admin', sysdate(), '已退料');

-- ============================================================
-- 3. 退料表状态字段默认值修正（已经是DEFAULT '0'，确认即可）
-- ============================================================
-- mms_return_material.status 已有 DEFAULT '0'，无需修改

-- ============================================================
-- 4. 清除 Redis 字典缓存（如果有Redis）
-- ============================================================
-- redis-cli DEL sys_dict:mms_return_status
